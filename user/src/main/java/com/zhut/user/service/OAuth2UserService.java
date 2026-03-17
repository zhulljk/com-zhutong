package com.zhut.user.service;

import com.zhut.user.exception.OAuth2ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2 用户服务
 * 带有熔断器保护的 OAuth2 用户信息服务
 */
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(OAuth2UserService.class);
    private static final String OAUTH2_CIRCUIT_BREAKER = "oauth2UserService";

    /**
     * 加载 OAuth2 用户信息（带熔断器保护）
     * @param userRequest OAuth2 用户请求
     * @return OAuth2 用户信息
     */
    @Override
    @CircuitBreaker(name = OAUTH2_CIRCUIT_BREAKER, fallbackMethod = "loadUserFallback")
    @Retry(name = OAUTH2_CIRCUIT_BREAKER, fallbackMethod = "loadUserFallback")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("正在加载 OAuth2 用户信息，注册中心：{}", userRequest.getClientRegistration().getRegistrationId());
        return super.loadUser(userRequest);
    }

    /**
     * 熔断器回退方法
     * 当 OAuth2 服务不可用时返回降级结果
     */
    public OAuth2User loadUserFallback(OAuth2UserRequest userRequest, Throwable t) {
        log.error("OAuth2 服务调用失败，使用回退策略。注册中心：{}, 错误：{}", 
                userRequest.getClientRegistration().getRegistrationId(), t.getMessage());
        // 抛出异常，让上层处理
        throw new OAuth2ServiceUnavailableException("OAuth2 服务暂时不可用，请稍后重试", t);
    }
}