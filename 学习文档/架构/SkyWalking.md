# SkyWalking

随着微服务架构的流行，一些微服务架构下的问题也会越来越突出，比如一个请求会涉及多个服务，而服务本身可能也会依赖其他服务，整个请求路径就构成了一个网状的调用链，而在整个调用链中一旦某个节点发生异常，整个调用链的稳定性就会受到影响，所以会深深的感受到 “银弹” 这个词是不存在的，每种架构都有其优缺点 。

面对以上情况， 我们就需要一些可以帮助理解系统行为、用于分析性能问题的工具，以便发生故障的时候，能够快速定位和解决问题，这时候 APM（应用性能管理）工具就该闪亮登场了。

目前主要的一些 APM 工具有: Cat、Zipkin、Pinpoint、SkyWalking，这里主要介绍 [SkyWalking](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fapache%2Fincubator-skywalking) ，它是一款优秀的国产 APM 工具，包括了分布式追踪、性能指标分析、应用和服务依赖分析等。

下面是 SkyWalking 6.x 的架构图：

![x](../../Resources/SkyWalking1.png)

**说明：** SkyWalking 的核心是数据分析和度量结果的存储平台，通过 HTTP 或 gRPC 方式向 SkyWalking Collecter 提交分析和度量数据，SkyWalking Collecter 对数据进行分析和聚合，存储到 Elasticsearch、H2、MySQL、TiDB 等其一即可，最后我们可以通过 SkyWalking UI 的可视化界面对最终的结果进行查看。Skywalking 支持从多个来源和多种格式收集数据：多种语言的 Skywalking Agent 、Zipkin v1/v2 、Istio 勘测、Envoy 度量等数据格式。

整体架构看似模块有点多，但在实际上还是比较清晰的，主要就是通过收集各种格式的数据进行存储，然后展示。所以搭建 Skywalking 服务我们需要关注的是 SkyWalking Collecter、SkyWalking UI 和 存储设备，SkyWalking Collecter、SkyWalking UI 官方下载安装包内已包含，最终我们只需考虑存储设备即可。

下面基于 Windows 环境使用 [SkyAPM-dotnet](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FSkyAPM%2FSkyAPM-dotnet) 来介绍一下 SkyWalking，[SkyAPM-dotnet](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FSkyAPM%2FSkyAPM-dotnet) 是 SkyWalking 的 .NET Agent。

**环境要求**

1. JDK8+
2. Elasticsearch 6.x
3. 8080,10800,11800,12800 端口不被占用

[Elasticsearch下载安装](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.elastic.co%2Fdownloads%2Felasticsearch) 参考官方教程，安装完成后需要对 config/elasticsearch.yml 做如下修改：

```yml
# 修改
# 如果 cluster.name 不设置为 CollectorDBCluster，则需要修改 SkyWalking 的配置文件
cluster.name: CollectorDBCluster   
network.host: 0.0.0.0
```

如果是 linux 环境，Elasticsearch 安装可能没有那么顺利，请参考 [Linux 环境下安装 Elasticsearch 5.x、6.x 问题汇总](https://www.jianshu.com/p/fce1474dc6e7)。

**下载 SkyWalking**

SkyWalking 个人建议直接下载官方编译好的，[下载地址](https://links.jianshu.com/go?to=http%3A%2F%2Fskywalking.apache.org%2Fdownloads%2F)

**启动 SkyWalking**

config/application.yml 的默认数据存储开启是的 h2，这里我们需要修改数据存储为 Elasticsearch（**在启动 SkyWalking 之前，确保 Elasticsearch 已启动**）

SkyWalking 的启动包括两部分，一个是 SkyWalking Collector（oapService），一个是 SkyWalking UI（webappService）。

打开SkyWalking 解压后的 bin 目录，bat 为 windows 环境使用，sh 为 linux 环境使用，我们可以分别启动 oapService 和 webappService，也可以通过 startup 一次性全部启动，从 startup 中的命令可以知道其实就是分别启动  oapService 和 webappService。

如果一切顺利（*不顺利请多看几遍 [快速入门](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fapache%2Fincubator-skywalking%2Fblob%2F5.x%2Fdocs%2Fcn%2FQuick-start-CN.md)*），这时候就可以通过 [http://localhost:8080](https://links.jianshu.com/go?to=http%3A%2F%2Flocalhost%3A8080) 来查看 SkyWalking UI 了（默认全是0，截图是测试效果），默认账号/密码：admin/admin。

**Java 项目接入**

参考 [部署 skywalking javaagent](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fapache%2Fincubator-skywalking%2Fblob%2F5.x%2Fdocs%2Fcn%2FDeploy-skywalking-agent-CN.md)，skywalking-agent.jar 位于下载包的 agent 目录下，具体效果这里就不测试了

**.NET 项目接入**

这里以 .NET Core 项目来测试，基于 .NET Framework 的项目目前也是支持的，只是相对不完善一些，参考 [SkyAPM-dotnet](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FSkyAPM%2FSkyAPM-dotnet)

1、新建一个.NET Core API 项目，安装 NuGet 包：

```sh
Install-Package SkyAPM.Agent.AspNetCore
```

2、添加项目环境变量（实际情况应该是在 CI 流程中设置环境变量，参考：[https://github.com/SkyAPM/SkyAPM-dotnet#examples](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FSkyAPM%2FSkyAPM-dotnet%23examples)）

```sh
set ASPNETCORE_HOSTINGSTARTUPASSEMBLIES=SkyAPM.Agent.AspNetCore
```

3、项目根目录下添加 skyapm.json 文件（来自官方例子），设置为 “如果较新则复制”，添加 SkyWalking 的配置信息，[更多默认配置参考](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FSkyAPM%2FSkyAPM-dotnet%2Fblob%2Fmaster%2Fsrc%2FSkyApm.Utilities.Configuration%2FConfigurationBuilderExtensions.cs)。（*不过从源码可以看出其实也不需要创建单独的 skyapm.json，直接在 appsettings.json 增加 SkyWalking 节点配置也是没问题的*）

```json
{
  "SkyWalking": {
    "ServiceName": "WebAPIServiceA", // 服务名
    "Transport": {
      "gRPC": {
        "Servers": "localhost:11800"  // 服务地址
      }
    }
  }
}
```

4、启动程序，请求的追踪结果就会被记录下来，通过 SkyWalking UI 查看。

单个服务的效果并不明显，看不出请求跨度、链路关系，下面是一个多服务的例子（[下载源码](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fbeckjin%2FSkyWalkingSample)）。

目前 SkyWalking 的 .NET Agent 还不支持 gRPC 的调用跟踪，整个项目还在不断的完善中，期待后续更多的进展。

**参考：**

- https://www.jianshu.com/p/2fd56627a3cf
- [Apache SkyWalking](https://links.jianshu.com/go?to=https%3A%2F%2Fskywalking.apache.org%2F)
- [SkyWalking-github](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fapache%2Fincubator-skywalking)
- [SkyWalking-dotnet](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FSkyAPM%2FSkyAPM-dotnet)
- [SkyWalking-sample](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fbeckjin%2FSkyWalkingSample)