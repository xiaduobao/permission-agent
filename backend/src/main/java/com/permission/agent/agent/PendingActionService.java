package com.permission.agent.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.agent.agent.dto.ChatMessage;
import com.permission.agent.agent.dto.ToolCall;
import com.permission.agent.agent.memory.AgentMemoryManager;
import com.permission.agent.agent.tool.AgentToolRegistry;
import com.permission.agent.common.GlobalExceptionHandler.BusinessException;
import com.permission.agent.entity.SysAiPendingAction;
import com.permission.agent.mapper.SysAiPendingActionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PendingActionService {

    private final SysAiPendingActionMapper pendingActionMapper;
    private final AgentToolRegistry toolRegistry;
    private final AgentMemoryManager memoryManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SysAiPendingAction create(AgentContext ctx, ToolCall call) {
        try {
            JsonNode args = objectMapper.readTree(call.getArguments());
            SysAiPendingAction action = new SysAiPendingAction();
            action.setSessionId(ctx.getSessionId());
            action.setUserId(ctx.getUserId());
            action.setToolName(call.getName());
            action.setArguments(call.getArguments());
            action.setPreview(toolRegistry.preview(ctx, call.getName(), args));
            action.setStatus(0);
            action.setExpireTime(LocalDateTime.now().plusMinutes(10));
            pendingActionMapper.insert(action);
            return action;
        } catch (Exception e) {
            throw new BusinessException("创建待确认操作失败: " + e.getMessage());
        }
    }

    public String confirm(Long actionId, Long userId) {
        SysAiPendingAction action = getPendingAction(actionId, userId);
        AgentContext ctx = new AgentContext(action.getSessionId());
        try {
            JsonNode args = objectMapper.readTree(action.getArguments());
            String result = toolRegistry.execute(ctx, action.getToolName(), args);
            action.setStatus(1);
            pendingActionMapper.updateById(action);
            ToolCall call = new ToolCall();
            call.setId("confirmed_" + actionId);
            call.setName(action.getToolName());
            call.setArguments(action.getArguments());
            memoryManager.append(ctx, ChatMessage.assistantWithTools(java.util.List.of(call)));
            memoryManager.append(ctx, ChatMessage.tool(call.getId(), call.getName(), result));
            memoryManager.append(ctx, ChatMessage.assistant("操作已完成：" + result));
            return result;
        } catch (Exception e) {
            throw new BusinessException("执行失败: " + e.getMessage());
        }
    }

    public void reject(Long actionId, Long userId) {
        SysAiPendingAction action = getPendingAction(actionId, userId);
        action.setStatus(2);
        pendingActionMapper.updateById(action);
        AgentContext ctx = new AgentContext(action.getSessionId());
        memoryManager.append(ctx, ChatMessage.assistant("用户已拒绝执行「" + action.getToolName() + "」操作。"));
    }

    private SysAiPendingAction getPendingAction(Long actionId, Long userId) {
        SysAiPendingAction action = pendingActionMapper.selectById(actionId);
        if (action == null || !action.getUserId().equals(userId)) {
            throw new BusinessException("待确认操作不存在");
        }
        if (action.getStatus() != 0) {
            throw new BusinessException("操作已处理");
        }
        if (action.getExpireTime() != null && action.getExpireTime().isBefore(LocalDateTime.now())) {
            action.setStatus(3);
            pendingActionMapper.updateById(action);
            throw new BusinessException("操作已过期");
        }
        return action;
    }

    public static String newToolCallId() {
        return "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
