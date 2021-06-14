# Consul代码

引用nuget包：[Winton.Extensions.Configuration.Consul](https://github.com/wintoncode/Winton.Extensions.Configuration.Consul)

```c#
public static void Main(string[] args)
        {
            // 设置配置文件
            var configurationBuilder = new ConfigurationBuilder()
                .SetBasePath(Directory.GetCurrentDirectory())
                .AddJsonFile("appsettings.json", optional: true, reloadOnChange: true)
                .AddJsonFile("ocelot.json", true, false)
                .AddEnvironmentVariables();
            var hostingconfig = configurationBuilder.Build();
            var url = hostingconfig["serveraddress"];
            var consulUrl = hostingconfig["ServiceDiscovery:Consul:HttpEndpoint"];

            var host = new WebHostBuilder()
                .UseKestrel()
                .UseContentRoot(Directory.GetCurrentDirectory())
                .UseConfiguration(hostingconfig)
                .ConfigureAppConfiguration(config =>
                {
                    InitConsulKey(config, consulUrl, "SysConfig", "ClientSecret");
                })
                .UseIISIntegration()
                .UseUrls(url)
                .UseStartup<Startup>()
                .Build();

            host.Run();
        }

        private static void InitConsulKey(IConfigurationBuilder config, string consulUrl, params string[] keys)
        {
            if (keys.Length > 0)
            {
                keys.ToList().ForEach(key =>
                {
                    config.AddConsul(key, options =>
                    {
                        options.Optional = true;
                        options.ReloadOnChange = true;
                        options.OnLoadException = exceptionContext => { exceptionContext.Ignore = true; };
                        options.ConsulConfigurationOptions = cco => { cco.Address = new Uri(consulUrl); };
                    });
                });
            }
        }
```



