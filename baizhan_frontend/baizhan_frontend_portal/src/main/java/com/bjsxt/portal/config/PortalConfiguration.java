package com.bjsxt.portal.config;

import com.bjsxt.config.RedisConfig;
import com.bjsxt.redis.dao.RedisDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 门户系统的配置类型，用于初始化当前系统中需要的环境。
 */
@Configuration
public class PortalConfiguration extends RedisConfig {
    /**
     * 创建当前系统中需要使用的RedisTemplate对象。
     * 1. 自定义创建。
     * 2. 使用父类型中的模板方法创建对象
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        return super.redisTemplate(factory);
    }

    /**
     * 创建当前系统中需要使用的RedisDao对象。
     * @param redisTemplate
     * @return
     */
    @Bean
    public RedisDao redisDao(RedisTemplate<String, Object> redisTemplate){
        return super.redisDao(redisTemplate);
    }
}
