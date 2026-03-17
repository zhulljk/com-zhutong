package com.zhut.user.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 配置类
 * 配置 OAuth2 服务的熔断器
 */
@Configuration
public class Resilience4jConfig {

    /**
     * 配置 OAuth2 服务的熔断器
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                // 滑动窗口大小（失败率计算基于此窗口内的请求）
                .slidingWindowSize(10)
                // 滑动窗口类型：COUNT_BASED（基于请求数）或 TIME_BASED（基于时间）
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 最小调用次数，达到此次数后才计算失败率
                .minimumNumberOfCalls(5)
                // 失败率阈值，超过此阈值熔断器打开
                .failureRateThreshold(50)
                // 慢调用率阈值
                .slowCallRateThreshold(80)
                // 慢调用时长阈值
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                // 熔断器打开后，经过此时长自动切换到半开状态
                .waitDurationInOpenState(Duration.ofSeconds(30))
                // 半开状态下允许通过的请求数
                .permittedNumberOfCallsInHalfOpenState(3)
                // 自动从注册表中移除熔断器的时间（如果长时间未使用）
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                // 记录为异常的异常类型
                .recordExceptions(Exception.class)
                // 忽略的异常类型（不记录为失败）
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    /**
     * 配置超时限制器
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                // 超时时长
                .timeoutDuration(Duration.ofSeconds(5))
                // 是否取消未来执行
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(timeLimiterConfig);
    }
}