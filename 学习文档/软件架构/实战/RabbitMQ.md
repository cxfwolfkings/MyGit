# RabbitMQ



## 安装

RabbitMQ 使用 Erlang，必须前置就绪。获取 RabbitMQ 二进制包（Generic UNIX binary）。

```sh
# 启动 RabbitMQ 服务器（AMQP 协议端口：5672）
./sbin/rabbitmq-server
```

RabbitMQ 提供了命令行管理工具 rabbitmqctl，但推荐使用 [Management](https://www.rabbitmq.com/management.html) 插件。RabbitMQ 通过 Management 插件，提供了 Web 控制台。

```sh
# 启用 Management 插件（需要重启消息代理）
./sbin/rabbitmq-plugins enable rabbitmq_management
```

Web 控制台：http://127.0.0.1:15672/#/，以 guest / guest 登录。支持交换器、队列、绑定的管理，以及消息代理的监控。

除了 Web 控制台，Management 插件亦提供 RESTful API 形式的接口。完整的 API 文档，位于：http://127.0.0.1:15672/api/index.html。



## 管理

通过 Web 控制台，创建虚拟主机 `hello`，供后续示例使用。

```sh
# 列出虚拟主机 hello，全部交换器
curl -u guest:guest http://127.0.0.1:15672/api/exchanges/hello
# 默认情况，RabbitMQ 包含了 “默认交换器” 和以 amq.* 命令，仅限内部使用的交换器。


# 于虚拟主机 hello，创建交换器 exchange_1，类型：direct
# 若成功创建，HTTP 返回码：201
curl -u guest:guest -H "Content-Type:application/json" -XPUT -d'{"type":"direct"}' http://127.0.0.1:15672/api/exchanges/hello/exchange_1
# 通过 -d 传递 JSON 格式的交换器属性，主要包括：
#   type：交换器类型，必选参数
#   auto_delete：是否自动删除，可选参数，默认 false
#   durable：是否持久化，可选参数，默认 false


# 删除虚拟主机 hello 中的交换器 exchange_1
# 若成功删除，HTTP 返回码：204
curl -u guest:guest -XDELETE http://127.0.0.1:15672/api/exchanges/hello/exchange_1?if-unused=true
# 通过 if-unused=true 参数控制：当且仅当交换器没有被绑定时，允许删除。


# 于虚拟主机 hello，创建队列 queue_1
# 若成功创建，HTTP 返回码：201
curl -u guest:guest -H "Content-Type:application/json" -XPUT -d'{}' http://127.0.0.1:15672/api/queues/hello/queue_1
# 通过 -d 传递 JSON 格式的队列属性，主要包括：
#   auto_delete：是否自动删除，默认 false
#   durable：是否持久化，可选参数，默认 false


# 删除虚拟主机 hello 中的交换器 queue_1
# 若成功删除，HTTP 返回码：204
curl -u guest:guest -XDELETE "http://127.0.0.1:15672/api/queues/hello/queue_1?if-unused=true&if-empty=true"
# 参数控制：
#   if-unused=true：当且仅当队列没有消费者时，允许删除
#   if-empty=true：当且仅当队列没有消息时，允许删除


# 于虚拟主机 hello，队列 queue_1 绑定到交换器 exchange_1，绑定键 B
# 若成功创建，HTTP 返回码：201
curl -u guest:guest -H "Content-Type:application/json" -XPOST -d'{"routing_key": "B"}' http://127.0.0.1:15672/api/bindings/hello/e/exchange_1/q/queue_1
# 若成功创建，将返回绑定的信息，其中的 properties_key，即为绑定的 “标示”。


# 查看虚拟主机 hello 中，绑定到交换器 exchange_1 的绑定信息
curl -u guest:guest http://127.0.0.1:15672/api/exchanges/hello/exchange_1/bindings/source
# 需要说明：RabbitMQ 支持交换器绑定到交换器，这里不予以展开。


# 查看虚拟主机 hello 中，队列 queue_1 的绑定信息
curl -u guest:guest http://127.0.0.1:15672/api/queues/hello/queue_1/bindings
# 需要说明：RabbitMQ 中的任何队列，都将以队列名称作为 “绑定键”，绑定到 “默认交换器”。


# 于虚拟主机 hello，删除队列 queue_1 与交换器 exchange_1，properties_key 为 B 的绑定
# 若成功删除，HTTP 返回码：204
curl -u guest:guest -XDELETE http://127.0.0.1:15672/api/bindings/hello/e/exchange_1/q/queue_1/B
```



## 编程

### 生产者

```java
package com.gitchat.rmq;

import com.rabbitmq.client.*;

public class Producer_1 {

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setVirtualHost("gitchat");

        // Connection -> Channel
        try (Connection connection = factory.newConnection()) {
            try (Channel channel = connection.createChannel()) {
                // 向交换器 “exchange_1” 发送消息，路由键 “C”
                channel.basicPublish("exchange_1", "C", MessageProperties.PERSISTENT_TEXT_PLAIN.builder().build(), "2 message...".getBytes());
            }
        }
    }
}
```

代码所示，其中：

1. 基于 `com.rabbitmq.client.ConnectionFactory` 配置消息代理的属性；
2. 通过 `ConnectionFactory` 建立连接（`com.rabbitmq.client.Connection`）；
3. 通过 `Connection` 建立信道（`com.rabbitmq.client.Channel`）；
4. 发送消息，`PERSISTENT_TEXT_PLAIN` 表示消息 “持久化”，且 `Content-Type` 属性为 `text/plain`。

需要说明：示例程序使用 try-with-resource 机制，确保信道和连接能够关闭。关于 `Connection` 和 `Channel`，通常的建议包括：

- `Connection` 和 `Channel`，不建议每次操作都新创建实例，建议使用 “资源池”；
- 使用 `Channel` 发送消息的部分，需要位于 “临界区”，避免多个线程并发操作相同的 `Channel` 实例。

### 消费者

RabbitMQ 支持 “Push API” 和 “Pull API”：

- 使用 “Push API”，即 “订阅” 队列，新的消息将自动 “投递” 到消费者；
- 使用 “Pull API”，即 “显式” 地获取新的消息。

##### **消费者，使用 Push API**

```java
package com.gitchat.rmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Consumer_1 {

    private static volatile boolean terminateFlag = false;
    private static Lock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://guest:guest@127.0.0.1:5672/gitchat");

        // Connection -> Channel
        try (Connection connection = factory.newConnection()) {
            try (Channel channel = connection.createChannel()) {
                // 消费者
                channel.basicConsume("queue_1", false, "consumerTag_1", new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        try {
                            System.out.println("message received: " + new String(body));

                            // processing...

                            getChannel().basicAck(envelope.getDeliveryTag(), false);
                        } catch (Exception e) {
                            getChannel().basicReject(envelope.getDeliveryTag(), true);
                        }
                    }
                });

                // main 线程阻塞
                lock.lock();
                while (!terminateFlag) {
                    condition.await();
                }
            }
        }
    }
}
```

代码所示：

1. 使用 “连接字符串” 配置 `ConnectionFactory` 的消息代理属性；
2. 通过 `basicConsume` 方法：
   1. 订阅队列 `queue_1`
   2. 关闭消息的 “自动确认”
   3. 设置消费者的 “唯一标示”
   4. 注册 “回调函数”，进行消息处理
3. 消费者通过 `basicAck` 确认消息（RabbitMQ 将消息由队列中移除），`basicReject` 拒绝消息并要求重新投递。

需要说明，示例程序中，将 main 线程阻塞，原因在于：消费者的 “回调函数” 位于独立的线程中调用。

##### **消费者，使用 Pull API**

```java
package com.gitchat.rmq;

import com.rabbitmq.client.*;

public class Consumer_2 {

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://guest:guest@172.22.3.119:5672/gitchat");

        // Connection -> Channel
        try (Connection connection = factory.newConnection()) {
            try (Channel channel = connection.createChannel()) {
                // 消费者，单次消费
                GetResponse response = channel.basicGet("queue_1", false);

                if (response != null) {
                    try {
                        System.out.println("message received: " + new String(response.getBody()));

                        // processing...

                        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    } catch (Exception e) {
                        channel.basicReject(response.getEnvelope().getDeliveryTag(), false);
                    }
                }
            }
        }
    }
}
```

代码所示，与 Push API 的区别：使用 `basicGet` “显式” 地获取消息（单次消费）。

为了实现高可用，启用“生产者确认”模式：

```java
// 启用 “生产者确认” 模式
channel.confirmSelect();

// 消息发送
channel.basicPublish("exchange_1", "C", MessageProperties.PERSISTENT_TEXT_PLAIN.builder().build(), "2 message...".getBytes());

// 等待 “确认”（1000 毫秒）
boolean allAck = false;

try {
    allAck = channel.waitForConfirms(1000);
} catch (TimeoutException ex) {
    // 超时
}

if (!allAck) {
    // 需要重新发送
}
```

代码中所示：

1. 使用 `confirmSelect` 方法，信道启用 “生产者确认” 模式
2. 完成消息发送，使用 `waitForConfirms` 等待确认，超时或未确认（`nack`），需要重新发送

若交换器无法将消息路由到任何队列，默认情况，消息将被 “丢弃”，特定的场景中，生产者需要感知

```java
// 交换器无法 “路由” 消息的 “回调函数”
channel.addReturnListener(new ReturnCallback() {
    @Override
    public void handle(Return returnMessage) {
        // ...
    }
});

// 消息发送
channel.basicPublish("exchange_1", "C", true, MessageProperties.PERSISTENT_TEXT_PLAIN.builder().build(), "2 message...".getBytes());
```

代码中所示：

1. 通过 `addReturnListener` 注册 “回调函数”
2. 消息发送时，设置 `mandatory` 标记位



参考：

- https://gitbook.cn/books/5cc6fe974cf1322991b853c5/index.html
- https://gitbook.cn/gitchat/activity/5d2e85497edd5a428e215e24
- https://gitbook.cn/gitchat/activity/5d4bd8c1edd832602d429479
- https://gitbook.cn/gitchat/activity/5b90f9214fb1bd5c9acd4338
- https://gitbook.cn/gitchat/activity/5cfcd4255656b03562c9166d
- https://gitbook.cn/gitchat/activity/5f71a7c03334370f1f80e223
- https://gitbook.cn/gitchat/activity/5b18f8fe02fa96300bc92dd4