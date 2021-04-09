# SpringBoot

Spring Boot 未出现之前，我们利用 Spring、Spring MVC 进行项目开发时，整个项目从引入依赖 Jar 包，书写配置文件，打包部署到开发环境、测试环境、生产环境都需要大量人力，但是最终的效果仍不尽如人意，甚至还会给一个项目组带来项目延期的风险等。

随着敏捷开发思想被越来越多的人接受以及开发者对开发效率的不断追求，最终推出了具有颠覆和划时代意义的框架 Spring Boot。

Spring Boot 首先遵循 **习惯优于配置** 原则，言外之意即是尽量使用自动配置让开发者从繁琐的书写配置文件的工作中解放出来；Spring Boot 另外一个比较明显的特点就是尽量不使用原来 Spring 框架中的 XML 配置，而主要使用 **注解** 代替。

Spring Boot 由 Spring、Spring MVC 演化而来，注解也继承自两者。下面我们看下 Spring MVC 中常用的注解：

1. `@Controller`：该注解用于类上，其表明该类是 Spring MVC 的 Controller
2. `@RequestMapping`：该注解主要用来映射 Web 请求，其可以用于类或者方法上
3. `@RequestParam`：该注解主要用于将请求参数数据映射到功能处理方法的参数上
4. `@ResponseBody`：该注解的作用是将方法的返回值放在 Response 中，而不是返回一个页面，其可以用于方法上或者方法返回值前；
5. `@RequestBody`：用于读取 HTTP 请求的内容（字符串），通过 Spring MVC 提供的 HttpMessageConverter 接口将读到的内容转换为 JSON、XML 等格式的数据并绑定到 Controller 方法的参数上；
6. `@PathVariable`：用于接收请求路径参数，将其绑定到方法参数上；
7. `@RestController`：该注解是一个组合注解，只能用于类上，其作用与 `@Controller`、`@ResponseBody` 一起用于类上等价。

>注：在 Spring 4.3 中引进了 `@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping`。

```java
@RestController
@RequestMapping("/springmvc")
public class TestAnnotationController {
    @RequestMapping(value = "/setUserInfo", method = RequestMethod.GET)
    public String setUserInfo(@RequestParam(name = "userName") String userName){
        System.out.println(userName);
        return "success";
    }
}
```



## 参考

- Demo源代码：https://github.com/huangchaobing
- spring-boot源码：https://github.com/spring-projects/spring-boot
- spring-boot文档：https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/html/getting-started.html#getting-started-installing-spring-boot
- https://my.oschina.net/u/200350/blog/3000793