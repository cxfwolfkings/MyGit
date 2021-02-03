package com.example.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;

/**
 * 在Controller类上必须打上@Controller注解，表明该类为Controller类，
 * 并扫描到Spring容器中，返回值为html名称，返回值类型必须为String
 * @author Colin
 */
@Controller
public class TestController {

    @Autowired
    private DataSource dataSource;

    /**
     * RequestMapping请求映射地址，如果是本地测试localhost:端口/hello
     * @return hello
     */
    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }
}
