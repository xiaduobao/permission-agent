package com.permission.agent.dto;

import com.permission.agent.entity.SysMenu;
import com.permission.agent.entity.SysRole;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private Long tenantId;
    private List<SysRole> roles;
    private List<SysMenu> menus;
    private List<String> permissions;
}
