package com.bjsxt.config;

import com.bjsxt.redis.dao.RedisDao;
import com.bjsxt.redis.dao.impl.RedisDaoImpl;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 定义一个通用的配置模板，其他工程需要使用Redis相关逻辑的时候，
 * 继承当前通用配置模板，可以快速实现配置。
 * 也可以自定义配置。
 */
//@Configuration
public abstract class RedisConfig {
    // 创建RedisTemplate方法
    //@Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }

    // 创建RedisDao对象的方法。
    public RedisDao redisDao(RedisTemplate<String, Object> redisTemplate){
        RedisDaoImpl dao = new RedisDaoImpl();
        dao.setRedisTemplate(redisTemplate);
        return dao;
    }
}
