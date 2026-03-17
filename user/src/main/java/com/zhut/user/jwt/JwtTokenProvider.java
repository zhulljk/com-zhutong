package com.zhut.user.jwt;

import com.zhut.user.entity.User;
import com.zhut.user.util.RedisTokenStore;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者
 * 集成 Redis 实现 token 存储和续期
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    @Value("${jwt.secret:zhutong-secret-key-for-jwt-token-generation-must-be-long-enough}")
    private String secret;
    
    @Value("${jwt.expiration:7200000}")
    private Long expiration;
    
    private final RedisTokenStore redisTokenStore;
    
    /**
     * 生成 JWT Token 并存储到 Redis
     * @param user 用户信息
     * @return JWT Token
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        SecretKey key = getSigningKey();
        
        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("nickname", user.getNickname())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
        
        // 存储到 Redis，设置 2 小时过期
        redisTokenStore.storeToken(user.getId(), token);
        
        return token;
    }
    
    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 验证 Token 是否有效
     * 1. 验证 JWT 签名
     * 2. 验证 Redis 中是否存在
     */
    public boolean validateToken(String token) {
        try {
            // 首先验证 JWT 签名是否有效
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            
            // 获取用户 ID
            Claims claims = getClaims(token);
            Long userId = claims.get("userId", Long.class);
            
            // 验证 Redis 中是否存在该 token
            if (userId != null) {
                return redisTokenStore.validateToken(userId, token);
            }
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 验证并刷新 Token
     * 验证成功后重置过期时间为 2 小时
     * @param token JWT Token
     * @return 是否验证成功
     */
    public boolean validateAndRefreshToken(String token) {
        try {
            // 首先验证 JWT 签名是否有效
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            
            // 获取用户 ID
            Claims claims = getClaims(token);
            Long userId = claims.get("userId", Long.class);
            
            // 验证 Redis 中是否存在该 token
            if (userId != null && redisTokenStore.validateToken(userId, token)) {
                // 刷新 Token 过期时间
                redisTokenStore.refreshToken(userId);
                return true;
            }
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 刷新 Token
     * @param token JWT Token
     */
    public void refreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            Long userId = claims.get("userId", Long.class);
            if (userId != null) {
                redisTokenStore.refreshToken(userId);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // 忽略刷新失败
        }
    }
    
    /**
     * 使 Token 失效（登出时使用）
     * @param token JWT Token
     */
    public void invalidateToken(String token) {
        try {
            Claims claims = getClaims(token);
            Long userId = claims.get("userId", Long.class);
            if (userId != null) {
                redisTokenStore.deleteToken(userId);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // 忽略
        }
    }
    
    /**
     * 获取过期时间（秒）
     */
    public Long getExpirationTime() {
        return expiration / 1000;
    }
    
    /**
     * 获取 Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}