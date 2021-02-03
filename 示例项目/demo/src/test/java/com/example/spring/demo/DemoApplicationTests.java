package com.example.spring.demo;

import com.example.spring.entity.Department;
import com.example.spring.entity.User;
import com.example.spring.service.IUserService;
import com.example.spring.util.Json;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private IUserService userService;

    @Test
    public void create(){
        User user = new User();
//      模拟数据库查询部门信息
        Department department = new Department();
        department.setId(4);
        department.setName("商品部");
        user.setDepartment(department);
        user.setName("张三");
        user.setPassword("123456");
        user.setIdentifyType(0);
        user.setIdentifyNumber("420612345678910001");
        userService.save(user);

    }

    @Test
    public void update(){
        User user = userService.fetchById(2);
        System.out.println(Json.toJson(user));
//      模拟数据库查询部门信息
        Department department = new Department();
        department.setId(4);
        department.setName("商品部");
        user.setDepartment(department);
        System.out.println(Json.toJson(user));
        userService.update(user);
    }

    @Test
    void fetchById(){
        System.out.println(Json.toJson(userService.fetchById(1)));
    }

}
