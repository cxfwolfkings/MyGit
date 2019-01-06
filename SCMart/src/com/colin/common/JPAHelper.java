package com.colin.common;

/**
 * JPA用于整合现有的ORM技术，可以简化现有Java EE和Java SE应用的对象持久化的开发工作，实现ORM的统一。
 * JPA作为一项对象持久化的标准，不但可以获得Java EE应用服务器的支持，还可以直接在Java SE中使用。
 * JPA必将成为Java持久化解决方案的主流，如果你是Hibernate或者TopLink的等ORM技术的忠实用户，一定要会使用JPA。
 * JPA的总体思想和Hibernate、TopLink、JDO等ORM框架大体一致，包括以下3方面技术：
 * 1、ORM映射元数据
 *   JPA支持XML和JDK 5.0注释（注解）两种元数据形式，元数据描述对象和表之间的映射关系，框架据此将实体对象持久化到数据库表中。
 * 2、Java持久化API
 *   用来操作实体对象，执行CRUD操作，框架在后台替我们完成所有的事情，开发者可以从繁琐的JDBC和SQL代码中解脱出来。
 * 3、查询语言
 *   这是持久化操作中很重要的一个方面，通过面向对象而非面向数据库的查询语言查询数据，避免程序的SQL语句紧密耦合。
 *   
 * 开发JPA依赖的jar文件
 * Hibernate核心包(8个)：
 *   hibernate-distribution-3.3.1.GA
 *   hibernate3.jar
 *   lib\bytecode\cglib\hibernate-cglib-repack-2.1_3.jar
 *   lib\required\*.jar
 * Hibernate注解包(3个)：
 *   hibernate-annotations-3.4.0.GA
 *   hibernate-annotations.jar
 *   lib\ejb3-persistence.jar、
 *   hibernate-commons-annotations.jar
 * Hibernate针对JPA的实现包(3个)：
 *   hibernate-entitymanager-3.4.0.GA
 *   hibernate- entitymanager.jar
 *   lib\test\log4j.jar、
 *   slf4j-log4j12.jar
 * 一般jar文件的完整目录不应包含中文或空格。
 * JPA规范要求在类路径的META-INF目录下放置persistence.xml，文件的名称是固定的，配置模板如：JPAConfig.xml
 * 
 * @author  Colin Chen
 * @create  2018年11月29日 下午10:34:38
 * @modify  2018年11月29日 下午10:34:38
 * @version A.1
 */
public class JPAHelper {

}
