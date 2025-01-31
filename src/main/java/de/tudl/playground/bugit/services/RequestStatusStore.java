package de.tudl.playground.bugit.services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RequestStatusStore {

    private final StringRedisTemplate redisTemplate;

    public RequestStatusStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setStatus(String requestId, String status) {
        redisTemplate.opsForValue().set(requestId, status, Duration.ofMinutes(10)); // 10 Minuten gültig
    }

    public String getStatus(String requestId) {
        return redisTemplate.opsForValue().get(requestId);
    }
}

