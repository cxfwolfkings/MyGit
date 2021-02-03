package com.test;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;

/**
 * 基于Dubbo API 方式的服务提供方搭建
 * @author 23907
 */
public class TestProviderApi {
 
	public static void main(String[] arg) throws InterruptedException {
		
		//（4.3.1-1）等价于	<bean id="userService" class="com.test.UserServiceImpl" />
		UserServiceBo userService = new UserServiceImpl();
		//（4.3.1-2）等价于 	<dubbo:application name="dubboProvider" />
		ApplicationConfig application = new ApplicationConfig();
		application.setName("dubboProvider");
		
		//（4.3.1-3）等价于 	<dubbo:registry address="zookeeper://127.0.0.1:2181" />
		RegistryConfig registry = new RegistryConfig();
		registry.setAddress("121.196.182.26:2181");
		registry.setProtocol("zookeeper");

		// （4.3.1-4）等价于	<dubbo:protocol name="dubbo" port="20880" />
		ProtocolConfig protocol = new ProtocolConfig();
		protocol.setName("dubbo");
		protocol.setPort(20880);

        //4.3.1-5）等价于 	<dubbo:monitor protocol="registry" />
		MonitorConfig monitorConfig = new MonitorConfig();
		monitorConfig.setProtocol("registry");

		//4.3.1-6）等价于 <dubbo:service interface="com.test.UserServiceBo" ref="userService"
		//group="dubbo"  version="1.0.0" timeout="3000"/>
		// 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
		ServiceConfig<UserServiceBo> service = new ServiceConfig<UserServiceBo>();
		service.setApplication(application);
		service.setMonitor(monitorConfig);
		// 多个注册中心可以用setRegistries()
		service.setRegistry(registry);
		// 多个协议可以用setProtocols()
		service.setProtocol(protocol);
		service.setInterface(UserServiceBo.class);
		service.setRef(userService);
		service.setVersion("1.0.0");
		service.setGroup("dubbo");
		service.setTimeout(3000);
		service.export();

	    //4.3.1-8） 挂起当前线程
		Thread.currentThread().join();
	}
}
