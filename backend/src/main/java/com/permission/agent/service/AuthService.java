package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.permission.agent.common.GlobalExceptionHandler.BusinessException;
import com.permission.agent.dto.LoginRequest;
import com.permission.agent.dto.LoginResponse;
import com.permission.agent.entity.SysMenu;
import com.permission.agent.entity.SysRole;
import com.permission.agent.entity.SysUser;
import com.permission.agent.mapper.SysUserMapper;
import com.permission.agent.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        List<SysRole> roles = userMapper.selectRolesByUserId(user.getId());
        List<SysMenu> menus = buildMenuTree(userMapper.selectMenusByUserId(user.getId()));
        List<String> permissions = userMapper.selectApiPermissionsByUserId(user.getId());
        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getTenantId());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .tenantId(user.getTenantId())
                .roles(roles)
                .menus(menus)
                .permissions(permissions)
                .build();
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        List<SysMenu> roots = menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        for (SysMenu root : roots) {
            root.setChildren(findChildren(root.getId(), menus));
        }
        return roots;
    }

    private List<SysMenu> findChildren(Long parentId, List<SysMenu> all) {
        return all.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .peek(m -> m.setChildren(findChildren(m.getId(), all)))
                .collect(Collectors.toList());
    }
}
