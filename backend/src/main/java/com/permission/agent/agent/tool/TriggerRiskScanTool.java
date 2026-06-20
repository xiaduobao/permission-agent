package com.permission.agent.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.permission.agent.agent.AgentContext;
import com.permission.agent.service.PermissionRiskAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TriggerRiskScanTool implements AgentTool {

    private final PermissionRiskAiService riskAiService;

    @Override
    public String name() {
        return "trigger_risk_scan";
    }

    @Override
    public String description() {
        return "触发当前租户的权限风险巡检（规则引擎+AI分析）";
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
        riskAiService.triggerManualCheck();
        return "权限风险巡检已触发，请稍后使用 list_pending_risks 查看结果";
    }
}
