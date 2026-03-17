package com.example.vo;

/**
 * 登录响应 VO
 */
public class LoginResponseVO {
    /**
     * 认证类型：jwt 或 oauth2
     */
    private String authType;
    /**
     * JWT token（密码登录时返回）
     */
    private String token;
    /**
     * OAuth2 授权 URI（验证码登录时返回）
     */
    private String oauth2Uri;
    /**
     * 用户信息
     */
    private UserVO user;
    /**
     * 登录是否成功
     */
    private boolean success;
    /**
     * 消息
     */
    private String message;

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOauth2Uri() {
        return oauth2Uri;
    }

    public void setOauth2Uri(String oauth2Uri) {
        this.oauth2Uri = oauth2Uri;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}