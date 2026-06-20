package com.permission.agent.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryUserPermissionTool implements AgentTool {

    private final SysUserService userService;

    @Override
    public String name() {
        return "query_user_permission";
    }

    @Override
    public String description() {
        return "查询指定用户或当前用户的权限信息，包括角色、菜单、接口权限";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "username", Map.of("type", "string", "description", "要查询的用户名，为空则查询当前用户")
                )
        );
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }

    @Override
    public List<String> requiredPermissions() {
        return List.of();
    }

    @Override
    public String execute(AgentContext ctx, JsonNode args) {
        String username = args.path("username").asText("").trim();
        if (username.isEmpty() || username.equals(ctx.getUsername())) {
            return userService.getUserPermissionInfo(ctx.getUserId());
        }
        if (!ctx.hasPermission("system:user:list")) {
            return "无权查询其他用户权限，仅可查询自己的权限";
        }
        return userService.getPermissionInfoByUsername(username, ctx.getTenantId());
    }
}
