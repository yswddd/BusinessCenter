package com.business.db;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class DBServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DBServiceApplication.class, args);
    }
}
