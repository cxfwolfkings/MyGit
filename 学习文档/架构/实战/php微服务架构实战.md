# php微服务架构实战

**微服务优势：**

- 应用按业务拆分成服务

- 各个服务均可独立部署

- 服务可被多个应用共享

- 服务之间可以通信

- 架构上系统更加清晰

- 核心模块稳定，以服务组件为单位进行升级，避免了频繁发布带来的风险

- 开发管理方便

- 单独团队维护、工作分明，职责清晰

- 业务复用、代码复用

- 可拓展性强

**微服务挑战：**

- 依赖关系复杂
- 网络开销变大
- 服务拆分难点（边界确定）

**php挑战：**

fpm 开发模式下，因为无法常驻内存，每一次请求都要从零开始加载到退出进程，增加很多无用的开销，数据库连接无法复用也得不到保护。

fpm是以进程为单位的，因此fpm的进程数也决定了并发数。

这就是fpm开发简单的同时，给我们带来的问题。

**php解决方案：Swoft**

Swoft是一个带有服务治理功能的RPC框架。

Swoft是首个PHP常驻内存协程全栈框架，基于高性能协程swoole打造的一个PHP界的Spring Boot。

Swoft提供了类似 Dubbo 的更为优雅地使用 RPC 服务的方式，Swoft 性能非常棒，近似Golang。

#### 优雅的服务治理

**服务注册与发现**

微服务治理过程中，经常会涉及注册启动的服务到第三方集群，比如 consul / etcd 等等，以 Swoft 框架中使用 swoft-consul 组件，实现服务注册与发现为例。

实现逻辑

```php
<?php declare(strict_types=1);

namespace App\Common;

use ReflectionException;
use Swoft\Bean\Annotation\Mapping\Bean;
use Swoft\Bean\Annotation\Mapping\Inject;
use Swoft\Bean\Exception\ContainerException;
use Swoft\Consul\Agent;
use Swoft\Consul\Exception\ClientException;
use Swoft\Consul\Exception\ServerException;
use Swoft\Rpc\Client\Client;
use Swoft\Rpc\Client\Contract\ProviderInterface;

/**
 * Class RpcProvider
 * @since 2.0
 * @Bean()
 */
class RpcProvider implements ProviderInterface
{
    /**
     * @Inject()
     * @var Agent
     */
    private $agent;

    /**
     * @param Client $client
     * @return array
     * @throws ReflectionException
     * @throws ContainerException
     * @throws ClientException
     * @throws ServerException
     * @example
     * [
     *     'host:port',
     *     'host:port',
     *     'host:port',
     * ]
     */
    public function getList(Client $client): array
    {
        // Get health service from consul
        $services = $this->agent->services();
        $services = [];
        return $services;
    }
}
```

**服务熔断**

在分布式环境下，特别是微服务结构的分布式系统中， 一个软件系统调用另外一个远程系统是非常普遍的。这种远程调用的被调用方可能是另外一个进程，或者是跨网路的另外一台主机，这种远程的调用和进程的内部调用最大的区别是，远程调用可能会失败，或者挂起而没有任何回应，直到超时。更坏的情况是， 如果有多个调用者对同一个挂起的服务进行调用，那么就很有可能的是一个服务的超时等待迅速蔓延到整个分布式系统，引起连锁反应，从而消耗掉整个分布式系统大量资源。最终可能导致系统瘫痪。

断路器（Circuit Breaker）模式就是为了防止在分布式系统中出现这种瀑布似的连锁反应导致的灾难。

基本的断路器模式下，保证了断路器在open状态时，保护supplier不会被调用， 但我们还需要额外的措施可以在supplier恢复服务后，可以重置断路器。一种可行的办法是断路器定期探测supplier的服务是否恢复， 一但恢复， 就将状态设置成close。断路器进行重试时的状态为半开（half-open）状态。

熔断器的使用想到简单且功能强大，使用一个 @Breaker 注解即可，Swoft 的熔断器可以用于任何场景，例如服务调用的时候使用，请求第三方的时候都可以对它进行熔断降级。

```php
<?php declare(strict_types=1);

namespace App\Model\Logic;

use Exception;
use Swoft\Bean\Annotation\Mapping\Bean;
use Swoft\Breaker\Annotation\Mapping\Breaker;

/**
 * Class BreakerLogic
 * @since 2.0
 * @Bean()
 */
class BreakerLogic
{
    /**
     * @Breaker(fallback="loopFallback")
     * @return string
     * @throws Exception
     */
    public function loop(): string
    {
        // Do something
        throw new Exception('Breaker exception');
    }

    /**
     * @return string
     * @throws Exception
     */
    public function loopFallback(): string
    {
        // Do something
    }
}
```

**[服务限流](https://link.zhihu.com/?target=https%3A//shimo.im/docs/KcrgrdtVcJWVGG6D/read)**

限流、熔断、降级都很重要。服务不行的时候一定要熔断。限流是一个保护自己的利器，如果没有自我保护机制，那么不管有多少连接都会被接收，一旦后端处理不过来，前端流量又很大的时候服务就挂了。

限流是对稀缺资源访问时，比如秒杀，抢购的商品时，限制并发和请求的数量，从而有效的进行削峰并使得流量曲线平滑。限流的目的是对并发访问和并发请求进行限速，或者一个时间窗口内请求进行限速从而来保护系统，一旦达到或超过限制速率就可以拒绝服务或者进行排队等待等。

Swoft 限流器底层采用的是令牌桶算法，底层依赖于 Redis 实现分布式限流。

Swoft 限速器不仅可以限流控制器，也可以限制任何 bean 里面的方法，可以控制方法的访问速率。这里以下面使用示例详解：

```php
<?php declare(strict_types=1);

namespace App\Model\Logic;

use Swoft\Bean\Annotation\Mapping\Bean;
use Swoft\Limiter\Annotation\Mapping\RateLimiter;

/**
 * Class LimiterLogic
 * @since 2.0
 * @Bean()
 */
class LimiterLogic
{
    /**
     * @RequestMapping()
     * @RateLimiter(rate=20, fallback="limiterFallback")
     * @param Request $request
     * @return array
     */
    public function requestLimiter2(Request $request): array
    {
        $uri = $request->getUriPath();
        return ['requestLimiter2', $uri];
    }
    
    /**
     * @param Request $request
     * @return array
     */
    public function limiterFallback(Request $request): array
    {
        $uri = $request->getUriPath();
        return ['limiterFallback', $uri];
    }
}
```

key 这里支持 `symfony/expression-language` 表达式， 如果被限速会调用 `fallback`中定义的`limiterFallback` 方法

[**配置中心**](https://link.zhihu.com/?target=https%3A//shimo.im/docs/KcrgrdtVcJWVGG6D/read)

说起配置中心前我们先说说配置文件，我们并不陌生，它提供我们可以动态修改程序运行能力。引用别人的一句话就是：

> 系统运行时(runtime)飞行姿态的动态调整！

我可以把我们的工作称之为在快速飞行的飞机上修理零件。我们人类总是无法掌控和预知一切。对于我们系统来说，我们总是需要预留一些控制线条，以便在我们需要的时候做出调整，控制系统方向（如灰度控制、限流调整），这对于拥抱变化的互联网行业尤为重要。

对于单机版，我们称之为配置（文件）；对于分布式集群系统，我们称之为配置中心（系统）;

到底什么是分布式配置中心？

随着业务的发展、微服务架构的升级，服务的数量、程序的配置日益增多（各种微服务、各种服务器地址、各种参数），传统的配置文件方式和数据库的方式已无法满足开发人员对配置管理的要求：

- 安全性：配置跟随源代码保存在代码库中，容易造成配置泄漏；

- 时效性：修改配置，需要重启服务才能生效；

- 局限性：无法支持动态调整：例如日志开关、功能开关；

因此，我们需要配置中心来统一管理配置！把业务开发者从复杂以及繁琐的配置中解脱出来，只需专注于业务代码本身，从而能够显著提升开发以及运维效率。同时将配置和发布包解藕也进一步提升发布的成功率，并为运维的细力度管控、应急处理等提供强有力的支持。

关于分布式配置中心，网上已经有很多开源的解决方案，例如：

Apollo是携程框架部门研发的分布式配置中心，能够集中化管理应用不同环境、不同集群的配置，配置修改后能够实时推送到应用端，并且具备规范的权限、流程治理等特性，适用于微服务配置管理场景。

以 Apollo 为例，从远端配置中心拉取配置以及安全重启服务。如果对 Apollo 不熟悉，可以先看 Swoft 扩展 Apollo 组件以及阅读 Apollo 官方文档。

以 Swoft 中使用 Apollo 为例，当 Apollo 配置变更后，重启服务(http-server / rpc-server/ ws-server)。如下是一个 agent 例子：

```php
<?php declare(strict_types=1);

namespace App\Model\Logic;

use Swoft\Apollo\Config;
use Swoft\Apollo\Exception\ApolloException;
use Swoft\Bean\Annotation\Mapping\Bean;
use Swoft\Bean\Annotation\Mapping\Inject;

/**
 * Class ApolloLogic
 * @since 2.0
 * @Bean()
 */
class ApolloLogic
{
    /**
     * @Inject()
     * @var Config
     */
    private $config;

    /**
     * @throws ApolloException
     */
    public function pull(): void
    {
        $data = $this->config->pull('application');    
        // Print data
        var_dump($data);
    }
}
```

以上就是一个简单的 Apollo 配置拉取，`Swoft-Apollo`除此方法外，还提供了更多的使用方法。

很多coder在进阶的时候都会遇到一些问题和瓶颈，业务代码写多了没有方向感，不知道该从那里入手去提升，对此我们需要自己总结并整理多个知识点的高级进阶干货（读书百遍，其意自现），包括但不限于：分布式架构、高可扩展、高性能、高并发、服务器性能调优、TP6，laravel，YII2，Redis，Swoole、Swoft、Kafka、Mysql优化、shell脚本、Docker、微服务、Nginx等。

**参考：**

- https://www.swoft.org/#referral
- https://shimo.im/docs/KcrgrdtVcJWVGG6D/read