package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.agent.common.GlobalExceptionHandler.BusinessException;
import com.permission.agent.common.PageResult;
import com.permission.agent.dto.UserSaveRequest;
import com.permission.agent.entity.*;
import com.permission.agent.mapper.*;
import com.permission.agent.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<SysUser> page(int pageNum, int pageSize, String keyword) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or().like(SysUser::getNickname, keyword))
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public SysUser getById(Long id) {
        return userMapper.selectById(id);
    }

    public String getPermissionInfoByUsername(String username, Long tenantId) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getTenantId, tenantId));
        if (user == null) {
            return "用户不存在: " + username;
        }
        return getUserPermissionInfo(user.getId());
    }

    @Transactional
    public String assignRolesByNames(String username, List<String> roleNames, Long tenantId) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getTenantId, tenantId));
        if (user == null) {
            return "用户不存在: " + username;
        }
        List<SysRole> allRoles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId));
        List<Long> existingRoleIds = userMapper.selectRolesByUserId(user.getId()).stream()
                .map(SysRole::getId).toList();
        List<Long> newRoleIds = new ArrayList<>(existingRoleIds);
        List<String> added = new ArrayList<>();
        for (String roleName : roleNames) {
            allRoles.stream()
                    .filter(r -> r.getName().equals(roleName) || r.getName().contains(roleName))
                    .findFirst()
                    .ifPresent(role -> {
                        if (!newRoleIds.contains(role.getId())) {
                            newRoleIds.add(role.getId());
                            added.add(role.getName());
                        }
                    });
        }
        if (added.isEmpty()) {
            return "未找到匹配角色或角色已分配，目标角色: " + String.join(", ", roleNames);
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        for (Long roleId : newRoleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
        return "已成功为用户 " + username + " 新增角色: " + String.join(", ", added)
                + "。当前全部角色: " + userMapper.selectRolesByUserId(user.getId()).stream()
                .map(SysRole::getName).collect(Collectors.joining(", "));
    }

    public String getUserPermissionInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return "用户不存在";
        }
        SysDept dept = user.getDeptId() != null ? deptMapper.selectById(user.getDeptId()) : null;
        List<SysRole> roles = userMapper.selectRolesByUserId(userId);
        List<SysMenu> menus = userMapper.selectMenusByUserId(userId);
        List<String> apis = userMapper.selectApiPermissionsByUserId(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("用户: ").append(user.getNickname()).append("(").append(user.getUsername()).append(")\n");
        sb.append("部门: ").append(dept != null ? dept.getName() : "无").append("\n");
        sb.append("岗位: ").append(user.getPosition() != null ? user.getPosition() : "无").append("\n");
        sb.append("角色: ").append(roles.stream().map(SysRole::getName).collect(Collectors.joining(", "))).append("\n");
        sb.append("菜单权限: ").append(menus.stream().map(SysMenu::getName).collect(Collectors.joining(", "))).append("\n");
        sb.append("接口权限: ").append(apis != null ? String.join(", ", apis) : "无");
        return sb.toString();
    }

    @Transactional
    public void save(UserSaveRequest req) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setDeptId(req.getDeptId());
        user.setUsername(req.getUsername());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPosition(req.getPosition());
        user.setStatus(req.getStatus() != null ? req.getStatus() : 1);

        if (req.getId() != null) {
            SysUser existing = userMapper.selectById(req.getId());
            if (existing == null) {
                throw new BusinessException("用户不存在");
            }
            existing.setDeptId(req.getDeptId());
            existing.setUsername(req.getUsername());
            existing.setNickname(req.getNickname());
            existing.setEmail(req.getEmail());
            existing.setPhone(req.getPhone());
            existing.setPosition(req.getPosition());
            existing.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            if (StringUtils.hasText(req.getPassword())) {
                existing.setPassword(passwordEncoder.encode(req.getPassword()));
            }
            userMapper.updateById(existing);
            user.setId(existing.getId());
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, req.getId()));
        } else {
            if (!StringUtils.hasText(req.getPassword())) {
                throw new BusinessException("密码不能为空");
            }
            long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, req.getUsername()));
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            userMapper.insert(user);
        }

        if (req.getRoleIds() != null) {
            for (Long roleId : req.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    public List<SysUser> listAll() {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, SecurityUtils.getCurrentTenantId())
                .eq(SysUser::getStatus, 1));
    }
}
