# Apollo部署



## compose搭建apollo

**准备工作：**

首先部署mysql，创建用户并设置密码，这里是`root`/`123`

```sh
mkdir -p /data/mysql/1/data /data/mysql/1/conf /data/mysql/1/logs \
         /data/mysql/2/data /data/mysql/2/conf /data/mysql/2/logs \
         /data/apollo/1/logs /data/apollo/2/logs
vim /data/mysql/docker-compose.yml
```

mysql 的 docker-compose.yml

```yaml
version: '3.7'

services:
  mysql1:
    image: mysql
    container_name: mysql1
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_general_ci
      --lower_case_table_names=1
    restart: unless-stopped # docker的重启策略：在容器退出时总是重启容器，但是不考虑在Docker守护进程启动时就已经停止了的容器
    environment:
      MYSQL_ROOT_PASSWORD: 123 # root用户的密码
    ports:
      - 3306:3306
    volumes:
      - /data/mysql/1/data:/var/lib/mysql
      - /data/mysql/1/conf:/etc/mysql/conf.d
      - /data/mysql/1/logs:/logs
    networks:
      default:
        ipv4_address: 172.18.1.100

mysql2:
    image: mysql
    container_name: mysql2
    command:
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_general_ci
      --lower_case_table_names=1
    restart: unless-stopped # docker的重启策略：在容器退出时总是重启容器，但是不考虑在Docker守护进程启动时就已经停止了的容器
    environment:
      MYSQL_ROOT_PASSWORD: 123 # root用户的密码
    ports:
      - 3307:3306
    volumes:
      - /data/mysql/2/data:/var/lib/mysql
      - /data/mysql/2/conf:/etc/mysql/conf.d
      - /data/mysql/2/logs:/logs
    networks:
      default:
        ipv4_address: 172.18.1.101

networks:
  default:
    external:
      name: lead_pm1
```

导入apollo数据库脚本

```sh
cd /software
git clone https://github.com/ctripcorp/apollo.git
mysql -uroot -p123 < apollo/scripts/sql/apolloportaldb.sql
mysql -uroot -p123 < apollo/scripts/sql/apolloconfigdb.sql
# 编辑 apollo 脚本
vim /data/apollo/docker-compose.yml
```

apollo 的 docker-compose.yaml

```yaml
version: '3.7'

services:
  apollo-configservice1:
    container_name: apollo-configservice1
    image: apolloconfig/apollo-configservice
    volumes:
      - type: volume
        source: logs1
        target: /opt/logs
    ports:
      - 8081:8080
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://172.18.1.100:3306/ApolloConfigDB?characterEncoding=utf8
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123
      - EUREKA_INSTANCE_HOME_PAGE_URL=http://172.18.1.2:8080
    restart: always
    networks:
      default:
        ipv4_address: 172.18.1.2

  apollo-adminservice1:
    depends_on:
      - apollo-configservice1
    container_name: apollo-adminservice1
    image: apolloconfig/apollo-adminservice
    volumes:
      - type: volume
        source: logs1
        target: /opt/logs
    ports:
      - 8082:8090
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://172.18.1.100:3306/ApolloConfigDB?characterEncoding=utf8
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123
      - EUREKA_INSTANCE_HOME_PAGE_URL=http://172.18.1.2:8080
    restart: always
    networks:
      default:
        ipv4_address: 172.18.1.3

  apollo-portal:
    depends_on:
      - apollo-adminservice1
      - apollo-adminservice2
    container_name: apollo-portal
    image: apolloconfig/apollo-portal
    volumes:
      - type: volume
        source: logs1
        target: /opt/logs
    ports:
      - 8083:8070
    environment:  
      - SPRING_DATASOURCE_URL=jdbc:mysql://172.18.1.100:3306/ApolloPortalDB?characterEncoding=utf8
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123
      - APOLLO_PORTAL_ENVS=PRO
      - PRO_META=http://172.18.1.2:8080
    restart: always
    networks:
      default:
        ipv4_address: 172.18.1.4

  apollo-configservice2:
    container_name: apollo-configservice2
    image: apolloconfig/apollo-configservice
    volumes:
      - type: volume
        source: logs2
        target: /opt/logs
    ports:
      - 8084:8080
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://172.18.1.101:3306/ApolloConfigDB?characterEncoding=utf8
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123
      - EUREKA_INSTANCE_HOME_PAGE_URL=http://172.18.1.5:8080
    restart: always
    networks:
      default:
        ipv4_address: 172.18.1.5

  apollo-adminservice2:
    depends_on:
      - apollo-configservice2
    container_name: apollo-adminservice2
    image: apolloconfig/apollo-adminservice
    volumes:
      - type: volume
        source: logs2
        target: /opt/logs
    ports:
      - 8085:8090
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://172.18.1.101:3306/ApolloConfigDB?characterEncoding=utf8
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123
      - EUREKA_INSTANCE_HOME_PAGE_URL=http://172.18.1.5:8080
    restart: always
    networks:
      default:
        ipv4_address: 172.18.1.6

volumes:
  logs1:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/apollo/1/logs
  logs2:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/apollo/2/logs
      
networks:
  default:
    external:
      name: lead_pm1
```

参数说明：

- `SPRING_DATASOURCE_URL`：对应环境 ApolloPortalDB 的地址
- `SPRING_DATASOURCE_USERNAME`：对应环境 ApolloPortalDB 的用户名

- `SPRING_DATASOURCE_PASSWORD`：对应环境 ApolloPortalDB 的密码

- `APOLLO_PORTAL_ENVS`（可选）：对应 ApolloPortalDB 中的 apollo.portal.envs 配置项，如果没有在数据库中配置的话，可以通过此环境参数配置

- `DEV_META/PRO_META`（可选）：配置对应环境的 Meta Service 地址，以 `${ENV}_META` 命名，如果 ApolloPortalDB 中配置了 apollo.portal.meta.servers，则以 apollo.portal.meta.servers 中的配置为准

```sh
# 生成
docker-compose up -d
docker-compose ps
```

访问web界面：http://0.0.0.0:8083/，默认账户密码是 `apollo / admin`。

Eureka地址（内嵌在configservice容器中）：http://0.0.0.0:8081



## k8s搭建Apollo

**configMap VS Apollo**

configMap 只有在创建 pods 的时候会拉取，后面如果修改了 configMap，不会同步到 pods 中，如果手动去拉取 ，又和 Apollo 没有什么区别。

另外，Apollo 有审计、有灰度、有历史变更记录，还有一套权限系统。

### kubernetes部署

Apollo 是由 java 语言编写，使用了 spring 框架，以及 Eureka 服务发现，如果使用 k8s 环境的话，当然是使用 k8s 自带的服务发现最佳，提升系统可读性，可维护性和稳定性。wiki 上已经有详细的文档，这里说一下重点步骤。

#### 添加数据库配置文件

首先下载 [https://github.com/ctripcorp/apollo/tree/master/scripts/sql](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fctripcorp%2Fapollo%2Ftree%2Fmaster%2Fscripts%2Fsql) 两个 sql 文件，然后导入到自己各个环境的数据库中。

`apolloconfigdb.sql` 是各个环境都需要配置的 service 服务需要的数据库，`apolloportaldb.sql` 是 portal 管理端服务需要的数据库，如果暂时搞不清楚，两个文件可以都导入，后面再去理解原理。

> 下面的描述中提到的 service 均指 configservice 和 adminservice 。

#### helm 添加 Apollo 的源

```sh
helm repo add apollo http://ctripcorp.github.io/apollo/charts
helm search repo apollo
# ------------------------------------------------------------------------------------
NAME                    CHART VERSION  APP VERSION   DESCRIPTION
apollo/apollo-portal    0.1.1          1.7.1         A Helm chart for Apollo Portal
apollo/apollo-service   0.1.1          1.7.1         A Helm chart for Apollo Config Service and Apol...
# ------------------------------------------------------------------------------------
```

#### 拉取模板文件

这里拉取文件是为了可以详细的看下作者是如何设计这个 chart，以及可以拿到一份完整的 values.xml 文件。基于 k8s 的思想，配置都要留在文件里，helm 如果使用命令行的方式，可以测试使用，但是如果是线上环境那终究没有里面存档，命令行的参数过长也会造成阅读不便。

```sh
helm pull apollo/apollo-portal
helm pull apollo/apollo-service
ls -l
# ------------------------------------------------------------------------------------
total 16
-rw-r--r--  1 jake  staff  3568 Sep 16 17:01 apollo-portal-0.1.1.tgz
-rw-r--r--  1 jake  staff  3478 Sep 16 17:00 apollo-service-0.1.1.tgz
# ------------------------------------------------------------------------------------
```

两个压缩文件，两个 chart 根目录中的 values.yaml 文件可以复制出来，重命名一下备用。

apollo-portal-values.yaml

```yaml
name: apollo-portal
fullNameOverride: ""
replicaCount: 2
containerPort: 8070
image:
  repository: apolloconfig/apollo-portal
  pullPolicy: IfNotPresent
imagePullSecrets: []
service:
  fullNameOverride: ""
  port: 8070
  targetPort: 8070
  type: ClusterIP
  sessionAffinity: ClientIP
ingress:
  enabled: false
  annotations: {}
  hosts:
    - host: ""
      paths: []
  tls: []
liveness:
  initialDelaySeconds: 100
  periodSeconds: 10
readiness:
  initialDelaySeconds: 30
  periodSeconds: 5
# environment variables passed to the container, e.g. JAVA_OPTS
env: {}
strategy: {}
resources: {}
nodeSelector: {}
tolerations: []
affinity: {}

config:
  # spring profiles to activate
  profiles: "github,auth"
  # specify the env names, e.g. dev,pro
  envs: ""
  # specify the meta servers, e.g.
  # dev: http://apollo-configservice-dev:8080
  # pro: http://apollo-configservice-pro:8080
  metaServers: {}
  # specify the context path, e.g. /apollo
  contextPath: ""
  # extra config files for apollo-portal, e.g. application-ldap.yml
  files: {}

portaldb:
  name: apollo-portaldb
  # apolloportaldb host
  host:
  port: 3306
  dbName: ApolloPortalDB
  # apolloportaldb user name
  userName:
  # apolloportaldb password
  password:
  connectionStringProperties: characterEncoding=utf8
  service:
    # whether to create a Service for this host or not
    enabled: false
    fullNameOverride: ""
    port: 3306
    type: ClusterIP
```

apollo-service-values.yaml

```yaml
configdb:
  name: apollo-configdb
  # apolloconfigdb host
  host: ""
  port: 3306
  dbName: ApolloConfigDB
  # apolloconfigdb user name
  userName: ""
  # apolloconfigdb password
  password: ""
  connectionStringProperties: characterEncoding=utf8
  service:
    # whether to create a Service for this host or not
    enabled: false
    fullNameOverride: ""
    port: 3306
    type: ClusterIP

configService:
  name: apollo-configservice
  fullNameOverride: ""
  replicaCount: 2
  containerPort: 8080
  image:
    repository: apolloconfig/apollo-configservice
    pullPolicy: IfNotPresent
  imagePullSecrets: []
  service:
    fullNameOverride: ""
    port: 8080
    targetPort: 8080
    type: ClusterIP
  liveness:
    initialDelaySeconds: 100
    periodSeconds: 10
  readiness:
    initialDelaySeconds: 30
    periodSeconds: 5
  config:
    # spring profiles to activate
    profiles: "github,kubernetes"
    # override apollo.config-service.url: config service url to be accessed by apollo-client 
    configServiceUrlOverride: ""
    # override apollo.admin-service.url: admin service url to be accessed by apollo-portal 
    adminServiceUrlOverride: ""
  # environment variables passed to the container, e.g. JAVA_OPTS
  env: {}
  strategy: {}
  resources: {}
  nodeSelector: {}
  tolerations: []
  affinity: {}

adminService:
  name: apollo-adminservice
  fullNameOverride: ""
  replicaCount: 2
  containerPort: 8090
  image:
    repository: apolloconfig/apollo-adminservice
    pullPolicy: IfNotPresent
  imagePullSecrets: []
  service:
    fullNameOverride: ""
    port: 8090
    targetPort: 8090
    type: ClusterIP
  liveness:
    initialDelaySeconds: 100
    periodSeconds: 10
  readiness:
    initialDelaySeconds: 30
    periodSeconds: 5
  config:
    # spring profiles to activate
    profiles: "github,kubernetes"
  # environment variables passed to the container, e.g. JAVA_OPTS
  env: {}
  strategy: {}
  resources: {}
  nodeSelector: {}
  tolerations: []
  affinity: {}
```

#### 部署核心文件

由于 Apollo 的设计原则，可以一个 portal 管理多个 service，我们可以在各个环境（DEV，PRO）都部署一套 service，然后用 portal 单独去管理。

**配置 service**

我们的各个环境应该都有自己的一套数据库，Apollo 唯一的依赖也是 mysql，所以只需要修改 configdb 中的数据库字段

```yaml
configdb:
  name: apollo-configdb
  # apolloconfigdb host
  host: "mysql8" # 修改为自己的数据库地址
  port: 3306 # 修改为自己的数据库端口
  dbName: apolloconfigdb  # 修改为自己的数据库名字
  # apolloconfigdb user name
  userName: "root"  # 修改为自己的数据库用户
  # apolloconfigdb password
  password: "1"  # 修改为自己的数据库用户密码
  connectionStringProperties: characterEncoding=utf8
  service:
    # whether to create a Service for this host or not
    enabled: false
    fullNameOverride: ""
    port: 3306
    type: ClusterIP
```

可以将 `apollo-service-values.yaml` 中的其他内容都删除只保留一个 configdb， 其他属性会走默认的配置字段，如果有特殊需求就自行编辑。

```sh
helm install -f apollo-service-values.yaml apollo-service apollo/apollo-service -n sv-dev
...
NOTES:
Get meta service url for current release by running these commands:
  echo http://apollo-service-apollo-configservice.sv-dev:8080
...
```

这里的 namespace 是 sv-dev，核心的一段是 `http://apollo-service-apollo-configservice.sv-dev:8080`，需要暂时存储一下。

然后替换 configdb 中的数据库信息，将 apollo-service 配置到各个环境中去。

**配置 protal**

```yaml
replicaCount: 1 # **切记切记**，这里一定要写 1，如果有多个 protal ，使用的 session 之间不共享内存，导致我需要反复登录

config:
  # spring profiles to activate
  profiles: "github,auth"
  # specify the env names, e.g. dev,pro
  envs: "dev,pro" # 如果是部署了多个环境，这里写多个环境以逗号隔开
  # specify the meta servers, e.g.
  # dev: http://apollo-configservice-dev:8080
  # pro: http://apollo-configservice-pro:8080
  metaServers: 
       dev: "http://apollo-service-apollo-configservice.sv-dev:8080" # 这里填写上面 helm 部署成功得到的地址
       pro: "http://apollo-service-apollo-configservice.sv-pro:8080" # 这里填写上面 helm 部署成功得到的地址
  # specify the context path, e.g. /apollo
  contextPath: ""
  # extra config files for apollo-portal, e.g. application-ldap.yml
  files: {}

portaldb:
  name: apollo-portaldb
  # apolloportaldb host
  host: "" # 修改为自己的数据库地址
  port: 3306 # 修改为自己的数据库端口
  dbName: ApolloPortalDB # 修改为自己的数据库名字
  # apolloportaldb user name
  userName: "" # 修改为自己的数据库用户
  # apolloportaldb password
  password: ""  # 修改为自己的数据库用户密码
  connectionStringProperties: characterEncoding=utf8
  service:
    # whether to create a Service for this host or not
    enabled: false
    fullNameOverride: ""
    port: 3306
    type: ClusterIP

ingress:
  enabled: true
  annotations: {}
  hosts:
    - host: "apollo.xxxx.com" # 选一个自己喜欢的域名，绑定一下
      paths: 
        - "/"
  tls: []
```

无关的配置默认就好，可以直接删除掉，只保留核心的需要修改配置。

```sh
helm install -f apollo-portal-values.yaml apollo-portal apollo/apollo-portal -n apollo
```

这里将 apollo-portal 部署到单独的一个 namespace 中。

**参考文档**

- [官方文档](https://ctripcorp.github.io/apollo/#/zh/README)

- [Apollo配置中心设计](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fctripcorp%2Fapollo%2Fwiki%2FApollo%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83%E8%AE%BE%E8%AE%A1)
- [基于Kubernetes原生服务发现](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fctripcorp%2Fapollo%2Fwiki%2F%E5%88%86%E5%B8%83%E5%BC%8F%E9%83%A8%E7%BD%B2%E6%8C%87%E5%8D%97%2324-kubernetes%E9%83%A8%E7%BD%B2)