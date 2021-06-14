# RabbitMQ部署

1. 安装
2. 集群部署
3. [管理](#管理)
4. 编程



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



## 集群部署

部署目标

|       | IP         | 名称    | 存储方式 | 说明 |
| ----- | ---------- | ------- | -------- | ---- |
| 节点1 | 172.19.0.2 | rabbit1 | disk     | 主   |
| 节点2 | 172.19.0.3 | rabbit2 | ram      | 从   |
| 节点3 | 172.19.0.4 | rabbit3 | ram      | 从   |

创建映射目录

```sh
mkdir -p /data/rabbit/1/data /data/rabbit/1/config \
         /data/rabbit/2/data /data/rabbit/2/config \
         /data/rabbit/3/data /data/rabbit/3/config
```

**docker-compose方式**

docker-compose.yml

```yaml
version: '3.7'

services:
  rabbit1:
    container_name: rabbit1
    image: rabbitmq:management
    restart: always
    hostname: rabbit1
    environment:
      - RABBITMQ_ERLANG_COOKIE=CURIOAPPLICATION
      - RABBITMQ_DEFAULT_USER=root
      - RABBITMQ_DEFAULT_PASS=123
    ports:
      - "4369:4369"
      - "5671:5671"
      - "5672:5672"
      - "15671:15671"
      - "15672:15672"
      - "25672:25672"
    volumes:
      - /data/rabbit/1/data:/var/lib/rabbitmq
      - /data/rabbit/1/config/rabbitmq.sh:/etc/rabbitmq/rabbitmq.sh
      - /data/rabbit/hosts:/etc/hosts
    networks:
      lead:
        ipv4_address: 172.19.0.2
      
      
  rabbit2:
    container_name: rabbit2
    image: rabbitmq:management
    restart: always
    hostname: rabbit2
    environment:
      - RABBITMQ_ERLANG_COOKIE=CURIOAPPLICATION
      - RABBITMQ_DEFAULT_USER=root
      - RABBITMQ_DEFAULT_PASS=123
    ports:
      - "4379:4369"
      - "5681:5671"
      - "5682:5672"
      - "15681:15671"
      - "15682:15672"
      - "25682:25672"
    volumes:
      - /data/rabbit/2/data:/var/lib/rabbitmq
      - /data/rabbit/2/config/rabbitmq.sh:/etc/rabbitmq/rabbitmq.sh
      - /data/rabbit/hosts:/etc/hosts
    networks:
      lead:
        ipv4_address: 172.19.0.3
        
  rabbit3:
    container_name: rabbit3
    image: rabbitmq:management
    restart: always
    hostname: rabbit3
    environment:
      - RABBITMQ_ERLANG_COOKIE=CURIOAPPLICATION
      - RABBITMQ_DEFAULT_USER=root
      - RABBITMQ_DEFAULT_PASS=123
    ports:
      - "4389:4369"
      - "5691:5671"
      - "5692:5672"
      - "15691:15671"
      - "15692:15672"
      - "25692:25672"
    volumes:
      - /data/rabbit/3/data:/var/lib/rabbitmq
      - /data/rabbit/3/config/rabbitmq.sh:/etc/rabbitmq/rabbitmq.sh
      - /data/rabbit/hosts:/etc/hosts
    networks:
      lead:
        ipv4_address: 172.19.0.4
      
networks:
  lead:
    ipam:
      config:
      - subnet: 172.19.0.0/24
```

自定义网路名称：`<rootDirName>_lead`

在 /data/rabbit/hosts 文件中，添加各节点的信息

```sh
172.19.0.2 rabbit1
172.19.0.3 rabbit2
172.19.0.4 rabbit3
```

启动容器

```sh
docker-compose up -d
```

执行集群命令：

```sh
# 事先规划好了各节点rabbitmq的存储方式，即节点1是disk，节点2和节点3都是ram
docker exec -it rabbit1 /bin/bash
# disk节点（节点1主节点）执行
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app
# ram节点（节点2和节点3）执行：
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl join_cluster --ram rabbit@rabbit1
rabbitmqctl start_app
# 如果后期需要修改节点的存储方式可以使用：
rabbitmqctl change_cluster_node_type disc/ram  # 更改节点为磁盘或内存节点
# 查看哪些是disk nodes，哪些是ram nodes，正在运行的节点，各节点的版本信息等等
rabbitmqctl cluster_status
```

参考：

- https://blog.csdn.net/wzc900810/article/details/108507298
- https://www.jianshu.com/p/34d60096b33f



**k8s方式**

rabbit-sc.yml

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: alicloud-nas-subpath-public
provisioner: nasplugin.csi.alibabacloud.com
mountOptions:
- nolock,tcp,noresvport
- vers=3
parameters:
  volumeAs: subpath
  server: "xxxxxx.cn-hangzhou.nas.aliyuncs.com:/"
reclaimPolicy: Retain
```

rabbit-ns.yml

```yml
apiVersion: v1
kind: Namespace
metadata:
  name: wind-rabbit
```

rabbit-cfm.yaml

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: rmq-cluster-config
  namespace: wind-rabbit
  labels:
    addonmanager.kubernetes.io/mode: Reconcile
data:
    enabled_plugins: |
      [rabbitmq_management,rabbitmq_peer_discovery_k8s].
    rabbitmq.conf: |
      loopback_users.guest = false

      ## Clustering
      cluster_formation.peer_discovery_backend = rabbit_peer_discovery_k8s
      cluster_formation.k8s.host = kubernetes.default.svc.cluster.local
      cluster_formation.k8s.address_type = hostname
      ##################################################
      # public-service is rabbitmq-cluster's namespace #
      ##################################################
      cluster_formation.k8s.hostname_suffix = .rmq-cluster.public-service.svc.cluster.local
      cluster_formation.node_cleanup.interval = 10
      cluster_formation.node_cleanup.only_log_warning = true
      cluster_partition_handling = autoheal
      ## queue master locator
      queue_master_locator=min-masters
```

rabbit-rbac.yaml

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: rmq-cluster
  namespace: wind-rabbit
  
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: Role
metadata:
  name: rmq-cluster
  namespace: wind-rabbit
rules:
  - apiGroups:
      - ""
    resources:
      - endpoints
    verbs:
      - get

---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: RoleBinding
metadata:
  name: rmq-cluster
  namespace: wind-rabbit
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: rmq-cluster
subjects:
- kind: ServiceAccount
  name: rmq-cluster
  namespace: wind-rabbit
```

rabbit-secret.yaml

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: rmq-cluster-secret
  namespace: wind-rabbit
stringData:
  cookie: ERLANG_COOKIE
  username: admin
  password: admin123
type: Opaque
```

rabbit-svc.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: rmq-cluster
  namespace: wind-rabbit
  labels:
    app: rmq-cluster
spec:
  selector:
    app: rmq-cluster
  clusterIP: 172.21.11.245  # 指定clusterIP，方便使用
  ports:
  - name: http
    port: 15672
    protocol: TCP
    targetPort: 15672
  - name: amqp
    port: 5672
    protocol: TCP
    targetPort: 5672
  type: ClusterIP
```

rabbit-sts.yaml

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rmq-cluster
  namespace: wind-rabbit
  labels:
    app: rmq-cluster
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rmq-cluster
  serviceName: rmq-cluster
  template:
    metadata:
      labels:
        app: rmq-cluster
    spec:
      serviceAccountName: rmq-cluster
      terminationGracePeriodSeconds: 30
      containers:
      - name: rabbitmq
        image: rabbitmq:3.7-management
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 15672
          name: http
          protocol: TCP
        - containerPort: 5672
          name: amqp
          protocol: TCP
        command:
        - sh
        args:
        - -c
        - cp -v /etc/rabbitmq/rabbitmq.conf ${RABBITMQ_CONFIG_FILE}; exec docker-entrypoint.sh
          rabbitmq-server
        env:
        - name: RABBITMQ_DEFAULT_USER
          valueFrom:
            secretKeyRef:
              key: username
              name: rmq-cluster-secret
        - name: RABBITMQ_DEFAULT_PASS
          valueFrom:
            secretKeyRef:
              key: password
              name: rmq-cluster-secret
        - name: RABBITMQ_ERLANG_COOKIE
          valueFrom:
            secretKeyRef:
              key: cookie
              name: rmq-cluster-secret
        - name: K8S_SERVICE_NAME
          value: rmq-cluster
        - name: POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: RABBITMQ_USE_LONGNAME
          value: "true"
        - name: RABBITMQ_NODENAME
          value: rabbit@$(POD_NAME).rmq-cluster.$(POD_NAMESPACE).svc.cluster.local
        - name: RABBITMQ_CONFIG_FILE
          value: /var/lib/rabbitmq/rabbitmq.conf
        livenessProbe:
          exec:
            command:
            - rabbitmqctl
            - status
          initialDelaySeconds: 30
          timeoutSeconds: 10
        readinessProbe:
          exec:
            command:
            - rabbitmqctl
            - status
          initialDelaySeconds: 10
          timeoutSeconds: 10
        volumeMounts:
        - name: config-volume
          mountPath: /etc/rabbitmq
          readOnly: false
        - name: rabbitmq-storage
          mountPath: /var/lib/rabbitmq
          readOnly: false
      volumes:
      - name: config-volume
        configMap:
          items:
          - key: rabbitmq.conf
            path: rabbitmq.conf
          - key: enabled_plugins
            path: enabled_plugins
          name: rmq-cluster-config
      - name: rabbitmq-storage
        persistentVolumeClaim:
          claimName: rabbitmq-cluster-storage

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: rabbitmq-cluster-storage
  namespace: wind-rabbit
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 20Gi
  storageClassName: alicloud-nas-subpath-public
```

rabbit-ingress.yaml

```yaml
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: rabbitmq
  namespace: wind-rabbit
spec:
  rules:
    - host: rabbitmq.lzxlinux.com
      http:
        paths:
          - path: /
            backend:
              serviceName: rmq-cluster
              servicePort: 15672
```

部署完毕后

```yaml
kubectl get all -n wind-rabbit
```

添加hosts：`rabbitmq.lzxlinux.com`，使用初始账号密码`guest/guest`登录即可。

如果使用的是 default 命名空间，就使用 sed 命令将yaml文件中 wind-rabbit 全局替换为 default：

```sh
sed -i 's/wind-rabbit/default/g' ./*
kubectl apply -f .
```

如果使用的是nfs持久化存储，对于nfs目录赋予755权限，然后其它节点安装nfs即可：

```sh
# 选择一个节点上做nfs共享
yum install -y nfs-utils rpcbind
mkdir -p /data/rabbitmq
vim /etc/exports
# ----------------------------------------------------
/data/rabbitmq 192.168.30.0/24(rw,sync,no_root_squash)
# ----------------------------------------------------
chmod -R 755 /data/rabbitmq
exportfs -arv
systemctl enable rpcbind && systemctl start rpcbind
systemctl enable nfs && systemctl start nfs

# nfs部署完毕。对于需要使用nfs的node节点，都要安装nfs：
yum install -y nfs-utils
```

rabbit-pv.yaml

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: rabbitmq-pv
  labels:
    app: rmq-cluster
spec:
  capacity:
    storage: 5Gi
  accessModes:
    - ReadWriteMany
  nfs:
    server: 192.168.30.129
    path: /data/rabbitmq
```

rabbit-pvc.yaml

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: rabbitmq-cluster-storage
  namespace: default
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 5Gi
  selector:
    matchLabels:
      app: rmq-cluster
```

再次部署一遍

```sh
kubectl get pv
kubectl get pvc
kubectl get pod
kubectl get svc
```

参考：

- https://blog.csdn.net/miss1181248983/article/details/106440068/



## 管理

Web控制台地址：http://0.0.0.0:15672/，默认账号：guest / guest（此文档compose安装时指定了 root / 123）

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

**技术术语：**

- Broker：简单来说就是消息队列服务器实体。

- producer：消息生产者，就是投递消息的程序。

- consumer：消息消费者，就是接受消息的程序。

- vhost：虚拟主机，一个broker里可以开设多个vhost，用作权限分离，把不同的系统使用的rabbitmq区分开，共用一个消息队列服务器，但看上去就像各自在用不用的rabbitmq服务器一样。

- Connection：一个网络连接，比如TCP/IP套接字连接。

- channel：消息通道，是建立在真实的TCP连接内的虚拟连接（是我们与RabbitMQ打交道的最重要的一个接口）。仅仅创建了客户端到Broker之间的连接后，客户端还是不能发送消息的，需要为每一个Connection创建Channel，AMQP协议规定只有通过Channel才能执行AMQP的命令。AMQP的命令都是通过信道发送出去的（我们大部分的业务操作是在Channel这个接口中完成的，包括定义Queue、定义Exchange、绑定Queue与Exchange、发布消息等）。每条信道都会被指派一个唯一ID。在客户端的每个连接里，可建立多个channel，每个channel代表一个会话任务，理论上无限制，减少TCP创建和销毁的开销，实现共用TCP的效果。之所以需要Channel，是因为TCP连接的建立和释放都是十分昂贵的，如果一个客户端每一个线程都需要与Broker交互，如果每一个线程都建立一个TCP连接，暂且不考虑TCP连接是否浪费，就算操作系统也无法承受每秒建立如此多的TCP连接。

  > 注1：一个生产者或一个消费者与MQ服务器之间只有一条TCP连接 
  >
  > 注2：RabbitMQ建议客户端线程之间不要共用Channel，至少要保证共用Channel的线程发送消息必须是串行的，但是建议尽量共用Connection。

- Exchange：消息交换机，生产者不是直接将消息投递到Queue中的，实际上是生产者将消息发送到Exchange，由Exchange将消息路由到一个或多个Queue中（或者丢弃）。

- Exchange Types RabbitMQ常用的Exchange Type有fanout、direct、topic、headers这四种（AMQP规范里还提到两种Exchange Type，分别为system与自定义，这里不予以描述），之后会分别进行介绍。
- Queue：消息队列载体，每个消息都会被投入到一个或多个队列。
- Binding：绑定，它的作用就是把exchange和queue按照路由规则绑定起来，这样RabbitMQ就知道如何正确地将消息路由到指定的Queue了。

参考：

- https://blog.csdn.net/mingongge/article/details/99512557



## 编程

**生产者**

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

**消费者**

RabbitMQ 支持 “Push API” 和 “Pull API”：

- 使用 “Push API”，即 “订阅” 队列，新的消息将自动 “投递” 到消费者；
- 使用 “Pull API”，即 “显式” 地获取新的消息。

**消费者，使用 Push API**

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

**消费者，使用 Pull API**

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
- [官网](https://www.rabbitmq.com/documentation.html)