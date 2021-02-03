package com.example.spring.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 23907
 * 启动类注解解释
 * SpringBootApplication 启动类必要注解，代表该类为Springboot程序入口
 * ComponentScan 扫描注解，将@Controller,@Service,…扫进容器中
 * @MapperScan Mybatis扫描，用于将javaMapper类，扫描到容器中
 */
@SpringBootApplication
@MapperScan("com.example.spring.dao")
@EnableScheduling
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
