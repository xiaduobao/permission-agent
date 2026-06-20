package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.permission.agent.entity.*;
import com.permission.agent.mapper.*;
import com.permission.agent.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionRiskService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionRiskMapper riskMapper;

    public String getAllEnterprisePermissionData(Long tenantId) {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getStatus, 1));

        StringBuilder sb = new StringBuilder();
        for (SysUser user : users) {
            List<SysRole> roles = userMapper.selectRolesByUserId(user.getId());
            sb.append("用户:").append(user.getUsername())
                    .append(",岗位:").append(user.getPosition())
                    .append(",角色:").append(roles.stream().map(SysRole::getName).collect(Collectors.joining("|")))
                    .append(",最后登录:").append(user.getLastLoginTime())
                    .append("\n");
        }
        return sb.toString();
    }

    public void ruleBasedRiskCheck() {
        ruleBasedRiskCheckForTenant(SecurityUtils.getCurrentTenantId());
    }

    public void ruleBasedRiskCheckForTenant(Long tenantId) {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId));

        for (SysUser user : users) {
            List<SysRole> roles = userMapper.selectRolesByUserId(user.getId());
            checkAdminOverflow(user, roles, tenantId);
            checkIdlePermission(user, tenantId);
        }
    }

    private void checkAdminOverflow(SysUser user, List<SysRole> roles, Long tenantId) {
        boolean hasAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getCode()) || "super_admin".equals(r.getCode()));
        if (hasAdmin && !"admin".equals(user.getUsername()) && !"CEO".equals(user.getPosition())) {
            saveRisk(tenantId, user, "权限溢出", "非管理员岗位拥有管理员角色", "回收管理员角色或调整岗位");
        }
    }

    private void checkIdlePermission(SysUser user, Long tenantId) {
        if (user.getLastLoginTime() != null &&
                user.getLastLoginTime().isBefore(LocalDateTime.now().minusDays(90))) {
            saveRisk(tenantId, user, "闲置权限", "用户超过90天未登录仍持有权限", "建议回收或冻结账号");
        }
    }

    private void saveRisk(Long tenantId, SysUser user, String type, String desc, String suggestion) {
        long exists = riskMapper.selectCount(new LambdaQueryWrapper<SysPermissionRisk>()
                .eq(SysPermissionRisk::getUserId, user.getId())
                .eq(SysPermissionRisk::getRiskType, type)
                .eq(SysPermissionRisk::getStatus, 0));
        if (exists > 0) {
            return;
        }
        SysPermissionRisk risk = new SysPermissionRisk();
        risk.setTenantId(tenantId);
        risk.setUserId(user.getId());
        risk.setUsername(user.getUsername());
        risk.setRiskType(type);
        risk.setDescription(desc);
        risk.setSuggestion(suggestion);
        risk.setStatus(0);
        riskMapper.insert(risk);
    }

    public void parseAndSaveAiRiskResult(String aiResult, Long tenantId) {
        if (aiResult == null || aiResult.isBlank()) {
            return;
        }
        for (String line : aiResult.split("\n")) {
            if (line.contains("风险类型") || line.contains("|")) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    SysPermissionRisk risk = new SysPermissionRisk();
                    risk.setTenantId(tenantId);
                    risk.setUsername(parts[1].trim());
                    risk.setRiskType(parts[0].trim());
                    risk.setDescription(parts[2].trim());
                    risk.setSuggestion(parts[3].trim());
                    risk.setStatus(0);
                    riskMapper.insert(risk);
                }
            }
        }
    }
}
