package com.zhut.user.dto;

/**
 * 用户登录请求 DTO
 */
public class LoginRequest {
    /**
     * 用户名/邮箱/手机号
     */
    private String account;
    
    /**
     * 密码
     */
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}