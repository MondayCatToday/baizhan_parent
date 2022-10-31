package com.bjsxt.cart.config;

import com.bjsxt.cart.interceptor.CartInterceptor;
import com.bjsxt.config.RedisConfig;
import com.bjsxt.redis.dao.RedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CartConfiguration extends RedisConfig implements WebMvcConfigurer {
    @Autowired
    private CartInterceptor cartInterceptor;
    /**
     * 增加拦截器配置
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartInterceptor)
                .addPathPatterns("/**");
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        return super.redisTemplate(factory);
    }

    @Bean
    public RedisDao redisDao(RedisTemplate<String, Object> template){
        return super.redisDao(template);
    }
}
