package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.permission.agent.entity.SysMenu;
import com.permission.agent.mapper.SysMenuMapper;
import com.permission.agent.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;

    public List<SysMenu> tree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getTenantId, SecurityUtils.getCurrentTenantId())
                .orderByAsc(SysMenu::getSort));
        return buildTree(all, 0L);
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .peek(m -> m.setChildren(buildTree(all, m.getId())))
                .collect(Collectors.toList());
    }

    public void save(SysMenu menu) {
        menu.setTenantId(SecurityUtils.getCurrentTenantId());
        if (menu.getId() != null) {
            menuMapper.updateById(menu);
        } else {
            menuMapper.insert(menu);
        }
    }

    public void delete(Long id) {
        menuMapper.deleteById(id);
    }
}
