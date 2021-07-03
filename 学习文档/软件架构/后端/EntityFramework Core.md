# EntityFramework Core

引用：

1. `Microsoft.EntityFrameworkCore`：**必需**。这是ef core的核心包

2. `Microsoft.EntityFrameworkCore.SqlServer`：**必需**。sqlserver 数据库驱动包

3. `Microsoft.EntityFrameworkCore.Tools`：**必需**。工具扩展包

4. `Microsoft.EntityFrameworkCore.Proxies`：延迟加载实现包

命令：

```sh
# 选择Repository项目
Add-Migration Init
Update-Database
```

编写 DbContext 类：

```c#
public class TestDbContext:DbContext
{
    public DbSet<UserInfo> userInfos;
    
    public TestDbContext()
    {
        // Unable to create an object of type 'Context'. 
        // For the different patterns supported at design time.
        // 出现该错误时，添加默认构造器
    }
  
    public TestDbContext(DbContextOptions<TestDbContext> options) : base(options)
    {
     
    }
    
    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder) {
        optionsBuilder.UseLazyLoadingProxies(false);
        // No database provider has been configured for this DbContext
        // 出现该错时，设置数据库连接字符串
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder) {
        base.OnModelCreating(modelBuilder);
        modelBuilder.Entity<UserInfo>(entity=> {
            entity.Property(t => t.password).HasDefaultValue("123456");
        });
    }
}
```







**参考：**

- https://mp.weixin.qq.com/s?__biz=MjM5NjMzMzE2MA==&mid=2451733860&idx=2&sn=ebe0a8e0a24f396a5753a31792b74451&chksm=b13c0e63864b8775e1a4004638e7e7f5e1f694847e7b605da454b78c8a7d81a729236612ad0b&mpshare=1&scene=23&srcid=&sharer_sharetime=1580070468840&sharer_shareid=83c85f3c4ddf8afec618435580a94a3e#rd