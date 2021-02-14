# Consul集群搭建

**为什么选用consul？**

1. 因为Eureka即将停止维护了

2. Consul要更强大点,带配置中心
   - 每台机都必须运行一个consul agent.
   - consul agent有两种模式,1,server模式 2,client模式.
   - consul server,每个数据中心会有一个leader有server选举出来,故障后会重新选举.
   - consul server,保持数据一致性,维护集群状态,转发查询,转发请求给leader或者远程数据中心,交换wan gossip.
   - consul client用于注册服务,运行健康检查和转发对server的查询.
   - node名字必须在集群中唯一

## Docker

1、拉取consul镜像

```sh
docker pull consul
```

2、网络配置

>我们准备搭建 3 个 server 节点和 1 个 client 节点，因此我们先需要配置 4 个 docker 容器节点的网络。
>
>因为 docker 默认的 docker0 虚拟网卡是不支持直接设置静态ip的。所以我们先创建一个自己的虚拟网络。

```sh
docker network create --driver bridge leadchina1
# 执行完可以通过下面命令查看
docker network --help
# 私有网络ip选取小知识：
# 这三个地址段分别位于A、B、C三类地址内：
# A类地址：10.0.0.0--10.255.255.255
# B类地址：172.16.0.0--172.31.255.255
# C类地址：192.168.0.0--192.168.255.255
```

3、启动 server 节点

我们将要创建的3个 server 节点命名为 s1, s2, s3。

```sh
mkdir -p /opt/config/consul/consul_server_config
```

下面先创建好配置文件，在 `/opt/config/consul/consul_server_config` 目录添加配置文件 basic_config1.json，其中的内容如下：

```sh
vim /opt/config/consul/consul_server_config/basic_config1.json
```

```json
{
  "datacenter": "wuxi", // 指定数据中心名称，默认是dc1
  "log_level": "INFO",
  "node_name": "s1", // 指定节点名，同集群内不能重复
  "server": true, // 以server模式运行，不添加默认以client模式运行
  "bootstrap_expect": 3, // 组成集群需要启动的server数量
  "bind_addr": "0.0.0.0",
  "client_addr": "0.0.0.0", // 可以访问的ip
  "ui": true, // 是否启用面板管理
  "ports": {
    "dns": 8600,
    "http": 8500,
    "https": -1,
    "grpc": 8400,
    "server": 8300,
    "serf_lan": 8301,
    "serf_wan": 8302
  },
  "rejoin_after_leave": true,
  "retry_join": [ // 在意外断开连接后，会不会自动重新加入集群
    "172.19.0.2",
    "172.19.0.3",
    "172.19.0.4"
  ],
  "retry_interval": "30s",
  "reconnect_timeout": "72h"
}
```

同目录下创建 basic_config2.json 和 basic_config3.json 文件作为 s2、s3的配置文件。

Consul Server 是一个有状态的容器，它有两个目录可以挂载本地的目录进去，方便改动配置和数据持久化：

- `/consul/config`：配置目录，如果 agent 可以把 json 配置放这里，会自动加载
- `/consul/data`：consul 数据目录，存放节点、KV、datacenter等数据

>为了 consul server 能稳定提供服务，一般都建议有 3-5 个 consul server 组成集群。如果有很多台机器，在启动足够的 consul server 后，其它主机可以都作为 client 运行。

启动Consul：

```sh
# s1节点
docker run -d -p 8500:8500 -p 8300-8302:8300-8302 -p 8600:8600 \
-v /data/consul_data/1/data:/consul/data \
-v /data/consul_data/1/conf:/consul/config \
-v /opt/config/consul/consul_server_config/basic_config1.json:/consul/config/basic_config.json \
-h node1 --name consul_s1 --network leadchina1 \
consul agent -server -bootstrap-expect=3 -node=node1 \
-rejoin -client 0.0.0.0 -ui \
-data-dir /consul/data \
-config-dir /consul/config

# s2节点
docker run -d -p 8510:8500 -p 8310-8312:8300-8302 -p 8610:8600 \
-v /data/consul_data/2/data:/consul/data \
-v /data/consul_data/2/conf:/consul/config \
-v /opt/config/consul/consul_server_config/basic_config2.json:/consul/config/basic_config.json \
-h node2 --name consul_s2 --network leadchina1 \
consul agent -server -bootstrap-expect=3 -node=node2 \
-rejoin -client 0.0.0.0 -ui \
-data-dir /consul/data \
-config-dir /consul/config

# s3节点
docker run -d -p 8520:8500 -p 8320-8322:8300-8302 -p 8620:8600 \
-v /data/consul_data/3/data:/consul/data \
-v /data/consul_data/3/conf:/consul/config \
-v /opt/config/consul/consul_server_config/basic_config3.json:/consul/config/basic_config.json \
-h node3 --name consul_s3 --network leadchina1 \
consul agent -server -bootstrap-expect=3 -node=node3 \
-rejoin -client 0.0.0.0 -ui \
-data-dir /consul/data \
-config-dir /consul/config
```

都启动完成后，可以通过如下命令观察consul日志，了解启动情况：

```sh
docker logs 容器id/容器名称
```

查看docker image的构建过程：

```sh
docker history 镜像id/镜像名称 --format "table {{.ID}}\t{{.CreatedBy}}" --no-trunc
```

**参考：**

- https://www.cnblogs.com/duanxz/p/10564502.html



## Linux

1、更改主机名

```sh
#!/bin/bash
echo "192.168.10.101 server1" >> /etc/hosts
echo "192.168.10.102 server2" >> /etc/hosts
echo "192.168.10.103 server3" >> /etc/hosts
ip=$(ifconfig ens160 | grep netmask | awk '{print $2}')
cat /etc/hosts | grep $ip | awk '{print $2}' > /etc/hostname
reboot
```

2、在每台服务器上创建目录把需要的安装依赖包上传

```sh
# 解压consul包
tar -zxf consul_install.tgz -C /
# 创建软链接
ln /opt/consul/bin/consul /bin/
# Consul命令生成一个唯一的mak
consul keygen
# 修改server的配置文件
vim /opt/consul/etc/consul/config-server.json
# ---------------------------------------------------------------
{
  "server": true,
  "datacenter": "wuxi",
  "data_dir": "var/consul",
  "node_name": "server1",
  "encrypt": "xxx",  # consul keygen生成
  "ui": true,
  "bootstrap": true,
  "disable_update_check": true,
  "start_join": ["192.168.10.101"]
}
# ---------------------------------------------------------------
# 修改服务配置文件
vim /opt/consul/etc/systemd/consul-server.service
# ---------------------------------------------------------------
[Unit]
Description=Consul
After=network.target

[Service]
EnviromentFile=/opt/consul/etc/systemd/consul.conf
ExecStartPre=/opt/consul/script/consul_bind_ip_generate.sh
ExecStart=/opt/consul/bin/consul agent -config-dir /opt/consul/etc/ -config-file /opt/consul/etc/consul/config-server.json -b {_BIND_ADDRESS} -client 0.0.0.0

[Install]
WantedBy=multi-user.target
# ---------------------------------------------------------------
cd /opt/consul/
./install.sh
# 进入目录 安装consul
systemctl restart consul-server
systemctl status consul-server
# 启动成功
consul members
```



