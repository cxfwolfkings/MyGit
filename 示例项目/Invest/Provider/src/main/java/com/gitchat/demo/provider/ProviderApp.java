package com.gitchat.demo.provider;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基于 dubbo-spring-boot-starter 搭建服务提供方
 * @author 23907
 */
@SpringBootApplication
@EnableDubboConfiguration
@RestController
@ComponentScan(basePackages = { "com.gitchat.demo.provider.service" })
public class ProviderApp {
    @RequestMapping("/")
    String home() {
        return "Hello Demo!";
    }
    public static void main(String[] args) {
        SpringApplication.run(ProviderApp.class, args);
    }
}
