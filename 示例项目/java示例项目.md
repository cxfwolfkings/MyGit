# Hello World

项目整体目录结构：

```txt
MyStudy                                 ——项目名称
     ├── src/main/java                  ——Java 源码根目录
            ├── controller              ——控制层目录
            ├── dao                     ——dao层目录
            ├── entity                  ——实体层目录
            ├── service                 ——业务层目录
                └── impl                ——业务实现类目录
            └── utiles                  ——工具类目录
     ├── src/main/resources             ——配置文件根目录
            └── mappers                 ——mapper文件目录
     ├── src/main/webapp                ——网站 Web 资源
     └── pom.xml                        ——pom文件
``````
项目构建：`mvn clean package`
