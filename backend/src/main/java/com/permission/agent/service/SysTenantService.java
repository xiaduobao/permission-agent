package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.agent.common.PageResult;
import com.permission.agent.entity.SysTenant;
import com.permission.agent.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysTenantService {

    private final SysTenantMapper tenantMapper;

    public PageResult<SysTenant> page(int pageNum, int pageSize) {
        Page<SysTenant> page = tenantMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysTenant>().orderByDesc(SysTenant::getCreateTime));
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public void save(SysTenant tenant) {
        if (tenant.getId() != null) {
            tenantMapper.updateById(tenant);
        } else {
            tenantMapper.insert(tenant);
        }
    }

    public void delete(Long id) {
        tenantMapper.deleteById(id);
    }
}
