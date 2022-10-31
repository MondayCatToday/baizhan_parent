package com.bjsxt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 注解是否生效。只看启动类上的注解及，启动类可以扫描到的所有类型上的注解。
 * SessionConfig类上的是注解EnableRedisHttpSession生效。
 */
@SpringBootApplication
public class FrontendPassportApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontendPassportApplication.class,args);
    }
}
