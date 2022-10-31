package com.bjsxt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RabbitReceiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(RabbitReceiveApplication.class,args);
    }
}
