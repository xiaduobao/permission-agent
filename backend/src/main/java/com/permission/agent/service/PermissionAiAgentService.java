package com.permission.agent.service;

import com.permission.agent.agent.AgentOrchestrator;
import com.permission.agent.agent.dto.ChatMessage;
import com.permission.agent.agent.memory.AgentMemoryManager;
import com.permission.agent.entity.SysAiSessionMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionAiAgentService {

    private final AgentOrchestrator orchestrator;
    private final AgentMemoryManager memoryManager;

    public SseEmitter streamChat(Long userId, String sessionId, String userQuery) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }
        return orchestrator.streamChat(sessionId, userQuery);
    }

    public List<ChatMessage> getSessionHistory(String sessionId) {
        return memoryManager.getDisplayHistory(sessionId, null);
    }

    public List<ChatMessage> getSessionHistory(String sessionId, Long userId) {
        return memoryManager.getDisplayHistory(sessionId, userId);
    }

    public List<SysAiSessionMeta> listSessions(Long userId) {
        return memoryManager.listUserSessions(userId);
    }

    public void deleteSession(String sessionId, Long userId) {
        memoryManager.deleteSession(sessionId, userId);
    }
}
