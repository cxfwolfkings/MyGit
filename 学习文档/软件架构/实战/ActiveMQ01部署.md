# ActiveMQ部署

准备工作：

| 主机 | IP          | 角色    | 职责                   |
| ---- | ----------- | ------- | ---------------------- |
| A    | 172.18.1.41 | cluster | 消费者                 |
| B    | 172.18.1.42 | master  | 生产者、消费者         |
| C    | 172.18.1.43 | slave   | 生产者（预备）、消费者 |

1. 首先启动activemq：`docker run -it -d registry.cn-hangzhou.aliyuncs.com/daydayup/activemq`

2. 得到容器id，进入容器：`docker -exec -it 容器id /bin/bash`

3. 将容器里activemq的配置文件拿出来：`docker cp xxx:/opt/apache-activemq-5.13.3/conf/activemq.xml /myConfig/activemq/activemq-cluster-a.xml`

4. 将配置文件增加两份，分别为active-master-b.xml和active-slave-c.xml

5. 修改配置

   active-cluster-a.xml

   ```xml
   <transportConnectors>
     <!-- DOS protection, limit concurrent connections to 1000 and frame size to 100MB -->
     <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
     <!-- <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="amqp" uri="amqp://0.0.0.0:5672?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="mqtt" uri="mqtt://0.0.0.0:1883?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/> -->
   </transportConnectors>
   <!-- 配置网络代理，cluster 节点需要与 master 跟 slave 进行穿透 -->
   <networkConnectors>
     <networkConnector uri="static:(tcp://192.168.1.3:61617,tcp://192.168.1.4:61618)" duplex="true" />
   </networkConnectors>
   ```

   active-master-b.xml

   ```xml
   <transportConnectors>
     <!-- DOS protection, limit concurrent connections to 1000 and frame size to 100MB -->
     <transportConnector name="openwire" uri="tcp://0.0.0.0:61617?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
     <!-- <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="amqp" uri="amqp://0.0.0.0:5672?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="mqtt" uri="mqtt://0.0.0.0:1883?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/> -->
   </transportConnectors>
   <!-- 配置网络代理，cluster 节点需要与 master 跟 slave 进行穿透 -->
   <networkConnectors>
     <networkConnector uri="static:(tcp://192.168.1.2:61616)" duplex="true" />
   </networkConnectors>
   ```

   active-slave-c.xml

   ```xml
   <transportConnectors>
     <!-- DOS protection, limit concurrent connections to 1000 and frame size to 100MB -->
     <transportConnector name="openwire" uri="tcp://0.0.0.0:61618?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
     <!-- <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="amqp" uri="amqp://0.0.0.0:5672?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="mqtt" uri="mqtt://0.0.0.0:1883?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
          <transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/> -->
   </transportConnectors>
   <!-- 配置网络代理，cluster 节点需要与 master 跟 slave 进行穿透 -->
   <networkConnectors>
     <networkConnector uri="static:(tcp://192.168.1.2:61616)" duplex="true" />
   </networkConnectors>
   ```

6. 创建activemq的共享目录 `/usr/share/activemq/kahadb`

7. 创建docker网络进行ip规划

   ```sh
   docker network create --subnet=192.168.1.0/16 mynet123
   ```

8. 启动三个容器

   ```sh
   docker run -it -d -p 61617:61616 -p 8171:8161 --hostname=master --network="mynet123" --ip="192.168.1.2" -v /myConfig/activemq/activemq-master-b.xml:/opt/apache-activemq-5.13.3/conf/activemq.xml -v /usr/share/activemq/kahadb:/opt/apache-activemq-5.13.3/data/kahadb registry.cn-hangzhou.aliyuncs.com/daydayup/activemq
   docker run -it -d -p 61616:61616 -p 8161:8161 --hostname=cluster --network="mynet123" --ip="192.168.1.3" -v /myConfig/activemq/activemq-clusters-a.xml:/opt/apache-activemq-5.13.3/conf/activemq.xml registry.cn-hangzhou.aliyuncs.com/daydayup/activemq
   docker run -it -d -p 61618:61616 -p 8181:8161 --hostname=slave --network="mynet123" --ip="192.168.1.4" -v /myConfig/activemq/activemq-slave-c.xml:/opt/apache-activemq-5.13.3/conf/activemq.xml -v /usr/share/activemq/kahadb:/opt/apache-activemq-5.13.3/data/kahadb registry.cn-hangzhou.aliyuncs.com/daydayup/activemq 
   ```

9. 然后就可以通过`nestat -an |grep 61616`这样的命令来检查，我这里搭建的控制台的地址提示是0.0.0.0:8161, 0.0.0.0:8171这样进行访问
10. 使用idea编写java客户端进行生产和消费。

**notes:**

1. 在设置activemq的静态网络时候两个之间不能有空格，否则会报错。(tcp://192.168.1.3:61617,tcp://192.168.1.4:61618)
2. 在出现容器起来马上就退出了是因为activemq没有启动成功。
3. 查错可以使用 `docker logs -f 容器id` 进行查看日志。查看所有容器的命令是 `docker ps -a`
4. [参见的博文](http://blog.csdn.net/yang857160548/article/details/75577311)

