# Zookeeper集群搭建

1、本地创建目录：

```sh
mkdir /usr/local/zookeeper-cluster
mkdir /usr/local/zookeeper-cluster/node1
mkdir /usr/local/zookeeper-cluster/node2
mkdir /usr/local/zookeeper-cluster/node3
# 查看目录信息
ll /usr/local/zookeeper-cluster/
```

2、创建集群

```sh
docker pull zookeeper

docker run -d -p 2181:2181 --name zookeeper_node1 --privileged --restart always --network colin_default --ip 172.18.0.4 \
-v /usr/local/zookeeper-cluster/node1/volumes/data:/data \
-v /usr/local/zookeeper-cluster/node1/volumes/datalog:/datalog \
-v /usr/local/zookeeper-cluster/node1/volumes/logs:/logs \
-e ZOO_MY_ID=1 \
-e "ZOO_SERVERS=server.1=172.18.0.4:2888:3888;2181 server.2=172.18.0.5:2888:3888;2181 server.3=172.18.0.6:2888:3888;2181" zookeeper

docker run -d -p 2182:2181 --name zookeeper_node2 --privileged --restart always --network colin_default --ip 172.18.0.5 \
-v /usr/local/zookeeper-cluster/node2/volumes/data:/data \
-v /usr/local/zookeeper-cluster/node2/volumes/datalog:/datalog \
-v /usr/local/zookeeper-cluster/node2/volumes/logs:/logs \
-e ZOO_MY_ID=2 \
-e "ZOO_SERVERS=server.1=172.18.0.4:2888:3888;2181 server.2=172.18.0.5:2888:3888;2181 server.3=172.18.0.6:2888:3888;2181" zookeeper

docker run -d -p 2183:2181 --name zookeeper_node3 --privileged --restart always --network colin_default --ip 172.18.0.6 \
-v /usr/local/zookeeper-cluster/node3/volumes/data:/data \
-v /usr/local/zookeeper-cluster/node3/volumes/datalog:/datalog \
-v /usr/local/zookeeper-cluster/node3/volumes/logs:/logs \
-e ZOO_MY_ID=3 \
-e "ZOO_SERVERS=server.1=172.18.0.4:2888:3888;2181 server.2=172.18.0.5:2888:3888;2181 server.3=172.18.0.6:2888:3888;2181" zookeeper
```

>`--privileged=true` 参数是为了解决【chown: changing ownership of '/data': Permission denied】，也可以省略 true
>
>`docker logs -f zookeeper_node1` 查看日志  
>`docker inspect 4bfa6bbeb936` 查看配置  

3、进入容器内部验证

```sh
# 进入容器
docker exec -it zookeeper_node1 bash
# ----------------进入容器--------------------
./bin/zkServer.sh status
exit
```

4、开启防火墙，供外部访问

```sh
firewall-cmd --zone=public --add-port=2181/tcp --permanent
firewall-cmd --zone=public --add-port=2182/tcp --permanent
firewall-cmd --zone=public --add-port=2183/tcp --permanent
systemctl restart firewalld
firewall-cmd --list-all
```

5、在本地，用zookeeper的客户端连接虚拟机上的集群

- 下载：[https://www.apache.org/dyn/closer.cgi/zookeeper/](https://www.apache.org/dyn/closer.cgi/zookeeper/)
- 解压，修改zoo_sample.cfg 文件名为 zoo.cfg
- 修改配置：

```sh
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just
# example sakes.
# 数据目录
dataDir=E:\\Arms\\apache-zookeeper-3.5.8-bin\\data
# 日志目录
dataLogDir=E:\\Arms\\apache-zookeeper-3.5.8-bin\\log
# the port at which the clients will connect
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
```

配置文件简单解析：

1. tickTime：这个时间是作为 zookeeper 服务器之间或客户端与服务器之间维持心跳的时间间隔，也就是每个 tickTime 时间就会发送一个心跳。

2. dataDir：顾名思义就是 zookeeper 保存数据的目录，默认情况下，Zookeeper 将写数据的日志文件也保存在这个目录里。

3. dataLogDir：顾名思义就是 zookeeper 保存日志文件的目录

4. clientPort：这个端口就是客户端连接 zookeeper 服务器的端口，Zookeeper 会监听这个端口，接受客户端的访问请求。

本地客户端连接集群：

```sh
zkCli.cmd -server 121.196.182.26:2181,121.196.182.26:2182,121.196.182.26:2183
```

**通过 `docker stack deploy` 或 `docker-compose` 安装**

docker-compose.yml

```yml
version: '3.1'

services:
  zoo1:
    image: zookeeper
    restart: always
    privileged: true
    hostname: zoo1
    ports:
      - 2181:2181
    volumes: # 挂载数据
      - /usr/local/zookeeper-cluster/node4/data:/data
      - /usr/local/zookeeper-cluster/node4/datalog:/datalog
    environment:
      ZOO_MY_ID: 4
      ZOO_SERVERS: server.4=0.0.0.0:2888:3888;2181 server.5=zoo2:2888:3888;2181 server.6=zoo3:2888:3888;2181
    networks:
      default:
        ipv4_address: 172.18.0.14

  zoo2:
    image: zookeeper
    restart: always
    privileged: true
    hostname: zoo2
    ports:
      - 2182:2181
    volumes: # 挂载数据
      - /usr/local/zookeeper-cluster/node5/data:/data
      - /usr/local/zookeeper-cluster/node5/datalog:/datalog
    environment:
      ZOO_MY_ID: 5
      ZOO_SERVERS: server.4=zoo1:2888:3888;2181 server.5=0.0.0.0:2888:3888;2181 server.6=zoo3:2888:3888;2181
    networks:
      default:
        ipv4_address: 172.18.0.15

  zoo3:
    image: zookeeper
    restart: always
    privileged: true
    hostname: zoo3
    ports:
      - 2183:2181
    volumes: # 挂载数据
      - /usr/local/zookeeper-cluster/node6/data:/data
      - /usr/local/zookeeper-cluster/node6/datalog:/datalog
    environment:
      ZOO_MY_ID: 6
      ZOO_SERVERS: server.4=zoo1:2888:3888;2181 server.5=zoo2:2888:3888;2181 server.6=0.0.0.0:2888:3888;2181
    networks:
      default:
        ipv4_address: 172.18.0.16

networks: # 自定义网络
  default:
    external:
      name: colin_default
```

