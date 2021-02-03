# 示例项目

用springBoot来创建单个服务，用SpringCloud来管理这些微服务。

在使用本项目之前，需要对[SpringBoot](http://spring.io/projects/spring-boot)，[freemaker](https://freemarker.apache.org/)，[layui](https://www.layui.com/)，[flyway](https://flywaydb.org/)等基本操作有所了解。

**SpringCloud的五大神兽**

1. 注册/服务发现——Netflix Eureka

   管理服务器地址和ip的

2. 客服端负载均衡——Netflix Ribbon\Feign

   服务请求的分配

3. 断路器——Netflix Hystrix

   对有故障的服务进行处理

4. 服务网关——Netflix Zuul

   微服务的统一入口。

5. 分布式配置——Spring Cloud Config

   对微服务的配置文件做同一管理

**SpringCloud入门步奏**

1. 注册中心：用于管理微服务的地址

   1.1 当微服务可以解决注册的注册表（IP、端口、服务名称）

   1.2 从每个微服务注册微服务地址清单

   1.3 使用心跳检测机制：每N秒发送一个请求到注册表，告诉注册表，我还活着；如果微服务挂掉，心跳检测失败，注册表微服务将更新地址列表

2. 用户管理微服务

3. 订单管理微服务

**第三方包功能：**

1. spring-boot-starter-web包提供访问网页的能力
2. mysql-connector-java 选择访问的数据库
3. mybatis-spring-boot-starter 持久层框架包
4. druid 数据源连接池
5. spring-boot-starter-thymeleaf 解析html模板专用
6. spring-boot-starter-data-redis spring集成redis所需要的包



## 1. 数据库设计

准备两张带有级联关系的数据表User表和Department表，先创建Department表，在User表中维护关联关系，并向Department表中添加数据。

```sql
CREATE TABLE `department` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

INSERT INTO `department` VALUES (1, '研发部');
INSERT INTO `department` VALUES (2, '销售部');
INSERT INTO `department` VALUES (3, '测试部');
INSERT INTO `department` VALUES (4, '商品部');
INSERT INTO `department` VALUES (5, '采购部');

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `identify_type` tinyint(4) NOT NULL,
  `identify_number` varchar(45) NOT NULL,
  `dept_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_dept` (`dept_id`),
  CONSTRAINT `fk_user_dept` FOREIGN KEY (`dept_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
```



## 2. 注册中心配置

先创建个普通的maven项目父级模块，在pom.xml里配置公用的jar包管理





3、创建POJO对象。因添加有lombok，使用@Data注解，可以代替getter/setter方法。生成一个无参构造器和全参构造器。

4、在application.properties中配置MySQL和MyBatis。需要注意：

- MySQL驱动版本不同，driver-class-name使用com.mysql.cj.jdbc.Driver而不是原来的com.mysql.jdbc.Driver。使用原来的会输出过期提示。
- 需要添加`useSSL=false&serverTimezone=UTC`。不使用SSL，并设置时区。若不设置时区，会出现时区错误。
- MyBatis的type-aliases-package设置的是POJO取别名。

5、创建Dao，UserMapper.java

6、在resources资源文件目录下创建mapper文件夹并创建UserMapper.xml文件。

**一句话总结，resultMap中property映射的是对象的属性，column映射的是sql语句中的列，若需要做对应，则需指定对应的列别名**

7、创建service。IBaseService.java，BaseServiceImpl.java，IUserService.java，UserServiceImpl.java

8、启用MapperScan扫描dao，com.example.spring.dao为dao存放的包路径。

9、进行测试：src/test/java/com/example/spring/demo目录下创建DemoApplicationTests.java进行测试。

**注意：**个人建议在查询时最好将所有需要的列写出来，而不要用 `select *`。在级联情况下具有同名字段时，最好另取别名。在查询条件中如果条件是级联表中同名字段，需要指定具体查询哪张表。