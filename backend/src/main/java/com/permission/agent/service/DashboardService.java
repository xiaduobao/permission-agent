package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.agent.common.PageResult;
import com.permission.agent.entity.SysOperationLog;
import com.permission.agent.entity.SysPermissionRisk;
import com.permission.agent.entity.SysPermissionTemplate;
import com.permission.agent.mapper.SysOperationLogMapper;
import com.permission.agent.mapper.SysPermissionRiskMapper;
import com.permission.agent.mapper.SysPermissionTemplateMapper;
import com.permission.agent.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SysPermissionRiskMapper riskMapper;
    private final SysOperationLogMapper logMapper;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysPermissionTemplateMapper templateMapper;

    public Map<String, Object> stats() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userService.listAll().size());
        stats.put("roleCount", roleService.listAll().size());
        stats.put("pendingRisks", riskMapper.selectCount(new LambdaQueryWrapper<SysPermissionRisk>()
                .eq(SysPermissionRisk::getTenantId, tenantId)
                .eq(SysPermissionRisk::getStatus, 0)));
        stats.put("totalRisks", riskMapper.selectCount(new LambdaQueryWrapper<SysPermissionRisk>()
                .eq(SysPermissionRisk::getTenantId, tenantId)));
        return stats;
    }

    public PageResult<SysPermissionRisk> riskPage(int pageNum, int pageSize, Integer status) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        LambdaQueryWrapper<SysPermissionRisk> wrapper = new LambdaQueryWrapper<SysPermissionRisk>()
                .eq(SysPermissionRisk::getTenantId, tenantId)
                .eq(status != null, SysPermissionRisk::getStatus, status)
                .orderByDesc(SysPermissionRisk::getCreateTime);
        Page<SysPermissionRisk> page = riskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public void handleRisk(Long id, Integer status) {
        SysPermissionRisk risk = riskMapper.selectById(id);
        if (risk != null) {
            risk.setStatus(status);
            risk.setHandleTime(LocalDateTime.now());
            riskMapper.updateById(risk);
        }
    }

    public PageResult<SysOperationLog> logPage(int pageNum, int pageSize) {
        Page<SysOperationLog> page = logMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperationLog>()
                        .eq(SysOperationLog::getTenantId, SecurityUtils.getCurrentTenantId())
                        .orderByDesc(SysOperationLog::getCreateTime));
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public List<SysPermissionTemplate> templates() {
        return templateMapper.selectList(new LambdaQueryWrapper<SysPermissionTemplate>()
                .eq(SysPermissionTemplate::getTenantId, SecurityUtils.getCurrentTenantId()));
    }
}
