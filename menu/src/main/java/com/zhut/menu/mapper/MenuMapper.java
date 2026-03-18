package com.zhut.menu.mapper;

import com.zhut.menu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper 接口
 */
@Mapper
public interface MenuMapper {

    /**
     * 根据 ID 查询菜单
     */
    Menu selectById(@Param("id") Long id);

    /**
     * 根据父菜单 ID 查询子菜单列表
     */
    List<Menu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有菜单
     */
    List<Menu> selectAll();

    /**
     * 根据菜单名称和父 ID 查询菜单（用于重名校验）
     */
    Menu selectByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    /**
     * 插入菜单
     */
    int insert(Menu menu);

    /**
     * 更新菜单
     */
    int update(Menu menu);

    /**
     * 删除菜单
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询指定菜单下是否有子菜单
     */
    int countByParentId(@Param("parentId") Long parentId);
}
