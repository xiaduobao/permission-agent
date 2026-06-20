package com.permission.agent.controller;

import com.permission.agent.agent.PendingActionService;
import com.permission.agent.agent.dto.ChatMessage;
import com.permission.agent.common.Result;
import com.permission.agent.dto.AiChatRequest;
import com.permission.agent.entity.SysAiSessionMeta;
import com.permission.agent.security.SecurityUtils;
import com.permission.agent.service.DashboardService;
import com.permission.agent.service.PermissionAiAgentService;
import com.permission.agent.service.PermissionRiskAiService;
import com.permission.agent.common.PageResult;
import com.permission.agent.entity.SysOperationLog;
import com.permission.agent.entity.SysPermissionRisk;
import com.permission.agent.entity.SysPermissionTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiAndDashboardController {

    private final PermissionAiAgentService aiAgentService;
    private final PermissionRiskAiService riskAiService;
    private final DashboardService dashboardService;
    private final PendingActionService pendingActionService;

    @PostMapping("/ai/chat/stream")
    public SseEmitter streamChat(@RequestBody AiChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return aiAgentService.streamChat(userId, request.getSessionId(), request.getMessage());
    }

    @GetMapping("/ai/sessions")
    public Result<List<SysAiSessionMeta>> listSessions() {
        return Result.ok(aiAgentService.listSessions(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/ai/sessions/{sessionId}")
    public Result<List<ChatMessage>> sessionHistory(@PathVariable String sessionId) {
        return Result.ok(aiAgentService.getSessionHistory(sessionId, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/ai/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        aiAgentService.deleteSession(sessionId, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @PostMapping("/ai/actions/{id}/confirm")
    public Result<String> confirmAction(@PathVariable Long id) {
        return Result.ok(pendingActionService.confirm(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/ai/actions/{id}/reject")
    public Result<Void> rejectAction(@PathVariable Long id) {
        pendingActionService.reject(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(dashboardService.stats());
    }

    @GetMapping("/risks")
    public Result<PageResult<SysPermissionRisk>> risks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(dashboardService.riskPage(pageNum, pageSize, status));
    }

    @PutMapping("/risks/{id}/handle")
    public Result<Void> handleRisk(@PathVariable Long id, @RequestParam Integer status) {
        dashboardService.handleRisk(id, status);
        return Result.ok();
    }

    @PostMapping("/risks/check")
    public Result<Void> triggerRiskCheck() {
        riskAiService.triggerManualCheck();
        return Result.ok();
    }

    @GetMapping("/logs")
    public Result<PageResult<SysOperationLog>> logs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(dashboardService.logPage(pageNum, pageSize));
    }

    @GetMapping("/templates")
    public Result<List<SysPermissionTemplate>> templates() {
        return Result.ok(dashboardService.templates());
    }
}
