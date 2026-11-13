package com.walletcore.service;

import java.time.Duration;

public interface RedisService {

    void set(String key, String value, Duration ttl);

    String get(String key);

    void delete(String key);
}