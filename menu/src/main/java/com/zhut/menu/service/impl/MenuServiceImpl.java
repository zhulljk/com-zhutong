package com.zhut.menu.service.impl;

import com.zhut.menu.dto.MenuCreateRequest;
import com.zhut.menu.dto.MenuTreeVO;
import com.zhut.menu.dto.MenuUpdateRequest;
import com.zhut.menu.entity.Menu;
import com.zhut.menu.mapper.MenuMapper;
import com.zhut.menu.service.MenuService;
import com.zhut.menu.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    @Transactional
    public Menu create(MenuCreateRequest request) {
        // 校验菜单名称
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("菜单名称不能为空");
        }

        // 设置默认父菜单 ID
        Long parentId = request.getParentId() != null ? request.getParentId() : 0L;

        // 校验父菜单是否存在（非顶级菜单时）
        if (parentId != 0L) {
            Menu parentMenu = menuMapper.selectById(parentId);
            if (parentMenu == null) {
                throw new RuntimeException("父菜单不存在");
            }
        }

        // 校验同级菜单下名称是否重复
        Menu existingMenu = menuMapper.selectByNameAndParentId(request.getName(), parentId);
        if (existingMenu != null) {
            throw new RuntimeException("同级菜单下已存在相同名称的菜单");
        }

        // 创建菜单
        Menu menu = new Menu();
        menu.setId(SnowflakeIdGenerator.generate());
        menu.setParentId(parentId);
        menu.setName(request.getName());
        menu.setIcon(request.getIcon());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setType(request.getType() != null ? request.getType() : 1);
        menu.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        menu.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        menu.setVisible(request.getVisible() != null ? request.getVisible() : 1);

        menuMapper.insert(menu);
        return menu;
    }

    @Override
    @Transactional
    public Menu update(MenuUpdateRequest request) {
        if (request.getId() == null) {
            throw new RuntimeException("菜单 ID 不能为空");
        }

        // 校验菜单是否存在
        Menu existingMenu = menuMapper.selectById(request.getId());
        if (existingMenu == null) {
            throw new RuntimeException("菜单不存在");
        }

        // 如果修改了父菜单，校验父菜单是否存在
        if (request.getParentId() != null) {
            // 不能将自身设为父菜单
            if (request.getParentId().equals(request.getId())) {
                throw new RuntimeException("不能将自身设为父菜单");
            }
            if (request.getParentId() != 0L) {
                Menu parentMenu = menuMapper.selectById(request.getParentId());
                if (parentMenu == null) {
                    throw new RuntimeException("父菜单不存在");
                }
            }
        }

        // 如果修改了名称，校验同级菜单下名称是否重复
        if (request.getName() != null) {
            Long parentId = request.getParentId() != null ? request.getParentId() : existingMenu.getParentId();
            Menu duplicateMenu = menuMapper.selectByNameAndParentId(request.getName(), parentId);
            if (duplicateMenu != null && !duplicateMenu.getId().equals(request.getId())) {
                throw new RuntimeException("同级菜单下已存在相同名称的菜单");
            }
        }

        // 更新菜单
        Menu menu = new Menu();
        menu.setId(request.getId());
        menu.setParentId(request.getParentId());
        menu.setName(request.getName());
        menu.setIcon(request.getIcon());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setType(request.getType());
        menu.setSortOrder(request.getSortOrder());
        menu.setStatus(request.getStatus());
        menu.setVisible(request.getVisible());

        menuMapper.update(menu);
        return menuMapper.selectById(request.getId());
    }

    @Override
    public Menu getById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        return menu;
    }

    @Override
    public List<Menu> getByParentId(Long parentId) {
        return menuMapper.selectByParentId(parentId);
    }

    @Override
    public List<Menu> listAll() {
        return menuMapper.selectAll();
    }

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAll();
        return buildTree(allMenus);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 校验菜单是否存在
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }

        // 校验是否有子菜单
        int childCount = menuMapper.countByParentId(id);
        if (childCount > 0) {
            throw new RuntimeException("该菜单下存在子菜单，无法删除");
        }

        menuMapper.deleteById(id);
    }

    /**
     * 构建菜单树
     */
    private List<MenuTreeVO> buildTree(List<Menu> menus) {
        // 将菜单转换为 TreeVO
        List<MenuTreeVO> treeVOList = menus.stream()
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());

        // 按 parentId 分组
        Map<Long, List<MenuTreeVO>> parentIdMap = treeVOList.stream()
                .collect(Collectors.groupingBy(MenuTreeVO::getParentId));

        // 为每个节点设置子节点
        for (MenuTreeVO vo : treeVOList) {
            List<MenuTreeVO> children = parentIdMap.get(vo.getId());
            vo.setChildren(children != null ? children : new ArrayList<>());
        }

        // 返回顶级菜单（parentId 为 0 的菜单）
        return treeVOList.stream()
                .filter(vo -> vo.getParentId() == 0L)
                .collect(Collectors.toList());
    }

    /**
     * Menu 转换为 MenuTreeVO
     */
    private MenuTreeVO convertToTreeVO(Menu menu) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setIcon(menu.getIcon());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setPermission(menu.getPermission());
        vo.setType(menu.getType());
        vo.setSortOrder(menu.getSortOrder());
        vo.setStatus(menu.getStatus());
        vo.setVisible(menu.getVisible());
        vo.setCreateTime(menu.getCreateTime());
        vo.setUpdateTime(menu.getUpdateTime());
        return vo;
    }
}
