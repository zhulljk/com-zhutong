package com.zhut.user.controller;

import com.zhut.user.common.Result;
import com.zhut.user.dto.LoginRequest;
import com.zhut.user.dto.LoginResponse;
import com.zhut.user.dto.RegisterRequest;
import com.zhut.user.entity.User;
import com.zhut.user.jwt.JwtTokenProvider;
import com.zhut.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录（本地登录）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 刷新 Access Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            // 提取 Refresh Token（去掉 "Bearer " 前缀）
            String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
            
            // 验证 Refresh Token
            if (!jwtTokenProvider.validateRefreshToken(token)) {
                return Result.error("Refresh Token 已过期或无效");
            }
            
            // 获取用户 ID
            Long userId = jwtTokenProvider.getUserIdFromRefreshToken(token);
            User user = userService.getById(userId);
            
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 生成新的 Access Token
            String newAccessToken = jwtTokenProvider.refreshAccessToken(token, user);
            if (newAccessToken == null) {
                return Result.error("Token 刷新失败");
            }
            
            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(token); // 继续使用旧的 Refresh Token
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenProvider.getAccessTokenExpirationTime());
            response.setRefreshExpiresIn(jwtTokenProvider.getRefreshTokenExpirationTime());
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("Token 刷新失败：" + e.getMessage());
        }
    }
    
    /**
     * OAuth2 登录页面（重定向到 OAuth2 提供商）
     */
    @GetMapping("/oauth2/login")
    public void oauth2Login() {
        // 由 Spring Security 处理重定向
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<CurrentUserVO> getCurrentUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        if (user == null) {
            return Result.error("未登录");
        }
        // 从 SecurityContext 获取完整用户信息
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof User) {
            User u = (User) auth.getPrincipal();
            return Result.success(new CurrentUserVO(u.getId(), u.getUsername(), u.getNickname(), u.getEmail(), u.getPhone(), u.getAvatar()));
        }
        return Result.error("用户信息获取失败");
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            jwtTokenProvider.invalidateToken(token);
        }
        return Result.success();
    }
    
    /**
     * 当前用户信息 VO
     */
    public static class CurrentUserVO {
        private final Long id;
        private final String username;
        private final String nickname;
        private final String email;
        private final String phone;
        private final String avatar;
        
        public CurrentUserVO(Long id, String username, String nickname, String email, String phone, String avatar) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.email = email;
            this.phone = phone;
            this.avatar = avatar;
        }
        
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getNickname() { return nickname; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getAvatar() { return avatar; }
    }
}