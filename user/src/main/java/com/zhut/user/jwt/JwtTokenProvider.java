package com.zhut.user.jwt;

import com.zhut.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者
 * 实现 Access Token + Refresh Token 双 Token 机制
 */
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret:zhutong-secret-key-for-jwt-token-generation-must-be-long-enough}")
    private String secret;
    
    @Value("${jwt.access-token.expiration:900000}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration:604800000}")
    private Long refreshTokenExpiration;
    
    /**
     * 生成 Access Token
     * @param user 用户信息
     * @return Access Token
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        SecretKey key = getSigningKey();
        
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("nickname", user.getNickname())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * 生成 Refresh Token
     * @param user 用户信息
     * @return Refresh Token
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        
        SecretKey key = getSigningKey();
        
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * 从 Access Token 中获取用户 ID
     */
    public Long getUserIdFromAccessToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从 Refresh Token 中获取用户 ID
     */
    public Long getUserIdFromRefreshToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从 Access Token 中获取用户名
     */
    public String getUsernameFromAccessToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 验证 Access Token 是否有效
     * 1. 验证 JWT 签名
     * 2. 验证 token 类型为 access
     * 3. 验证是否过期
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = getClaims(token);
            String type = claims.get("type", String.class);
            return "access".equals(type) && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 验证 Refresh Token 是否有效
     * 1. 验证 JWT 签名
     * 2. 验证 token 类型为 refresh
     * 3. 验证是否过期
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type) && !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 使用 Refresh Token 刷新 Access Token
     * @param refreshToken 刷新令牌
     * @param user 用户信息
     * @return 新的 Access Token，如果刷新失败返回 null
     */
    public String refreshAccessToken(String refreshToken, User user) {
        if (validateRefreshToken(refreshToken)) {
            return generateAccessToken(user);
        }
        return null;
    }
    
    /**
     * 使 Token 失效（登出时使用）
     * 由于不再使用 Redis，客户端只需删除本地存储的 token 即可
     * @param token JWT Token
     */
    public void invalidateToken(String token) {
        // 无状态 token，无需特殊处理
        // 如果需要立即失效，可以考虑使用黑名单机制
    }
    
    /**
     * 获取 Access Token 过期时间（秒）
     */
    public Long getAccessTokenExpirationTime() {
        return accessTokenExpiration / 1000;
    }
    
    /**
     * 获取 Refresh Token 过期时间（秒）
     */
    public Long getRefreshTokenExpirationTime() {
        return refreshTokenExpiration / 1000;
    }
    
    /**
     * 检查 token 是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
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