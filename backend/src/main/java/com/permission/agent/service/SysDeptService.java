package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.permission.agent.entity.SysDept;
import com.permission.agent.mapper.SysDeptMapper;
import com.permission.agent.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptService {

    private final SysDeptMapper deptMapper;

    public List<SysDept> tree() {
        List<SysDept> all = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getTenantId, SecurityUtils.getCurrentTenantId())
                .orderByAsc(SysDept::getSort));
        return buildTree(all, 0L);
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        return all.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .peek(d -> d.setChildren(buildTree(all, d.getId())))
                .collect(Collectors.toList());
    }

    public void save(SysDept dept) {
        dept.setTenantId(SecurityUtils.getCurrentTenantId());
        if (dept.getId() != null) {
            deptMapper.updateById(dept);
        } else {
            deptMapper.insert(dept);
        }
    }

    public void delete(Long id) {
        deptMapper.deleteById(id);
    }
}
