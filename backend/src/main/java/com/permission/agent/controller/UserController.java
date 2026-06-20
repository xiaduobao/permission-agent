package com.permission.agent.controller;

import com.permission.agent.common.PageResult;
import com.permission.agent.common.Result;
import com.permission.agent.dto.UserSaveRequest;
import com.permission.agent.entity.SysUser;
import com.permission.agent.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService userService;

    @GetMapping
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/all")
    public Result<List<SysUser>> listAll() {
        return Result.ok(userService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody UserSaveRequest req) {
        userService.save(req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }
}
