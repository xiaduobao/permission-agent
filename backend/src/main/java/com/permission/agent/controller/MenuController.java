package com.permission.agent.controller;

import com.permission.agent.common.Result;
import com.permission.agent.entity.SysMenu;
import com.permission.agent.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuService menuService;

    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.ok(menuService.tree());
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.ok();
    }
}
