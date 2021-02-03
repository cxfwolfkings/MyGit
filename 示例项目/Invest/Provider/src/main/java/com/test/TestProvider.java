package com.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 基于 Spring 配置的服务提供方搭建
 * @author 23907
 */
public class TestProvider {

	public static void main(String[] arg) throws InterruptedException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:provider.xml");
	    //挂起当前线程，如果没有改行代码，服务提供者进程会消亡，服务消费者就发现不了提供者了
		Thread.currentThread().join();
	}
}
