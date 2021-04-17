# RocketMQ部署

目标：

| 节点        | 角色               |
| ----------- | ------------------ |
| rmqnamesrv1 | NameServer         |
| rmqnamesrv2 | NameServer         |
| rmqnamesrv3 | NameServer         |
| rmqbroker1  | Dledger Group 1 主 |
| rmqbroker2  | Dledger Group 1 从 |
| rmqbroker3  | Dledger Group 1 从 |
| rmqbroker4  | Dledger Group 2 主 |
| rmqbroker5  | Dledger Group 2 从 |
| rmqbroker6  | Dledger Group 2 从 |

准备工作：

```sh
mkdir -p /data/rocket/namesrv/1/logs /data/rocket/namesrv/1/store \
         /data/rocket/namesrv/2/logs /data/rocket/namesrv/2/store \
         /data/rocket/namesrv/3/logs /data/rocket/namesrv/3/store \
         /data/rocket/broker/1/logs /data/rocket/broker/1/store \
         /data/rocket/broker/2/logs /data/rocket/broker/2/store \
         /data/rocket/broker/3/logs /data/rocket/broker/3/store
```

docker-compose.yml

```yaml
version: '3.7'
services:
  rmqnamesrv1:
    restart: always
    image: foxiswho/rocketmq:server
    container_name: rmqnamesrv1
    ports:
      - 9876:9876
    volumes:
      - /data/rocket/namesrv/1/logs:/opt/logs
      - /data/rocket/namesrv/1/store:/opt/store
    networks:
      default:
        ipv4_address: 172.18.1.30
  rmqnamesrv2:
    restart: always
    image: foxiswho/rocketmq:server
    container_name: rmqnamesrv2
    ports:
      - 9877:9876
    volumes:
      - /data/rocket/namesrv/2/logs:/opt/logs
      - /data/rocket/namesrv/2/store:/opt/store
    networks:
      default:
        ipv4_address: 172.18.1.31
  rmqnamesrv3:
    restart: always
    image: foxiswho/rocketmq:server
    container_name: rmqnamesrv3
    ports:
      - 9878:9876
    volumes:
      - /data/rocket/namesrv/3/logs:/opt/logs
      - /data/rocket/namesrv/3/store:/opt/store
    networks:
      default:
        ipv4_address: 172.18.1.32
  
  rmqbroker1:
    restart: always
    image: foxiswho/rocketmq:broker
    container_name: rmqbroker1
    ports:
      - 10909:10909
      - 10910:10911
    volumes:
      - /data/rocket/broker/1/logs:/opt/logs
      - /data/rocket/broker/1/store:/opt/store
      - /data/rocket/broker/1/broker.conf:/etc/rocketmq/broker.conf
    environment:
        NAMESRV_ADDR: "rmqnamesrv1:9876"
        JAVA_OPTS: " -Duser.home=/opt"
        JAVA_OPT_EXT: "-server -Xms128m -Xmx1024m -Xmn128m"
    command: mqbroker -c /etc/rocketmq/broker.conf
    depends_on:
      - rmqnamesrv1
      - rmqnamesrv2
      - rmqnamesrv3
    networks:
      default:
        ipv4_address: 172.18.1.33
  rmqbroker2:
    restart: always
    image: foxiswho/rocketmq:broker
    container_name: rmqbroker2
    ports:
      - 10911:10909
      - 10912:10911
    volumes:
      - /data/rocket/broker/2/logs:/opt/logs
      - /data/rocket/broker/2/store:/opt/store
      - /data/rocket/broker/2/broker.conf:/etc/rocketmq/broker.conf
    environment:
        NAMESRV_ADDR: "rmqnamesrv1:9876"
        JAVA_OPTS: " -Duser.home=/opt"
        JAVA_OPT_EXT: "-server -Xms128m -Xmx1024m -Xmn128m"
    command: mqbroker -c /etc/rocketmq/broker.conf
    depends_on:
      - rmqnamesrv1
      - rmqnamesrv2
      - rmqnamesrv3
    networks:
      default:
        ipv4_address: 172.18.1.34
  rmqbroker3:
    restart: always
    image: foxiswho/rocketmq:broker
    container_name: rmqbroker3
    ports:
      - 10913:10909
      - 10914:10911
    volumes:
      - /data/rocket/broker/3/logs:/opt/logs
      - /data/rocket/broker/3/store:/opt/store
      - /data/rocket/broker/3/broker.conf:/etc/rocketmq/broker.conf
    environment:
        NAMESRV_ADDR: "rmqnamesrv1:9876"
        JAVA_OPTS: " -Duser.home=/opt"
        JAVA_OPT_EXT: "-server -Xms128m -Xmx1024m -Xmn128m"
    command: mqbroker -c /etc/rocketmq/broker.conf
    depends_on:
      - rmqnamesrv1
      - rmqnamesrv2
      - rmqnamesrv3
    networks:
      default:
        ipv4_address: 172.18.1.35
  
  rmqconsole:
    restart: always
    image: styletang/rocketmq-console-ng
    container_name: rmqconsole
    ports:
      - 8080:8080
    environment:
        JAVA_OPTS: "-Drocketmq.namesrv.addr=rmqnamesrv1:9876;rmqnamesrv2:9876;rmqnamesrv3:9876 -Dcom.rocketmq.sendMessageWithVIPChannel=false"
    depends_on:
      - rmqnamesrv1
      - rmqnamesrv2
      - rmqnamesrv3
    networks:
      default:
        ipv4_address: 172.18.1.36

networks:
  default:
    external:
      name: lead_pm1
```

rmqbroker1 的 broker.conf

```ini
#集群名称
brokerClusterName=WuxiCluster
#broker名称
brokerName=broker1
#brokerId master用0 slave用其他
brokerId=0
#清理时机
deleteWhen=4
#文件保留时长 48小时
fileReservedTime=48
#broker角色 -ASYNC_MASTER异步复制 -SYNC_MASTER同步双写 -SLAVllE
brokerRole=SYNC_MASTER
#刷盘策略 - ASYNC_FLUSH 异步刷盘 - SYNC_FLUSH 同步刷盘
flushDiskType=SYNC_FLUSH
#主机ip
brokerIP1=172.18.1.33
#对外服务的监听接口，同一台机器上部署多个broker，端口号要不相同
listenPort=10911
#namesvr
namesrvAddr=172.18.1.30:9876;172.18.1.31:9876;172.18.1.32:9876
#是否能够自动创建topic
autoCreateTopicEnable=true
```

rmqbroker2 的 broker.conf

```ini
#集群名称
brokerClusterName=WuxiCluster
#broker名称
brokerName=broker1
#brokerId master用0 slave用其他
brokerId=1
#清理时机
deleteWhen=4
#文件保留时长 48小时
fileReservedTime=48
#broker角色 -ASYNC_MASTER异步复制 -SYNC_MASTER同步双写 -SLAVE
brokerRole=SLAVE
#刷盘策略 - ASYNC_FLUSH 异步刷盘 - SYNC_FLUSH 同步刷盘
flushDiskType=SYNC_FLUSH
#主机ip
brokerIP1=172.18.1.34
#对外服务的监听接口，同一台机器上部署多个broker,端口号要不相同
listenPort=10911
#namesrv
namesrvAddr=172.18.1.30:9876;172.18.1.31:9876;172.18.1.32:9876
#是否能够自动创建topic
autoCreateTopicEnable=true
```

rmqbroker3 的 broker.conf

```ini
#集群名称
brokerClusterName=WuxiCluster
#broker名称
brokerName=broker1
#brokerId master用0 slave用其他
brokerId=2
#清理时机
deleteWhen=4
#文件保留时长 48小时
fileReservedTime=48
#broker角色 -ASYNC_MASTER异步复制 -SYNC_MASTER同步双写 -SLAVE
brokerRole=SLAVE
#刷盘策略 - ASYNC_FLUSH 异步刷盘 - SYNC_FLUSH 同步刷盘
flushDiskType=SYNC_FLUSH
#主机ip
brokerIP1=172.18.1.35
#对外服务的监听接口，同一台机器上部署多个broker,端口号要不相同
listenPort=10911
#namesrv
namesrvAddr=172.18.1.30:9876;172.18.1.31:9876;172.18.1.32:9876
#是否能够自动创建topic
autoCreateTopicEnable=true
```

rmqbroker4 的 broker.conf

```ini
#集群名称
brokerClusterName=WuxiCluster
#broker名称
brokerName=broker2
#brokerId master用0 slave用其他
brokerId=0
#清理时机
deleteWhen=4
#文件保留时长 48小时
fileReservedTime=48
#broker角色 -ASYNC_MASTER异步复制 -SYNC_MASTER同步双写 -SLAVE
brokerRole=SYNC_MASTER
#刷盘策略 - ASYNC_FLUSH 异步刷盘 - SYNC_FLUSH 同步刷盘
flushDiskType=SYNC_FLUSH
#主机ip
brokerIP1=172.18.1.36
#对外服务的监听接口，同一台机器上部署多个broker,端口号要不相同
listenPort=10911
#namesrv
namesrvAddr=172.18.1.30:9876;172.18.1.31:9876;172.18.1.32:9876
#是否能够自动创建topic
autoCreateTopicEnable=true
```

......

启动：

```sh
docker-compose up -d
```

命令：

```sh
docker exec -it rmqnamesrv1 bash
```

控制台：http://10.30.202.101:8080/

参考：

- [官网](http://rocketmq.apache.org/)
- [开发者指南](https://github.com/apache/rocketmq/tree/master/docs/cn)

