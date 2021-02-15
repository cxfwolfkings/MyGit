# Redis客户端

**.NET Core：**

- StackExchange

  高性能、通用的Redis .Net客户端；方便的应用Redis全功能；支持Redis Cluster。

  ```c#
  // 高性能的核心在于多路复用器（支持在多个调用线程高效共享Redis连接)，服务器端操作使用ConnectionMultiplexer 类
  ConnectionMultiplexer redis = ConnectionMultiplexer.Connect("server1:6379,server2:6379");
  // 日常应用的核心类库是IDatabase
  IDatabase db = redis.GetDatabase;
  // 支持Pub/Sub
  ISubscriber sub = redis.GetSubscriber;
  sub.Subscribe("messages", (channel, message) => {
    Console.WriteLine((string)message);
  });
  // ---
  sub.Publish("messages", "hello");
  ```

  > 如果你需要blocking pops，StackExchange.Redis官方推荐使用pub/sub模型模拟实现。

  日常操作的API请关注IDatabase接口，支持异步方法，这里我对【客户端操作Redis尽量不要使用异步方法】的说法不敢苟同，对于异步方法我认为还是遵守微软最佳实践：对于IO密集的操作，能使用异步尽量使用异步

  ```c#
  // 对应redis自增api：DECR mykey
  _redisDB0.StringDecrementAsync("ProfileUsageCap", (double)1);
  // 对应redis api：hget key field1
  _redisDB0.HashGetAsync(profileUsage, eqidPair.ProfileId));
  // 对应redis哈希自增api：HINCRBY myhash field -1
  _redisDB0.HashDecrementAsync(profileUsage, eqidPair.ProfileId, 1);  
  ```

  ConnectionMultiplexer 方式支持随时切换Redis DB，对于多个Redis DB的操作，我封装了一个常用的Redis DB操作客户端。

- Microsoft.Extensions.Caching.StackExchangeRedis

  从nuget doc可知，该组件库依赖于 StackExchange.Redis 客户端；是.NetCore针对分布式缓存提供的客户端，侧重点在Redis的缓存特性。

  另外能使用的函数签名也更倾向于【通用的增、查操作】

  ```c#
  // add Redis cache service
  services.AddStackExchangeRedisCache(options =>
  {
      options.Configuration = Configuration.GetConnectionString( "redis");
      options.InstanceName = "SampleInstance";
  });
  // Set Cache Item (by byte[])
  lifetime.ApplicationStarted.Register( =>
  {
      varcurrentTimeUTC = DateTime.UtcNow.ToString;
      byte[] encodedCurrentTimeUTC = Encoding.UTF8.GetBytes(currentTimeUTC);
      varoptions = newDistributedCacheEntryOptions.SetSlidingExpiration(TimeSpan.FromMinutes(20));
      cache.Set("cachedTimeUTC", encodedCurrentTimeUTC, options);
  });
  // Retrieve Cache Item
  [HttpGet]
  [Route("CacheRedis")]
  public async Task< string> GetAsync
  {
      var ret = "";
      var bytes = await_cache.GetAsync("cachedTimeUTC");
      if(bytes != null)
      {
          ret = Encoding.UTF8.GetString(bytes);
          _logger.LogInformation(ret);
      }
      return await Task.FromResult(ret);
  }
  ```

  1. 很明显，该Cache组件并不能做到自由切换 Redis DB，目前可在redis连接字符串一次性配置项目要使用哪个Redis DB
  2. 会在指定DB（默认为0）生成key = SampleInstancecachedTimeUTC 的redis缓存项
  3. Redis并不支持bytes[] 形式的存储值，以上byte[] 实际是以Hash的形式存储

- CSRedisCore

  该组件的功能更为强大，针对实际Redis应用场景有更多玩法。

  - 普通模式

  - 官方集群模式 redis cluster

  - 分区模式（作者实现）

  普通模式使用方法极其简单，这里要提示的是：该客户端也不支持 随意切换 Redis DB, 但是原作者给出一种缓解的方式：构造多客户端。

  ```c#
  var redisDB = new CSRedisClient[16]; // 多客户端
  for (var a = 0; a < redisDB.Length; a++)
      redisDB[a] = new CSRedisClient(Configuration.GetConnectionString("redis") + ",defualtDatabase=" + a);
  services.AddSingleton(redisDB);
  // ----------------------------
  _redisDB[0].IncrByAsync( "ProfileUsageCap", - 1)
  _redisDB[0].HGetAsync(profileUsage, eqidPair.ProfileId.ToString)
  _redisDB[0].HIncrByAsync(profileUsage, eqidPair.ProfileId.ToString, - 1);
  ```

  内置的静态操作类RedisHelper，与Redis-Cli命令完全一致，故他能原生支持"blocking pops"。

Redis的一点小经验：

  - 对自己要使用的Redis API 的时间复杂度心里要有数，尽量不要使用长时间运行的命令如keys *，可通过redis.io SlowLog命令观测 哪些命令耗费较长时间
  - Redis Key可按照“：”分隔定义成有业务意义的字符串，如NewUsers:201909:666666（某些Redis UI可直观友好查看该业务）
  - 合适确定Key-Value的大小：Redis对于small value更友好， 如果值很大，考虑划分到多个key
  - 关于缓存穿透，面试的时候会问，自行搜索布隆过滤器。
  - redis虽然有持久化机制，但在实际中会将key-value 持久化到关系型数据库，因为对于某些结构化查询，SQL更为有效。

总结：

以上三大客户端，Microsoft.Extensions.Caching.StackExchangeRedis 与其他两者的定位还是有很大差距的，单纯使用Redis 缓存特性， 有微软出品，必属精品情结的可使用此客户端；

StackExchange.Redis、CSRedisCore 对于Redis全功能特性支持的比较全，但是我也始终没有解决StackExchange.Redis ：RedisTimeoutException 超时的问题，换成CSRedisCore 确实没有出现Redis相关异常。

**Java：**

- Jedis
- Lettuce
- Redission

