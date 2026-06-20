package com.permission.agent.controller;

import com.permission.agent.common.PageResult;
import com.permission.agent.common.Result;
import com.permission.agent.entity.SysTenant;
import com.permission.agent.service.SysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final SysTenantService tenantService;

    @GetMapping
    public Result<PageResult<SysTenant>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(tenantService.page(pageNum, pageSize));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysTenant tenant) {
        tenantService.save(tenant);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.ok();
    }
}
