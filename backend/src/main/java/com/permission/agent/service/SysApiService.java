package com.permission.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.agent.common.PageResult;
import com.permission.agent.entity.SysApi;
import com.permission.agent.mapper.SysApiMapper;
import com.permission.agent.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysApiService {

    private final SysApiMapper apiMapper;

    public PageResult<SysApi> page(int pageNum, int pageSize) {
        Page<SysApi> page = apiMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysApi>()
                        .eq(SysApi::getTenantId, SecurityUtils.getCurrentTenantId())
                        .orderByDesc(SysApi::getCreateTime));
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public void save(SysApi api) {
        api.setTenantId(SecurityUtils.getCurrentTenantId());
        if (api.getId() != null) {
            apiMapper.updateById(api);
        } else {
            apiMapper.insert(api);
        }
    }

    public void delete(Long id) {
        apiMapper.deleteById(id);
    }
}
