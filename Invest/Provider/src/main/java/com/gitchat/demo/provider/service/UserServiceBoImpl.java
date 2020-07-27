package com.gitchat.demo.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.test.Person;
import com.test.UserServiceBo;
import org.springframework.stereotype.Component;

/**
 * 用户业务处理类
 * @author 23907
 */
@Service(interfaceClass = UserServiceBo.class,group="dubbo",version="1.0.0")
@Component
public class UserServiceBoImpl implements UserServiceBo {
    @Override
    public String testPojo(Person person) {
        return person.toString();
    }

    @Override
    public String sayHello(String name) {
        return "hello:" + name;
    }

    @Override
    public String sayHello2(String name) {
        return null;
    }
}
