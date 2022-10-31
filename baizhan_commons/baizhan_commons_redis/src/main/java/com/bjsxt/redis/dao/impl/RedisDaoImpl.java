package com.bjsxt.redis.dao.impl;

import com.bjsxt.redis.dao.RedisDao;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 通用Redis数据访问实现
 */
public class RedisDaoImpl implements RedisDao {
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> void set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> void set(String key, T value, long times, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, times, unit);
    }

    @Override
    public <T> boolean setnx(String key, T value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    @Override
    public <T> boolean setnx(String key, T value, long times, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, times, unit);
    }

    @Override
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void del(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void expire(String key, long times, TimeUnit unit) {
        redisTemplate.expire(key, times, unit);
    }

    @Override
    public long ttl(String key) {
        return redisTemplate.getExpire(key);
    }

    @Override
    public void persist(String key) {
        redisTemplate.persist(key);
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
