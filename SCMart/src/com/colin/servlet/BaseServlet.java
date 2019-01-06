package com.colin.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class BaseServlet
 * 特性：单例，一个类只有一个对象；当然可能存在多个Servlet类！线程不安全，所以效率高！Servlet类由我们写，但对象由服务器创建，并且由服务器来调用相应的方法。
 * 
 * servlet线程安全问题：servlet是线程不安全的
 * A.在默认情况下，有可能多个请求会访问同一个servlet实例
 * B.如果多个线程对servlet实例的属性进行修改，则会发生线程安全问题
 * 解决办法：
 * A.加锁。可以对整个service方法（尽量少用）或者对有线程安全问题的代码块使用synchronized加锁
 * B.Single ThreadModel接口（不建议用）。让servlet类实现该接口，容器对每一个请求都会创建一个新的servlet实例。
 * C.用局部变量代替实例变量
 * 
 * 转发和重定向：
 * 1、请求转发是通过RequestDispatcher对象的forward()方法完成的；重定向是通过HttpServletResponse对象的sendRediect()方法完成的
 * 2、请求转发是在一个请求中跨越多个动态资源（jsp/servlet），所以多个动态资源之间可以共享request数据；
 *   重定向是两次请求，第一次请求服务器响应给客户端的是302，以及Location响应头，通知客户端再次去请求新的资源，所以客户端又发出第二次请求。所以重定向中被请求的多个动态资源之间不能共享request数据。
 * 3、请求转发后，地址栏的url不会改变，因为是一个请求；重定向后，地址的url会改变，因为是两个请求
 * 
 * 数据包的结构：
 * 1）请求数据包：
 *   A.请求行：请求方式（get/post），请求资源路径（端口号后的内容），协议的类型与版本
 *   B.若干消息头（消息头是由w3c定义的一些有特殊含义的键值对）
 *   C.实体内容：如果post，请求参数和值放在这；如果get，则包含在请求资源路径里、
 * 2）响应数据包：
 *   A.状态行：协议类型与版本，状态码，状态码的描述
 *   B.若干消息头
 *   C.实体内容：服务器返回给浏览器的处理结果
 *   
 * 
 */
@WebServlet("/BaseServlet")
public class BaseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BaseServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
