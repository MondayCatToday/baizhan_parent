package com.bjsxt.config;

import com.bjsxt.redis.dao.RedisDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class DetailsConfiguration extends RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        return super.redisTemplate(factory);
    }

    @Bean
    public RedisDao redisDao(RedisTemplate<String, Object> template){
        return super.redisDao(template);
    }
}
