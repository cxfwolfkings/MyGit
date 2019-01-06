package com.colin.listener;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Application Lifecycle Listener implementation class BaseListener
 * 
 * 监听器是什么?
 *   servlet规范当中定义的一种特殊的类，用来监听容器产生一事件并进行相应的处理。事件有两大类：
 *   第一类事件：生命周期相关的事件，指的是当容器创建或者销毁  request,session,ServletContext(servlet上下文)
 *   第二类事件：绑订相关的事件，指的是当调用 setAttribute,removeAttribute方法时，产生的事件。
 * 
 * 如何写一个监听器
 *   step1 写一个java类，实现监听器接口。
 *   step2 在监听器接口声明的方法当中，实现监听的逻辑。
 *   step3 配置(web.xml)。
 * 
 * ServletContext(servlet上下文)
 *   容器在启动的时候，会为每一个应用创建唯一的一个ServletContext实例(符合ServletContext接口的对象),该实例会一直存在，除非容器关闭。
 *   a,如何获得ServletContext实例
 *     方式1:GenericServlet.getServletContext();
 *     方式2:HttpSession.getServletContext();
 *     方式3:ServletConfig.getServletContext();
 *   b,作用：绑订数据：setAttribute，getAttribute，removeAttribute
 *     访问全局的初始化参数：在web.xml文件中，使用<context-param>配置的参数， 称之为全局的初始化参数，写在所有servlet前面。
 *     <init-param>配置得参数只能被包含它的servlet访问
 *     String getInitParameter(String name);
 *     通过逻辑路径获得实际部署时的物理路径：String getRealPath(String path);
 *     和session不同（关闭浏览器后session对象即消失），除非把服务器关闭，否则在servletContext实例中保存的数据会一直存在！
 *     web配置顺序：<context-param> -> <listener> -> <filter> -> <servlet>
 *     
 * 上传文件(扩展)
 *   step1 在表单当中，设置提交方式为post方式，并且，设置enctype属性值为"multipart/form-data"。
 *     <form action="" method="post" enctype="multipart/form-data">
 *     enctype属性的缺省值是"application/x-www-form-urlencoded",通知服务器，浏览器会对表单中的参数值使用url编码，
 *     如果要上传文件，必须设置enctype属性值为"multipart/form-data",通知服务器，浏览器会将表单中的参数值不进行编码，而是以二进制字节数组的方式发送给服务器，
 *     此时，服务器端不能够直接使用request.getParameter(),需要通过InputStream request.getInputStream( )获得输入流，然后分析该流来获得数据。
 *   step2 在服务器端，借助于一些工具来分析InputStream。比如 apache提供的commons-fileupload.jar。
 * 
 * JSP
 *   a、java代码片段  <%  java代码   %>
 *   b、jsp表达式     <%= java表达式 %>
 *   c、jsp声明: 用来为对应的servlet添加相应的属性和方法  <%! %> 其中不可以使用隐含对象。
 * 隐含对象(Jsp内置对象) 9个
 *   在jsp文件中，可以直接使用的对象。因为服务器在生成的servlet类里面已经包含了创建该对象的代码，所以可以直接使用这些对象。
 *   out，request，response，session，application
 *   exception: 当isErrorPage的值为true时，可以通过该隐含对象获得jsp执行过程当中的错误信息。
 *   pageContext: PageContext的实例，容器会为每一个jsp实例(jsp对应的那个servlet对象)创建唯一的一个pageContext对象。
 *     作用1：绑订数据 setAttribute,getAttribute,removeAttribute
 *     作用2：找到其它8个隐含对象
 *   config: ServletConfig实例
 *   page: jsp实例本身
 * 指令
 *   通知服务器，在将.jsp文件转换成.java文件时，做一些额外的处理，比如导包，语法: <%@ 指令名称  属性名称=属性值  %>
 *   a、page指令
 *     import属性：导包，建议写在jsp文件的开头
 *       <%@page import="java.util.*" %>
 *       <%@page import="java.sql.*" %>
 *       也可以合着写一个，使用逗号分隔
 *       <%@page import="java.util.Date, java.util.List" %>
 *     pageEncoding属性：语法：<%@page pageEncoding="utf-8" %>
 *       告诉容器.jsp文件保存时的编码。因为容器需要读取.jsp文件，将其转换成.java文件， 所以需要告诉容器.jsp文件的编码，否则在解码时可能出错。      
 *     contentType属性：语法：<%@page contentType="text/html;charset=utf-8" %>
 *       用来设置response.setContentType()的内容。
 *     session属性:true(缺省)/false,如果为false,则对应的servlet不再提供声明和获得session的语句。也就是说不能使用session隐含对象了
 *     isELIgnored属性:true(缺省)/false, 如果为false,则容器不会忽略el表达式。
 *     errorPage属性: 其值是一个jsp文件，该jsp文件用来处理当前jsp执行过程当中产生的错误。
 *     isErrorPage属性:true/false(缺省),如果为true,表示当前页面为一个错误处理页面。   
 *       可以使用如下步骤来处理jsp产生的错误:
 *       step1 写一个错误处理页面,比如 errorHander.jsp，在该页面当中，使用isErrorPage="true",可以通过exception隐含对象获得错误信息。
 *       step2 对于某个页面，可以使用errorPage="errorHander.jsp"来处理该页面产生的错误。
 *   b、include指令
 *     file属性：在.jsp文件转换成.java文件时，在指令所在的位置插入某个文件的内容。
 *   c、taglib指令
 *     该指令用于引入一个标签库。uri属性:指定标签的命名空间。preifx属性:指定命名空间的前缀
 * 注释
 *   <!--注释内容-->: 注释内容可以是java代码，如果是java代码，会执行，但是执行结果不会显示在客户端。
 *   <%--注释内容--%>:注释的内容不会执行，也不会显示在客户端。
 *   修改编辑器jsp文件的编码格式：window->prefenences->General->Content Types->text->JSP 将编码改为utf-8，点击update OK
 *   
 * EL表达式的基本使用
 *   1)访问bean的属性
 *     第一种方式：${user.name}
 *       容器依次从pageContext,request,session,application中查找(执行request.getAttribute)绑订名为"user"的对象,如果找到了，则不再向下继续查找；找到了以后，会调用该对象的getName方法，最后将该方法执行的结果输出。
 *       相对于以前的方式有两个优点：如果找不到对应的对象，会输出""。如果值为null,会输出""。如果要指定查找的范围，可以使用pageScope,requestScope,sessionScope,applicationScope: 比如：${requestScope.user.name}
 *     第二种方式：${user["name"]},这样写有两个优点,第一个是，[]里面允许出现变量；第二个是，[]里面允许出现下标(从0开始的整数), 用来访问数组的某个元素。或者是${user[propname]},propname没有引号，表示变量
 *   2)获得请求参数值
 *     ${param.name} : 与request.getParameter(String name);等价。
 *     ${paramValues.interest}：与request.getParameterValues(String interest)等价。
 *   3) 进行一些简单的计算，计算结果可以直接输出，也可以给jsp标签赋值。
 *     a、算术运算:支持 "+","-","*","/"和"%",但是，"+"不能进行字符串的连接处理。
 *     b、关系运算:支持"=="，"!="，">"，"<"，">="，"<="。
 *     c、逻辑运算:支持"&&","||","!"，可以使用and，or，not替代
 *     d、empty运算: 判断一个字符串是否为空，一个集合是否为空。
 *     以下四种情况，结果为true：空字符串，空的集合，null值，找不到
 *     
 * jstl(java standard taglib 标准标签库)
 *   1)使用步骤
 *     step1 将标签对应的.jar文件 copy to WEB-INF\lib下。standard.jar, jstl.jar
 *     step2 在jsp文件里同，使用taglib指令引入相应的标签。
 *   2)核心标签
 *     a、<c:if test="" var="" scope="">标签体(可以是html或者java代码)</c:if>
 *       如果test的值为true,则执行标签体的内容。test属性必须赋值，可以使用el表达式进行计算。
 *     b、<c:choose>
 *       <c:when test="">
 *         标签体(可以是html或者java代码)
 *       </c:when>
 *       <c:otherwise>
 *         标签体(可以是html或者java代码)
 *       </c:otherwise>
 *       </c:choose>
 *       when表示一个分支，可以出现多次。otherwise表示例外，只能出现1次。当test的值为true时，执行标签体的内容	
 *     c、<c:forEach var="" items="" varStatus="">
 *       标签体(可以是html或者java代码)
 *       </c:forEach>	
 *       var属性指定绑订名，绑订范围是pageContext。items属性指定一个需要遍历的集合。
 *       varStatus属性指定一个绑订名，该绑订名对应于一个对象，该对象封装了当前迭代(也就是遍历)的状态。
 *       该对象提供了两个方法getCount(返回是第几次遍历，从1开始)，getIndex(返回当前遍历的对象的下标)。
 *       
 * 自定义标签
 *   step1 写一个java类，继承SimpleTagSupport类
 *   step2 override doTag方法，编写相应的逻辑。
 *   step3 在.tld文件当中描述标签。
 *     a、.tld文件要放在WEB-INF\lib下，或者放在META-INF下。
 *     b、.tld文件可以参考 c.tld文件的结构来写。
 *     c、body-content的内容可以是:
 *       c1: empty，该标签不允许出现标签体。
 *       c2: scriptless，标签允许出现标签体，但是，标签体的内容不能够出现java代码，即不允许出现<%   %>,  <%=  %>,<%!  %>
 *       c3: JSP，标签允许出现标签体,标签体的内容可以出现java代码。只能复杂标签技术才支持"JSP",简单标签技术只支持"empty"和"scriptless"。
 *     d.<rtexprvalue>true/false</rtexprvalue>
 *       true：属性值支持在运行时赋值
 *       false：不支持
 *       
 *       
 *       
 */
@WebListener
public class BaseListener implements ServletContextListener, ServletContextAttributeListener, HttpSessionListener, HttpSessionAttributeListener, HttpSessionActivationListener, HttpSessionBindingListener, ServletRequestListener, ServletRequestAttributeListener {

    /**
     * Default constructor. 
     */
    public BaseListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextAttributeListener#attributeRemoved(ServletContextAttributeEvent)
     */
    public void attributeRemoved(ServletContextAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletRequestAttributeListener#attributeAdded(ServletRequestAttributeEvent)
     */
    public void attributeAdded(ServletRequestAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionAttributeListener#attributeReplaced(HttpSessionBindingEvent)
     */
    public void attributeReplaced(HttpSessionBindingEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionActivationListener#sessionWillPassivate(HttpSessionEvent)
     */
    public void sessionWillPassivate(HttpSessionEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextAttributeListener#attributeAdded(ServletContextAttributeEvent)
     */
    public void attributeAdded(ServletContextAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
     */
    public void requestDestroyed(ServletRequestEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletRequestAttributeListener#attributeRemoved(ServletRequestAttributeEvent)
     */
    public void attributeRemoved(ServletRequestAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionActivationListener#sessionDidActivate(HttpSessionEvent)
     */
    public void sessionDidActivate(HttpSessionEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletRequestAttributeListener#attributeReplaced(ServletRequestAttributeEvent)
     */
    public void attributeReplaced(ServletRequestAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionAttributeListener#attributeAdded(HttpSessionBindingEvent)
     */
    public void attributeAdded(HttpSessionBindingEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionAttributeListener#attributeRemoved(HttpSessionBindingEvent)
     */
    public void attributeRemoved(HttpSessionBindingEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextAttributeListener#attributeReplaced(ServletContextAttributeEvent)
     */
    public void attributeReplaced(ServletContextAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionBindingListener#valueUnbound(HttpSessionBindingEvent)
     */
    public void valueUnbound(HttpSessionBindingEvent arg0)  { 
         // TODO Auto-generated method stub
    }
	
}
