package com.permission.agent.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.agent.MockAgentEngine;
import com.permission.agent.agent.PendingActionService;
import com.permission.agent.agent.dto.ChatMessage;
import com.permission.agent.agent.dto.LlmResponse;
import com.permission.agent.agent.dto.ToolCall;
import com.permission.agent.agent.memory.AgentMemoryManager;
import com.permission.agent.agent.tool.AgentToolRegistry;
import com.permission.agent.entity.SysAiPendingAction;
import com.permission.agent.service.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private static final int MAX_ROUNDS = 5;

    private final AgentMemoryManager memoryManager;
    private final AgentToolRegistry toolRegistry;
    private final OpenAiClient openAiClient;
    private final MockAgentEngine mockAgentEngine;
    private final PendingActionService pendingActionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public SseEmitter streamChat(String sessionId, String userQuery) {
        AgentContext ctx = new AgentContext(sessionId);
        memoryManager.append(ctx, ChatMessage.user(userQuery));

        SseEmitter emitter = new SseEmitter(120_000L);
        executor.execute(() -> runAgentLoop(ctx, userQuery, emitter));
        return emitter;
    }

    private void runAgentLoop(AgentContext ctx, String userQuery, SseEmitter emitter) {
        try {
            for (int round = 0; round < MAX_ROUNDS; round++) {
                List<ChatMessage> messages = memoryManager.loadContext(ctx);
                LlmResponse response = callLlm(ctx, messages, userQuery, round == 0);

                if (response.hasToolCalls()) {
                    memoryManager.append(ctx, ChatMessage.assistantWithTools(response.getToolCalls()));
                    StringBuilder toolResults = new StringBuilder();
                    for (ToolCall call : response.getToolCalls()) {
                        if (toolRegistry.requiresConfirmation(call.getName())) {
                            SysAiPendingAction action = pendingActionService.create(ctx, call);
                            sendEvent(emitter, "confirm_required", objectMapper.writeValueAsString(
                                    java.util.Map.of(
                                            "actionId", action.getId(),
                                            "tool", call.getName(),
                                            "preview", action.getPreview()
                                    )));
                            sendEvent(emitter, "message", "需要您确认以下操作：\n" + action.getPreview()
                                    + "\n\n请在下方点击「确认」或「取消」。");
                            sendEvent(emitter, "done", ctx.getSessionId());
                            emitter.complete();
                            return;
                        }
                        sendEvent(emitter, "tool_start", call.getName());
                        JsonNode args = objectMapper.readTree(call.getArguments());
                        String result = toolRegistry.execute(ctx, call.getName(), args);
                        memoryManager.append(ctx, ChatMessage.tool(call.getId(), call.getName(), result));
                        toolResults.append(mockAgentEngine.summarizeToolResult(call.getName(), result)).append("\n\n");
                        sendEvent(emitter, "tool_done", call.getName());
                    }
                    if (!openAiClient.isAvailable()) {
                        String summary = toolResults.toString().trim();
                        streamText(emitter, summary);
                        memoryManager.append(ctx, ChatMessage.assistant(summary));
                        sendEvent(emitter, "done", ctx.getSessionId());
                        emitter.complete();
                        return;
                    }
                    userQuery = "";
                    continue;
                }

                String content = response.getContent() != null ? response.getContent() : "抱歉，未能生成回复。";
                streamText(emitter, content);
                memoryManager.append(ctx, ChatMessage.assistant(content));
                sendEvent(emitter, "done", ctx.getSessionId());
                emitter.complete();
                return;
            }
            streamText(emitter, "抱歉，处理步骤过多，请简化问题后重试。");
            sendEvent(emitter, "done", ctx.getSessionId());
            emitter.complete();
        } catch (Exception e) {
            log.error("Agent loop error", e);
            try {
                sendEvent(emitter, "message", "处理出错：" + e.getMessage());
                sendEvent(emitter, "done", ctx.getSessionId());
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(e);
            }
        }
    }

    private LlmResponse callLlm(AgentContext ctx, List<ChatMessage> messages, String userQuery, boolean useMockPlan) {
        if (openAiClient.isAvailable()) {
            LlmResponse response = openAiClient.chatWithTools(messages, toolRegistry.getSchemas(ctx));
            if (response != null) {
                return response;
            }
        }
        if (useMockPlan) {
            LlmResponse plan = mockAgentEngine.plan(ctx, messages, userQuery);
            if (plan.hasToolCalls()) {
                return plan;
            }
            if (plan.getContent() != null) {
                return plan;
            }
        }
        return executeMockWithTools(ctx, userQuery);
    }

    private LlmResponse executeMockWithTools(AgentContext ctx, String userQuery) {
        LlmResponse plan = mockAgentEngine.plan(ctx, List.of(), userQuery);
        if (!plan.hasToolCalls()) {
            return plan;
        }
        StringBuilder sb = new StringBuilder();
        for (ToolCall call : plan.getToolCalls()) {
            if (toolRegistry.requiresConfirmation(call.getName())) {
                return plan;
            }
            try {
                JsonNode args = objectMapper.readTree(call.getArguments());
                String result = toolRegistry.execute(ctx, call.getName(), args);
                sb.append(mockAgentEngine.summarizeToolResult(call.getName(), result)).append("\n\n");
            } catch (Exception e) {
                sb.append("工具执行失败: ").append(e.getMessage()).append("\n\n");
            }
        }
        LlmResponse response = new LlmResponse();
        response.setContent(sb.toString().trim());
        return response;
    }

    private void streamText(SseEmitter emitter, String text) throws IOException, InterruptedException {
        int chunkSize = 20;
        for (int i = 0; i < text.length(); i += chunkSize) {
            String chunk = text.substring(i, Math.min(i + chunkSize, text.length()));
            sendEvent(emitter, "message", chunk);
            Thread.sleep(20);
        }
    }

    private void sendEvent(SseEmitter emitter, String event, String data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data));
    }
}
