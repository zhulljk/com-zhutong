package com.zhut.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis Token 存储工具
 * 用于实现 token 的存储、验证和续期
 */
@Component
@RequiredArgsConstructor
public class RedisTokenStore {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * Token 前缀
     */
    private static final String TOKEN_PREFIX = "auth:token:";
    
    /**
     * Token 过期时间（2 小时）
     */
    private static final long TOKEN_EXPIRE_SECONDS = 7200;
    
    /**
     * 存储 Token
     * @param userId 用户 ID
     * @param token JWT Token
     */
    public void storeToken(Long userId, String token) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * 验证 Token 是否存在
     * @param userId 用户 ID
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(Long userId, String token) {
        String key = TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        return token.equals(storedToken);
    }
    
    /**
     * 刷新 Token 过期时间
     * 每次验证成功后调用，重置为 2 小时
     * @param userId 用户 ID
     */
    public void refreshToken(Long userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.expire(key, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * 删除 Token（登出时使用）
     * @param userId 用户 ID
     */
    public void deleteToken(Long userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }
    
    /**
     * 获取 Token 剩余过期时间（秒）
     * @param userId 用户 ID
     * @return 剩余时间
     */
    public Long getExpire(Long userId) {
        String key = TOKEN_PREFIX + userId;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}