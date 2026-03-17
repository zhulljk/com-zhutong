package com.zhut.user.service;

import com.zhut.user.dto.LoginRequest;
import com.zhut.user.dto.LoginResponse;
import com.zhut.user.dto.RegisterRequest;
import com.zhut.user.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    User register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 根据 ID 查询用户
     */
    User getById(Long id);
    
    /**
     * 根据账号查询用户
     */
    User getByAccount(String account);
    
    /**
     * 根据第三方登录 ID 查询用户
     */
    User getByOauthProviderId(String oauthProviderId);
    
    /**
     * 创建或更新 OAuth2 用户
     */
    User createOrUpdateOauth2User(String providerId, String email, String nickname, String avatar);
    
    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long id);
}