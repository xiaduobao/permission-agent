package com.permission.agent.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private String sessionId;
    private String message;
}
