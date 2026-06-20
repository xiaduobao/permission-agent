package com.permission.agent.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new IllegalStateException("未登录");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUser().getId();
    }

    public static Long getCurrentTenantId() {
        return getCurrentUser().getUser().getTenantId();
    }
}
