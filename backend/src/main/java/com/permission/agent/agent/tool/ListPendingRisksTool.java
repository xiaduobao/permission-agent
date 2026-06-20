package com.permission.agent.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.common.PageResult;
import com.permission.agent.entity.SysPermissionRisk;
import com.permission.agent.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListPendingRisksTool implements AgentTool {

    private final DashboardService dashboardService;

    @Override
    public String name() {
        return "list_pending_risks";
    }

    @Override
    public String description() {
        return "列出当前租户待处理的权限风险工单";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }

    @Override
    public List<String> requiredPermissions() {
        return List.of("ai:risk:list");
    }

    @Override
    public String execute(AgentContext ctx, JsonNode args) {
        PageResult<SysPermissionRisk> page = dashboardService.riskPage(1, 20, 0);
        if (page.getRecords().isEmpty()) {
            return "当前无待处理风险工单";
        }
        return page.getRecords().stream()
                .map(r -> String.format("- [%s] 用户:%s, 描述:%s, 建议:%s",
                        r.getRiskType(), r.getUsername(), r.getDescription(), r.getSuggestion()))
                .collect(Collectors.joining("\n"));
    }
}
