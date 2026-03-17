package com.example.service;

public interface VerificationDeliveryService {
    DeliveryResult deliver(String pushType, String medium, String loginType, String code, long ttlSeconds);

    record DeliveryResult(boolean success, String message) {
    }
}
