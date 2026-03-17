package com.zhut.user.service.impl;

import com.zhut.user.dto.LoginRequest;
import com.zhut.user.dto.LoginResponse;
import com.zhut.user.dto.RegisterRequest;
import com.zhut.user.entity.User;
import com.zhut.user.jwt.JwtTokenProvider;
import com.zhut.user.mapper.UserMapper;
import com.zhut.user.service.UserService;
import com.zhut.user.util.SnowflakeIdGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            existingUser = userMapper.selectByEmail(request.getEmail());
            if (existingUser != null) {
                throw new RuntimeException("邮箱已被注册");
            }
        }
        
        // 检查手机号是否已存在
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            existingUser = userMapper.selectByPhone(request.getPhone());
            if (existingUser != null) {
                throw new RuntimeException("手机号已被注册");
            }
        }
        
        // 创建新用户
        User user = new User();
        // 使用雪花算法生成唯一 ID
        user.setId(SnowflakeIdGenerator.generate());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus(1);
        user.setRegisterSource("LOCAL");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据账号查询用户
        User user = userMapper.selectByAccount(request.getAccount());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        
        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId());
        
        // 生成 JWT Token
        String token = jwtTokenProvider.generateToken(user);
        
        // 构建响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn(jwtTokenProvider.getExpirationTime());
        loginResponse.setUserId(user.getId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setNickname(user.getNickname());
        loginResponse.setAvatar(user.getAvatar());
        loginResponse.setEmail(user.getEmail());
        loginResponse.setPhone(user.getPhone());
        
        return loginResponse;
    }
    
    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public User getByAccount(String account) {
        return userMapper.selectByAccount(account);
    }
    
    @Override
    public User getByOauthProviderId(String oauthProviderId) {
        return userMapper.selectByOauthProviderId(oauthProviderId);
    }
    
    @Override
    @Transactional
    public User createOrUpdateOauth2User(String providerId, String email, String nickname, String avatar) {
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
        // 使用雪花算法生成唯一 ID
        user.setId(SnowflakeIdGenerator.generate());
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
    
    @Override
    @Transactional
    public void updateLastLoginTime(Long id) {
        userMapper.updateLastLoginTime(id);
    }
}