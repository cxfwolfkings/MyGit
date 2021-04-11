# ASP.NET Core Web API

3种返回数据的方式：

1. 指定类型实例
   - 只返回数据，不附带http状态码
2. `IActionResult` 实例
   - 数据 + Http状态码。OKResult，NotFoundResult, CreatedResult, NoContentResult, BadRequestResult, UnauthorizedResult, UnsupportedMediaTypeResult ...
3. `ActionResult <T>` 实例
   - 前两种模式的自由切换（简化返回），ASP.NET Core 2.1引入



参考：

- [dotnet跨平台](https://mp.weixin.qq.com/s?__biz=MzAwNTMxMzg1MA==&mid=2654082113&idx=6&sn=962dd65c9ae10dc5586d8e93a1306aa1&chksm=80d83014b7afb9027254e0dd9f4c9a2128cde7c5f7473702950a01229aefad251a3645b18ab8&mpshare=1&scene=23&srcid=1225XaJ8bLFNf6eqGE7FWsds&sharer_sharetime=1608855257772&sharer_shareid=83c85f3c4ddf8afec618435580a94a3e#rd)
- [揭密ASP.NET Core Web API 最佳实践](https://mp.weixin.qq.com/s?__biz=MzIxNjIwNzQ5Mw==&mid=2649752590&idx=1&sn=43524b2f3e4db4d5a0020ac1739979e3&chksm=8f883de4b8ffb4f26ef2d79b6c2b023ddf3437d49ef48e4dff0ac19717439cad4efd8ad3c447&scene=21#wechat_redirect)