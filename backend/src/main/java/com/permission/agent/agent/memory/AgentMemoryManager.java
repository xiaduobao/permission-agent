package com.permission.agent.agent.memory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.agent.dto.ChatMessage;
import com.permission.agent.agent.dto.ToolCall;
import com.permission.agent.agent.prompt.AgentPromptService;
import com.permission.agent.entity.SysAiSession;
import com.permission.agent.entity.SysAiSessionMeta;
import com.permission.agent.mapper.SysAiSessionMapper;
import com.permission.agent.mapper.SysAiSessionMetaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentMemoryManager {

    private static final String REDIS_PREFIX = "pa:agent:session:";
    private static final int MAX_MESSAGES = 30;
    private static final long TTL_HOURS = 24;

    private final StringRedisTemplate redisTemplate;
    private final SysAiSessionMapper sessionMapper;
    private final SysAiSessionMetaMapper metaMapper;
    private final AgentPromptService promptService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ChatMessage> loadContext(AgentContext ctx) {
        List<ChatMessage> history = loadFromRedis(ctx.getSessionId());
        if (history.isEmpty()) {
            history = loadFromDb(ctx.getSessionId());
            if (!history.isEmpty()) {
                saveToRedis(ctx.getSessionId(), history);
            }
        }
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(promptService.buildSystemPrompt(ctx)));
        messages.addAll(trimHistory(history));
        return messages;
    }

    public void append(AgentContext ctx, ChatMessage message) {
        List<ChatMessage> history = loadFromRedis(ctx.getSessionId());
        if (history.isEmpty()) {
            history = loadFromDb(ctx.getSessionId());
        }
        history.add(message);
        if (history.size() > MAX_MESSAGES) {
            history = new ArrayList<>(history.subList(history.size() - MAX_MESSAGES, history.size()));
        }
        saveToRedis(ctx.getSessionId(), history);
        if (!"system".equals(message.getRole())) {
            persistMessage(ctx, message);
            updateMeta(ctx, message);
        }
    }

    public List<ChatMessage> getDisplayHistory(String sessionId, Long userId) {
        if (userId != null && !ownsSession(sessionId, userId)) {
            return List.of();
        }
        List<ChatMessage> history = loadFromRedis(sessionId);
        if (history.isEmpty()) {
            history = loadFromDb(sessionId);
        }
        return history.stream()
                .filter(m -> !"tool".equals(m.getRole()))
                .filter(m -> m.getToolCalls() == null || m.getToolCalls().isEmpty())
                .toList();
    }

    public List<SysAiSessionMeta> listUserSessions(Long userId) {
        return metaMapper.selectList(new LambdaQueryWrapper<SysAiSessionMeta>()
                .eq(SysAiSessionMeta::getUserId, userId)
                .orderByDesc(SysAiSessionMeta::getUpdateTime));
    }

    public void deleteSession(String sessionId, Long userId) {
        if (!ownsSession(sessionId, userId)) {
            return;
        }
        metaMapper.delete(new LambdaQueryWrapper<SysAiSessionMeta>()
                .eq(SysAiSessionMeta::getSessionId, sessionId)
                .eq(SysAiSessionMeta::getUserId, userId));
        sessionMapper.delete(new LambdaQueryWrapper<SysAiSession>()
                .eq(SysAiSession::getSessionId, sessionId)
                .eq(SysAiSession::getUserId, userId));
        try {
            redisTemplate.delete(REDIS_PREFIX + sessionId);
        } catch (Exception e) {
            log.debug("Redis delete skipped: {}", e.getMessage());
        }
    }

    private boolean ownsSession(String sessionId, Long userId) {
        SysAiSessionMeta meta = metaMapper.selectById(sessionId);
        return meta != null && meta.getUserId().equals(userId);
    }

    private List<ChatMessage> trimHistory(List<ChatMessage> history) {
        if (history.size() <= MAX_MESSAGES) {
            return history;
        }
        return new ArrayList<>(history.subList(history.size() - MAX_MESSAGES, history.size()));
    }

    private List<ChatMessage> loadFromRedis(String sessionId) {
        try {
            String json = redisTemplate.opsForValue().get(REDIS_PREFIX + sessionId);
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {});
        } catch (Exception e) {
            log.debug("Redis load failed, fallback to DB: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveToRedis(String sessionId, List<ChatMessage> messages) {
        try {
            String json = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForValue().set(REDIS_PREFIX + sessionId, json, TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.debug("Redis save failed: {}", e.getMessage());
        }
    }

    private List<ChatMessage> loadFromDb(String sessionId) {
        List<SysAiSession> rows = sessionMapper.selectList(new LambdaQueryWrapper<SysAiSession>()
                .eq(SysAiSession::getSessionId, sessionId)
                .orderByAsc(SysAiSession::getCreateTime));
        List<ChatMessage> messages = new ArrayList<>();
        for (SysAiSession row : rows) {
            if ("tool".equals(row.getRole())) {
                messages.add(ChatMessage.tool(row.getToolCallId(), row.getToolName(), row.getContent()));
            } else if (row.getToolCalls() != null && !row.getToolCalls().isBlank()) {
                try {
                    List<ToolCall> toolCalls = objectMapper.readValue(row.getToolCalls(),
                            new TypeReference<List<ToolCall>>() {});
                    messages.add(ChatMessage.assistantWithTools(toolCalls));
                } catch (Exception e) {
                    messages.add(ChatMessage.assistant(row.getContent()));
                }
            } else {
                messages.add("user".equals(row.getRole())
                        ? ChatMessage.user(row.getContent())
                        : ChatMessage.assistant(row.getContent()));
            }
        }
        return messages;
    }

    private void persistMessage(AgentContext ctx, ChatMessage message) {
        SysAiSession row = new SysAiSession();
        row.setSessionId(ctx.getSessionId());
        row.setUserId(ctx.getUserId());
        row.setRole(message.getRole());
        row.setContent(message.getContent());
        row.setToolName(message.getName());
        row.setToolCallId(message.getToolCallId());
        if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
            try {
                row.setToolCalls(objectMapper.writeValueAsString(message.getToolCalls()));
            } catch (Exception ignored) {}
        }
        sessionMapper.insert(row);
    }

    private void updateMeta(AgentContext ctx, ChatMessage message) {
        SysAiSessionMeta meta = metaMapper.selectById(ctx.getSessionId());
        if (meta == null) {
            meta = new SysAiSessionMeta();
            meta.setSessionId(ctx.getSessionId());
            meta.setUserId(ctx.getUserId());
            meta.setTenantId(ctx.getTenantId());
            meta.setMessageCount(0);
            meta.setCreateTime(LocalDateTime.now());
        }
        if ("user".equals(message.getRole()) && (meta.getTitle() == null || meta.getTitle().isBlank())) {
            String title = message.getContent();
            meta.setTitle(title.length() > 50 ? title.substring(0, 50) + "..." : title);
        }
        meta.setMessageCount((meta.getMessageCount() != null ? meta.getMessageCount() : 0) + 1);
        meta.setUpdateTime(LocalDateTime.now());
        if (metaMapper.selectById(ctx.getSessionId()) == null) {
            metaMapper.insert(meta);
        } else {
            metaMapper.updateById(meta);
        }
    }
}
