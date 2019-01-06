package com.colin.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * Servlet Filter implementation class BaseFilter
 * 什么是过滤器？
 *   servlet规范中定义的一种特殊的类，可以对servlet容器(web服务器)的调用过程进行拦截。
 * 编程
 *   step1 写一个java类，实现Filter接口。
 *   step2 在doFilter方法里面，实现拦截的逻辑。
 *   step3   配置过滤器(web.xml)			
 * 过滤器的优先级
 *   如果有多个过滤器都满足条件，则容器会依据<filter-mapping>的先后顺序来依次调用各个过滤器。
 * 初始化参数
 *   可以使用<init-param>给过滤器添加初始化参数，然后在过滤器类当中，使用FilterConfig.getInitParameter(String name)来访问该参数。
 * 过滤器的优点
 *   a.可以将多个组件相同的功能集中写在过滤器里面，方便代码的维护。
 *   b.可以实现代码的“可插拔性"，即增加或者减少某个功能模块，不会影响到整个程序的运行。
 *   
 */
@WebFilter("/BaseFilter")
public class BaseFilter implements Filter {

    /**
     * Default constructor. 
     */
    public BaseFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		// place your code here

		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
