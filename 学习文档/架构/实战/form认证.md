# form认证

先来了解ASP.NET是如何进行Form认证的：

1. 终端用户在浏览器的帮助下，发送 Form 认证请求。

2. 浏览器会发送存储在客户端的所有相关的用户数据。

3. 当服务器端接收到请求时，服务器会检测请求，查看是否存在 "Authentication Cookie" 的Cookie。

4. 如果查找到认证Cookie，服务器会识别用户，验证用户是否合法。

5. 如果未找到 "Authentication Cookie"，服务器会将用户作为匿名（未认证）用户处理，在这种情况下，如果请求的资源标记着 protected/secured，用户将会重定位到登录页面。

6. 实现Form认证

   web.config

   ```xml
   <authentication mode="Forms">
     <forms loginurl="~/Authentication/Login"></forms>
   </authentication>
   ```

7. 让Action方法更安全

   在Index action方法中添加认证属性 [Authorize]

   ```C#
   [Authorize]
   public ActionResult Index()
   {
       EmployeeListViewModel employeeListViewModel = new EmployeeListViewModel();
       // ......
   }
   ```

8. 创建 login action 方法

   通过调用业务层功能检测用户是否合法。  
   如果是合法用户，创建认证Cookie。可用于以后的认证请求过程中。  
   如果是非法用户，给当前的ModelState添加新的错误信息，将错误信息显示在View中。

    ```C#
    [HttpPost]
    public ActionResult DoLogin(UserDetails u)
    {
        EmployeeBusinessLayer bal = new EmployeeBusinessLayer();
        if (bal.IsValidUser(u))
        {
            // 在客户端创建一个新cookie
            FormsAuthentication.SetAuthCookie(u.UserName, false);
            return RedirectToAction("Index", "Employee");
        }
        else
        {
            ModelState.AddModelError("CredentialError", "Invalid Username or Password");
            return View("Login");
        }
    }
    ```

9. 在 View 中显示信息

   ```C#
   @Html.ValidationMessage("CredentialError", new {style="color:red;" })
   @using (Html.BeginForm("DoLogin", "Authentication", FormMethod.Post))
   {
       // ……
   }
   ```

理解：

1. 什么Dologin会添加 HttpPost属性，还有其他类似的属性吗？

   该属性使得DoLogin方法只能由Post请求调用。如果有人尝试用Get调用DoLogin，将不会起作用。还有很多类似的属性如HttpGet，HttpPut和HttpDelete属性.

2. FormsAuthentication.SetAuthCookie是必须写的吗？

   是必须写的。让我们了解一些小的工作细节。

   客户端通过浏览器给服务器发送请求。当通过浏览器生成后，所有相关的Cookies也会随着请求一起发送。

   服务器接收请求后，准备响应。请求和响应都是通过HTTP协议传输的，HTTP是无状态协议。每个请求都是新请求，因此当同一客户端发出二次请求时，服务器无法识别，为了解决此问题，服务器会在准备好的请求包中添加一个Cookie，然后返回。

   当客户端的浏览器接收到带有Cookie的响应，会在客户端创建Cookies。

   如果客户端再次给服务器发送请求，服务器就会识别。

   FormsAuthentication.SetAuthCookie 将添加 "Authentication" 这个特殊的Cookie来响应。

3. 是否意味着没有Cookies，FormsAuthentication将不会有作用？

   不是的，可以使用URI代替Cookie。打开Web.Config文件，修改Authentication/Forms部分：

   ```xml
   <forms cookieless="UseUri" loginurl="~/Authentication/Login"></forms>
   ```

   授权的Cookie会使用URL传递。通常情况下，Cookieless属性会被设置为"AutoDetect"，表示认证工作是通过不支持URL传递的Cookie完成的。

4. FormsAuthentication.SetAuthCookie中第二个参数"false"表示什么？

   false决定了是否创建永久有用的Cookie。临时Cookie会在浏览器关闭时自动删除，永久Cookie不会被删除。可通过浏览器设置或是编写代码手动删除。

5. 当凭证错误时，UserName 文本框的值是如何被重置的？

   HTML帮助类会从Post数据中获取相关值并重置文本框的值。这是使用 HTML 帮助类的一大优势。

6. "Authorize" 属性做了什么？
   In Asp.net MVC there is a concept called Filters. Which will be used to filter out requests and response. There are four kind of filters. We will discuss each one of them in our 7 days journey. Authorize attribute falls under Authorization filter. It will make sure that only authenticated requests are allowed for an action method.

7. Can we attach both HttpPost and Authorize attribute to same action method?
   Yes we can.

8. Why there is no ViewModel in this example?
   As per the discussion we had in Lab 6, View should not be connected to Model directly. We must always have ViewModel in between View and Model. It doesn't matter if view is a simple "display view" or "data entry view", it should always connected to ViewModel. Reason for not using ViewModel in our project is simplicity. In real time project I strongly recommend you to have ViewModel everywhere.

9. 需要为每个Action方法添加授权属性吗？
   不需要，可以将授权属性添加到Controller级别或 Global级别。When attached at controller level, it will be applicable for all the action methods in a controller. When attached at Global level, it will be applicable for all the action method in all the controllers.
   Controller Level

   ```c#
   [Authorize]
   public class EmployeeController : Controller
   {
       ....
   }
   ```

   Global level

   Step 1 - Open FilterConfig.cs file from App_start folder.

   Step 2 - Add one more line RegisterGlobalFilters as follows.

   ```c#
   public static void RegisterGlobalFilters(GlobalFilterCollection filters)
   {
       filters.Add(new HandleErrorAttribute());//Old line
       filters.Add(new AuthorizeAttribute());//New Line
   }
   ```

   Step 3 - Attach AllowAnonymous attribute to Authentication controller.

   ```c#
   [AllowAnonymous]
   public class AuthenticationController : Controller
   {Step 4 – Execute and Test the application in the same way we did before.
   ```

10. Why AllowAnonymous attribute is required for AuthenticationController?
    We have attached Authorize filter at global level. That means now everything is protected including Login and DoLogin action methods. AllowAnonymous opens action method for non-authenticated requests.

11. How come this RegisterGlobalFilters method inside FilterConfig class invoked?
    It was invoked in Application_Start event written inside Global.asax file.


通过 ASP.NET Core，开发者可轻松配置和管理其应用的安全性。ASP.NET Core 的功能包括管理身份验证、授权、数据保护、HTTPS 强制、应用机密、请求防伪保护及 CORS 管理。 通过这些安全功能，可以生成安全可靠的 ASP.NET Core 应用。

#### Cookie和Session

一、关于Cookie和Session此处简单介绍一下、作为初学者可以先了解以下两点

1、Cookie是存于客户端的（即用户电脑）、Session是存于服务端的。
2、Cookie数据所有的浏览器端共享、Session数据由服务器开辟内存保存、每一个浏览器都有一个唯一的SessionID

二、首先需要介绍一下 `FormsAuthentication` 密封类。

1、用户登录成功后、需要保存用户信息到Cookie（本地）。

可以调用如下方法、下次调用就会判断是否有Cookie信息，如果Cookie信息没有过期可以直接跳过登录。

该类有一个方法 `SetAuthCookie(string userName, bool createPersistentCookie)`

>注意：该方法如果被调用多次、则保存最后一个设置的Cookie

```C#
public ActionResult AdminLogin(SysAdmin objAdmin)
{
    string adminName = new BLL.SysAdminManager().AdminLogin(objAdmin);
    if (adminName != null)
    {
        FormsAuthentication.SetAuthCookie(adminName, true);
        TempData["adminName"] = adminName;
        return RedirectToAction("GetAllStuList", "Student");
    }
    else
    {
        ViewBag.Info = "用户或密码错误";
    }
    return View("AdminLogin");
}
```

2、判断用户是否通过身份验证并且获取用户名称、没有通过验证、则转到登录页面；已经通过验证、则不需要登录

```C#
public ActionResult Index()
{
    if (this.User.Identity.IsAuthenticated)
    {
        string adminName = this.User.Identity.Name;// 获取写入的adminName
        ViewBag.adminName = adminName;
        return View("StudentManage");
    }
    else
    {
        return RedirectToAction("Index", "SysAdmin");
    }
}
```

3、控制器或控制器方法如果添加了 `Authorize` 特性、则通过路由访问之前需要验证是否已经通过身份验证、作用类似于第二点if判断体。

```C#
[Authorize]
public ActionResult Index()
{
    string adminName = this.User.Identity.Name; //获取写入的adminName
    ViewBag.adminName = adminName;
    return View("StudentManage");
}
```

如果浏览器没有登录成功、那么url访问该路由就会失败。如果想要浏览器访问url失败后跳转到指定的页面、可以在Web.config的 `<system.web>` 节点中添加如下标签

```xml
<authentication mode="Forms">
  <forms loginUrl="~/SysAdmin/Index" timeout="2880" />
</authentication>
```

