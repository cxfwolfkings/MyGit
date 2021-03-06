# Zookeeper



## 安装



### 单机模式

1、配置JAVA环境，检验环境：`java -version`

2、下载并解压zookeeper

```sh
cd /usr/local
wget http://mirror.bit.edu.cn/apache/zookeeper/stable/zookeeper-3.4.12.tar.gz
tar -zxvf zookeeper-3.4.12.tar.gz
cd zookeeper-3.4.12
```

3、重命名配置文件zoo_sample.cfg

```sh
cp conf/zoo_sample.cfg conf/zoo.cfg
```

4、启动zookeeper

```sh
bin/zkServer.sh start
```

5、检测是否成功启动，用zookeeper客户端连接下服务端

```sh
bin/zkCli.sh
```

**Zookeeper使用**

```sh
# 1、查看当前 ZooKeeper 中所包含的内容
ls /
# 2、创建一个新的 znode
create /zkPro myData
# 3、查看现在 zookeeper 中所包含的内容
ls /
# 4、运行 get 命令来确认第二步中所创建的 znode 是否包含我们所创建的字符串：
get /zkPro myData
# 5、通过 set 命令来对 zk 所关联的字符串进行设置：
set /zkPro myData123
# 将刚才创建的 znode 删除
delete /zkPro
```

**使用Java API操作zookeeper**

1、引用包：

```xml
<dependency>
  <groupId>org.apache.zookeeper</groupId>
  <artifactId>zookeeper</artifactId>
  <version>3.4.12</version>
</dependency>
```

2、在zookeeper里增加一个目录节点，并且把配置信息存储在里面

```sh
create /username Colin
```

3、启动两个zookeeper客户端程序，代码如下所示

```java
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 分布式配置中心demo
 * @author Colin
 * @date   2020-06-15
 */
public class ZooKeeperProSync implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    private static Stat stat = new Stat();

    public static void main(String[] args) throws Exception {
        //zookeeper配置数据存放路径
        String path = "/username";
        //连接zookeeper并且注册一个默认的监听器
        zk = new ZooKeeper("192.168.31.100:2181", 5000, //
                new ZooKeeperProSync());
        //等待zk连接成功的通知
        connectedSemaphore.await();
        //获取path目录节点的配置数据，并注册默认的监听器
        System.out.println(new String(zk.getData(path, true, stat)));

        Thread.sleep(Integer.MAX_VALUE);
    }

    public void process(WatchedEvent event) {
        if (KeeperState.SyncConnected == event.getState()) {  //zk连接成功通知事件
            if (EventType.None == event.getType() && null == event.getPath()) {
                connectedSemaphore.countDown();
            } else if (event.getType() == EventType.NodeDataChanged) {  //zk目录节点数据变化通知事件
                try {
                    System.out.println("配置已修改，新值为：" + new String(zk.getData(event.getPath(), true, stat)));
                } catch (Exception e) {
                }
            }
        }
    }
}
```

两个程序启动后都正确的读取到了zookeeper的/username目录节点下的数据'Colin'

4、我们在zookeeper里修改下目录节点/username下的数据

```sh
set /username dragon
```

修改完成后，我们看见两个程序后台都及时收到了他们监听的目录节点数据变更后的值。



### 集群模式

**服务器安装**

本例搭建的是伪集群模式，即一台机器上启动三个zookeeper实例组成集群，真正的集群模式无非就是实例IP地址不同，搭建方法没有区别。

从单机模式第3步开始：

3、重命名 zoo_sample.cfg文件

```sh
cp conf/zoo_sample.cfg conf/zoo-1.cfg
```

4、修改配置文件zoo-1.cfg，原配置文件里有的，修改成下面的值，没有的则加上

```sh
vim conf/zoo-1.cfg
----------------------------
dataDir=/tmp/zookeeper-1
clientPort=2181
server.1=127.0.0.1:2888:3888
server.2=127.0.0.1:2889:3889
server.3=127.0.0.1:2890:3890
```

配置说明：

- tickTime：这个时间是作为 Zookeeper 服务器之间或客户端与服务器之间维持心跳的时间间隔，也就是每个 tickTime 时间就会发送一个心跳。
- initLimit：这个配置项是用来配置 Zookeeper 接受客户端（这里所说的客户端不是用户连接 Zookeeper 服务器的客户端，而是 Zookeeper 服务器集群中连接到 Leader 的 Follower 服务器）初始化连接时最长能忍受多少个心跳时间间隔数。当已经超过 10个心跳的时间（也就是 tickTime）长度后 Zookeeper 服务器还没有收到客户端的返回信息，那么表明这个客户端连接失败。总的时间长度就是 10*2000=20 秒
- syncLimit：这个配置项标识 Leader 与 Follower 之间发送消息，请求和应答时间长度，最长不能超过多少个 tickTime 的时间长度，总的时间长度就是 5*2000=10秒
- dataDir：顾名思义就是 Zookeeper 保存数据的目录，默认情况下，Zookeeper 将写数据的日志文件也保存在这个目录里。
- clientPort：这个端口就是客户端连接 Zookeeper 服务器的端口，Zookeeper 会监听这个端口，接受客户端的访问请求。
- server.A=B：C：D：其中 A 是一个数字，表示这个是第几号服务器；B 是这个服务器的 ip 地址；C 表示的是这个服务器与集群中的 Leader 服务器交换信息的端口；D 表示的是万一集群中的 Leader 服务器挂了，需要一个端口来重新进行选举，选出一个新的 Leader，而这个端口就是用来执行选举时服务器相互通信的端口。如果是伪集群的配置方式，由于 B 都是一样，所以不同的 Zookeeper 实例通信端口号不能一样，所以要给它们分配不同的端口号。

5、再从zoo-1.cfg复制两个配置文件zoo-2.cfg和zoo-3.cfg，只需修改dataDir和clientPort不同即可

```sh
cp conf/zoo-1.cfg conf/zoo-2.cfg
cp conf/zoo-1.cfg conf/zoo-3.cfg
vim conf/zoo-2.cfg
--------------------------------
dataDir=/tmp/zookeeper-2
clientPort=2182
--------------------------------
vim conf/zoo-2.cfg
--------------------------------
dataDir=/tmp/zookeeper-3
clientPort=2183
--------------------------------
```

6、标识Server ID

创建三个文件夹/tmp/zookeeper-1，/tmp/zookeeper-2，/tmp/zookeeper-3，在每个目录中创建 myid 文件，写入当前实例的server id，即1.2.3

```sh
cd /tmp/zookeeper-1
vim myid
-------------------
1
-------------------
cd /tmp/zookeeper-2
vim myid
-------------------
2
-------------------
cd /tmp/zookeeper-3
vim myid
-------------------
3
-------------------
```

7、启动三个zookeeper实例

```sh
bin/zkServer.sh start conf/zoo-1.cfg
bin/zkServer.sh start conf/zoo-2.cfg
bin/zkServer.sh start conf/zoo-3.cfg
```

8 、检测集群状态，也可以直接用命令 `zkCli.sh -server IP:PORT` 连接zookeeper服务端检测

```sh
bin/zkServer.sh status conf/zoo-1.cfg
bin/zkServer.sh status conf/zoo-2.cfg
bin/zkServer.sh status conf/zoo-3.cfg
```

至此，我们对zookeeper就算有了一个入门的了解，当然zookeeper远比我们这里描述的功能多，比如用zookeeper实现集群管理，分布式锁，分布式队列，zookeeper集群leader选举等等

推荐阅读：[https://www.roncoo.com/course/view/255bac222b1b4300b42838b58fea3a2e](https://www.roncoo.com/course/view/255bac222b1b4300b42838b58fea3a2e)

文章来源：[https://my.oschina.net/u/3796575/blog/1845035](https://my.oschina.net/u/3796575/blog/1845035)

https://zhuanlan.zhihu.com/p/67654401?from_voters_page=true

**docker安装**

1、准备工作

```sh
# 本地创建目录
mkdir -p /data/zookeeper/1/data /data/zookeeper/1/datalog /data/zookeeper/1/logs \
         /data/zookeeper/2/data /data/zookeeper/2/datalog /data/zookeeper/2/logs \
         /data/zookeeper/3/data /data/zookeeper/3/datalog /data/zookeeper/3/logs
# 查看目录信息
ll /data/zookeeper
```

2、创建集群

```sh
docker pull zookeeper

docker run -d -p 2181:2181 --name zookeeper_node1 --privileged --restart always --network colin_default --ip 172.18.0.4 \
-v /data/zookeeper/1/data:/data \
-v /data/zookeeper/1/datalog:/datalog \
-v /data/zookeeper/1/logs:/logs \
-e ZOO_MY_ID=1 \
-e "ZOO_SERVERS=server.1=172.18.1.5:2888:3888;2181 server.2=172.18.1.6:2888:3888;2181 server.3=172.18.1.7:2888:3888;2181" \
zookeeper

docker run -d -p 2182:2181 --name zookeeper_node2 --privileged --restart always --network colin_default --ip 172.18.0.5 \
-v /data/zookeeper/2/data:/data \
-v /data/zookeeper/2/datalog:/datalog \
-v /data/zookeeper/2/logs:/logs \
-e ZOO_MY_ID=2 \
-e "ZOO_SERVERS=server.1=172.18.1.5:2888:3888;2181 server.2=172.18.1.6:2888:3888;2181 server.3=172.18.1.7:2888:3888;2181" \
zookeeper

docker run -d -p 2183:2181 --name zookeeper_node3 --privileged --restart always --network colin_default --ip 172.18.0.6 \
-v /data/zookeeper/3/data:/data \
-v /data/zookeeper/3/datalog:/datalog \
-v /data/zookeeper/3/logs:/logs \
-e ZOO_MY_ID=3 \
-e "ZOO_SERVERS=server.1=172.18.1.5:2888:3888;2181 server.2=172.18.1.6:2888:3888;2181 server.3=172.18.1.7:2888:3888;2181" \
zookeeper
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

**compose安装**

docker-compose.yml

```yml
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
        ipv4_address: 172.18.1.5

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
        ipv4_address: 172.18.1.6

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
        ipv4_address: 172.18.1.7

networks:
  default:
    external:
      name: lead_pm1
```

执行

```sh
# 进入docker-compose.yml所在目录执行
docker-compose up -d
```

检查部署情况

```sh
docker exec -it zoo1 /bin/bash

zkServer.sh status
/*
ZooKeeper JMX enabled by default
Using config: /conf/zoo.cfg
Mode: follower
*/
```

zkui可视化管理zookeeper

第一种方式：源码编译

```sh
# 拉取项目 
git clone https://github.com/DeemOpen/zkui.git
# 编译
cd zkui
mvn clean install
# 修改配置文件
vim config.cfg
zkServer=10.30.202.101:2181,10.30.202.101:2182,10.30.202.101:2183
# 启动zkui
nohup java -jar target/zkui-2.0-SNAPSHOT-jar-with-dependencies.jar &
# 登录
# 账号密码 admin/manager
```

第二种方式：docker镜像

```sh
docker run -d --restart=always --name zkui -p 9090:9090 \
           -e ZKUI_ZK_SERVER=<external_DNS/IP>:2181 qnib/zkui
```

**问题：**

1、stat is not executed because it is not in the whitelist

方法1：在 zoo.cfg 文件里加入配置项让指令放行

```sh
# 开启四字命令
4lw.commands.whitelist=*
```

方法2：在zk的启动脚本zkServer.sh中新增放行指令

```sh
    ZOOMAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMXPORT -Dcom.sun.management.jmxremote.authenticate=$JMXAUTH -Dcom.sun.management.jmxremote.ssl=$JMXSSL -Dzookeeper.jmx.log4j.disable=$JMXLOG4J org.apache.zookeeper.server.quorum.QuorumPeerMain"
  fi
else
    echo "JMX disabled by user request" >&2
    ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
fi
 
# 添加VM环境变量-Dzookeeper.4lw.commands.whitelist=*
ZOOMAIN="-Dzookeeper.4lw.commands.whitelist=* ${ZOOMAIN}"
 
if [ "x$SERVER_JVMFLAGS" != "x" ]
then
    JVMFLAGS="$SERVER_JVMFLAGS $JVMFLAGS"
fi
```

配置完毕后，重启集群：`./zkServer.sh restart`