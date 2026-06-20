package com.permission.agent.controller;

import com.permission.agent.common.Result;
import com.permission.agent.entity.SysDept;
import com.permission.agent.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depts")
@RequiredArgsConstructor
public class DeptController {

    private final SysDeptService deptService;

    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        return Result.ok(deptService.tree());
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok();
    }
}
