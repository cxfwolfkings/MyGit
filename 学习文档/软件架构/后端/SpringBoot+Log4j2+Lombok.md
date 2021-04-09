# SpringBoot+Log4j2+Lombok

项目中使用log4j2打印日志是常用的方法，如果集成lombok插件，会使日志变的更简单可控。

下面，我们主要就使用IDEA编译器，在SpringBoot框架下，使用Log4j2+Lombok集成日志的方法：

**1、pom.xml文件中添加依赖**

要点：一定要去掉springboot默认的日志配置，否则会产生冲突，然后配置Log4j2依赖和Lombok依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <!-- 去掉默认配置 -->
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
         </exclusion>
    </exclusions>
</dependency>
<!-- 引入log4j2依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
<!--引入lombok依赖-->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.4</version>
    <scope>provided</scope>
</dependency>
```

**2、创建log4j2配置文件**

在resources中引入log4j2.xml，内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--设置log4j2的自身log级别为warn-->
<!--日志级别以及优先级排序: OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL -->
<!--Configuration后面的status，这个用于设置log4j2自身内部的信息输出，可以不设置，
    当设置成trace时，你会看到log4j2内部各种详细输出-->
<!--monitorInterval：Log4j能够自动检测修改配置 文件和重新配置本身，设置间隔秒数-->
<configuration status="warn" monitorInterval="30">
    <!--先定义所有的appender-->
    <appenders>
        <!--这个输出控制台的配置-->
        <console name="Console" target="SYSTEM_OUT">
            <!--输出日志的格式-->
            <PatternLayout pattern="[%d{HH:mm:ss:SSS}] [%p] - %l - %m%n"/>
        </console>
        <!--文件会打印出所有信息，这个log每次运行程序会自动清空，由append属性决定，这个也挺有用的，适合临时测试用-->
        <File name="log" fileName="log/test.log" append="false">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} %-5level %class{36} %L %M - %msg%xEx%n"/>
        </File>
        <!-- 这个会打印出所有的info及以下级别的信息，每次大小超过size，
        则这size大小的日志会自动存入按年份-月份建立的文件夹下面并进行压缩，作为存档-->
        <RollingFile name="RollingFileInfo" fileName="${sys:user.home}/logs/hpaasvc/info.log"
                     filePattern="${sys:user.home}/logs/hpaasvc/$${date:yyyy-MM}/info-%d{yyyy-MM-dd}-%i.log">
            <Filters>
                <!--控制台只输出level及以上级别的信息（onMatch），其他的直接拒绝（onMismatch）-->
                <ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY"/>
                <ThresholdFilter level="WARN" onMatch="DENY" onMismatch="NEUTRAL"/>
            </Filters>
            <PatternLayout pattern="[%d{HH:mm:ss:SSS}] [%p] - %l - %m%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="100 MB"/>
            </Policies>
        </RollingFile>
 
        <RollingFile name="RollingFileWarn" fileName="${sys:user.home}/logs/hpaasvc/warn.log"
                     filePattern="${sys:user.home}/logs/hpaasvc/$${date:yyyy-MM}/warn-%d{yyyy-MM-dd}-%i.log">
            <Filters>
                <ThresholdFilter level="WARN" onMatch="ACCEPT" onMismatch="DENY"/>
                <ThresholdFilter level="ERROR" onMatch="DENY" onMismatch="NEUTRAL"/>
            </Filters>
            <PatternLayout pattern="[%d{HH:mm:ss:SSS}] [%p] - %l - %m%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="100 MB"/>
            </Policies>
            <!-- DefaultRolloverStrategy属性如不设置，则默认为最多同一文件夹下7个文件，这里设置了20 -->
            <DefaultRolloverStrategy max="20"/>
        </RollingFile>
 
        <RollingFile name="RollingFileError" fileName="${sys:user.home}/logs/hpaasvc/error.log"
                     filePattern="${sys:user.home}/logs/hpaasvc/$${date:yyyy-MM}/error-%d{yyyy-MM-dd}-%i.log">
            <ThresholdFilter level="ERROR"/>
            <PatternLayout pattern="[%d{HH:mm:ss:SSS}] [%p] - %l - %m%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="100 MB"/>
            </Policies>
        </RollingFile>
 
    </appenders>
    <!--然后定义logger，只有定义了logger并引入的appender，appender才会生效-->
    <loggers>
        <!--过滤掉spring和hibernate的一些无用的debug信息-->
        <logger name="org.springframework" level="INFO">
        </logger>
        <logger name="org.mybatis" level="INFO">
        </logger>
        <root level="all">
            <appender-ref ref="Console"/>
            <!--<appender-ref ref="RollingFileInfo"/>-->
            <!--<appender-ref ref="RollingFileWarn"/>-->
            <!--<appender-ref ref="RollingFileError"/>-->
        </root>
    </loggers>
</configuration>
```

**3、添加lombok插件**

IDEA编译器：点击IDEA的【file】-->【Settings】-->【Plugins】，在搜索框中输入“lombok”，安装完重启idea即可。

**4、log4j2和lombok的使用**

使用log4j2只需要在相关的类上面添加@Slf4j/@Slf4j注解即可，该注解由lombok提供：

```java
import lombok.extern.log4j.Log4j2;
 
@Log4j2
public class CommonUtils {
    public static String getTest() {
        log.info("这是我的测试方法，不需要定义Logger属性，即可输出日志文件");
        return "hello,this is my test demo";
    }
}
```

@Data也是lombok提供的，免去了实体类中getter和setter方法，代码更简洁，编译的时候会自动生成getter和setter方法：

```java
import lombok.Data;

@Data
public class StudentDTO {
    private String name;
    private int age;
    private String sex;
    private String class;
    private String address;
    // 不需要再手动生成get()和set()
}
```

