package com.example.control;

import com.example.service.LoginService;
import com.example.vo.LoginRequestVO;
import com.example.vo.LoginResponseVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseVO> login(@RequestBody LoginRequestVO request) {
        LoginResponseVO response = loginService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 验证码登录
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login/code")
    public ResponseEntity<LoginResponseVO> loginWithCode(@RequestBody LoginRequestVO request) {
        LoginResponseVO response = loginService.loginWithCode(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}