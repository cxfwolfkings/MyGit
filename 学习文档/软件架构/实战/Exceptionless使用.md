# Exceptionless

1. 部署



## 部署

单容器：

```sh
# 测试环境
docker run --rm -it -d -p 5000:80 \
    -v /data/esdata:/usr/share/elasticsearch/data \
    --name exceptionless \
    --net lead_pm1 \
    --ip 172.18.1.90 \
    exceptionless/exceptionless:latest
```

多容器

```sh
# 生产环境
unzip Exceptionless-7.0.9.zip
cd Exceptionless-7.0.9

# 准备工作
# 创建新网段
docker network create --driver bridge --ip-range=172.19.1.0/24 \
                      --subnet 172.19.1.0/24 \
                      --gateway 172.19.1.1 \
                      exceptionless
# 创建映射目录
mkdir -p /data/exceptionless/appdata \
         /data/exceptionless/ssldata \
         /data/exceptionless/esdata7

# 启动
docker-compose up -d

# 内网部署时前端管理界面无法加载在线资源
# 先将容器内文件拷贝出来
docker cp exceptionless-709_ui_1:/app /data/exceptionless
# 目录改名
mv /data/exceptionless/app /data/exceptionless/ui
# 修改 index.html，将在线资源修改为本地资源（先从外网下载，再导入内网）
# 设置执行权限
chmod 755 index.html
```

docker-compose.yml

```yaml
version: '3.7'

services:
  app:
    depends_on:
      - elasticsearch
      - redis
    image: exceptionless/app:latest
    environment:
      EX_AppMode: Production
      EX_ConnectionStrings__Cache: provider=redis
      EX_ConnectionStrings__Elasticsearch: server=http://172.19.1.4:9200
      EX_ConnectionStrings__MessageBus: provider=redis
      EX_ConnectionStrings__Queue: provider=redis
      EX_ConnectionStrings__Redis: server=172.19.1.6:6379,abortConnect=false
      EX_ConnectionStrings__Storage: provider=folder;path=/app/storage
      EX_RunJobsInProcess: 'false'
    ports:
      - 5000:80
      - 5001:443
    volumes:
      - /data/exceptionless/appdata:/app/storage
      - /data/exceptionless/ssldata:/https
    networks:
      default:
        ipv4_address: 172.19.1.2

  jobs:
    depends_on:
      - app
    image: exceptionless/job:latest
    environment:
      EX_AppMode: Production
      EX_BaseURL: http://localhost:5100
      EX_ConnectionStrings__Cache: provider=redis
      EX_ConnectionStrings__Elasticsearch: server=http://172.19.1.4:9200
      EX_ConnectionStrings__MessageBus: provider=redis
      EX_ConnectionStrings__Queue: provider=redis
      EX_ConnectionStrings__Redis: server=172.19.1.6:6379,abortConnect=false
      EX_ConnectionStrings__Storage: provider=folder;path=/app/storage
    volumes:
      - /data/exceptionless/appdata:/app/storage
    networks:
      default:
        ipv4_address: 172.19.1.3

  elasticsearch:
    image: exceptionless/elasticsearch:7.10.0
    environment:
      discovery.type: single-node
      xpack.security.enabled: 'false'
      ES_JAVA_OPTS: -Xms1g -Xmx1g
    ports:
      - 9200:9200
      - 9300:9300
    networks:
      default:
        ipv4_address: 172.19.1.4

  kibana:
    depends_on:
      - elasticsearch
    image: docker.elastic.co/kibana/kibana:7.10.0
    ports:
      - 5601:5601
    networks:
      default:
        ipv4_address: 172.19.1.5

  redis:
    image: redis:6.0-alpine
    ports:
      - 6379:6379
    networks:
      default:
        ipv4_address: 172.19.1.6

  ui:
    image: exceptionless/ui:latest
    environment:
      AppMode: Development
      EX_ApiUrl: http://localhost:5000
      EX_Html5Mode: 'true'
    ports:
      - 5100:80
    networks:
      default:
        ipv4_address: 172.19.1.7
    
networks:
  default:
    external:
      name: exceptionless
```