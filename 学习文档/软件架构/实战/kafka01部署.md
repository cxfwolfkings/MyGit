# Kafka部署

准备工作：

```sh
# 本地创建目录
mkdir -p /data/zookeeper/1/data /data/zookeeper/1/datalog /data/zookeeper/1/logs \
         /data/zookeeper/2/data /data/zookeeper/2/datalog /data/zookeeper/2/logs \
         /data/zookeeper/3/data /data/zookeeper/3/datalog /data/zookeeper/3/logs \
         /data/kafka/1/logs /data/kafka/2/logs /data/kafka/3/logs \
         /data/kafka/bin
# 查看目录信息
ll /data/zookeeper
```

zk集群：

```yaml
version: '3.7'

services:
  zoo1:
    image: zookeeper
    container_name: zoo1
    restart: always
    privileged: true
    hostname: zoo1
    ports:
      - 2181:2181
    volumes:
      - /data/zookeeper/1/data:/data
      - /data/zookeeper/1/datalog:/datalog
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=0.0.0.0:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181
    networks:
      default:
        ipv4_address: 172.18.1.20

  zoo2:
    image: zookeeper
    container_name: zoo2
    restart: always
    privileged: true
    hostname: zoo2
    ports:
      - 2182:2181
    volumes:
      - /data/zookeeper/2/data:/data
      - /data/zookeeper/2/datalog:/datalog
    environment:
      ZOO_MY_ID: 2
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=0.0.0.0:2888:3888;2181 server.3=zoo3:2888:3888;2181
    networks:
      default:
        ipv4_address: 172.18.1.21

  zoo3:
    image: zookeeper
    container_name: zoo3
    restart: always
    privileged: true
    hostname: zoo3
    ports:
      - 2183:2181
    volumes:
      - /data/zookeeper/3/data:/data
      - /data/zookeeper/3/datalog:/datalog
    environment:
      ZOO_MY_ID: 3
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=0.0.0.0:2888:3888;2181
    networks:
      default:
        ipv4_address: 172.18.1.22

networks:
  default:
    external:
      name: lead_pm1
```

kafka集群以及kafka-manager

```yaml
version: '3.7'

services:
  kafka1:
    image: wurstmeister/kafka
    restart: always
    hostname: kafka1
    container_name: kafka1
    ports:
    - "9092:9092"
    - "9999:9999"
    environment:
      KAFKA_BROKER_ID: 1  # 不是初次部署则删除
      KAFKA_HOST_NAME: kafka1
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://10.30.202.101:9092
      KAFKA_ADVERTISED_HOST_NAME: kafka1
      KAFKA_ADVERTISED_PORT: 9092
      KAFKA_ZOOKEEPER_CONNECT: zoo1:2181,zoo2:2181,zoo3:2181
      JMX_PORT: 9999 
    volumes:
    - /data/kafka/1/logs:/kafka
    - /data/kafka/bin/kafka-run-class.sh:/opt/kafka/bin/kafka-run-class.sh
    external_links: # 连接本compose文件以外的container
    - zoo1
    - zoo2
    - zoo3
    networks:
      default:
        ipv4_address: 172.18.1.23

  kafka2:
    image: wurstmeister/kafka
    restart: always
    hostname: kafka2
    container_name: kafka2
    ports:
    - "9093:9092"
    - "9998:9999"
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_HOST_NAME: kafka2
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://10.30.202.101:9093
      KAFKA_ADVERTISED_HOST_NAME: kafka2
      KAFKA_ADVERTISED_PORT: 9093
      KAFKA_ZOOKEEPER_CONNECT: zoo1:2181,zoo2:2181,zoo3:2181
      JMX_PORT: 9999
    volumes:
    - /data/kafka/2/logs:/kafka
    external_links:
    - zoo1
    - zoo2
    - zoo3
    networks:
      default:
        ipv4_address: 172.18.1.24

  kafka3:
    image: wurstmeister/kafka
    restart: always
    hostname: kafka3
    container_name: kafka3
    ports:
    - "9094:9092"
    - "9997:9999"
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_HOST_NAME: kafka3
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://10.30.202.101:9094
      KAFKA_ADVERTISED_HOST_NAME: kafka3
      KAFKA_ADVERTISED_PORT: 9094
      KAFKA_ZOOKEEPER_CONNECT: zoo1:2181,zoo2:2181,zoo3:2181
      JMX_PORT: 9999
    volumes:
    - /data/kafka/3/logs:/kafka
    external_links:
    - zoo1
    - zoo2
    - zoo3
    networks:
      default:
        ipv4_address: 172.18.1.25

  kafka-manager:
    image: sheepkiller/kafka-manager:latest
    restart: always
    container_name: kafa-manager
    hostname: kafka-manager
    ports:
      - "9091:9000"
    links:           # 连接本compose文件创建的container
      - kafka1
      - kafka2
      - kafka3
    external_links:  # 连接本compose文件以外的container
      - zoo1
      - zoo2
      - zoo3
    environment:
      ZK_HOSTS: zoo1:2181,zoo2:2181,zoo3:2181
      KAFKA_BROKERS: kafka1:9092,kafka2:9093,kafka3:9094
      APPLICATION_SECRET: letmein
      KM_ARGS: -Djava.net.preferIPv4Stack=true
      KAFKA_MANAGER_AUTH_ENABLED: "true"  # 开启验证，不开启时下面账号也不需要
      KAFKA_MANAGER_USERNAME: "admin"  # 用户名
      KAFKA_MANAGER_PASSWORD: "Qwerty123456"  # 密码
    networks:
      default:
        ipv4_address: 172.18.1.26

networks:
  default:
    external:  # 使用已创建的网络
      name: lead_pm1
```

生成镜像并启动容器：

```sh
docker-compose up -d
```

当在 kafka-run-class.sh 中添加了 JMX_PORT 开启了 jmx 后，在使用 kafka bin/ 目录下的脚本时会报如下错误：

`java.rmi.server.ExportException: Port already in use`

解决方案：[#1983](https://github.com/apache/kafka/pull/1983/commits/2c5d40e946bcc149b1a9b2c01eced4ae47a734c5)

```sh
# 从容器复制到服务器
docker cp kafka1:/opt/kafka/bin/kafka-run-class.sh /data/kafka/bin/kafka-run-class.sh
# 修改
# 48行
# need to check if called to start server or client in order to correctly decide about JMX_PORT
ISKAFKASERVER="false"
if [[ "$*" =~ "kafka.Kafka" ]]; then
    ISKAFKASERVER="true"
fi

base_dir=$(dirname $0)/..

if [ -z "$SCALA_VERSION" ]; then
# ...
# 153行
fi

# JMX port to use
# if [  $JMX_PORT ]; then
if [  $JMX_PORT ] && [ -z "$ISKAFKASERVER" ]; then
  KAFKA_JMX_OPTS="$KAFKA_JMX_OPTS -Dcom.sun.management.jmxremote.port=$JMX_PORT "
fi
```

创建topic：

```sh
# 1. 验证，每个list理论上都可以看到新建的topic
docker exec -it kafka1 bash
kafka-topics.sh --create --zookeeper zoo1:2181 --replication-factor 1 --partitions 3 --topic test001
kafka-topics.sh --list --zookeeper zoo1:2181
kafka-topics.sh --list --zookeeper zoo2:2181
kafka-topics.sh --list --zookeeper zoo3:2181
# 2. 生产消息
kafka-console-producer.sh --broker-list kafka1:9092,kafka2:9092,kafka3:9092 --topic test001
# 3. 消费消息
kafka-console-consumer.sh --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --topic test001 --from-beginning

# 查询集群描述
kafka-topics.sh --describe --zookeeper zoo1:2181
# 查看topic消息
kafka-console-consumer.sh --bootstrap-server kafka1:9092 --topic test001 --from-beginning
kafka-console-consumer.sh --bootstrap-server kafka2:9092 --topic test001 --from-beginning
kafka-console-consumer.sh --bootstrap-server kafka3:9092 --topic test001 --from-beginning
```

server.properties常用参数介绍：

```properties
# 当前机器在集群中的唯一标识，和zookeeper的myid性质一样，要求集群中每个broker.id都说不一样的，可以从0开始递增，也可以从1开始递增
broker.id=0
# 监听地址，需要提供外网服务的话，要设置本地的IP地址，当前kafka对外提供服务的端口默认是9092
listeners=PLAINTEXT://test1:9092
# 这个是borker进行网络处理的线程数
num.network.threads=3
# 这个是borker进行I/O处理的线程数
num.io.threads=8
# 发送缓冲区buffer大小，数据不是一下子就发送的，先回存储到缓冲区了到达一定的大小后在发送，能提高性能
socket.send.buffer.bytes=102400
# kafka接收缓冲区大小，当数据到达一定大小后在序列化到磁盘
socket.receive.buffer.bytes=102400
# 这个参数是向kafka请求消息或者向kafka发送消息的请请求的最大数，这个值不能超过java的堆栈大小
socket.request.max.bytes=104857600
# 消息存放的目录，这个目录可以配置为“，”逗号分割的表达式，上面的num.io.threads要大于这个目录的个数这个目录，
# 如果配置多个目录，新创建的topic他把消息持久化的地方是，当前以逗号分割的目录中，那个分区数最少就放那一个
log.dirs=/tmp/kafka-logs
# 默认的分区数，一个topic默认1个分区数
num.partitions=1
# 每个数据目录用来日志恢复的线程数目
num.recovery.threads.per.data.dir=1
# topic的offset的备份份数
offsets.topic.replication.factor=1
# 事务主题的复制因子（设置更高以确保可用性）。 内部主题创建将失败，直到群集大小满足此复制因素要求。
transaction.state.log.replication.factor=1
# 覆盖事务主题的min.insync.replicas配置。
transaction.state.log.min.isr=1
# 默认消息的最大持久化时间，168小时，7天
log.retention.hours=168
# 日志达到删除大小的阈值。每个topic下每个分区保存数据的最大文件大小；注意，这是每个分区的上限，因此这个数值乘以分区的个数就是每个topic保存的数据总量
log.retention.bytes=1073741824
# 这个参数是：因为kafka的消息是以追加的形式落地到文件，当超过这个值的时候，kafka会新起一个文件
log.segment.bytes=1073741824
# 每隔300000毫秒去检查上面配置的log失效时间
log.retention.check.interval.ms=300000
# 是否启用log压缩，一般不用启用，启用的话可以提高性能
log.cleaner.enable=true
# 设置zookeeper的连接端口,多个地址以逗号(,)隔开，后面可以跟一个kafka在Zookeeper中的根znode节点的路径
zookeeper.connect=localhost:2181
# 设置zookeeper的连接超时时间
zookeeper.connection.timeout.ms=18000
# 在执行第一次再平衡之前，group协调员将等待更多消费者加入group的时间
group.initial.rebalance.delay.ms=0
```



参考：

- [kafka命令](https://www.orchome.com/454)
- [Kafka基础教程](https://www.cnblogs.com/shanfeng1000/p/13035700.html)



