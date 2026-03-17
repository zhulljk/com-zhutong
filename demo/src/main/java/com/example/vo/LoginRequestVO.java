package com.example.vo;

/**
 * 登录请求 VO
 */
public class LoginRequestVO {
    /**
     * 登录类型：password(密码登录), email(邮箱验证码), phone(手机验证码)
     */
    private String loginType;
    /**
     * 用户名/邮箱/手机号
     */
    private String account;
    /**
     * 密码或验证码
     */
    private String credential;
    /**
     * 验证码（用于验证码登录）
     */
    private String code;

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}