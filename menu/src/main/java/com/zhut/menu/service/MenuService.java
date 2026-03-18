package com.zhut.menu.service;

import com.zhut.menu.dto.MenuCreateRequest;
import com.zhut.menu.dto.MenuTreeVO;
import com.zhut.menu.dto.MenuUpdateRequest;
import com.zhut.menu.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 创建菜单
     */
    Menu create(MenuCreateRequest request);

    /**
     * 更新菜单
     */
    Menu update(MenuUpdateRequest request);

    /**
     * 根据 ID 查询菜单
     */
    Menu getById(Long id);

    /**
     * 根据父菜单 ID 查询子菜单列表
     */
    List<Menu> getByParentId(Long parentId);

    /**
     * 查询所有菜单（平铺列表）
     */
    List<Menu> listAll();

    /**
     * 查询菜单树形结构
     */
    List<MenuTreeVO> getMenuTree();

    /**
     * 删除菜单
     */
    void delete(Long id);
}
