# grpc示例项目

**简介：**

封装通用的审批业务，对外提供grpc接口。

1. 审批操作
2. 查询审批历史



## .NET服务端



## .NET客户端

1、安装nuget包

```sh
Install-Package Grpc.Net.Client
Install-Package Google.Protobuf
Install-Package Grpc.Tools
```

参考：https://github.com/dotnet/AspNetCore.Docs/tree/master/aspnetcore/tutorials/grpc



调试报错：引用包中有相同命名空间相同名称的类，去掉其中一个的引用。

通过 obj/project.assets.json 查询包的依赖关系，将引用的源头删除即可！



## Web客户端

1、安装gRPC-web运行环境

```sh
cnpm i grpc-web -g
```

2、下载[protoc-gen-grpc-web](https://github.com/grpc/grpc-web/releases)、[protoc](https://github.com/protocolbuffers/protobuf/releases)，添加到PATH环境变量

3、从`.proto`协议文件

参考：

- https://www.jdon.com/50508
- https://www.ctolib.com/grpc-grpc-web.html



## protobuf教程

### 通信方式

1、一个请求对象对应一个返回对象

```protobuf
rpc Login(LoginRequest) returns (LoginResponse) {}
```

2、一个请求对象，服务器返回多个结果

```protobuf
rpc Login(LoginRequest) returns (stream LoginResponse) {}
```

3、多个请求对象，一个返回结果

```protobuf
rpc Login(stream LoginRequest) returns (LoginResponse) {}
```

4、多个请求对象，服务器返回多个结果

```protobuf
rpc Login(stream LoginRequest) returns (stream LoginResponse) {}
```

关键字 `stream` 表示多个值

### Protobuf定义

1、**syntax="proto3";**

> 文件的第一行指定了你使用的是proto3的语法：如果你不指定，protocol buffer 编译器就会认为你使用的是proto2的语法。这个语句必须出现在.proto文件的非空非注释的第一行。

2、**package user;**

> 编译完成之后，包名为user

3、**service 定义服务**

```protobuf
service UserService {
  rpc Login(LoginRequest) returns (LoginResponse);
}
```

4、**message 定义结构体**

```protobuf
message LoginRequest {
	string username=1;
	string password=2;
}
```

#### 数据类型

- strings 默认值是空字符串
- int32 默认是0（编译之后为go语言中的int类型）
- int64 默认是0 （编译之后为go语言中的int64）
- float 默认为0.0 （编译之后为go语言中的 float32）
- double 默认为0.0 （编译之后为go语言中的 float64）
- uint32 （编译之后为go语言中的 uint32）
- uint64 （编译之后为go语言中的 uint64）
- bytes 默认值是空bytes
- bool 默认值是false
- enum 默认值是第一个枚举值（value必须为0)

#### 字段修饰符

- `repeated`：用来定义数组，一个字段可以重复出现一定次数（包括零次）
- `required`：值不可为空 (proto3中已删除)
- `optional`：可选字段 (proto3中已删除)
- `singular`：符合语法规则的消息包含零个或者一个这样的字段 (proto3中已删除)
- 默认值： `string code=2 [default=200];` (proto3中已删除)
- 预留字段：`reserved 6 to 8;`

#### 其他类型

1、枚举定义

```protobuf
// 枚举类型，必须从0开始，序号可跨越。同一包下不能重名，所以加前缀来区别
enum Role {
	Role_Admin=0;
	Role_Guest=1;
	Role_User=2;
	Role_Other=9;
}
```

2、Map类型

```protobuf
map<key_type, value_type> map_field = N;
```

> 其中key_type可以是任意Integer或者string类型（所以，除了floating和bytes的任意标量类型都是可以的）value_type可以是任意类型。

例如，如果你希望创建一个project的映射，每个Project使用一个string作为key

```protobuf
map<string, Project> projects = 3;
```

### 示例

```protobuf
syntax="proto3";
package user;

service UserService{
	// 注册
	rpc Signup(SignupRequest) returns (SignupResponse);
}

// 定义错误枚举，必须从0开始
enum LoginError {
	//密码错误
	Error_Password=0;
	//用户名错误
	Error_UserName=1;
	//内部服务器错误
	Error_Server=9;
}

message ID {
	int32 id=1;
}

// 注册请求体
message SignupRequest {
	string username=1;
	string password=2;
	int32 code=3;
	// 数组
	repeated string hobby=4;
	// map
	map<string,string> maps = 5;
}

// 注册响应体
message SignupResponse {
	ID id=1;
	int32 code=2 [default=200];
	string msg=3;
}
```

### 数据校验

插件地址及文档：[https://github.com/envoyproxy/protoc-gen-validate](https://link.zhihu.com/?target=https%3A//github.com/envoyproxy/protoc-gen-validate)

### 流的使用

```protobuf
rpc GetStatus (GetReq) return (stream GetResp);
```

service实现

```go
func (HelloService) GetStatus(req *pb.GetReq,stream pb.HelloService_GetStatusServer) err {
	for i:=0;i<10;i++{
		stream.Send(&pb.GetResp{
			Status: i,
		})
		if err:=stream.Context().Err();err!=nil {
			fmt.Println("客户端关闭流...")
			return err
		}
	}
	return nil
}
```

使用`grpc-web`访问流： 文档：[https://github.com/grpc/grpc-web](https://link.zhihu.com/?target=https%3A//github.com/grpc/grpc-web)

```js
var helloService= new proto.mypackage.HelloServiceClient('http://localhost:8080');
var getReq= new proto.mypackage.GetReq();
getReq.setMessage(msg);
var metadata = {'custom-header-1': 'value1'};
var stream = helloService.getStatus(getReq, metadata);
stream.on('data', function(response) {
  console.log(response.getStatus());
});
stream.on('status', function(status) {
  console.log(status.code);
  console.log(status.details);
  console.log(status.metadata);
});
stream.on('end', function(end) {
  // stream end signal
});
```



## 问题解决

> 先将客户端异步调用改为同步调用，更易发现问题。

**1. Channel报错**

原因：管道IP配置错误

**2. Unimplemented...**

原因：服务端没有注册service

```c#
public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
{
    // ...
    app.UseEndpoints(endpoints =>
    {
        // 注册service
        endpoints.MapGrpcService<GreeterService>();
        endpoints.MapGrpcService<HealthCheckService>();
        endpoints.MapGrpcService<ApproveService>();

        endpoints.MapGet("/", async context =>
        {
            await context.Response.WriteAsync("Communication with gRPC endpoints must be made through a gRPC client. To learn how to create a client, visit: https://go.microsoft.com/fwlink/?linkid=2086909");
        });
    });
}
```

> 服务端和客户端 proto 文件中的 package 尽量保持一致