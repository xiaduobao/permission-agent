package com.permission.agent.controller;

import com.permission.agent.common.PageResult;
import com.permission.agent.common.Result;
import com.permission.agent.dto.RoleSaveRequest;
import com.permission.agent.entity.SysRole;
import com.permission.agent.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService roleService;

    @GetMapping
    public Result<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(roleService.page(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> listAll() {
        return Result.ok(roleService.listAll());
    }

    @PostMapping
    public Result<Void> save(@RequestBody RoleSaveRequest req) {
        roleService.save(req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }
}
