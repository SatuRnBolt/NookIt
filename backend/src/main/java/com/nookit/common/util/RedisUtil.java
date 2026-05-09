package com.nookit.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 字符串操作的轻量封装。复杂结构请直接注入 {@link StringRedisTemplate}。
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, Duration ttl) {
        return redisTemplate.expire(key, ttl);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long incrementWithExpire(String key, Duration ttl) {
        Long value = redisTemplate.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, ttl);
        }
        return value;
    }

    /**
     * SETNX：仅在 key 不存在时设置。可用于轻量场景的占位锁；高并发请用 Redisson。
     */
    public Boolean setIfAbsent(String key, String value, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }
}
