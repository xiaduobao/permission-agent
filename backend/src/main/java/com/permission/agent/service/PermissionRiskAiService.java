package com.permission.agent.service;

import com.permission.agent.entity.SysTenant;
import com.permission.agent.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionRiskAiService {

    private static final String RISK_CHECK_PROMPT = """
            请分析以下企业权限数据，识别风险：权限溢出、多人超配高危权限、长期闲置权限、岗位权限不匹配
            权限数据：%s
            输出格式：风险类型|风险用户|风险原因|整改建议（每行一条）
            """;

    private final PermissionRiskService riskService;
    private final OpenAiClient openAiClient;
    private final SysTenantMapper tenantMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledRiskCheck() {
        log.info("开始权限风险巡检...");
        List<SysTenant> tenants = tenantMapper.selectList(null);
        for (SysTenant tenant : tenants) {
            runCheckForTenant(tenant.getId());
        }
        log.info("权限风险巡检完成");
    }

    public void triggerManualCheck() {
        runCheckForTenant(com.permission.agent.security.SecurityUtils.getCurrentTenantId());
    }

    private void runCheckForTenant(Long tenantId) {
        riskService.ruleBasedRiskCheckForTenant(tenantId);
        if (openAiClient.isAvailable()) {
            try {
                String data = riskService.getAllEnterprisePermissionData(tenantId);
                String result = openAiClient.chat(RISK_CHECK_PROMPT.formatted(data));
                if (result != null) {
                    riskService.parseAndSaveAiRiskResult(result, tenantId);
                }
            } catch (Exception e) {
                log.warn("AI风险巡检失败(tenant={}): {}", tenantId, e.getMessage());
            }
        }
    }
}
