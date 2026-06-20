package com.permission.agent.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatMessage {
    private String role;
    private String content;
    private List<ToolCall> toolCalls;
    private String toolCallId;
    private String name;

    public static ChatMessage system(String content) {
        ChatMessage m = new ChatMessage();
        m.setRole("system");
        m.setContent(content);
        return m;
    }

    public static ChatMessage user(String content) {
        ChatMessage m = new ChatMessage();
        m.setRole("user");
        m.setContent(content);
        return m;
    }

    public static ChatMessage assistant(String content) {
        ChatMessage m = new ChatMessage();
        m.setRole("assistant");
        m.setContent(content);
        return m;
    }

    public static ChatMessage assistantWithTools(List<ToolCall> toolCalls) {
        ChatMessage m = new ChatMessage();
        m.setRole("assistant");
        m.setContent("");
        m.setToolCalls(toolCalls);
        return m;
    }

    public static ChatMessage tool(String toolCallId, String name, String content) {
        ChatMessage m = new ChatMessage();
        m.setRole("tool");
        m.setToolCallId(toolCallId);
        m.setName(name);
        m.setContent(content);
        return m;
    }
}
