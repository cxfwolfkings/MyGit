# ABP-MicroService

源码：https://github.com/WilliamXu96/ABP-MicroService

1. [AuthServer](#AuthServer)



参考：

1. https://www.cnblogs.com/william-xu/



项目分为微服务层(microservices)、网关层(gateways)、应用层(applications)和模块层(modules)。



## AuthServer

日志：Serilog.AspNetCore

```xml
<PackageReference Include="Serilog.AspNetCore" Version="3.4.0" />
<PackageReference Include="Serilog.Extensions.Hosting" Version="3.1.0" />
<PackageReference Include="Serilog.Sinks.File" Version="4.1.0" />
```

日志配置项：

```c#
Log.Logger = new LoggerConfiguration()
                .MinimumLevel.Debug()
                .MinimumLevel.Override("Microsoft", LogEventLevel.Information)
                .Enrich.WithProperty("Application", "AuthServer")
                .Enrich.FromLogContext()
                .WriteTo.File("Logs/logs.txt")
                .WriteTo.Elasticsearch(
                    new ElasticsearchSinkOptions(new Uri(configuration["ElasticSearch:Url"]))
                    {
                        AutoRegisterTemplate = true,
                        AutoRegisterTemplateVersion = AutoRegisterTemplateVersion.ESv6,
                        IndexFormat = "xdlms-log-{0:yyyy.MM}"
                    })
                .WriteTo.Console()
                .CreateLogger();
```

添加系统环境变量：

```c#
new ConfigurationBuilder().AddEnvironmentVariables();
```

服务管道中添加应用AuthServerHostModule

```c#
[DependsOn(
    typeof(AbpAutofacModule),
    typeof(AbpPermissionManagementEntityFrameworkCoreModule),
    typeof(AbpAuditLoggingEntityFrameworkCoreModule),
    typeof(AbpSettingManagementEntityFrameworkCoreModule),
    typeof(AbpIdentityEntityFrameworkCoreModule),
    typeof(AbpIdentityServerEntityFrameworkCoreModule),
    typeof(AbpTenantManagementEntityFrameworkCoreModule),
    typeof(AbpEntityFrameworkCoreSqlServerModule),
    typeof(AbpAccountWebIdentityServerModule),
    typeof(AbpAccountApplicationModule),
    typeof(AbpAspNetCoreMvcUiBasicThemeModule)
)]
public class AuthServerHostModule : AbpModule
{
    private const string DefaultCorsPolicyName = "Default";

    public override void ConfigureServices(ServiceConfigurationContext context)
    {
        var configuration = context.Services.GetConfiguration();
      
        // ...
    }
}
```

添加默认仓储：

```c#
context.Services.AddAbpDbContext<AuthServerDbContext>(options =>
{
    options.AddDefaultRepositories();
});
```

AuthServerDbContext：

```c#
public class AuthServerDbContext : AbpDbContext<AuthServerDbContext>
{
    public AuthServerDbContext(DbContextOptions<AuthServerDbContext> options)
        : base(options) { }
  
    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        modelBuilder.ConfigureIdentityServer();
    }
}
```

配置数据库：

```c#
Configure<AbpDbContextOptions>(options =>
{
    options.UseSqlServer();
});
```

国际化：

```c#
Configure<AbpLocalizationOptions>(options =>
{
    options.Languages.Add(new LanguageInfo("en", "en", "English"));
});
```

配置缓存：

```c#
context.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = configuration["Redis:Configuration"];
});
```

> StackExchangeRedis有连接超时的bug，不推荐

CORS：跨域资源共享"（Cross-origin resource sharing）。允许浏览器向跨源服务器，发出[`XMLHttpRequest`](http://www.ruanyifeng.com/blog/2012/09/xmlhttprequest_level_2.html)请求，从而克服AJAX只能[同源](http://www.ruanyifeng.com/blog/2016/04/same-origin-policy.html)使用的限制。

CORS与JSONP的使用目的相同，但是比JSONP更强大。JSONP只支持`GET`请求，CORS支持所有类型的HTTP请求。JSONP的优势在于支持老式浏览器，以及可以向不支持CORS的网站请求数据。

参考：http://www.ruanyifeng.com/blog/2016/04/cors.html

```c#
context.Services.AddCors(options =>
{
    options.AddPolicy(DefaultCorsPolicyName, builder =>
    {
        builder.WithOrigins(configuration["CorsOrigins"]
                .Split(",", StringSplitOptions.RemoveEmptyEntries)
                .Select(o => o.RemovePostFix("/"))
                .ToArray())
            .WithAbpExposedHeaders()
            .SetIsOriginAllowedToAllowWildcardSubdomains()
            .AllowAnyHeader()
            .AllowAnyMethod()
            .AllowCredentials();
    });
});
```

CorsOrigins的配置，多个地址以逗号隔开

```json
{ "CorsOrigins": "http://localhost:9527" }
```

审计配置

```c#
Configure<AbpAuditingOptions>(options =>
{
    options.IsEnabledForGetRequests = true;
    options.ApplicationName = "AuthServer";
});
```

数据保护

```c#
var redis = ConnectionMultiplexer.Connect(configuration["Redis:Configuration"]);
context.Services.AddDataProtection()
    .PersistKeysToStackExchangeRedis(redis, "DataProtection-Keys");
```

应用初始化

```c#
public override void OnApplicationInitialization(ApplicationInitializationContext context)
{
    var app = context.GetApplicationBuilder();

    app.UseCorrelationId();
    app.UseVirtualFiles();
    app.UseRouting();
    app.UseCors(DefaultCorsPolicyName);
    app.UseAuthentication();
    app.UseMultiTenancy();
    app.UseIdentityServer();
    app.UseAuthorization();
    app.UseAbpRequestLocalization();
    app.UseAuditing();

    AsyncHelper.RunSync(async () =>
    {
        using (var scope = context.ServiceProvider.CreateScope())
        {
            await scope.ServiceProvider
                .GetRequiredService<IDataSeeder>()
                .SeedAsync();
        }
    });
}
```

