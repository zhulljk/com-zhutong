package com.example.service;

import com.example.vo.LoginRequestVO;
import com.example.vo.LoginResponseVO;

/**
 * 登录服务接口
 */
public interface LoginService {
    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponseVO login(LoginRequestVO request);

    /**
     * 验证码登录（OAuth2 方式）
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponseVO loginWithCode(LoginRequestVO request);
}