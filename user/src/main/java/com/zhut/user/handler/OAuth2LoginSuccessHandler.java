package com.zhut.user.handler;

import com.zhut.user.common.Result;
import com.zhut.user.dto.LoginResponse;
import com.zhut.user.entity.User;
import com.zhut.user.jwt.JwtTokenProvider;
import com.zhut.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * OAuth2 登录成功处理器
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    
    public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider, UserMapper userMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 获取 OAuth2 用户信息
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();
        
        // 获取提供商信息
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        
        // 获取用户唯一标识
        String providerId = provider + ":" + oauth2User.getAttribute("sub");
        if (providerId.contains(":null")) {
            // 尝试使用其他常见 ID 字段
            if (oauth2User.getAttribute("id") != null) {
                providerId = provider + ":" + oauth2User.getAttribute("id");
            } else if (oauth2User.getAttribute("openId") != null) {
                providerId = provider + ":" + oauth2User.getAttribute("openId");
            }
        }
        
        // 获取用户信息
        String email = oauth2User.getAttribute("email");
        String nickname = oauth2User.getAttribute("name");
        String avatar = oauth2User.getAttribute("picture");
        if (avatar == null) {
            avatar = oauth2User.getAttribute("avatar");
        }
        
        // 创建或更新用户
        User user = createOrUpdateOauth2User(providerId, email, nickname, avatar);
        
        // 生成 Access Token 和 Refresh Token
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        // 构建响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn(jwtTokenProvider.getAccessTokenExpirationTime());
        loginResponse.setRefreshExpiresIn(jwtTokenProvider.getRefreshTokenExpirationTime());
        loginResponse.setUserId(user.getId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setNickname(user.getNickname());
        loginResponse.setAvatar(user.getAvatar());
        loginResponse.setEmail(user.getEmail());
        loginResponse.setPhone(user.getPhone());
        
        // 返回 JSON 响应
        response.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(Result.success(loginResponse)));
    }
    
    /**
     * 创建或更新 OAuth2 用户
     */
    private User createOrUpdateOauth2User(String providerId, String email, String nickname, String avatar) {
        // 先根据 providerId 查询
        User user = userMapper.selectByOauthProviderId(providerId);
        if (user != null) {
            // 更新用户信息
            user.setNickname(nickname);
            user.setAvatar(avatar);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.update(user);
            return user;
        }
        
        // 检查邮箱是否已存在
        if (email != null && !email.isEmpty()) {
            user = userMapper.selectByEmail(email);
            if (user != null) {
                // 绑定第三方登录 ID
                user.setOauthProviderId(providerId);
                user.setRegisterSource("OAUTH2");
                user.setNickname(nickname);
                user.setAvatar(avatar);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.update(user);
                return user;
            }
        }
        
        // 创建新用户
        user = new User();
        user.setUsername("oauth_" + System.currentTimeMillis());
        user.setPassword("");
        user.setEmail(email);
        user.setNickname(nickname != null ? nickname : "OAuth User");
        user.setAvatar(avatar);
        user.setStatus(1);
        user.setRegisterSource("OAUTH2");
        user.setOauthProviderId(providerId);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }
}