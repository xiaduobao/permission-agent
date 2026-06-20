package com.permission.agent.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.common.GlobalExceptionHandler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AgentToolRegistry {

    private final List<AgentTool> tools;

    public List<AgentTool> availableTools(AgentContext ctx) {
        return tools.stream()
                .filter(t -> ctx.hasAnyPermission(t.requiredPermissions()))
                .toList();
    }

    public List<Map<String, Object>> getSchemas(AgentContext ctx) {
        return availableTools(ctx).stream()
                .map(this::toOpenAiSchema)
                .toList();
    }

    public String describeForPrompt(AgentContext ctx) {
        return availableTools(ctx).stream()
                .map(t -> "- " + t.name() + ": " + t.description())
                .collect(Collectors.joining("\n"));
    }

    public boolean requiresConfirmation(String toolName) {
        return findTool(toolName).requiresConfirmation();
    }

    public String execute(AgentContext ctx, String toolName, JsonNode args) {
        AgentTool tool = findTool(toolName);
        if (!ctx.hasAnyPermission(tool.requiredPermissions())) {
            throw new BusinessException("无权执行工具: " + toolName);
        }
        return tool.execute(ctx, args);
    }

    public String preview(AgentContext ctx, String toolName, JsonNode args) {
        AgentTool tool = findTool(toolName);
        if ("assign_user_roles".equals(toolName)) {
            String username = args.path("username").asText("");
            String roles = args.path("role_names").isArray()
                    ? args.get("role_names").toString()
                    : args.path("role_names").asText("");
            return "将为用户「" + username + "」分配角色: " + roles;
        }
        return "执行 " + tool.getClass().getSimpleName() + ": " + args;
    }

    private AgentTool findTool(String toolName) {
        return tools.stream()
                .filter(t -> t.name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未知工具: " + toolName));
    }

    private Map<String, Object> toOpenAiSchema(AgentTool tool) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.name());
        function.put("description", tool.description());
        function.put("parameters", tool.parametersSchema());
        return Map.of("type", "function", "function", function);
    }
}
