package com.zhut.user.mapper;

import com.zhut.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据 ID 查询用户
     */
    User selectById(@Param("id") Long id);
    
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(@Param("email") String email);
    
    /**
     * 根据手机号查询用户
     */
    User selectByPhone(@Param("phone") String phone);
    
    /**
     * 根据账号（用户名/邮箱/手机号）查询用户
     */
    User selectByAccount(@Param("account") String account);
    
    /**
     * 根据第三方登录 ID 查询用户
     */
    User selectByOauthProviderId(@Param("oauthProviderId") String oauthProviderId);
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户
     */
    int update(User user);
    
    /**
     * 更新最后登录时间
     */
    int updateLastLoginTime(@Param("id") Long id);
}