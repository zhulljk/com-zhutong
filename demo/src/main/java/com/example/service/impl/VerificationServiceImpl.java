package com.example.service.impl;

import com.example.service.VerificationService;
import com.example.vo.VerificationRequestVO;
import com.example.vo.VerificationResultVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VerificationServiceImpl implements VerificationService {
    private static final int CODE_LENGTH = 6;
    private static final long CODE_TTL_SECONDS = 300;
    private final StringRedisTemplate stringRedisTemplate;
    private final com.example.service.VerificationDeliveryService deliveryService;

    public VerificationServiceImpl(StringRedisTemplate stringRedisTemplate,
                                   com.example.service.VerificationDeliveryService deliveryService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.deliveryService = deliveryService;
    }

    @Override
    public VerificationResultVO pushCode(VerificationRequestVO request) {
        validateRequest(request);
        String code = generateCode();
        String redisKey = buildRedisKey(request.getPushType(), request.getMedium());
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("loginType", request.getLoginType());
        payload.put("pushType", request.getPushType());
        payload.put("medium", request.getMedium());
        payload.put("code", code);
        payload.put("createdAt", java.time.Instant.now().toString());
        stringRedisTemplate.opsForHash().putAll(redisKey, payload);
        stringRedisTemplate.expire(redisKey, CODE_TTL_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        com.example.service.VerificationDeliveryService.DeliveryResult deliveryResult =
                deliveryService.deliver(request.getPushType(), request.getMedium(), request.getLoginType(), code, CODE_TTL_SECONDS);
        VerificationResultVO result = new VerificationResultVO();
        result.setSuccess(deliveryResult.success());
        result.setMessage(deliveryResult.message());
        result.setCode(code);
        result.setRedisKey(redisKey);
        return result;
    }

    private void validateRequest(VerificationRequestVO request) {
        if (request == null) {
            throw new IllegalArgumentException("request is empty");
        }
        if (!StringUtils.hasText(request.getLoginType())) {
            throw new IllegalArgumentException("loginType is empty");
        }
        if (!StringUtils.hasText(request.getPushType())) {
            throw new IllegalArgumentException("pushType is empty");
        }
        if (!StringUtils.hasText(request.getMedium())) {
            throw new IllegalArgumentException("medium is empty");
        }
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int value = new java.security.SecureRandom().nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", value);
    }

    private String buildRedisKey(String pushType, String medium) {
        return "verify:" + normalize(pushType) + ":" + normalize(medium);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
