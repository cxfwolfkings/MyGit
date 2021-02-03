package com.wind.fmart.control;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 23907
 */
@RestController
@RequestMapping(value = "/restful")
public class TestRestfulInterfaceController{
    /**
     * 示例链接：http://localhost:8080/restful/test?name=Colin
     * @param name 姓名
     * @return hello {name}
     */
    @RequestMapping("/test")
    public String testRestfulInterface(@RequestParam("name") String name) {
        return "hello " + name;
    }
}
