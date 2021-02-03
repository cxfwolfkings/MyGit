# Hello World

Program

```c#
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;
using System.IO;

namespace Hello
{
    public class Program
    {
        public static void Main(string[] args)
        {
            CreateHostBuilder(args).Build().Run();
        }

        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureWebHostDefaults(webBuilder =>
                {
                    var configurationBuilder = new ConfigurationBuilder()
                        .SetBasePath(Directory.GetCurrentDirectory())
                        .AddJsonFile("appsettings.json", optional: true, reloadOnChange: true)
                        .AddJsonFile("appsettings.Development.json", true, false)
                        .AddJsonFile("appsettings.Production.json", true, false);

                    var hostingConfig = configurationBuilder.Build();
                    //var urls = hostingConfig["serverAddress"].Split(',');

                    webBuilder.UseContentRoot(Directory.GetCurrentDirectory())
                        .UseConfiguration(hostingConfig)
                        //.UseUrls(urls)
                        .UseStartup<Startup>();
                });
    }
}
```

Startup

```c#
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using StackExchange.Redis;
using System;

namespace Hello
{
    public class Startup
    {
        public IConfiguration Configuration { get; }

        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        // This method gets called by the runtime. Use this method to add services to the container.
        // For more information on how to configure your application, visit https://go.microsoft.com/fwlink/?LinkID=398940
        public void ConfigureServices(IServiceCollection services)
        {
            services.Configure<CookiePolicyOptions>(options =>
            {
                // This lambda determines whether user consent for non-essential cookies is needed for a given request.
                options.CheckConsentNeeded = context => true;
                options.MinimumSameSitePolicy = SameSiteMode.None;
            });
            // 在使用session之前要注入cacheing，因为session依赖于cache进行存储
            services.AddDistributedMemoryCache();

            #region 使用Redis保存Session
            var redisConn = Configuration["Redis:Connection"];
            var redisInstanceName = Configuration["Redis:InstanceName"];
            //Session 过期时长分钟
            var sessionOutTime = Configuration.GetValue("Session:TimeOut", 30);

            var redis = ConnectionMultiplexer.Connect(redisConn);
            services.AddDataProtection().PersistKeysToStackExchangeRedis(redis, "DataProtection-Test-Keys");
            //services.AddDistributedRedisCache(option =>
            //{
            //    //redis 连接字符串
            //    option.Configuration = redisConn;
            //    //redis 实例名
            //    option.InstanceName = redisInstanceName;
            //});
            #endregion

            // session 设置
            services.AddSession(options =>
            {
                // 官方文档 https://docs.microsoft.com/zh-cn/aspnet/core/fundamentals/app-state
                // 设置 Session 过期时间
                options.IdleTimeout = TimeSpan.FromDays(90);
                //options.CookieHttpOnly = true;
            });

            services.AddMvc().SetCompatibilityVersion(CompatibilityVersion.Version_3_0);
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }
            else
            {
                app.UseExceptionHandler("/Home/Error");
            }

            app.UseStaticFiles();
            app.UseCookiePolicy();
            // 添加Session服务，要添加在MVC服务前面，因为MVC里面要用
            app.UseSession();

            app.UseRouting();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapGet("/", async context =>
                {
                    await context.Response.WriteAsync("Hello World!");
                });
            });
        }
    }
}
```



Dockerfile

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
ENTRYPOINT ["dotnet", "LeadChina.Hello.API.dll"]
```

生成镜像

```sh
docker build -t hello .
```

启动容器

```sh
docker run -d -v /data/sftp/mysftp/upload/hello/:/app --name hello hello
```

## 网关配置

```json
// 限流
"RateLimitOptions": {
    "ClientWhitelist": [],
    "EnableRateLimiting": true,  # 是否启用
    "Period": "1s",  # 统计时间段
    "PeriodTimespan": 1,  # 多少秒后客户端能重试
    "Limit": 1  # 统计时间段内允许请求的次数
}

// 熔断
"QoSOptions": {
    "ExceptionsAllowedBeforeBreaking":3,  # 允许多少个异常请求
    "DurationOfBreak":5,  # 熔断时间
    "TimeoutValue":5000  # 如果下游请求的处理时间超过该值则自动将请求设置为超时
}

// 负载均衡
{
    "DownstreamPathTemplate": "/api/posts/{postId}",
    "DownstreamScheme": "https",
    "DownstreamHostAndPorts": [
            {
                "Host": "10.0.1.10",
                "Port": 5000,
            },
            {
                "Host": "10.0.1.11",
                "Port": 5000,
            }
        ],
    "UpstreamPathTemplate": "/posts/{postId}",
    "LoadBalancer": "RoundRobin",  # LeastConnection（连接数最少）、RoundRobin（轮发）、NoLoadBalance（不启用）
    "UpstreamHttpMethod": [ "Put", "Delete" ]
}

// 请求聚合
{
    "ReRoutes": [
        {
            "DownstreamPathTemplate": "/",
            "UpstreamPathTemplate": "/laura",
            "UpstreamHttpMethod": [
                "Get"
            ],
            "DownstreamScheme": "http",
            "DownstreamHostAndPorts": [
                {
                    "Host": "localhost",
                    "Port": 51881
                }
            ],
            "Key": "Laura"
        },
        {
            "DownstreamPathTemplate": "/",
            "UpstreamPathTemplate": "/tom",
            "UpstreamHttpMethod": [
                "Get"
            ],
            "DownstreamScheme": "http",
            "DownstreamHostAndPorts": [
                {
                    "Host": "localhost",
                    "Port": 51882
                }
            ],
            "Key": "Tom"
        }
    ],
    "Aggregates": [
        {
            "ReRouteKeys": [
                "Tom",
                "Laura"
            ],
            "UpstreamPathTemplate": "/"
        }
    ]
}
```



