package com.permission.agent.controller;

import com.permission.agent.common.PageResult;
import com.permission.agent.common.Result;
import com.permission.agent.entity.SysApi;
import com.permission.agent.service.SysApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
public class ApiController {

    private final SysApiService apiService;

    @GetMapping
    public Result<PageResult<SysApi>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(apiService.page(pageNum, pageSize));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysApi api) {
        apiService.save(api);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        apiService.delete(id);
        return Result.ok();
    }
}
