# Kafka

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
```



参考：

- [kafka命令](https://www.orchome.com/454)



