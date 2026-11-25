package com.walletcore.service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PinAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final int MAX_TRANSACTION_RETRIES = 10;

    private final StringRedisTemplate redisTemplate;

    private enum Action {
        CHECK, FAILURE, SUCCESS
    }

    public PinAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkAllowed(UUID userId) {
        execute(userId, Action.CHECK);
    }

    public void recordFailure(UUID userId) {
        execute(userId, Action.FAILURE);
    }

    public void recordSuccess(UUID userId) {
        execute(userId, Action.SUCCESS);
    }

    private void execute(UUID userId, Action action) {
        String key = "walletcore:pin:attempts:" + userId;
        for (int retry = 0; retry < MAX_TRANSACTION_RETRIES; retry++) {
            Long remainingSeconds;
            try {
                remainingSeconds = redisTemplate.execute(new SessionCallback<Long>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <K, V> Long execute(RedisOperations<K, V> operations) {
                        return updateAttempts((RedisOperations<String, String>) operations, key, action);
                    }
                });
            } catch (DataAccessException | NumberFormatException exception) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Không thể xác thực PIN lúc này, vui lòng thử lại sau", exception);
            }

            if (remainingSeconds == null) {
                continue;
            }
            if (remainingSeconds > 0) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Nhập sai PIN quá nhiều lần, vui lòng thử lại sau " + remainingSeconds + " giây");
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Có nhiều yêu cầu xác thực PIN đồng thời, vui lòng thử lại sau");
    }

    private Long updateAttempts(RedisOperations<String, String> operations, String key, Action action) {
        boolean transactionStarted = false;
        try {
            operations.watch(key);
            String value = operations.opsForValue().get(key);
            long attempts = value == null ? 0 : Long.parseLong(value);
            Long ttl = operations.getExpire(key, TimeUnit.SECONDS);

            if (ttl == null) {
                operations.unwatch();
                return null;
            }
            if (attempts >= MAX_ATTEMPTS) {
                operations.unwatch();
                return Math.max(ttl, 1);
            }
            if (action == Action.CHECK) {
                operations.unwatch();
                return 0L;
            }

            operations.multi();
            transactionStarted = true;
            boolean locksUser = action == Action.FAILURE && attempts + 1 >= MAX_ATTEMPTS;
            if (action == Action.FAILURE) {
                operations.opsForValue().increment(key);
                if (locksUser) {
                    operations.expire(key, LOCK_DURATION);
                } else if (attempts == 0 || ttl < 0) {
                    operations.expire(key, ATTEMPT_WINDOW);
                }
            } else {
                operations.delete(key);
            }

            var results = operations.exec();
            transactionStarted = false;
            if (results == null || results.isEmpty()) {
                return null;
            }
            return locksUser ? LOCK_DURATION.getSeconds() : 0L;
        } catch (RuntimeException exception) {
            try {
                if (transactionStarted) {
                    operations.discard();
                } else {
                    operations.unwatch();
                }
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }
}
