package com.permission.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.agent.agent.dto.ChatMessage;
import com.permission.agent.agent.dto.LlmResponse;
import com.permission.agent.agent.dto.ToolCall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class OpenAiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String model;

    public boolean isAvailable() {
        return !mockEnabled && apiKey != null && !apiKey.isBlank() && !"sk-demo".equals(apiKey);
    }

    public String chat(String prompt) {
        if (!isAvailable()) {
            return null;
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "temperature", 0.1,
                    "max_tokens", 2048,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );
            return extractContent(postChat(body));
        } catch (Exception e) {
            log.warn("OpenAI API call failed: {}", e.getMessage());
            return null;
        }
    }

    public LlmResponse chatWithTools(List<ChatMessage> messages, List<Map<String, Object>> tools) {
        if (!isAvailable()) {
            return null;
        }
        try {
            List<Map<String, Object>> msgList = new ArrayList<>();
            for (ChatMessage m : messages) {
                msgList.add(toApiMessage(m));
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("temperature", 0.1);
            body.put("max_tokens", 2048);
            body.put("messages", msgList);
            if (tools != null && !tools.isEmpty()) {
                body.put("tools", tools);
                body.put("tool_choice", "auto");
            }
            JsonNode root = postChat(body);
            return parseResponse(root);
        } catch (Exception e) {
            log.warn("OpenAI tools call failed: {}", e.getMessage());
            return null;
        }
    }

    private JsonNode postChat(Map<String, Object> body) throws Exception {
        String url = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return objectMapper.readTree(response.getBody());
    }

    private String extractContent(JsonNode root) {
        return root.path("choices").path(0).path("message").path("content").asText();
    }

    private LlmResponse parseResponse(JsonNode root) {
        JsonNode message = root.path("choices").path(0).path("message");
        LlmResponse response = new LlmResponse();
        response.setContent(message.path("content").asText(null));

        JsonNode toolCallsNode = message.path("tool_calls");
        if (toolCallsNode.isArray() && !toolCallsNode.isEmpty()) {
            List<ToolCall> toolCalls = new ArrayList<>();
            for (JsonNode tc : toolCallsNode) {
                ToolCall call = new ToolCall();
                call.setId(tc.path("id").asText());
                call.setName(tc.path("function").path("name").asText());
                call.setArguments(tc.path("function").path("arguments").asText());
                toolCalls.add(call);
            }
            response.setToolCalls(toolCalls);
        }
        return response;
    }

    private Map<String, Object> toApiMessage(ChatMessage m) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("role", m.getRole());
        if ("tool".equals(m.getRole())) {
            msg.put("tool_call_id", m.getToolCallId());
            msg.put("content", m.getContent());
        } else if (m.getToolCalls() != null && !m.getToolCalls().isEmpty()) {
            msg.put("content", m.getContent() != null ? m.getContent() : "");
            List<Map<String, Object>> toolCalls = new ArrayList<>();
            for (ToolCall tc : m.getToolCalls()) {
                Map<String, Object> call = new LinkedHashMap<>();
                call.put("id", tc.getId());
                call.put("type", "function");
                Map<String, Object> fn = new LinkedHashMap<>();
                fn.put("name", tc.getName());
                fn.put("arguments", tc.getArguments());
                call.put("function", fn);
                toolCalls.add(call);
            }
            msg.put("tool_calls", toolCalls);
        } else {
            msg.put("content", m.getContent());
        }
        return msg;
    }
}
