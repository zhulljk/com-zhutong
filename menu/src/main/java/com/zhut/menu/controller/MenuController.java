package com.zhut.menu.controller;

import com.zhut.menu.common.Result;
import com.zhut.menu.dto.MenuCreateRequest;
import com.zhut.menu.dto.MenuTreeVO;
import com.zhut.menu.dto.MenuUpdateRequest;
import com.zhut.menu.entity.Menu;
import com.zhut.menu.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 创建菜单
     */
    @PostMapping
    public Result<Menu> create(@RequestBody MenuCreateRequest request) {
        try {
            Menu menu = menuService.create(request);
            return Result.success(menu);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping
    public Result<Menu> update(@RequestBody MenuUpdateRequest request) {
        try {
            Menu menu = menuService.update(request);
            return Result.success(menu);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据 ID 查询菜单
     */
    @GetMapping("/{id}")
    public Result<Menu> getById(@PathVariable Long id) {
        try {
            Menu menu = menuService.getById(id);
            return Result.success(menu);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据父菜单 ID 查询子菜单列表
     */
    @GetMapping("/children/{parentId}")
    public Result<List<Menu>> getByParentId(@PathVariable Long parentId) {
        try {
            List<Menu> menus = menuService.getByParentId(parentId);
            return Result.success(menus);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询所有菜单（平铺列表）
     */
    @GetMapping("/list")
    public Result<List<Menu>> listAll() {
        try {
            List<Menu> menus = menuService.listAll();
            return Result.success(menus);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询菜单树形结构
     */
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> getMenuTree() {
        try {
            List<MenuTreeVO> tree = menuService.getMenuTree();
            return Result.success(tree);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            menuService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
