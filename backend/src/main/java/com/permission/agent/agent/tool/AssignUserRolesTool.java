package com.permission.agent.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AssignUserRolesTool implements AgentTool {

    private final SysUserService userService;

    @Override
    public String name() {
        return "assign_user_roles";
    }

    @Override
    public String description() {
        return "为用户分配角色（写操作，需用户确认后执行）";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "username", Map.of("type", "string", "description", "目标用户名"),
                        "role_names", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string"),
                                "description", "要分配的角色名称列表"
                        )
                ),
                "required", List.of("username", "role_names")
        );
    }

    @Override
    public boolean requiresConfirmation() {
        return true;
    }

    @Override
    public List<String> requiredPermissions() {
        return List.of("system:user:save");
    }

    @Override
    public String execute(AgentContext ctx, JsonNode args) {
        String username = args.path("username").asText("").trim();
        List<String> roleNames = new ArrayList<>();
        if (args.has("role_names") && args.get("role_names").isArray()) {
            args.get("role_names").forEach(n -> roleNames.add(n.asText()));
        }
        return userService.assignRolesByNames(username, roleNames, ctx.getTenantId());
    }
}
