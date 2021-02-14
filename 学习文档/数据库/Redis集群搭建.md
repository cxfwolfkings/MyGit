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