package com.colin.common;

/**
 * Servlet引擎一般情况下是第三方的插件，它们由第三方制造商以他们专有的手段连接到Web服务器上。(JSWDK、JRun、ServletExec、Tomcat)
 * Servlet的技术特点：1、高效 ；2、方便；3、可移植；4、安全
 * Tomcat设置：
 * 设置虚拟目录 
 *   虚拟目录应该是/tomcat/webapps下的子目录，例如给它命名myweb。在Tomcat的安装路径下有一个conf文件夹，打开其中的server.xml文件，在文件中添加一行
 *   <Context path="/myweb" docBase="myweb" debug="0" reloadable="ture"></Context>
 * 会话管理
 * 1、HttpSession API 
 *   HttpSession API技术是建立在Cookie或URL重写之上的。当浏览器支持Cookie时，服务器会使用Cookie，当浏览器不支持Cookie时，服务器就会自动改用URL重写。
 * 2、Cookie
 *   会话跟踪的第一个障碍就是如何在服务器端和客户端之间保持唯一的会话ID。有人提出使用客户机的IP地址，因为IP地址是唯一的。这个方案是否可行呢？如果同一个客户端发出了不同的请求，或者客户端的请求是通过代理服务器提出的。在这些情况下IP地址都不能作为唯一的标识。其实Cookie和URL重写可以用来保存唯一的标识。
 * 3、URL重写
 *   这种技术主要提供给由于种种原因无法使用Cookie的用户，但使用中应当注意两点：
 *   一、是网络站点返回给客户的URL会追加一些额外的信息；
 *   二、是如果用户退出会话，再通过书签或链接返回原处时，有可能丢失会话的信息。
 * 
 * JSP
 * 1.JSP和ASP的比较
 *   ASP是与IIS捆绑在一起的，依赖于COM组件，所以它不具跨平台性；
 *   JSP默认的脚本语言是Java，并使用JavaBean组件，这就保证了JSP的平台无关性。
 *   JSP只需编译一次，ASP每次请求都要求解释，影响了服务器的效率
 * 2.JSP和CGI的比较
 *   JSP在每次向服务器发出请求时，服务器只需启动一个线程，这样大大的节省了开销 
 *   CGI程序本身不能实现跨平台运行。而且在传统的CGI环境中，每次客户端向服务器端发出请求，都会使服务器产生一个新的进程来装载和运行CGI程序，如果客户端同一时间提出多个请求或有多个客户端同时提出请求，会严重影响服务器的性能。
 * 3.JSP和PHP的比较 
 *   PHP被许多平台所支持，它有许多预先定义好的函数，例如，访问数据库，邮件服务器，创建PDF文档等等，其中所包含和数据库相连接的函数可以很方便的操作数据库。
 *   但它提供的数据库接口彼此不统一，比如对Oracle、MySQL、Sybase的接口，彼此都不一样。PHP还缺乏规模支持和多层结构支持，不适合大型商务站点。
 * JSP用于创建动态WEB网页，默认脚本语言是Java。JSP编程与平台无关。
 * 
 * 1. HTTP 协议方式
 * 浏览器和Web服务器或中间层机器和Web服务器之间进行通信所使用的协议，被称为HTTP超文本传输协议(HyperText Transport Protocol)，是所有Web应用程序设计的基础。
 * 发送请求时，浏览器是以URI统一资源标识符(Uniform Resource Identifier)或URL统一资源定位符(Uniform Resource Locator)开始的。
 * URL是URI的特殊形式，可以完全确定一个Web资源 。
 * 浏览器可以发送下面的请求：GET http://www.chinabs.net/jsp/default.asp/HTTP/1.1
 * 应答是当服务器收到请求时，处理后返回给客户端的信息。作为客户端，我们关心的是得到应答，而不是请求处理的过程，所以我们只需要知道服务器处理这个请求可以有很多种方式，当服务器接到非法请求时还会返回给客户端错误消息就行了。
 * HTTP的特点
 * （1）HTTP是一种无状态协议，服务器在送回问答后，不保留以前的请求或有关会话的历史纪录。
 * （2）HTTP是建立在应用层上的，是一种面向连接的传输协议，但它不能保证完全可靠的进行传输，也不提供重传机制。
 * （3）一旦建立了传输会话，一端必须向响应的另一端发送HTTP请求。
 * （4）Web应用程序不能很容易的做出及时响应，为了减少响应时间，浏览器可以将它接收的每个Web页的副本放入高速缓存。
 * （5）支持在浏览器和服务器之间放置代理服务器。
 * 2. HTML用户信息交互 
 * HTML超文本标记语言(HyperText Markup Language)是WEB网上信息格式的语言标准。HTML文档属于纯文本文件，用它的语法规则建立的文档可以运行在不同操作系统的平台上。
 * 
 * JSP编程规范
 * 1. 编译指令 <%指令语句%>
 * 2. 声明语句 <%!声明语句%>
 * 3. 表达式语句 <%=表达式%>
 * 4. 程序代码语句 <%程序代码%> 
 * 5. 注释语句 <%--注解--%> 或 <!--注解-->
 * 
 * JSP指令 
 * 1、Page指令
 *   Page指令定义了一系列与网页相关的属性，可以放置在文件中的任何位置。在同一个JSP页面中，page指令可以出现多次，但是每一种属性只能出现一次，重复的属性设置将覆盖掉先前的设置。Page指令的基本语法形式如下：
 *   <%@ page page_directive-attr_list %>
 * 2、Include指令
 *   Include指令允许在JSP转换为Servlet时引入一个文件，应该放置在需要引入该文件的位置。基本语法格式为：
 *   <%@ include file="relative-URL" %>
 *   relative-URL为需要引入文件的相对路径。
 *   除此之外，JSP还有另外一种引入其它文件的方法，格式为：
 *   <jsp:include page="relative-URL"/>
 *   两种include方法的异同：
 *   何时引入          对象                备注  
 *   编译                  静态                jsp引擎对引入的文件进行语法分析
 *   运行                  静态和动态     不作语法分析
 * 3、Taglib指令
 *   Taglib指令用来定义自定义标记。它的基本语法格式为：
 *   <%@ taglib uri="tagLibraryURI" prefix="tagPrefix" %>
 * 4、标准操作元素
 *   <jsp:useBean>操作元素，格式为：
 *   <jsp:useBean id="name" scope="page│request│session│application" typeSpec/>
 *   <jsp:getProperty>操作元素，格式为
 *   <jsp:getProperty name="name" property="propertyName"/>
 *   <jsp:setProperty> 操作元素，格式为
 *   <jsp:setProperty name="beanName" prop_expr/>
 *   <jsp:include>操作元素，格式为
 *   <jsp:include page="urlSpec" flush="true"/> 或 <jsp:include page="urlSpec" flush="true">
 *     {<jsp:param…/>}
 *   </jsp:include>
 *   <jsp:forward>操作元素，格式为
 *   <jsp:forward page="relativeURLspec"/> 或 <jsp:forward page="urlSpec">
 *     {<jsp:param…/>}*
 *   </jsp:forward>
 *   <jsp:plugin>
 *   该操作为Web开发人员提供在JSP页面中嵌入客户端运行的java程序（如Applet，JavaBean）的途径。
 *   <jsp:param>操作元素，语法格式为
 *   <jsp:param name="name" value="value"/>
 *   JavaBeans的制作 
 *   可以制作一个Bean，命名为JspBean.java。
 *   public class JspBean {
 *     private String message = "Not any message";
 *     public JspBean() {} // 为一个空构造方法
 *     public String getMessage() {                   
 *       return(message);
 *     }
 *     public void setMessage(String message) {         
 *       this.message = message;
 *     }
 *   }
 *   使用JavaBeans
 *   <jsp:useBean id="jspBean" class="JspBean" scope="page"/> 
 *   // (1)Initial value (TheOne):
 *   <jsp:getProperty name="jspBean" property="message" /> 
 *   // (2)Initial value (TheTwo):
 *   <%= jspBean.getMessage() %>  
 *   // (3)
 *   <jsp:setProperty name="jspBean" property="message" value="The JavaBean is new" />            // ④ Value after setting property with setProperty:
 *   <jsp:getProperty name="jspBean" property="message" />
 *   // (4)
 *   <% jspBean.setMessage("I like JavaBean with JSP"); %> 
 *   // (5)Value after setting property with scriptlet:
 *   <%= jspBean.getMessage() %>
 * 
 * JSP内置对象
 * 变量名                                    JAVA类型 
 * application       javax.servlet.ServletContext
 * config            javax.servlet.ServletConfig
 * exception         java.lang.Throwable
 * out               javax.servlet.jsp.JspWriter
 * page              java.lang.Object
 * pageContext       javax.servlet.jsp.PageContext
 * request           javax.servlet.http.HttpServletRequest
 * response          javax.servlet.http.HttpServletResponse
 * session           javax.servlet.http.HttpSession
 * 
 * @author  Colin Chen
 * @create  2018年11月18日 下午9:39:59
 * @modify  2018年11月18日 下午9:39:59
 * @version A.1
 */
public class ServletHelper {

}
