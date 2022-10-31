package com.bjsxt.redis.dao;

import java.util.concurrent.TimeUnit;

/**
 * 定义的Redis数据访问通用接口。
 */
public interface RedisDao {
    // 保存数据
    <T> void set(String key, T value);
    // 保存数据，并同时设置有效时长
    <T> void set(String key, T value, long times, TimeUnit unit);
    // 保存新数据，key存在，不保存并返回false，key不存在，保存并返回true。
    <T> boolean setnx(String key, T value);
    // 保存新数据，同时设置有效时长
    <T> boolean setnx(String key, T value, long times, TimeUnit unit);
    // 根据key查询value
    <T> T get(String key);
    // 根据key，删除键值对
    void del(String key);
    // 设置键值对的有效时长
    void expire(String key, long times, TimeUnit unit);
    // 查询键值对的剩余有效时长
    long ttl(String key);
    // 删除键值对的有效时长
    void persist(String key);
}
