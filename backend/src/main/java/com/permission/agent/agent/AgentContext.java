package com.permission.agent.agent;

import com.permission.agent.security.LoginUser;
import com.permission.agent.security.SecurityUtils;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class AgentContext {
    private final Long userId;
    private final Long tenantId;
    private final String username;
    private final String sessionId;
    private final Set<String> permissions;

    public AgentContext(String sessionId) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        this.userId = loginUser.getUser().getId();
        this.tenantId = loginUser.getUser().getTenantId();
        this.username = loginUser.getUser().getUsername();
        this.sessionId = sessionId;
        this.permissions = Set.copyOf(loginUser.getPermissions());
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return true;
        }
        return permissions.contains(permission) || permissions.contains("super_admin")
                || permissions.stream().anyMatch(p -> p.contains("admin"));
    }

    public boolean hasAnyPermission(List<String> required) {
        if (required == null || required.isEmpty()) {
            return true;
        }
        return required.stream().anyMatch(this::hasPermission);
    }
}
