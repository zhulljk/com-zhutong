package com.zhut.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * OAuth2 服务不可用异常
 * 当 OAuth2 提供商服务暂时不可用时抛出
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class OAuth2ServiceUnavailableException extends RuntimeException {

    public OAuth2ServiceUnavailableException(String message) {
        super(message);
    }

    public OAuth2ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}