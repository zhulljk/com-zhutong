package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.service.LoginService;
import com.example.service.VerificationService;
import com.example.util.JwtUtil;
import com.example.vo.LoginRequestVO;
import com.example.vo.LoginResponseVO;
import com.example.vo.UserVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录服务实现类
 */
@Service
public class LoginServiceImpl implements LoginService {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;
    private final VerificationService verificationService;
    private final StringRedisTemplate stringRedisTemplate;

    public LoginServiceImpl(UserDao userDao,
                           JwtUtil jwtUtil,
                           VerificationService verificationService,
                           StringRedisTemplate stringRedisTemplate) {
        this.userDao = userDao;
        this.jwtUtil = jwtUtil;
        this.verificationService = verificationService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public LoginResponseVO login(LoginRequestVO request) {
        if (request == null || !StringUtils.hasText(request.getLoginType())) {
            return createErrorResponse("登录类型不能为空");
        }

        String loginType = request.getLoginType().toLowerCase();
        
        // 密码登录 - 返回 JWT token
        if ("password".equals(loginType)) {
            return loginWithPassword(request);
        }
        // 邮箱验证码登录 - 返回 OAuth2 URI
        else if ("email".equals(loginType)) {
            return loginWithCode(request);
        }
        // 手机验证码登录 - 返回 OAuth2 URI
        else if ("phone".equals(loginType)) {
            return loginWithCode(request);
        }
        
        return createErrorResponse("不支持的登录类型：" + loginType);
    }

    @Override
    public LoginResponseVO loginWithCode(LoginRequestVO request) {
        if (request == null || !StringUtils.hasText(request.getLoginType())) {
            return createErrorResponse("登录类型不能为空");
        }

        String loginType = request.getLoginType().toLowerCase();
        
        // 验证验证码
        if (!StringUtils.hasText(request.getCode())) {
            return createErrorResponse("验证码不能为空");
        }

        String redisKey = buildRedisKey(loginType, request.getAccount());
        String storedCode = (String) stringRedisTemplate.opsForHash().get(redisKey, "code");

        if (!request.getCode().equals(storedCode)) {
            return createErrorResponse("验证码错误");
        }

        // 验证码正确，查找或创建用户
        UserVO user = findOrCreateUser(request.getAccount(), loginType);
        if (user == null) {
            return createErrorResponse("用户不存在");
        }

        // 删除已使用的验证码
        stringRedisTemplate.delete(redisKey);

        // 返回 OAuth2 类型的登录结果
        LoginResponseVO response = new LoginResponseVO();
        response.setAuthType("oauth2");
        response.setSuccess(true);
        response.setMessage("登录成功");
        response.setUser(user);
        // 生成 OAuth2 授权 URI（模拟）
        String oauth2Uri = "/oauth2/authorization/" + loginType + "?code=" + request.getCode();
        response.setOauth2Uri(oauth2Uri);
        
        return response;
    }

    /**
     * 密码登录
     */
    private LoginResponseVO loginWithPassword(LoginRequestVO request) {
        if (!StringUtils.hasText(request.getAccount())) {
            return createErrorResponse("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getCredential())) {
            return createErrorResponse("密码不能为空");
        }

        // 根据用户名查找用户
        UserVO user = findByUsername(request.getAccount());
        if (user == null) {
            return createErrorResponse("用户不存在");
        }

        // 验证密码
        if (!request.getCredential().equals(user.getPassword())) {
            return createErrorResponse("密码错误");
        }

        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        LoginResponseVO response = new LoginResponseVO();
        response.setAuthType("jwt");
        response.setToken(token);
        response.setSuccess(true);
        response.setMessage("登录成功");
        response.setUser(user);

        return response;
    }

    /**
     * 根据用户名查找用户
     */
    private UserVO findByUsername(String username) {
        // 这里需要通过用户名查询用户
        // 由于当前 UserDao 只支持按 ID 查询，需要扩展或临时处理
        // 临时方案：遍历所有用户查找
        try {
            com.example.vo.PageVO<UserVO> page = userDao.findPage(1, 1000);
            if (page != null && page.items() != null) {
                for (UserVO user : page.items()) {
                    if (username.equals(user.getUsername()) || 
                        username.equals(user.getEmail()) || 
                        username.equals(user.getPhone())) {
                        return user;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }

    /**
     * 查找或创建用户
     */
    private UserVO findOrCreateUser(String account, String loginType) {
        UserVO user = findByUsername(account);
        if (user != null) {
            return user;
        }

        // 创建新用户
        UserVO newUser = new UserVO();
        newUser.setUsername(account);
        
        if ("email".equals(loginType)) {
            // 如果是邮箱，设置为 email
        } else if ("phone".equals(loginType)) {
            newUser.setPhone(account);
        }
        
        try {
            return userDao.create(newUser);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建 Redis key
     */
    private String buildRedisKey(String loginType, String medium) {
        return "verify:" + loginType + ":" + medium.toLowerCase().trim();
    }

    /**
     * 创建错误响应
     */
    private LoginResponseVO createErrorResponse(String message) {
        LoginResponseVO response = new LoginResponseVO();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}