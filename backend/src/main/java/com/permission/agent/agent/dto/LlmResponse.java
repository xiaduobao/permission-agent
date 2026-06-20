package com.permission.agent.agent.dto;

import lombok.Data;

import java.util.List;

@Data
public class LlmResponse {
    private String content;
    private List<ToolCall> toolCalls;

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
