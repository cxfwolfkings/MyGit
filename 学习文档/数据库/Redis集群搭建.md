# Redis集群安装

为了高可用

## 单机安装

官方站点：redis.io 下载最新版或者最新stable版

**windows:**

```sh
# 启动临时服务（redis.windows.conf是默认配置，可以省略。输入之后，会显示图标界面）：
redis-server.exe redis.windows.conf

# 客户端调用（启一个 cmd 窗口，原来的不要关闭）：
# 参数：
#   -a: 输入密码
#   --raw: 避免中文乱码
redis-cli.exe -h 127.0.0.1 -p 6379

# 安装服务：
redis-server.exe --service-install redis.windows.conf --service-name redisserver1 --loglevel verbose --port 6379
# 启动服务：
redis-server.exe  --service-start --service-name redisserver1
# 停止服务：
redis-server.exe  --service-stop --service-name redisserver1
# 卸载服务：
redis-server.exe  --service-uninstall --service-name redisserver1
```

**Linux:**

1、下载[安装包](https://redis.io/download)

redis是C语言开发，安装redis需要先将官网下载的源码进行编译，编译依赖gcc环境。如果没有gcc环境，需要安装gcc：`yum install gcc-c++`

2、编译安装

```sh
yum -y install gcc tcl
wget http://download.redis.io/releases/redis-6.0.4.tar.gz
# wget找不到时：yum install -y wget
tar xzf redis-6.0.4.tar.gz
cd redis-6.0.4
# 编译失败！未解决
make PREFIX=/usr/local/redis/6.0.4 install
# 启动
src/redis-server
# 使用另外的窗口
src/redis-cli
redis> set foo bar
OK
redis> get foo
"bar"
# 删除
make clean
rm -rf redis-6.0.4
```

**其它配置：**

1、以后台进程的形式运行：

编辑conf配置文件，修改如下内容：`daemonize yes`

2、开启远程访问：

修改redis.conf，注释掉 `bind 127.0.0.1` 可以使所有的 ip 访问 redis；若是想指定多个 ip 访问，但并不是全部的 ip 访问，可以 bind。

在 redis3.2 之后，redis 增加了 protected-mode，在这个模式下，即使注释掉了 `bind 127.0.0.1`，再访问 redis 的时候还是报错，修改办法：`protected-mode no`

3、设置密码：

把 `#requirepass foobared` 的 # 号去掉，并把 foobared 改为自己的密码即可

**客户端图形工具：**

RedisDesktopManager



## 集群安装

1、创建新网桥

```sh
# 创建 'redis_net' docker 网络
docker network create --driver bridge redis_net
```

2、创建集群

```sh
# 拉取镜像
docker pull redis

# 默认3主3备

# docker run --network redis_net -it -p 7000:7000 -p 7001:7001 -p 7002:7002 -p 7003:7003 -p 7004:7004 -p 7005:7005 -p 7006:7006 -p 7007:7007 grokzen/redis-cluster

# 创建六个redis
for port in $(seq 1 6);
do
mkdir -p /mydata/redis/node-${port}/conf
touch /mydata/redis/node-${port}/conf/redis.conf
cat << EOF >/mydata/redis/node-${port}/conf/redis.conf
port 6379
bind 0.0.0.0
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
cluster-announce-ip 172.19.0.${port}
cluster-announce-port 6379
cluster-announce-bus-port 16379
appendonly yes
EOF
done

vim /mydata/redis/node-1/conf/redis.conf
vim /mydata/redis/node-2/conf/redis.conf
vim /mydata/redis/node-3/conf/redis.conf
vim /mydata/redis/node-4/conf/redis.conf
vim /mydata/redis/node-5/conf/redis.conf
vim /mydata/redis/node-6/conf/redis.conf


```



**参考：**

- https://www.cnblogs.com/yslss/p/12985791.html
- https://www.cnblogs.com/hutao722/