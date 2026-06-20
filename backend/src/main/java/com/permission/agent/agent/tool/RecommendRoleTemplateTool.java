package com.permission.agent.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.entity.SysPermissionTemplate;
import com.permission.agent.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendRoleTemplateTool implements AgentTool {

    private final DashboardService dashboardService;

    @Override
    public String name() {
        return "recommend_role_template";
    }

    @Override
    public String description() {
        return "根据岗位推荐权限模板配置";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "position", Map.of("type", "string", "description", "岗位名称，如：研发工程师、产品经理")
                ),
                "required", List.of("position")
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
        String position = args.path("position").asText("").trim();
        List<SysPermissionTemplate> templates = dashboardService.templates();
        if (templates.isEmpty()) {
            return "暂无权限模板";
        }
        List<SysPermissionTemplate> matched = templates.stream()
                .filter(t -> position.isEmpty()
                        || (t.getPosition() != null && t.getPosition().contains(position))
                        || (t.getName() != null && t.getName().contains(position)))
                .toList();
        if (matched.isEmpty()) {
            return "未找到匹配「" + position + "」的模板。可用模板：\n" + templates.stream()
                    .map(t -> "- " + t.getName() + "（岗位:" + t.getPosition() + "）: " + t.getDescription())
                    .collect(Collectors.joining("\n"));
        }
        return matched.stream()
                .map(t -> "【" + t.getName() + "】岗位:" + t.getPosition()
                        + ", 角色ID:" + t.getRoleIds() + ", 说明:" + t.getDescription())
                .collect(Collectors.joining("\n"));
    }
}
