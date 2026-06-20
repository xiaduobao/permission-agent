package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.agent.common.PageResult;
import com.permission.agent.dto.RoleSaveRequest;
import com.permission.agent.entity.*;
import com.permission.agent.mapper.*;
import com.permission.agent.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleApiMapper roleApiMapper;
    private final SysDataScopeMapper dataScopeMapper;

    public PageResult<SysRole> page(int pageNum, int pageSize) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Page<SysRole> page = roleMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getTenantId, tenantId)
                        .orderByDesc(SysRole::getCreateTime));
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, SecurityUtils.getCurrentTenantId())
                .eq(SysRole::getStatus, 1));
    }

    @Transactional
    public void save(RoleSaveRequest req) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setName(req.getName());
        role.setCode(req.getCode());
        role.setDataScope(req.getDataScope() != null ? req.getDataScope() : 4);
        role.setRemark(req.getRemark());
        role.setStatus(req.getStatus() != null ? req.getStatus() : 1);

        if (req.getId() != null) {
            role.setId(req.getId());
            roleMapper.updateById(role);
            clearRoleRelations(req.getId());
        } else {
            roleMapper.insert(role);
        }

        bindRoleRelations(role.getId(), req);
    }

    private void clearRoleRelations(Long roleId) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        roleApiMapper.delete(new LambdaQueryWrapper<SysRoleApi>().eq(SysRoleApi::getRoleId, roleId));
        dataScopeMapper.delete(new LambdaQueryWrapper<SysDataScope>().eq(SysDataScope::getRoleId, roleId));
    }

    private void bindRoleRelations(Long roleId, RoleSaveRequest req) {
        if (req.getMenuIds() != null) {
            for (Long menuId : req.getMenuIds()) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        if (req.getApiIds() != null) {
            for (Long apiId : req.getApiIds()) {
                SysRoleApi ra = new SysRoleApi();
                ra.setRoleId(roleId);
                ra.setApiId(apiId);
                roleApiMapper.insert(ra);
            }
        }
        if (req.getDeptIds() != null) {
            for (Long deptId : req.getDeptIds()) {
                SysDataScope ds = new SysDataScope();
                ds.setRoleId(roleId);
                ds.setDeptId(deptId);
                dataScopeMapper.insert(ds);
            }
        }
    }

    public void delete(Long id) {
        roleMapper.deleteById(id);
        clearRoleRelations(id);
    }
}
