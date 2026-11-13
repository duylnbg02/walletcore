package com.walletcore.service.impl;

import com.walletcore.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(String key, String value, Duration ttl) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, ttl);

        return Boolean.TRUE.equals(result);
    }

    @Override
    public void unlock(String key, String value) {
        // TODO: Dùng Lua script để chỉ xóa lock
        // khi value hiện tại trùng với value của request.
        // Không dùng redisTemplate.delete(key) trực tiếp.
    }
}