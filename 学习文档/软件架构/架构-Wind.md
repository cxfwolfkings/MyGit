# 微服务架构实战

**格言：**

结果 = 思维方式 * 热情 * 能力

不懒惰，不妥协，人生是一张票，能不能赶上时代的快车，你的步伐掌握在自己脚下！  

**公式：**

- 1.01^365^ = 37.8，0.99^365^ = 0.03  积硅步致千里，积怠倦堕深渊
- 1.02^365^ = 1377.4  只比你努力一点的人，其实已经甩你很远
- 1.01^3^ * 0.99^2^ < 1.01  三天打鱼，两天晒网，一无所获

Wind组织系统架构：

1. 备忘录
   - 一尾蜂
2. 安装部署
   - [docker安装](#docker安装)
   - [docker-compose](#docker-compose)
   - [Portainer](#Portainer)
   - [搭建私有云](#搭建私有云)
3. 运维监控
4. [负载均衡](#负载均衡)
5. [服务注册中心](#服务注册中心)
6. [API网关](#API网关)
8. [部署前准备](#部署前准备)
9. [压缩备份](#压缩备份)
10. [镜像生成](#镜像生成)
11. [部署前端应用](#部署前端应用)
12. [部署后端微服务](#部署后端微服务)
13. [外网服务器部署](#外网服务器部署)
    - [nginx安装](#nginx安装)
    - [nginx配置](#nginx配置)
14. [一键部署](#一键部署)
15. [消息队列](#消息队列)
    - [安装RabbitMQ](#安装RabbitMQ)
16. [分布式日志平台](#分布式日志平台)
17. [附录](#附录)



- [Linux常用命令](#linux常用命令)
- [安装软件](#安装软件)
  
- [关闭防火墙](#关闭防火墙)
- [docker常用命令](#docker常用命令)
- [宝塔Linux面板](#宝塔linux面板)
- [docker阿里云镜像配置](#docker阿里云镜像配置)
- [compose](#compose)
- [搭建私有镜像仓库](#搭建私有镜像仓库)
- [centos7](#centos7)
- [Portainer](#portainer)
- [Grafana](#grafana)
- [ExceptionLess](#ExceptionLess)
- [安装图片服务器](#安装图片服务器)
- [前端部署](#前端部署)
- [后端部署](#后端部署)
- [设置共享目录](#设置共享目录)

- [Feng](#feng)
  - [onlyoffice](#onlyoffice)
- [Angel](#angel)

  - [网络共享](#网络共d享)
  - [judpyter](#jupyter)
  - [Deepnote](#Deepnote)
  - [conda配置清华源](#conda配置清华源)
  - [shadowsocks](#shadowsocks)
- [箴言格律](#箴言格律)
- [收藏网址](#收藏网址)
- [账号信息](#账号信息)



## 备忘录



### 一尾蜂

```sh
wxdh：
-------------------------------------------------------------
FTP: hyw6485860001.my3w.com    hyw6485860001 / Cxf5609757
DB:  hds161730424.my3w.com     hds161730424 / Cxf5609757
-------------------------------------------------------------

viyitech：
-------------------------------------------------------------
FTP: qxu1606550082.my3w.com    qxu1606550082 / Cxf5609757
DB:  qdm643953646.my3w.com     qdm643953646 / Cxf5609757
-------------------------------------------------------------

Wind：
-------------------------------------------------------------
url:  121.196.182.26
user: root
pwd:  Cxf5609757*
vnc:  388038

sftp：
host: 121.196.182.26
user: sftp
pwd:  Cxf5609757
dir:  /data/sftp/mysftp

宝塔Linux面板：
Bt-Panel: http://121.196.182.26:8888/9619dfd1
username: 2ft3rt6f
password: 16631019
# 官网：https://www.bt.cn/download/linux.html
# Centos安装脚本：
yum install -y wget && wget -O install.sh http://download.bt.cn/install/install_6.0.sh && sh install.sh
# Ubuntu/Deepin安装脚本：
wget -O install.sh http://download.bt.cn/install/install-ubuntu_6.0.sh && sudo bash install.sh
# Debian安装脚本：
wget -O install.sh http://download.bt.cn/install/install-ubuntu_6.0.sh && bash install.sh
# Fedora安装脚本：
wget -O install.sh http://download.bt.cn/install/install_6.0.sh && bash install.sh
# 默认端口：8888
为了提高安全性，当前宝塔新安装的已经开启了安全目录登录，新装机器都会随机一个8位字符的目录名，亦可以在面板设置处修改，如您没记录或不记得了，可以使用以下方式解决：登陆SSH终端输入以下一种命令来解决
# 1. 查看面板入口：
/etc/init.d/bt default
# 2. 关闭入口验证：
rm -f /www/server/panel/data/admin_path.pl

Docker公共镜像库：https://hub.docker.com/
账号：wolfkings  
密码：Cxf5609757
阿里云镜像库：https://opsx.alibaba.com/mirror


```







基础命令：

```sh
# 查看进程
top
# 看内存占用
free -m
# 看硬盘占用率
df -h
```





#### 关闭防火墙

```sh
# 查看防火墙状态
systemctl status firewalld.service
# 关闭运行的防火墙（重启失效）
systemctl stop firewalld.service
# 禁止防火墙服务器（永久生效）
systemctl disable firewalld.service
```



### docker阿里云镜像配置

提升获取官方镜像的速度：

```sh
mkdir -p /etc/dockertee /etc/docker/daemon.json <<-'EOF'{  "registry-mirrors": ["https://6o1rxqal.mirror.aliyuncs.com"]}EOFsystemctl daemon-reloadsystemctl restart docker
```

镜像仓库申请地址：[https://cr.console.aliyun.com/cn-shanghai/instances/repositories](#https://cr.console.aliyun.com/cn-shanghai/instances/repositories)

注册登录，创建命名空间，创建镜像仓库

```sh
# 登录阿里云 Docker Registrydocker login --username=一尾蜂 registry.cn-shanghai.aliyuncs.com# 用于登录的用户名为阿里云账号全名，密码为开通服务时设置的密码。# 从Registry中拉取镜像docker pull registry.cn-shanghai.aliyuncs.com/daniel-hub/nginx-docker:[镜像版本号]# 将镜像推送到Registrydocker tag [ImageId] registry.cn-shanghai.aliyuncs.com/daniel-hub/nginx-docker:[镜像版本号]docker push registry.cn-shanghai.aliyuncs.com/daniel-hub/nginx-docker:[镜像版本号]@ 请根据实际镜像信息替换示例中的[ImageId]和[镜像版本号]参数。
```

选择合适的镜像仓库地址：从ECS推送镜像时，可以选择使用镜像仓库内网地址，推送速度将得到提升并且将不会损耗您的公网流量。

如果您使用的机器位于经典网络，请使用 registry-internal.cn-shanghai.aliyuncs.com 作为Registry的域名登录，并作为镜像命名空间前缀。

如果您使用的机器位于VPC网络，请使用 registry-vpc.cn-shanghai.aliyuncs.com 作为Registry的域名登录，并作为镜像命名空间前缀。



### compose

简介：Docker Compose 是一个用来定义和运行复杂应用的 Docker 工具。使用 Docker Compose 不再需要使用 shell 脚本来启动容器（通过 docker-compose.yml 配置）。

本质：编排和配置容器集群的工具。

编排：定义被部署的对象的各组成部分之间的耦合关系，部署流程中各个动作的执行顺序，部署过程所需要的依赖文件和被部署文件的存储位置和获取方式，以及如何验证部署成功。这些信息都会在编排工具中以指定的格式定义并保存下来，从而保证这个流程可以在新的环境中快速的复现。

**安装：**

```sh
# 下载docker-compose 二进制文件curl -L "https://github.com/docker/compose/releases/download/1.25.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose# 授予执行权限chmod +x /usr/local/bin/docker-compose# 查看是否成功安装docker-compose --help
```

【黑魔法】下载地址：`https://github.com/docker/compose/releases`，可以访问外网的环境，在线安装省心

**卸载：**

```sh
rm /usr/local/bin/docker-compose
```

**命令：**

1、Docker compose 的使用非常类似于 docker 命令的使用，但是需要注意的是**大部分的 compose 命令都需要到 docker-compose.yml 文件所在的目录下才能执行**。

2、compose 以守护进程模式运行加 `-d` 选项。服务状态：Up

```sh
docker-compose up -ddocker-compose -f docker-compose.yml up -d
```

3、查看有哪些服务，使用 `docker-compose ps` 命令，非常类似于 docker 的 ps 命令。

4、查看 compose 日志

```sh
docker-compose logs webdocker-compose logs redis
```

5、停止 compose 服务。服务状态：Exit 0（所有关联的活动容器也被停止）

```sh
docker-compose stopdocker-compose ps
```

6、重启 compose 服务

```sh
docker-compose restartdocker-compose ps
```

7、kill compose 服务。服务状态：Exit 137

```sh
docker-compose killdocker-compose ps
```

8、删除 compose 服务（删除所有已停止的关联容器）

```sh
docker-compose rm
```

9、帮助命令

```sh
docker-compose --help
```

>注意：yaml文件里不能有tab，只能有空格。关于 version 与 Docker 版本的关系如下：

| Compose file format | Docker engine |
| ------------------- | ------------- |
| 1                   | 1.9.0+        |
| 2.0                 | 1.10.0+       |
| 2.1                 | 1.12.0+       |
| 2.2, 3.0, 3.1, 3.2  | 1.13.0+       |
| 2.3, 3.3, 3.4, 3.5  | 17.06.0+      |
| 2.4                 | 17.12.0+      |
| 3.6                 | 18.02.0+      |
| 3.7                 | 18.06.0+      |

docker-compose 源码实例

```yaml
# docker-Compose的版本version: '3'# 建立2个service 一个wordpress 一个 mysqlservices:  wordpress:    image: wordpress    ports:  # 端口映射 80映射到8080端口      - 8080:80    environment:  # 环境变量2个      WORDPRESS_DB_HOST: mysql      WORDPRESS_DB_PASSWORD: root    networks:      - my-bridge  mysql:    image: mysql    environment:      MYSQL_ROOT_PASSWORD: root      MYSQL_DATABASE: wordpress    volumes:      - mysql-data:/var/lib/mysql    networks:      - my-bridge# 建立一个volumes volumes:  mysql-data:# 建立一个networksnetworks:  my-bridge:    driver: bridge
```

services:

1. 一个 service 代表一个 container，这个 container 可以从 docker hub 的 image 来创建，也可以从本地的 Dockerfile build 出来的 image 来创建。

2. service 的启动类似 docker run，我们可以给其指定 network 和 volume，所以可以给 service 指定 network 和 volume 的引用

源码地址：[https://github.com/limingios/docker ](https://github.com/limingios/docker )中的No.4

**水平扩展和负载均衡**

原文：[https://idig8.com/2018/07/29/docker-zhongji-40/](https://idig8.com/2018/07/29/docker-zhongji-40/)

**scale** 命令的使用

设置为一个服务启动的容器数量，数量是以这样的参数形式指定的：service=num

```sh
docker-compose up --scale web=3 -d
```

如果报错，请修改 docker-compose.yml 配置文件，将 web 对应的端口映射去掉！示例：

```sh
# -d后台运行sudo docker-compose up -d# 启动了2个容器，1个web，1个resdissudo docker-compose ps# 水平扩展给web的容器增加到3个sudo docker-compose up --scale web=3 -d# 启动了4个容器，3个web，1个resdissudo docker-compose ps
```

但是问题来了没有暴露到外边的端口都是5000内部端口，所以出来了一个命令负载均衡工具：**haproxy**

**参考：**

- 官网：[https://docs.docker.com/compose/compose-file/](https://docs.docker.com/compose/compose-file/)
- 简介：[https://idig8.com/2018/07/27/docker-chuji-12/](#https://idig8.com/2018/07/27/docker-chuji-12/)

**示例：**

场景：redis容器，tomcat容器，nginx容器，mysql容器，这4个容器的启动是有顺序性的，docker compose就是为了组合启动的，而不是手动来启动。（本例子讲的是2个容器，redis和web）

准备环境：

1） 创建测试项目文件夹

```sh
mkdir composetestcd composetest
```

默认python2.7在centos都安装了，就用python来演示，演示之前需要安装pip工具，类似java的maven管理python模块的工具

```sh
#这个软件包会自动配置yum的软件仓库。yum install -y epel-releaseyum install -y python-pip
```

当然你也可以不安装epel这个包，自己配置软件仓库也是一样的，自己手工添加软件仓库配置文件：

```sh
vi /etc/yum.repos.d/epel.repo[epel]name=epelmirrorlist=http://mirrors.fedoraproject.org/mirrorlist?repo=epel-$releasever&arch=$basearchenabled=1gpgcheck=0
```

添加完毕之后：`yum clean all && yum update`

最后终极大发——有pptpd的yum源

```sh
rpm -Uvh http://poptop.sourceforge.net/yum/stable/rhel6/pptp-release-current.noarch.rpm
```

2） 编辑app.py并保存

（描述：简单的一个httpserver，主要是为了类似tomcat的一个sevlet，当访问一次，redis节点就增加一个，就可以看到相应的输出）

```py
from flask import Flaskfrom redis import Redisapp = Flask(__name__)redis = Redis(host='redis', port=6379)@app.route('/')def hello():  redis.incr('hits')  return 'Hello World! I have been seen %s times.' %redis.get('hits')if __name__ == "__main__":  app.run(host="192.168.101.13", debug=True)
```

3） 在项目目录创建requirements.txt并保存

命令：

```sh
vi requirements.txt
```

内容：

```txt
flaskredis
```

利用Dockerfile创建docker镜像（重头戏）

命令：

```sh
vi Dockerfile
```

内容：

```sh
FROM python:2.7ADD . /codeWORKDIR /codeRUN pip install -r requirements.txtCMD python app.py
```

命令：

```sh
docker build -t web .
```

定义服务

创建docker-compose.yml文件，Compose文件定义了2个服务，web和redis。

Web服务：

1. 从当前目录下的dockerfile创建
2. 容器的5000端口与宿主机5000端口绑定
3. 将项目目录与容器内的/code目录绑定
4. web服务与redis服务建立连接

命令：

```sh
vi docker-compose.yml
```

内容（实践时要将注释内容删除）：

```yml
version: '2'        -- version版本services:           -- services服务  web:              -- web服务名称    build: .        -- build当前目录    ports:      - "5000:5800" -- ports映射的端口    volumes:      - .:/code     -- 挂载    depends_on:     -- 前置服务redis      - redis  redis:            -- redis依赖的镜像    image: redis   -- 不能用tab，必须用空格
```

通过compose运行app服务

```sh
docker-compose up
```

备注：

```sh
docker-compose up –d （后台启动）docker-compose stop （停止运行）
```

Compose命令集：[https://docs.docker.com/compose/reference/](#https://docs.docker.com/compose/reference/)

**练习：**

配置文件：[docker-compose.yml](./Codes/docker-compose.yml)

初始化：`docker swarm init`

运行：

```sh
docker stack deploy -c docker-compose.yml getstartedlab #应用程序命名为getstartedlab
```

查看应用程序服务：`docker service ls` 或者 `docker stack services getstartedlab`

在服务中运行的单个容器称为任务，任务被赋予以数字递增的唯一ID。列出您的服务任务：`docker service ps getstartedlab_web`

如果您只列出系统上的所有容器，则任务也会显示，但不会被服务过滤：`docker container ls -q`

您可以 `curl -4 http://localhost:4000` 连续多次运行，或者在浏览器中转到该URL并点击刷新几次。无论哪种方式，容器ID都会发生变化，从而证明负载均衡；对于每个请求，以循环方式选择5个任务中的一个来响应

您可以通过更改docker-compose.yml，保存并重新运行 `docker stack deploy` 命令来扩展应用程序，Docker执行实时更新，无需首先删除应用或杀死任何容器。

Take the app down：`docker stack rm getstartedlab`  
Take down the swarm：`docker swarm leave --force`

基本命令：

```sh
docker stack ls  # List stacks or appsdocker stack deploy -c <composefile> <appname>  # Run the specified Compose filedocker service ls  # List running services associated with an appdocker service ps <service>  # List tasks associated with an appdocker inspect <task or container>  # Inspect task or containerdocker container ls -q # List container IDsdocker stack rm <appname> # Tear down an applicationdocker swarm leave --force # Take down a single node swarm from the manager
```

【集群】准备工作：

```sh
# 下载Oracle VirtualBoxwget http1s://download.virtualbox.org/virtualbox/6.0.6/VirtualBox-6.0-6.0.6_130049_el7-1.x86_64.rpm# 安装VirtualBoxyum install VirtualBox-6.0-6.0.6_130049_el7-1.x86_64.rpm# 安装docker-machinebase=https://github.com/docker/machine/releases/download/v0.16.1 && curl -L $base/docker-machine-$(uname -s)-$(uname -m) >/tmp/docker-machine && sudo install /tmp/docker-machine /usr/local/bin/docker-machine# 通过显示机器版本来检查安装：docker-machine version
```

Machine资源库提供了几个bash脚本，可添加以下功能：

- 命令完成
- 一个在shell提示符下显示活动计算机的函数
- 一个函数包装器，它添加一个docker-machine use子命令来切换活动机器

确认版本并将脚本保存到 /etc/bash_completion.d 或 /usr/local/etc/bash_completion.d：

```bash
base=https://raw.githubusercontent.com/docker/machine/v0.14.0for i in docker-machine-prompt.bash docker-machine-wrapper.bash docker-machine.bashdo  sudo wget "$base/contrib/completion/bash/${i}" -P /etc/bash_completion.ddone
```

然后，您需要 `source /etc/bash_completion.d/docker-machine-prompt.bash` 在bash终端中运行，告诉您的设置，它可以找到docker-machine-prompt.bash您之前下载的文件。

要启用 docker-machineshell 提示，请添加 $(__docker_machine_ps1) 到您的PS1设置中~/.bashrc。

```sh
PS1='[\u@\h \W$(__docker_machine_ps1)]\$ '
```

可以在每个脚本顶部的[注释](https://github.com/docker/machine/tree/master/contrib/completion/bash)中找到其他文档。

如何卸载Docker Machine？

- （可选）删除您创建的计算机。

要单独删除每台机器： `docker-machine rm <machine-name>`

要删除所有计算机：(docker-machine rm -f $(docker-machine ls -q) 您可能需要 -force 在 Windows 上使用）。

删除计算机是一个可选步骤，因为在某些情况下，您可能希望将现有计算机保存并迁移到 Docker for Mac 或 Docker for Windows 环境。

- 删除可执行文件：`rm $(which docker-machine)`

注意：作为信息点config.json，与创建的每个虚拟机相关的证书和其他数据docker-machine 存储在 ~/.docker/machine/machines/Mac 和 Linux 以及 ~\.docker\machine\machines\Windows 上。

我们建议您不要直接编辑或删除这些文件，因为这只会影响 Docker CLI 的信息，而不会影响实际的VM，无论它们是本地还是远程服务器。

继续示例：

使用VirtualBox驱动程序创建2个VM

```sh
docker-machine create --driver virtualbox myvm1docker-machine create --driver virtualbox myvm2
```

如果报错：`yum -y install kernel-devel-3.10.0-862.el7.x86_64`



### 搭建私有镜像仓库

1、下载镜像registry

```sh
docker pull registry
```

2、运行registry容器

```sh
docker run -itd -v /data/registry:/var/lib/registry -p 5000:5000 --restart=always --name registry registry:latest# 测试镜像仓库中所有的镜像curl http://127.0.0.1:5000/v2/_catalog
```

参数说明：

- -itd：在容器中打开一个伪终端进行交互操作，并在后台运行；
- -v：把宿主机的/data/registry目录绑定 到 容器/var/lib/registry目录（这个目录是registry容器中存放镜像文件的目录），来实现数据的持久化；
- -p：映射端口；访问宿主机的5000端口就访问到registry容器的服务了；
- --restart=always：这是重启的策略，假如这个容器异常退出会自动重启容器；
- --name registry：创建容器命名为registry，你可以随便命名；
  registry:latest：这个是刚才pull下来的镜像；

3、为镜像打标签

```sh
docker tag consul:latest 10.30.100.103:5000/consul:v1
```

- consul:lastest 这是源镜像，也是刚才pull下来的镜像文件；
- 10.30.100.103:5000/consul:v1：这是目标镜像，也是registry私有镜像服务器的IP地址和端口；

4、上传到镜像服务器

```sh
docker push 10.30.100.103:5000/consul:v1
```

提示：`Get https://10.30.100.103:5000/v2/: http: server gave HTTP response to HTTPS client`

注意，这是报错了，需要https的方法才能上传，我们可以修改下daemon.json来解决：

```sh
vim /etc/docker/daemon.json
```

```json
{  "registry-mirrors": ["https://registry.docker-cn.com"],  "insecure-registries": ["10.30.100.103:5000"]}
```

添加私有镜像服务器的地址，注意书写格式为json，有严格的书写要求，然后重启docker服务：

```sh
systemctl  restart docker
```

再次上传，没问题。

5、拉取私有镜像

```sh
docker pull 10.30.100.103:5000/consul:v1# 测试镜像仓库中所有的镜像curl http://127.0.0.1:5000/v2/_catalog# 列出consul镜像有哪些tagcurl http://127.0.0.1:5000/v2/consul/tags/list
```



### centos7

用于测试容器间通信：

```sh
docker pull centos:7# 启动并进入容器docker run -it centos:7 /bin/bash
```



### Portainer

好用的图形化管理界面：

```txt
url:  http://121.196.182.26:9000/usr:  adminpwd:  Cxf5609757
```

安装：

```sh
docker pull portainer/portainer
docker run -d -p 9000:9000 \  --restart=always \  -v /var/run/docker.sock:/var/run/docker.sock \  --name prtainer-dev1 \  portainer/portainer
```



### 搭建私有云

包含功能：

- [sftp](#sftp)

- 云笔记：Joplin
- 云流程图：draw.io

#### sftp

```sh
# 查看openssh版本，openssh版本必须大于4.8p1
ssh -V
# 创建sftp组
groupadd sftp
# 创建sftp用户
useradd -g sftp -s /sbin/nologin -M sftp
passwd sftp
输入密码
# 建立目录
mkdir -p /data/sftp/mysftp/
usermod -d /data/sftp/mysftp sftp

# 修改sshd_config
vim /etc/ssh/sshd_config
# 注释掉
# Subsystem sftp /usr/libexec/openssh/sftp-server
# 添加
Subsystem sftp internal-sftp
Match Group sftp
ChrootDirectory /data/sftp/mysftp
ForceCommand internal-sftp
AllowTcpForwarding no
X11Forwarding no

# 设置Chroot目录权限
chown root:sftp /data/sftp/mysftp
chmod 755 /data/sftp/mysftp

# 设置可以写入的目录
mkdir /data/sftp/mysftp/upload
chown sftp:sftp /data/sftp/mysftp/upload
chmod 755 /data/sftp/mysftp/upload

# 关闭selinux：
vim /etc/selinux/config
# 将文件中的 SELINUX=enforcing 修改为 SELINUX=disabled，然后保存。

# 执行：
setenforce 0
service sshd restart
# 或
systemctl restart sshd.service

# 测试
sftp sftp@127.0.0.1
```

问题：

1、修改sshd_config文件后重启 sshd，报错（通过`sshd -t`查看）：`Directive 'UseDNS' is not allowed within a Match block`

语法错误，原因未知。只需将新加的配置放在下面配置之后就不报错了。

```conf
# 新加配置放在这一段之后
UseDNS no
AddressFamily inet
PermitRootLogin yes
SyslogFacility AUTHPRIV
PasswordAuthentication yes

# 下面是新加的配置
Subsystem sftp internal-sftp
UsePAM yes
Match user sftpuser1
ForceCommand internal-sftp
ChrootDirectory /data/wwwroot/user1/
```

2、新用户通过 sftp 访问时，权限不全，只能读不能写

试着用 root 账号去把该用户的 Home 目录权限改成 777，但是会出现该用户 sftp 登陆不了的情况。（报错：`Server unexpectedly closed network connection`）

google 了原因如下：

给新用户的 Home 目录的权限设定有两个要点：

1. 由 ChrootDirectory 指定的目录开始一直往上到系统根目录为止的目录拥有者都只能是 root
2. 由 ChrootDirectory 指定的目录开始一直往上到系统根目录为止都不可以具有群组写入权限（最大权限 755）

如果违反了上面的两条要求，那么就会出现新用户访问不了 sftp
的情况。

所以 /data/wwwroot/user1/ 及上级的所有目录属主一定要是
root，并且组权限和公共权限不能有写入权限，如果一定需要有写入权限，那们可以在 /data/wwwroot/user1/ 下建立 777 权限的文件夹。

```sh
mkdir /data/wwwroot/user1/upload
chown -R sftpuser1:root /data/wwwroot/user1/upload
```

这样 sftpuser1 用户就可以在 /data/wwwroot/user1/upload 里随意读写文件了。

#### Wind镜像

镜像文件：

```dockerfile
FROM centos:latest
# 作者：Colin
LABEL maintainer="Colin Chen <399596326@qq.com>"
# 设置时区、创建初始化目录
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo 'Asia/Shanghai' >/etc/timezone \
    && mkdir -p /usr/local/java/jdk \
    && mkdir -p /usr/local/tomcat
# 从客户机拷贝文件到容器中（自动解压）
# ADD jdk-8u291-linux-x64.tar.gz /usr/local/java/
# 创建符号链接
# RUN ln -s /usr/local/java/jdk1.8.0_291 /usr/local/java/jdk
# 设置环境变量（分两步）
ENV JAVA_HOME /usr/local/java/jdk
ENV JRE_HOME=${JAVA_HOME}/jre \
    CLASSPATH=.:${JAVA_HOME}/lib:${JAVA_HOME}/jre/lib \
    PATH=${JAVA_HOME}/bin:$PATH
```

执行命令：

```sh
# 进入根目录
cd /data/sftp/mysftp/upload/wind
# 解压文件
unzip apache-tomcat-8.5.57.zip
tar -xzvf jdk-8u291-linux-x64.tar.gz
# 授权
chown -R sftp:sftp /data/sftp/mysftp/upload/wind/apache-tomcat-8.5.57
chmod +x -R /data/sftp/mysftp/upload/wind/jdk1.8.0_291
chmod +x -R /data/sftp/mysftp/upload/wind/apache-tomcat-8.5.57
# 生成镜像
docker build -t owf/wind .
# 启动容器
docker run -itd \
  -v /data/sftp/mysftp/upload/wind/jdk1.8.0_291/:/usr/local/java/jdk \
  -v /data/sftp/mysftp/upload/wind/apache-tomcat-8.5.57/:/usr/local/tomcat \
  -p 8080:8080 \
  --name wind \
  owf/wind \
  /bin/bash
# 进入容器
docker exec -it wind bash
# 启动tomcat
/usr/local/tomcat/bin/startup.sh
# 关闭tomcat
/usr/local/tomcat/bin/shutdown.sh
```





### Grafana

下载地址：[https://grafana.com/grafana/download?platform=windows](https://grafana.com/grafana/download?platform=windows)

1、把下载的.zip文件解压到您的想运行Grafana的任何地方，然后进入conf目录复制一份sample.ini并重命名为custom.ini。以后所有的配置应该编辑custom.ini，永远不要去修改defaults.ini。



### ExceptionLess

官网：https://exceptionless.com/，用户名：chenxiao8516@163.com，密码：Cxf5609757



### 安装图片服务器

搭建 nginx 服务器实现图片的预览（承载在 docker 中）

Dockerfile：

```dockerfile
# 拉取nginx镜像FROM nginx:alpine# 工作目录WORKDIR /app# 从客户机复制到容器中COPY nginx.conf /etc/nginx/nginx.conf
```

nginx.conf：

```yml
user root; # 设置为和启动用户一致，否则可能报403错误worker_processes  1;#error_log  logs/error.log;#error_log  logs/error.log  notice;#error_log  logs/error.log  info;#pid        logs/nginx.pid;events {    worker_connections  1024;}http {    include       mime.types;    default_type  application/octet-stream;    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '    #                  '$status $body_bytes_sent "$http_referer" '    #                  '"$http_user_agent" "$http_x_forwarded_for"';    #access_log  logs/access.log  main;    sendfile        on;    #tcp_nopush     on;    #keepalive_timeout  0;    keepalive_timeout  65;    #gzip  on;    server {        listen       6100; # 1.你想让你的这个项目跑在哪个端口        server_name  121.196.182.26; # 2.当前服务器ip        #charset koi8-r;        #access_log  logs/host.access.log  main;        location / {            root   /app/; # 3.根目录的位置（工作目录设置为/app了）            index  index.html index.htm;        #   try_files $uri $uri/ /index.html; # 4.重定向,内部文件的指向（照写）        }        #location /api { # 4.当请求跨域时配置端口转发        #    proxy_pass http://47.92.76.97:8848/api; # 5.转发地址        #}        #error_page  404              /404.html;        # redirect server error pages to the static page /50x.html        #        error_page   500 502 503 504  /50x.html;        location = /50x.html {            root   html;        }        # proxy the PHP scripts to Apache listening on 127.0.0.1:80        #        #location ~ \.php$ {        #    proxy_pass   http://127.0.0.1;        #}        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000        #        #location ~ \.php$ {        #    root           html;        #    fastcgi_pass   127.0.0.1:9000;        #    fastcgi_index  index.php;        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;        #    include        fastcgi_params;        #}        # deny access to .htaccess files, if Apache's document root        # concurs with nginx's one        #        #location ~ /\.ht {        #    deny  all;        #}    }    # another virtual host using mix of IP-, name-, and port-based configuration    #    #server {    #    listen       8000;    #    listen       somename:8080;    #    server_name  somename  alias  another.alias;    #    location / {    #        root   html;    #        index  index.html index.htm;    #    }    #}    # HTTPS server    #    #server {    #    listen       443 ssl;    #    server_name  localhost;    #    ssl_certificate      cert.pem;    #    ssl_certificate_key  cert.key;    #    ssl_session_cache    shared:SSL:1m;    #    ssl_session_timeout  5m;    #    ssl_ciphers  HIGH:!aNULL:!MD5;    #    ssl_prefer_server_ciphers  on;    #    location / {    #        root   html;    #        index  index.html index.htm;    #    }    #}}
```

>跨域问题在这里着重说一下：  
>这里的跨域配置是打包后，nginx 做的代理转发。和开发模式的 proxytable 没有任何关系。  
>如果 nginx 不进行跨域的配置，虽然项目部署了，但是服务会访问不到。  
>关于跨域的介绍可以看：[https://blog.csdn.net/weixin_42565137/article/details/90578780](https://blog.csdn.net/weixin_42565137/article/details/90578780)

生成镜像，启动容器

```sh
# 进入目录cd /data/sftp/mysftp/upload/web/# 生成镜像docker build -t feng-web .# 启动docker run -d -p 6100:6100 -v /data/sftp/mysftp/upload/web/:/app --name feng-web feng-web
```



### 前端部署



### 后端部署



### 设置共享目录

**尚未连通！**

使用 samba 实现，安装：

```sh
yum -y install samba# 配置文件修改vim /etc/samba/smb.conf
```

说明：

```ini
[shared_name] #共享名称comment = Comment String #注释信息path = /share  #共享的目录路径public =  {yes|no}   #是否公开，受限浏览guest ok = {yes|no}  # 是否启用来宾账号writable = {yes|no} | read only = {yes|no} # 共享目录是否可写valid users = lxtone,root #被许可访问该共享目录的用户账号write list = lxtone,root #允许写入的用户账号，前面有+是代表允许可写的组。
```

上传目录设置为共享：

```ini
[upload]comment = Upload Directorypath = /data/sftp/mysftp/upload/public = yesguest ok = yeswritable = yes
```

重启服务：

```sh
systemctl restart smb
```



## Feng

无线网不要共享！

k8s-node1 ~ k8s-node4:

```sh
host:  192.168.1.101 ~ 192.168.1.104       192.168.101.101 ~ 192.168.101.104user:  rootpass:  Qwerty123456# 102gitlab-ce# 103docker# 104sftp, elasticsearch, kibanauser:  sftppass:  Qwerty123456/home/colin/mysftp/upload
```



### onlyoffice

```sh
# 创建 'onlyoffice' docker 网络
docker network create --driver bridge onlyoffice
# 安装ONLYOFFICE Document Server.sudo 
docker run --net onlyoffice -i -t -d \
           --restart=always \
           --name onlyoffice-document-server \
           -v/app/onlyoffice/DocumentServer/data:/var/www/onlyoffice/Data \-v/app/onlyoffice/DocumentServer/logs:/var/log/onlyoffice \onlyoffice/documentserver# 安装 ONLYOFFICE Mail Server.sudo docker run --net onlyoffice --privileged -i -t -d --restart=always --name onlyoffice-mail-server \-p 6025:25 -p 6143:143 -p 6587:587 \-v /app/onlyoffice/MailServer/data:/var/vmail \-v /app/onlyoffice/MailServer/data/certs:/etc/pki/tls/mailserver \-v /app/onlyoffice/MailServer/logs:/var/log \-v /app/onlyoffice/MailServer/mysql:/var/lib/mysql \-h intellif.com \onlyoffice/mailserver# 安装ONLYOFFICE Community Serversudo docker run --net onlyoffice -i -t -d --restart=always --name onlyoffice-community-server \-p 6081:80 -p 5222:5222 -p 6444:443 \-v /app/onlyoffice/CommunityServer/data:/var/www/onlyoffice/Data \-v /app/onlyoffice/CommunityServer/mysql:/var/lib/mysql \-v /app/onlyoffice/CommunityServer/logs:/var/log/onlyoffice \-v /app/onlyoffice/DocumentServer/data:/var/www/onlyoffice/DocumentServerData \-e DOCUMENT_SERVER_PORT_80_TCP_ADDR=onlyoffice-document-server \-e MAIL_SERVER_DB_HOST=onlyoffice-mail-server \onlyoffice/communityserver
```

通过 [IP](http://121.196.182.26:6081/) 访问 `onlyoffice web` 端，输入一个密码并指定下次访问你的office 所使用的电子邮件地址，进入页面。



## Angel



### 网络共享

一台centos7的系统，有2个网卡，一个连接外网网段10.8.20.0/24，一个连接内网网段172.168.10.0/24，其ip为：172.168.10.1，局域网中的其他机器只能连接172.168.10.0/24网段，例如其中一台的IP为：172.168.1.100，现在要使局域网中的其他机器能连上外网，可以在这台centos7的系统上做如下操作实现：

```sh
# 开启NAT转发 firewall-cmd --permanent --zone=public --add-masquerade# 开放DNS使用的53端口，否则可能导致内网服务器虽然设置正确的DNS，但是依然无法进行域名解析。firewall-cmd --zone=public --add-port=53/tcp --permanent# 重启防火墙systemctl restart firewalld.service
```

这样，在局域网的其他机器上，设置其IP为172.168.10.0/24网段，默认网关为172.168.10.1，即可连接外网



### Deepnote

https://zhuanlan.zhihu.com/p/288348633



### conda配置清华源

```sh
# 1. 直接打开cmd输入以下命令conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free/conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud//pytorch/conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/conda-forge/conda config --set show_channel_urls yes# 查看conda config --show channels# 移除清华源conda config --remove channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/conda-forge/# 2. 或者可以通过修改用户目录下的 .condarc 文件------------------------------------------------------------------channels:  - defaultsshow_channel_urls: truedefault_channels:  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/rcustom_channels:  conda-forge: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud  msys2: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud  bioconda: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud  menpo: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud  pytorch: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud  simpleitk: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud------------------------------------------------------------------# 即可添加 Anaconda Python 免费仓库。Windows 用户无法直接创建名为 .condarc 的文件，可先执行 conda config --set show_channel_urls yes 生成该文件之后再修改。# 检测环境变量conda info --envs
```



### shadowsocks

参考：https://github.com/shadowsocks/shadowsocks-windows/releases



## 箴言格律

>Keep working, we will find a way out. This is Finley, welcome to join us.
>
>生活的意义是什么？  
>我一直弄不懂这个问题。  
>于是，我彷徨，我不知道我该做些什么；  
>什么才是真的有意义！
>
>我读历史，可是历史却连人是怎么来的都弄不清楚；  
>我读未来，未来又告诉我人类所在的地球将会毁灭。  
>我便读现在，可历史却告诉我：现在的一切都是不可辨的，对错需要让后人去评说；  
>我相信哲学，可未来也告诉我：哲学是会变的，它只是人的世界观的一种反映。
>
>我活着是为了什么？  
>为了享乐？快乐和痛苦一样，只是一种感受；哪一种更好受，是随着心境而不同。  
>为了人类的延续？可是，地球的生命毕竟有限！  
>生活的意义，我一直在探索......
>
>突然有一天，未来告诉我：  
>借助计算机科技，人类将有走出地球的希望！  
>于是，我终于知道我是为了什么而活着：  
>为了人类尽早能够走出地球！
>
>当然，我没有那么大的能力。  
>当然，我有我的历史使命。  
>我只能用我的力所能及，为人类出一份力。  
>那就是加快计算机的发展！
>
>计算机是有限的机器；  
>科学却有无限的能力；  
>我们必须利用计算机来探索科学；  
>然后利用科学来扩大计算机的有限范围；
>
>继续、继续、继续；  
>终于有一天，我们能够坐着由计算机控制的  
>一种现在还不知道的东西，飞出地球，  
>飞到人类新的天地！
>
>之前：  
>只要我没有弄清生活的意义，我不会随便放弃；  
>之后：  
>宇宙只给你一个生命，其它的要你自己去把握。

## 收藏网址

- [docker公共镜像仓库](https://hub.docker.com/)

- [清华大学开源软件镜像站](https://mirror.tuna.tsinghua.edu.cn/)

- [北京理工大学开源软件镜像服务](https://mirror.bit.edu.cn/web/)

- [gitlab](https://docs.gitlab.com/)

  

## 账号信息

新浪：chenxiaofengshen / Cxf19870518





## 安装部署



## 运维监控



### docker安装

**环境介绍：**

| 操作系统      | docker版本 | 说明                                                         |
| ------------- | ---------- | ------------------------------------------------------------ |
| 64bit CentOS7 | 最新版本   | [https://github.com/docker/docker/blob/master/CHANGELOG.md](https://github.com/docker/docker/blob/master/CHANGELOG.md) |
|               |            |                                                              |
|               |            |                                                              |

**安装步骤：**

```sh
# 查看当前内核版本
uname -r
# 更改网卡配置
vim /etc/sysconfig/network-scripts/ifcfg-xxx  # 当前网卡
# -----------------------------------------------------
ONBOOT=yes
# -----------------------------------------------------
# 更改完后重启服务：
service network restart  # systemctl restart network 
# 注意：如果ifconfig命令不识别的话需要安装：  
yum install net-tools
```

**阿里云安装：**

1、确保服务器连网，配置网络Yum源，安装docker需要extra源

```sh
cd /etc/yum.repos.d/
# 将阿里云的Centos-7.repo下载保存到该目录
wget http://mirrors.aliyun.com/repo/Centos-7.repo
sed -i 's/$releasever/7/g' Centos-7.repo
```

2、安装Docker依赖

```sh
yum install -y yum-utils device-mapper-persistent-data lvm2
```

3、配置Docker的Yum源

```sh
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum-config-manager --enable docker-ce-nightly
yum-config-manager --enable docker-ce-test
yum-config-manager --disable docker-ce-nightly
```

4、安装Docker CE

```sh
yum -y install docker-ce docker-ce-cli containerd.io
```

5、启动Docker

```sh
systemctl start docker
# 查看docker安装版本信息
docker info
```

**加速器：**

注册：[daocloud](https://www.daocloud.io/) 或者 [阿里巴巴](#/accelerator) 这里我用的是daocloud

```sh
curl -sSL https://get.daocloud.io/daotools/set_mirror.sh | sh -s http://b81aace9.m.daocloud.io
```

**配置**

docker配置（docker控制应该有个专门的用户）：

```sh
adduser Colin #添加用户
passwd Colin #更改密码
su Colin #切换用户
#将用户Colin加入sudo files
sudo groupadd docker     #添加docker用户组
sudo gpasswd -a $USER docker     #将登陆用户加入到docker用户组中
newgrp docker     #更新用户组
docker ps    #测试docker命令是否可以使用sudo正常使用
# 验证在不使用sudo的情况下docker是否正常工作：
docker run hello-world
# 设置docker开机启动
chkconfig docker on
```

**卸载**

```sh
# 查看安装包
yum list installed | grep docker
# 移除安装包：
sudo yum -y remove docker-engine.x86_64
# 清除所有docker依赖文件：
rm -rf /var/lib/docker
# 删除用户创建的配置文件
```



### docker-compose

简介：

Docker Compose 是一个用来定义和运行复杂应用的 Docker 工具。使用 Docker Compose 不再需要使用 shell 脚本来启动容器（通过 docker-compose.yml 配置）。

本质：

编排和配置容器集群的工具。

编排：

定义被部署的对象的各组成部分之间的耦合关系，部署流程中各个动作的执行顺序，部署过程所需要的依赖文件和被部署文件的存储位置和获取方式，以及如何验证部署成功。这些信息都会在编排工具中以指定的格式定义并保存下来，从而保证这个流程可以在新的环境中快速的复现。

**安装：**

```sh
# 下载docker-compose 二进制文件
curl -L https://get.daocloud.io/docker/compose/releases/download/1.25.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
# 授予执行权限
chmod +x /usr/local/bin/docker-compose
# 查看是否成功安装
docker-compose --help
```

【黑魔法】下载地址：`https://github.com/docker/compose/releases`，可以访问外网的环境，在线安装省心

**卸载：**

```sh
rm /usr/local/bin/docker-compose
```

**命令：**

1、Docker compose 的使用非常类似于 docker 命令的使用，但是需要注意的是**大部分的 compose 命令都需要到 docker-compose.yml 文件所在的目录下才能执行**。

2、compose 以守护进程模式运行加 `-d` 选项。服务状态：Up

```sh
docker-compose up -d
docker-compose -f docker-compose.yml up -d
```

3、查看有哪些服务，使用 `docker-compose ps` 命令，非常类似于 docker 的 ps 命令。

4、查看 compose 日志

```sh
docker-compose logs web
docker-compose logs redis
```

5、停止 compose 服务。服务状态：Exit 0（所有关联的活动容器也被停止）

```sh
docker-compose stop
docker-compose ps
```

6、重启 compose 服务

```sh
docker-compose restart
docker-compose ps
```

7、kill compose 服务。服务状态：Exit 137

```sh
docker-compose kill
docker-compose ps
```

8、删除 compose 服务（删除所有已停止的关联容器）

```sh
docker-compose rm
```

9、帮助命令

```sh
docker-compose --help
```

>注意：yaml文件里不能有tab，只能有空格。关于 version 与 Docker 版本的关系如下：

| Compose file format | Docker engine |
| ------------------- | ------------- |
| 1                   | 1.9.0+        |
| 2.0                 | 1.10.0+       |
| 2.1                 | 1.12.0+       |
| 2.2, 3.0, 3.1, 3.2  | 1.13.0+       |
| 2.3, 3.3, 3.4, 3.5  | 17.06.0+      |
| 2.4                 | 17.12.0+      |
| 3.6                 | 18.02.0+      |
| 3.7                 | 18.06.0+      |

docker-compose 源码实例

```yaml
# docker-Compose的版本
version: '3'

# 建立2个service 一个wordpress 一个 mysql
services:

  wordpress:
    image: wordpress
    ports:  # 端口映射 80映射到8080端口
      - 8080:80
    environment:  # 环境变量2个
      WORDPRESS_DB_HOST: mysql
      WORDPRESS_DB_PASSWORD: root
    networks:
      - my-bridge

  mysql:
    image: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: wordpress
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - my-bridge

# 建立一个volumes 
volumes:
  mysql-data:
# 建立一个networks
networks:
  my-bridge:
    driver: bridge
```

services:

1. 一个 service 代表一个 container，这个 container 可以从 docker hub 的 image 来创建，也可以从本地的 Dockerfile build 出来的 image 来创建。

2. service 的启动类似 docker run，我们可以给其指定 network 和 volume，所以可以给 service 指定 network 和 volume 的引用

源码地址：[https://github.com/limingios/docker ](https://github.com/limingios/docker )中的No.4

**水平扩展和负载均衡**

原文：[https://idig8.com/2018/07/29/docker-zhongji-40/](https://idig8.com/2018/07/29/docker-zhongji-40/)

**scale** 命令的使用

设置为一个服务启动的容器数量，数量是以这样的参数形式指定的：service=num

```sh
docker-compose up --scale web=3 -d
```

如果报错，请修改 docker-compose.yml 配置文件，将 web 对应的端口映射去掉！示例：

```sh
# -d后台运行
sudo docker-compose up -d
# 启动了2个容器，1个web，1个resdis
sudo docker-compose ps
# 水平扩展给web的容器增加到3个
sudo docker-compose up --scale web=3 -d
# 启动了4个容器，3个web，1个resdis
sudo docker-compose ps
```

但是问题来了没有暴露到外边的端口都是5000内部端口，所以出来了一个命令负载均衡工具：**haproxy**

**参考：**

- 官网：[https://docs.docker.com/compose/compose-file/](https://docs.docker.com/compose/compose-file/)
- 简介：[https://idig8.com/2018/07/27/docker-chuji-12/](#https://idig8.com/2018/07/27/docker-chuji-12/)

**示例：**

场景：redis容器，tomcat容器，nginx容器，mysql容器，这4个容器的启动是有顺序性的，docker compose就是为了组合启动的，而不是手动来启动。（本例子讲的是2个容器，redis和web）

准备环境：

1） 创建测试项目文件夹

```sh
mkdir composetest
cd composetest
```

默认python2.7在centos都安装了，就用python来演示，演示之前需要安装pip工具，类似java的maven管理python模块的工具

```sh
#这个软件包会自动配置yum的软件仓库。
yum install -y epel-release
yum install -y python-pip
```

当然你也可以不安装epel这个包，自己配置软件仓库也是一样的，自己手工添加软件仓库配置文件：

```sh
vi /etc/yum.repos.d/epel.repo

[epel]
name=epel
mirrorlist=http://mirrors.fedoraproject.org/mirrorlist?repo=epel-$releasever&arch=$basearch
enabled=1
gpgcheck=0
```

添加完毕之后：`yum clean all && yum update`

最后终极大发——有pptpd的yum源

```sh
rpm -Uvh http://poptop.sourceforge.net/yum/stable/rhel6/pptp-release-current.noarch.rpm
```

2） 编辑app.py并保存

（描述：简单的一个httpserver，主要是为了类似tomcat的一个sevlet，当访问一次，redis节点就增加一个，就可以看到相应的输出）

```py
from flask import Flask
from redis import Redis
app = Flask(__name__)
redis = Redis(host='redis', port=6379)
@app.route('/')
def hello():
  redis.incr('hits')
  return 'Hello World! I have been seen %s times.' %redis.get('hits')
if __name__ == "__main__":
  app.run(host="192.168.101.13", debug=True)
```

3） 在项目目录创建requirements.txt并保存

命令：

```sh
vi requirements.txt
```

内容：

```txt
flask
redis
```

利用Dockerfile创建docker镜像（重头戏）

命令：

```sh
vi Dockerfile
```

内容：

```sh
FROM python:2.7
ADD . /code
WORKDIR /code
RUN pip install -r requirements.txt
CMD python app.py
```

命令：

```sh
docker build -t web .
```

定义服务

创建docker-compose.yml文件，Compose文件定义了2个服务，web和redis。

Web服务：

1. 从当前目录下的dockerfile创建
2. 容器的5000端口与宿主机5000端口绑定
3. 将项目目录与容器内的/code目录绑定
4. web服务与redis服务建立连接

命令：

```sh
vi docker-compose.yml
```

内容（实践时要将注释内容删除）：

```yml
version: '2'        -- version版本
services:           -- services服务
  web:              -- web服务名称
    build: .        -- build当前目录
    ports:
      - "5000:5800" -- ports映射的端口
    volumes:
      - .:/code     -- 挂载
    depends_on:     -- 前置服务redis
      - redis
  redis:            -- redis依赖的镜像
    image: redis   -- 不能用tab，必须用空格
```

通过compose运行app服务

```sh
docker-compose up
```

备注：

```sh
docker-compose up -d  # 后台启动
docker-compose stop  # 停止运行
```

Compose命令集：[https://docs.docker.com/compose/reference/](#https://docs.docker.com/compose/reference/)

**练习：**

配置文件：[docker-compose.yml](./Codes/docker-compose.yml)

初始化：`docker swarm init`

运行：

```sh
docker stack deploy -c docker-compose.yml getstartedlab #应用程序命名为getstartedlab
```

查看应用程序服务：`docker service ls` 或者 `docker stack services getstartedlab`

在服务中运行的单个容器称为任务，任务被赋予以数字递增的唯一ID。列出您的服务任务：`docker service ps getstartedlab_web`

如果您只列出系统上的所有容器，则任务也会显示，但不会被服务过滤：`docker container ls -q`

您可以 `curl -4 http://localhost:4000` 连续多次运行，或者在浏览器中转到该URL并点击刷新几次。无论哪种方式，容器ID都会发生变化，从而证明负载均衡；对于每个请求，以循环方式选择5个任务中的一个来响应

您可以通过更改docker-compose.yml，保存并重新运行 `docker stack deploy` 命令来扩展应用程序，Docker执行实时更新，无需首先删除应用或杀死任何容器。

Take the app down：`docker stack rm getstartedlab`  
Take down the swarm：`docker swarm leave --force`

基本命令：

```sh
docker stack ls  # List stacks or apps
docker stack deploy -c <composefile> <appname>  # Run the specified Compose file
docker service ls  # List running services associated with an app
docker service ps <service>  # List tasks associated with an app
docker inspect <task or container>  # Inspect task or container
docker container ls -q # List container IDs
docker stack rm <appname> # Tear down an application
docker swarm leave --force # Take down a single node swarm from the manager
```

【集群】准备工作：

```sh
# 下载Oracle VirtualBox
wget http1s://download.virtualbox.org/virtualbox/6.0.6/VirtualBox-6.0-6.0.6_130049_el7-1.x86_64.rpm

# 安装VirtualBox
yum install VirtualBox-6.0-6.0.6_130049_el7-1.x86_64.rpm

# 安装docker-machine
base=https://github.com/docker/machine/releases/download/v0.16.1 && curl -L $base/docker-machine-$(uname -s)-$(uname -m) >/tmp/docker-machine && sudo install /tmp/docker-machine /usr/local/bin/docker-machine

# 通过显示机器版本来检查安装：
docker-machine version
```

Machine资源库提供了几个bash脚本，可添加以下功能：

- 命令完成
- 一个在shell提示符下显示活动计算机的函数
- 一个函数包装器，它添加一个docker-machine use子命令来切换活动机器

确认版本并将脚本保存到 /etc/bash_completion.d 或 /usr/local/etc/bash_completion.d：

```bash
base=https://raw.githubusercontent.com/docker/machine/v0.14.0
for i in docker-machine-prompt.bash docker-machine-wrapper.bash docker-machine.bash
do
  sudo wget "$base/contrib/completion/bash/${i}" -P /etc/bash_completion.d
done
```

然后，您需要 `source /etc/bash_completion.d/docker-machine-prompt.bash` 在bash终端中运行，告诉您的设置，它可以找到docker-machine-prompt.bash您之前下载的文件。

要启用 docker-machineshell 提示，请添加 $(__docker_machine_ps1) 到您的PS1设置中~/.bashrc。

```sh
PS1='[\u@\h \W$(__docker_machine_ps1)]\$ '
```

可以在每个脚本顶部的[注释](https://github.com/docker/machine/tree/master/contrib/completion/bash)中找到其他文档。

如何卸载Docker Machine？

- （可选）删除您创建的计算机。

要单独删除每台机器： `docker-machine rm <machine-name>`

要删除所有计算机：(docker-machine rm -f $(docker-machine ls -q) 您可能需要 -force 在 Windows 上使用）。

删除计算机是一个可选步骤，因为在某些情况下，您可能希望将现有计算机保存并迁移到 Docker for Mac 或 Docker for Windows 环境。

- 删除可执行文件：`rm $(which docker-machine)`

注意：作为信息点config.json，与创建的每个虚拟机相关的证书和其他数据docker-machine 存储在 ~/.docker/machine/machines/Mac 和 Linux 以及 ~\.docker\machine\machines\Windows 上。

我们建议您不要直接编辑或删除这些文件，因为这只会影响 Docker CLI 的信息，而不会影响实际的VM，无论它们是本地还是远程服务器。

继续示例：

使用VirtualBox驱动程序创建2个VM

```sh
docker-machine create --driver virtualbox myvm1
docker-machine create --driver virtualbox myvm2
```

如果报错：`yum -y install kernel-devel-3.10.0-862.el7.x86_64`



### Portainer

好用的图形化管理界面：

```txt
url:  http://10.120.68.12:9000/
usr:  admin
pwd:  Cxf5609757
```

安装：

方式1：直接启动

```sh
docker run -d --name portainer1 \
  --restart=always \
  -p 9000:9000 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  portainer/portainer
```

方式2：docker-compose.yml（默认会建设新网段）

```yaml
version: '3.7'

services:
  portainer:
    container_name: portainer1
    image: portainer/portainer
    restart: always
    ports:
      - "9000:9000"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    networks:
      default:
        ipv4_address: 172.18.0.2

networks:
  default:
    external:
      name: bridge
  extnetwork:
    ipam:
      config:
      - subnet: 172.18.0.0/24
```



## 负载均衡

**1. 怎么保证不挂？**

- DNS多IP
- keepalived

**2. 实战**

参考：

1. https://www.cnblogs.com/zhhx/p/12656813.html
2. https://blog.csdn.net/qq_39565646/article/details/99516237

**1）下载centos7.6基础镜像**

```sh
docker pull centos:7.6
```

**2）在centos7.6中安装keepalived**

```sh
yum install iproute
yum install net-tools
yum install keepalived
```

**3）Keepalived检测nginx的脚本**

/etc/keepalived 中 新建 check_nginx.sh

脚本如下：

```sh
#!/bin/bash
A=$(ps -ef  | grep nginx: | grep -v  grep |  wc  -l)
if [ $A -eq 0 ];then
  nginx
  echo  "restart nginx, sleep 2 s"
  sleep  2
  num=$(ps -ef  | grep  nginx:  |  grep -v grep | wc  -l)
  if [ $num -eq  0  ];then
    ps -ef | grep keepalived  |  grep  -v grep  | awk '{print $2}'| xargs kill -9
    echo  "start nginx failed,kill keepalived"
  fi
else
  echo  "nginx not dead"
fi
```

> **注意：**
>
> 1. grep nginx: 冒号，因为执行的时候脚本的名字中也有nginx，会导致计算出来的数量不对，所以要用nginx:
> 2. check_nginx.sh必须要加权限，否则 不会执行

```sh
chmod +x check_nginx.sh
# 查看
ls -l
ll
```

**4）Keepalived配置**

修改 `/etc/keepalived` 中配置文件

注释`vrrp_strict`否则会导致VIP无法访问

添加VIP，注意 VIP 和本机IP在 同一个 网段内，否则也无法访问 

本机172.22.0.2/16 虚IP设置 为172.22.0.4/16

```sh
! Configuration File for keepalived

global_defs {
   notification_email {
     acassen@firewall.loc
     failover@firewall.loc
     sysadmin@firewall.loc
   }
   notification_email_from Alexandre.Cassen@firewall.loc
   smtp_server 192.168.200.1
   smtp_connect_timeout 30
   router_id LVS_DEVEL
   vrrp_skip_check_adv_addr
   # vrrp_strict  # 注释,否则会导致VIP无法访问
   vrrp_garp_interval 0
   vrrp_gna_interval 0
   # 添加运行健康检查脚本的用户或者组
   # 解决错误：default user 'keepalived_script' for script execution does not exist...
   script_user root
   enable_script_security
}

# 增加检测脚本
vrrp_script check_nginx {
    script "etc/keepalived/check_nginx.sh"
    interval 2
    weight -5
    fall 3
    rise 2
}

vrrp_instance VI_1 {
    state MASTER
    interface eth0
    virtual_router_id 51
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        172.22.0.4/16  # 虚IP设置（VIP必须和本机在同一个网段）
    }
    track_script {
        check_nginx  # 检测脚本
    }
}

...
```

**5）使用yum安装nginx**

```sh
yum install yum-utils
cd /etc/yum.repos.d/
vim nginx.repo
########################################################################
[nginx-stable]
name=nginx stable repo
baseurl=http://nginx.org/packages/centos/$releasever/$basearch/
gpgcheck=1
enabled=1
gpgkey=https://nginx.org/keys/nginx_signing.key

[nginx-mainline]
name=nginx mainline repo
baseurl=http://nginx.org/packages/mainline/centos/$releasever/$basearch/
gpgcheck=1
enabled=0
gpgkey=https://nginx.org/keys/nginx_signing.key
########################################################################
yum install nginx
# 启动
nginx
# 查看是否成功
curl localhost:80
```

**6）制作镜像**

```sh
docker commit {ContainId} keepavled_nginx:v1

# 导出导入、权限控制
docker save -o kn.tar keepavled_nginx:v1
mv kn.tar /data/sftp/mysftp/upload/kn.tar
chown -R sftp:sftp kn.tar

# tag更新
docker tag keepavled_nginx:v2 keepavled_nginx:v1
# 删除tag（镜像）
docker rmi keepavled_nginx:v2
```

**7）启动主备keepalived容器**

```sh
# 创建网络
docker network create --driver bridge keep
# 启动容器
docker run -d --net keep --privileged --name keepalived_master keepavled_nginx:v1 /usr/sbin/init
docker run -d --net keep --privileged --name keepalived_salve keepavled_nginx:v1 /usr/sbin/init
```

**8）验证keepalived**

```sh
# 备keepalived修改配置
vrrp_instance VI_1 {
    state BACKUP  # 状态为备，与主区分
    interface eth0
    virtual_router_id 51
    priority 90  # 优先级比主低
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        172.22.0.4/16  # 虚IP设置
    }
    track_script {
        check_nginx  # 检测脚本
    }
}

# 启动主keepalived
systemctl start keepalived
# 查看启动状态
systemctl status keepalived
# 查看绑定ip，发现虚拟IP已经绑定
ip a

# 错误：IPVS: Can't initialize ipvs: Protocol not available
# 解决：
#   在客户机执行：
#   1. lsmod | grep ip_vs  查看IP模块是否加载
#   2. 无，则执行 modprobe ip_vs; modprobe ip_vs_wrr;
#   3. 再用 lsmod | grep ip_vs 查看。
#   4. 还是没有，yum install ipvsadm，重复以上步骤
#   5. 开机自动加载IPVS模块：/etc/rc.local 中加入 modprobe ip_vs; modprobe ip_vs_wrr;

# 启动备keepalived，发现虚拟IP没有绑定
# Kill掉主的keepalived进程后，主不再绑定VIP
ps -ef | grep keepalive
kill -9 tid
# systemctl stop keepalived
# 重新查看备，发现绑定了VIP
# 再启动主keepalived，会发现VIP重新绑定在主keepalived服务器上
```

**9）验证nginx**

```sh
# Kill nginx，再次查看会发现nginx被重新启动，因为keepalived检测脚本会自动检查，自动重启，
# 启动不成功则kill keepalived。见脚本check_nginx.sh
ps -ef | grep nginx

# 先分别修改主备nginx的页面并重启nginx
vi /usr/share/nginx/html/index.html
# 客户机访问nginx，此时访问的是主服务器:
curl 172.22.0.4:80
# 把主服务器的keepalived kill掉，此时VIP绑定在备服务器，访问的是备服务器的nginx
```



## 服务注册中心

创建新网段：

```sh
docker network create --driver bridge lead_department_photovoltaic
```

启动Consul：

```sh
docker run -d \
-p 8300-8302:8300-8302 \
-p 8500:8500 \
-p 8600:8600 \
-h node1 \
--name consul \
--net lead_department_photovoltaic \
--restart=always \
consul agent \
-server \
-bootstrap-expect=1 \
-node=node1 \
-rejoin \
-client 0.0.0.0 \
-advertise 192.168.99.100 \
-ui \
-data-dir /consul/data \
-config-dir /consul/config
```

精简命令

```sh
docker run -d \
-p 8501:8500 \
-p 8601:8600 \
--name department_photovoltaic_consul \
--net lead_department_photovoltaic \
--restart=always \
consul agent \
-server \
-bootstrap-expect=1 \
-node=department_photovoltaic_consul \
-rejoin \
-client 0.0.0.0 \
-ui
```



## API网关

```json
{
  "ReRoutes": [
    {
      "DownstreamPathTemplate": "/api/values",
      "DownstreamScheme": "http",
      "UpstreamPathTemplate": "/api/values",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "ServiceName": "LeadChina.JwtServer",
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      }
    },
    {
      "DownstreamPathTemplate": "/auth/{url}",
      "DownstreamScheme": "http",
      "UpstreamPathTemplate": "/auth/{url}",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "ServiceName": "LeadChina.JwtServer",
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 5000
      }
    },
    {
      "DownstreamPathTemplate": "/api/base/values",
      "DownstreamScheme": "http",
      "UpstreamPathTemplate": "/api/base/values",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "ServiceName": "LeadChina.Base",
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 5000
      }
    },
    {
      "DownstreamPathTemplate": "/api/base/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/base/{url}",
      "ServiceName": "LeadChina.Base",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/report/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/report/{url}",
      "ServiceName": "LeadChina.PM.Report",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
	  {
      "DownstreamPathTemplate": "/api/msg/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/msg/{url}",
      "ServiceName": "LeadChina.PM.Message",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/doc/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/doc/{url}",
      "ServiceName": "LeadChina.PM.Document",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/task/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/task/{url}",
      "ServiceName": "LeadChina.PM.Task",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/proj/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/proj/{url}",
      "ServiceName": "LeadChina.PM.Project",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/syssetting/values",
      "DownstreamScheme": "http",
      "UpstreamPathTemplate": "/api/syssetting/values",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "ServiceName": "LeadChina.PM.SysSetting",
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 5000
      }
    },
    {
      "DownstreamPathTemplate": "/api/syssetting/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/syssetting/{url}",
      "ServiceName": "LeadChina.PM.SysSetting",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 5000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/suggestion/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/suggestion/{url}",
      "ServiceName": "LeadChina.PM.Suggestion",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 20000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/file/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/file/{url}",
      "ServiceName": "LeadChina.Upload",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 20000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/upload/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
       "DownstreamHostAndPorts": [
              {
                  "Host": "10.30.100.102",
                  "Port": 8170,
              }
          ],
      "UpstreamPathTemplate": "/upload/{url}",
      "UpstreamHttpMethod": [ "Get" ],
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      }
    },
    {
      "DownstreamPathTemplate": "/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
       "DownstreamHostAndPorts": [
              {
                  "Host": "10.30.100.102",
                  "Port": 7080,
              }
          ],
      "UpstreamPathTemplate": "/update/{url}",
      "UpstreamHttpMethod": [ "Get" ],
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      }
    },
    {
      "DownstreamPathTemplate": "/api/performance/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/performance/{url}",
      "ServiceName": "LeadChina.PM.Performance",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      
    },
    {
      "DownstreamPathTemplate": "/api/read/file/owadownload?{url}",
      "DownstreamScheme": "http",
      "UpstreamPathTemplate": "/api/read/file/owadownload?{url}",
       "DownstreamHostAndPorts": [
          {
              "Host": "10.30.100.102",
              "Port": 8171
          }
      ],
      "UpstreamHttpMethod": [ "Get" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      }
    },
    {
      "DownstreamPathTemplate": "/api/read/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/read/{url}",
      "ServiceName": "LeadChina.Readfile",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/attend/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/attend/{url}",
      "ServiceName": "LeadChina.Attence",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 20000
      },
      "AuthenticationOptions": {
        "AuthenticationProviderKey": "Bearer",
        "AllowedScopes": []
      }
    },
    {
      "DownstreamPathTemplate": "/api/general/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/general/{url}",
      "ServiceName": "LeadChina.General",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 20000
      }
    },
    {
      "DownstreamPathTemplate": "/examples/decision/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
       "DownstreamHostAndPorts": [
              {
                  "Host": "10.30.100.102",
                  "Port": 6101,
              }
          ],
      "UpstreamPathTemplate": "/examples/decision/{url}",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put"  ],
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 60000
      }
    },
        {
      "DownstreamPathTemplate": "/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": false,
       "DownstreamHostAndPorts": [
              {
                  "Host": "10.30.100.103",
                  "Port": 16080,
              }
          ],
      "UpstreamPathTemplate": "/department/machinery/{url}",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put"  ],
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 10000
      }
    },
    {
      "DownstreamPathTemplate": "/api/dispatch/{url}",
      "DownstreamScheme": "http",
      "UseServiceDiscovery": true,
      "UpstreamPathTemplate": "/api/dispatch/{url}",
      "ServiceName": "LeadChina.Dispatch",
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ],
      "LoadBalancerOptions": {
        "Type": "LeastConnection"
      },
      "QoSOptions": {
        "ExceptionsAllowedBeforeBreaking": 3,
        "DurationOfBreak": 10,
        "TimeoutValue": 20000
      }
    },
    {
      "DownstreamPathTemplate": "/op/{url}",
      "DownstreamScheme": "http",
      "UpstreamPathTemplate": "/op/{url}",
             "DownstreamHostAndPorts": [
              {
                  "Host": "10.30.100.106",
                  "Port": 80,
              }
          ],
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ]
    },
	  {
      "DownstreamPathTemplate": "/wv/{url}",
      "DownstreamScheme": "http",
      "UpstreamPathTemplate": "/wv/{url}",
             "DownstreamHostAndPorts": [
              {
                  "Host": "10.30.100.106",
                  "Port": 80,
              }
          ],
      "UpstreamHttpMethod": [ "Get", "Post", "Delete", "Put" ]
    }
  ],
  "GlobalConfiguration": {
    "RequestIdKey": "ot-traceid",
    "BaseUrl": "http://172.17.0.5:6080",
    "ServiceDiscoveryProvider": {
      "Host": "172.17.0.4",
      "Port": 8500,
      "Type": "Consul"
    }
  }
}
```



## 实战

部署环境：

```txt
服务器：   CentOS 7
上传工具： SFTP
运行环境： Docker
```



### 部署前准备

1、docker 时区

```sh
# 共享主机的localtime
docker run --name <name> -v /etc/localtime:/etc/localtime:ro ....
# 复制主机的localtime
docker cp /etc/localtime [容器ID或者NAME]:/etc/localtime
# 示例，message是容器名
docker cp ../usr/share/zoneinfo/Asia/Shanghai message:/etc/localtime
# 完成后，通过date命令进行查看当前时间
# 但是，在容器中运行的程序的时间不一定能更新过来，比如在容器运行的MySQL服务，这时候必须要重启mysql服务或者重启Docker容器
```

2、查看 docker 启动命令：

```sh
# 外部
docker inspect
docker inspect container
# 内部
ps -fe
```



### 压缩备份

```sh
# 安装工具
yum install -y zip unzip
# 压缩文件
zip -r xxx.zip xxx/

# dir2不存在
cp -r dir1 dir2
# dir2存在
cp -r dir1/. dir2

# 示例
cp -r setting setting1
cp -r base base1
cp -r project project1
cp -r task task1
cp -r message message1
cp -r suggestion suggestion1
cp -r document document1
cp -r attend attend1

# 授权sftp
chown -R sftp:sftp /data/sftp/mysftp/upload/setting1
chmod -R 755 /data/sftp/mysftp/upload/setting1
chown -R sftp:sftp /data/sftp/mysftp/upload/base1
chmod -R 755 /data/sftp/mysftp/upload/base1
chown -R sftp:sftp /data/sftp/mysftp/upload/project1
chmod -R 755 /data/sftp/mysftp/upload/project1
chown -R sftp:sftp /data/sftp/mysftp/upload/task1
chmod -R 755 /data/sftp/mysftp/upload/task1
chown -R sftp:sftp /data/sftp/mysftp/upload/message1
chmod -R 755 /data/sftp/mysftp/upload/message1
chown -R sftp:sftp /data/sftp/mysftp/upload/suggestion1
chmod -R 755 /data/sftp/mysftp/upload/suggestion1
chown -R sftp:sftp /data/sftp/mysftp/upload/document1
chmod -R 755 /data/sftp/mysftp/upload/document1
chown -R sftp:sftp /data/sftp/mysftp/upload/attend1
chmod -R 755 /data/sftp/mysftp/upload/attend1
# 移动
cd /data/sftp/mysftp/upload
mkdir backup
# 移动时顺带修改目录名
mv /data/sftp/mysftp/upload/setting2 /data/sftp/mysftp/upload/backup/setting
mv /data/sftp/mysftp/upload/base2 /data/sftp/mysftp/upload/backup/base
mv /data/sftp/mysftp/upload/project2 /data/sftp/mysftp/upload/backup/project
mv /data/sftp/mysftp/upload/task2 /data/sftp/mysftp/upload/backup/task
mv /data/sftp/mysftp/upload/message2 /data/sftp/mysftp/upload/backup/message
mv /data/sftp/mysftp/upload/suggestion2 /data/sftp/mysftp/upload/backup/suggestion
mv /data/sftp/mysftp/upload/document2 /data/sftp/mysftp/upload/backup/document
mv /data/sftp/mysftp/upload/attend2 /data/sftp/mysftp/upload/backup/attend
# 打包
zip -r backup.zip backup/
# 删除原文件
rm -rf /data/sftp/mysftp/upload/backup
# 压缩文件移动到父目录（最后没有/）
mv ./backup20210209.zip /data/sftp/mysftp
```



### 镜像生成

java应用服务器：tomcat

```Dockerfile
# 拉取 tomcat 镜像
FROM tomcat
ENV CATALINA_HOME /usr/local/tomcat

# 设置时区
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
  && echo 'Asia/Shanghai' >/etc/timezone \
```

前端：nginx

```dockerfile
# 拉取 nginx 镜像
FROM nginx:alpine
WORKDIR /app
# 从客户机复制到容器中
COPY nginx.conf /etc/nginx/nginx.conf

# 设置时区
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
  && echo 'Asia/Shanghai' >/etc/timezone \
```

网关：

```Dockerfile
# 拉取 .net core 2.1 镜像
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
# 工作目录
WORKDIR /app
# COPY . .
# 开放端口
# EXPOSE 80
# EXPOSE 443
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
# ENV TZ=Asia/Shanghai
# RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.Gateway.dll"]
```

认证服务器：

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.JwtServer.dll"]
```

系统设置微服务：

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.BasicData.API.dll"]
```

基础数据微服务：

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.Base.API.dll"]
```

项目微服务：

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.PM.Project.API.dll"]
```

任务微服务：

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.PM.Task.API.dll"]
```

消息微服务：

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.PM.Message.API.dll"]
```

文档微服务：

```Dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.PM.Document.API.dll"]
```

生成镜像：

```sh
# 前端
cd /data/sftp/mysftp/upload/pmweb/
docker build -t pmweb .

# 网关
cd /data/sftp/mysftp/upload/gateway/
docker build -t gateway .

# 认证服务器
cd /data/sftp/mysftp/upload/JwtServer/
docker build -t jwtserver .

# 系统设置微服务
cd /data/sftp/mysftp/upload/setting/SysSetting/
docker build -t pmsetting .

# 基础数据微服务
cd /data/sftp/mysftp/upload/base
docker build -t basic .

# 项目微服务
cd /data/sftp/mysftp/upload/project
docker build -t project .

# 任务微服务
cd /data/sftp/mysftp/upload/task
docker build -t task .

# 消息微服务
cd /data/sftp/mysftp/upload/message
docker build -t message .

# 文档微服务
cd /data/sftp/mysftp/upload/document
docker build -t document .

# 报表微服务
cd /data/sftp/mysftp/pv_upload/Report
docker build -t pv-report .
```

### 部署前端应用

```sh
# 启动前端
docker run -d -p 6100:6100 -v /data/sftp/mysftp/upload/pmweb/:/app --name pmweb pmweb
```

### 部署后端微服务

获取当前容器的IP启动服务

```c#
public class Program
{
    public static void Main(string[] args)
    {
        var configurationBuilder = new ConfigurationBuilder()
            .SetBasePath(Directory.GetCurrentDirectory())
            .AddJsonFile("appsettings.json", optional: true, reloadOnChange: true)
            .AddJsonFile("appsettings.Development.json", true, false)
            .AddJsonFile("appsettings.Production.json", true, false);

        var hostingconfig = configurationBuilder.Build();
        var url = $"http://{Dns.GetHostEntry(Dns.GetHostName()).AddressList.FirstOrDefault(address => address.AddressFamily == AddressFamily.InterNetwork)?.ToString()}";
        Console.WriteLine($"服务URL：{url}");

        IWebHostBuilder builder = new WebHostBuilder();
        builder.ConfigureServices(s => { s.AddSingleton(builder); });
        builder.UseKestrel()
            .UseContentRoot(Directory.GetCurrentDirectory())
            .UseConfiguration(hostingconfig)
            .UseIISIntegration()
            .UseUrls(url)
            .UseStartup<Startup>();
        var host = builder.Build();
        host.Run();
    }
}
```

下面的启动命令中，只有网关需要映射端口了（2021-02-14更新）：

```sh
# 启动网关
docker run -d -p 6082:80 -v /data/sftp/mysftp/pv_upload/gateway/:/app --name department_photovoltaic_gateway --net lead_department_photovoltaic gateway

# 启动认证服务
docker run -d -v /data/sftp/mysftp/pv_upload/JwtServer/:/app --name department_photovoltaic_jwtserver --net lead_department_photovoltaic jwtserver

# 启动系统设置服务
docker run -d -v /data/sftp/mysftp/pv_upload/setting/:/app --name department_photovoltaic_setting --net lead_department_photovoltaic setting

# 启动基础数据服务
docker run -d -v /data/sftp/mysftp/pv_upload/base/:/app --name department_photovoltaic_base --net lead_department_photovoltaic basic

# 启动项目服务
docker run -d -v /data/sftp/mysftp/pv_upload/project/:/app --name department_photovoltaic_project --net lead_department_photovoltaic project

# 启动任务服务
docker run -d -v /data/sftp/mysftp/pv_upload/task/:/app --name department_photovoltaic_task --net lead_department_photovoltaic task

# 启动消息服务
docker run -d -v /data/sftp/mysftp/pv_upload/message/:/app --name department_photovoltaic_message --net lead_department_photovoltaic message

# 启动文档服务
docker run -d -v /data/sftp/mysftp/pv_upload/document/:/app --name department_photovoltaic_document --net lead_department_photovoltaic document

# 启动考勤服务
docker run -d -v /data/sftp/mysftp/pv_upload/attend/:/app --name department_photovoltaic_attend --net lead_department_photovoltaic attend

# 启动绩效管理服务
docker run -d -v /data/sftp/mysftp/pv_upload/performance/:/app --name department_photovoltaic_performance --net lead_department_photovoltaic performance

# 启动报表服务
docker run -d -v /data/sftp/mysftp/pv_upload/Report/:/app --name department_photovoltaic_report --net lead_department_photovoltaic pv-report

# 时区设置
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_gateway:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_jwtserver:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_setting:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_base:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_project:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_task:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_message:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_document:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_attend:/etc/localtime
docker cp ../usr/share/zoneinfo/Asia/Shanghai department_photovoltaic_report:/etc/localtime
```

**注意：**上面的目录大小写是敏感的！



### 外网服务器部署

#### nginx安装

**centos7**

```sh
yum install gcc-c++
yum install -y pcre pcre-devel
yum install -y zlib zlib-devel
yum install -y openssl openssl-devel
# 官网：https://nginx.org/en/download.html
yum install wget
# 当前最新稳定版本是1.18.0
wget -c https://nginx.org/download/nginx-1.18.0.tar.gz
# 解压
tar -zxvf nginx-1.18.0.tar.gz
cd nginx-1.18.0
# 配置
./configure
# 自定义配置（不推荐）
./configure \
--prefix=/usr/local/nginx \
--conf-path=/usr/local/nginx/conf/nginx.conf \
--pid-path=/usr/local/nginx/conf/nginx.pid \
--lock-path=/var/lock/nginx.lock \
--error-log-path=/var/log/nginx/error.log \
--http-log-path=/var/log/nginx/access.log \
--with-http_gzip_static_module \
--http-client-body-temp-path=/var/temp/nginx/client \
--http-proxy-temp-path=/var/temp/nginx/proxy \
--http-fastcgi-temp-path=/var/temp/nginx/fastcgi \
--http-uwsgi-temp-path=/var/temp/nginx/uwsgi \
--http-scgi-temp-path=/var/temp/nginx/scgi
# 编译安装
make
make install
# 查找安装路径：
whereis nginx
# 启动、停止nginx
cd /usr/local/nginx/sbin/
./nginx 
./nginx -s stop
./nginx -s quit
./nginx -s reload
# 80端口被占用:
# 解决办法：1、安装net-tool 包：yum install net-tools
# ./nginx -s quit: 此方式停止步骤是待nginx进程处理任务完毕进行停止。
# ./nginx -s stop: 此方式相当于先查出nginx进程id再使用kill命令强制杀掉进程。
# 查询nginx进程：
ps aux|grep nginx
# 重启 nginx
# 1.先停止再启动（推荐）：
./nginx -s quit
./nginx
# 重新加载配置文件：
./nginx -s reload
# 开机自启动
# 即在rc.local增加启动代码就可以了。
vi /etc/rc.local
# 增加一行 /usr/local/nginx/sbin/nginx
# 设置执行权限：
chmod 755 rc.local
```

容器

```sh
docker search nginx
docker pull nginx:latest
docker run --name gateway -p 80:80 -d nginx
# 进入容器，执行新命令 
docker exec -it gateway bash
# 退出
exit
```

#### nginx配置

**反向代理**

编辑配置文件：

```sh
vim /usr/local/nginx/conf/nginx.conf
```

配置内容：

```nginx
server {
  listen 80;
  server_name ilead.leadchina.cn;  # 外网域名
  location /dq/ {
    proxy_pass http://10.30.100.103:6080/;  # 注意最末位/的作用 
  }
  location /jx/ {
    proxy_pass http://10.30.100.103:6080/department/machinery/;
  }
}
```

**参考：**

- Nginx负载均衡配置实战 http://www.linuxidc.com/Linux/2014-12/110036.htm
- CentOS 6.2实战部署Nginx+MySQL+PHP http://www.linuxidc.com/Linux/2013-09/90020.htm
- 使用Nginx搭建WEB服务器 http://www.linuxidc.com/Linux/2013-09/89768.htm
- 搭建基于Linux6.3+Nginx1.2+PHP5+MySQL5.5的Web服务器全过程 http://www.linuxidc.com/Linux/2013-09/89692.htm
- CentOS 6.3下Nginx性能调优 http://www.linuxidc.com/Linux/2013-09/89656.htm
- CentOS 6.3下配置Nginx加载ngx_pagespeed模块 http://www.linuxidc.com/Linux/2013-09/89657.htm
- CentOS 6.4安装配置Nginx+Pcre+php-fpm http://www.linuxidc.com/Linux/2013-08/88984.htm
- Nginx安装配置使用详细笔记 http://www.linuxidc.com/Linux/2014-07/104499.htm
- Nginx日志过滤 使用ngx_log_if不记录特定日志 http://www.linuxidc.com/Linux/2014-07/104686.htm
- **Nginx 的详细介绍**：[请点这里](http://www.linuxidc.com/Linux/2012-03/56786.htm)
- **Nginx 的下载地址**：[请点这里](http://www.linuxidc.com/down.aspx?id=342)



### 一键部署

```yml
version: '3.7'

services:
  gateway:
    name: gateway
    image: gateway
    privileged: true
    hostname: gateway
    ports:
      - 7080:7080
    volumes:
      - /data/sftp/mysftp/upload/lead/gateway/:/app
    networks:
      default:
        ipv4_address: 172.20.0.3

  auth:
    name: auth
    image: jwtserver
    privileged: true
    hostname: auth
    ports:
      - 7081:7081
    volumes:
      - /data/sftp/mysftp/upload/lead/auth/:/app
    networks:
      default:
        ipv4_address: 172.20.0.4

  setting:
    name: setting
    image: pmsetting
    privileged: true
    hostname: setting
    ports:
      - 7082:7082
    volumes:
      - /data/sftp/mysftp/upload/lead/setting/:/app
    networks:
      default:
        ipv4_address: 172.20.0.5

  base:
    name: base
    image: basic
    privileged: true
    hostname: base
    ports:
      - 7083:7083
    volumes:
      - /data/sftp/mysftp/upload/lead/base/:/app
    networks:
      default:
        ipv4_address: 172.20.0.6

  project:
    name: project
    image: project
    privileged: true
    hostname: project
    ports:
      - 7084:7084
    volumes:
      - /data/sftp/mysftp/upload/lead/project/:/app
    networks:
      default:
        ipv4_address: 172.20.0.7

  task:
    name: task
    image: task
    privileged: true
    hostname: task
    ports:
      - 7085:7085
    volumes:
      - /data/sftp/mysftp/upload/lead/task/:/app
    networks:
      default:
        ipv4_address: 172.20.0.8

  message:
    name: message
    image: message
    privileged: true
    hostname: message
    ports:
      - 7086:7086
    volumes:
      - /data/sftp/mysftp/upload/lead/message/:/app
    networks:
      default:
        ipv4_address: 172.20.0.9

  document:
    name: document
    image: document
    privileged: true
    hostname: document
    ports:
      - 7087:7087
    volumes:
      - /data/sftp/mysftp/upload/lead/document/:/app
    networks:
      default:
        ipv4_address: 172.20.0.10

  suggestion:
    name: suggestion
    image: suggestion
    privileged: true
    hostname: suggestion
    ports:
      - 7088:7088
    volumes:
      - /data/sftp/mysftp/upload/lead/suggestion/:/app
    networks:
      default:
        ipv4_address: 172.20.0.11

  report:
    name: report
    image: report
    privileged: true
    hostname: report
    ports:
      - 7089:7089
    volumes:
      - /data/sftp/mysftp/upload/lead/report/:/app
    networks:
      default:
        ipv4_address: 172.20.0.12

  performance:
    name: performance
    image: performance
    privileged: true
    hostname: performance
    ports:
      - 7090:7090
    volumes:
      - /data/sftp/mysftp/upload/lead/performance/:/app
    networks:
      default:
        ipv4_address: 172.20.0.13

  attend:
    name: attend
    image: attend
    privileged: true
    hostname: attend
    ports:
      - 7091:7091
    volumes:
      - /data/sftp/mysftp/upload/lead/attend/:/app
    networks:
      default:
        ipv4_address: 172.20.0.14

  general:
    name: general
    image: general
    privileged: true
    hostname: general
    ports:
      - 7092:7092
    volumes:
      - /data/sftp/mysftp/upload/lead/general/:/app
    networks:
      default:
        ipv4_address: 172.20.0.15

networks: # 自定义网络
  default:
    external:
      name: lead_dev
```

解释：

- `privileged`：使用该参数，container 内的 root 拥有真正的 root 权限。
  否则，container 内的 root 只是外部的一个普通用户权限。privileged 启动的容器，可以看到很多 host 上的设备，并且可以执行 mount。甚至允许你在 docker 容器中启动 docker 容器。



## 消息队列



### 安装RabbitMQ

```sh
# 查找RabbitMQ镜像
docker search rabbitmq
# 拉取RabbitMQ镜像
docker pull rabbitmq #（镜像未配有控制台）
docker pull rabbitmq:management #（镜像配有控制台）
```

>注意：rabbitmq是官方镜像，该镜像不带控制台。如果要安装带控制台的镜像，需要在拉取镜像时附带tag标签，例如：management。tag标签可以通过[https://hub.docker.com/_/rabbitmq?tab=tags](https://hub.docker.com/_/rabbitmq?tab=tags)来查询。

```sh
# 安装并运行容器
docker run --name rabbitmq -d -p 15672:15672 -p 5672:5672 rabbitmq:management
# 停止容器
docker stop rabbitmq
# 启动容器
docker start rabbitmq
# 重启
docker restart rabbitmq
# 查看进程信息
```

启动容器后，可以浏览器中访问 [http://localhost:15672](http://localhost:15672) 来查看控制台信息。

RabbitMQ默认的用户名：guest，密码：guest



## 分布式日志平台



### ELK



### ExceptionLess

项目地址：https://github.com/exceptionless/Exceptionless

webhook异常实时通知：https://github.com/RabbitTeam/exceptionless-webhooks



## 附录

#### Rancher

一个开源的企业级容器管理平台。通过Rancher，企业再也不必自己使用一系列的开源软件去从头搭建容器服务平台。Rancher提供了在生产环境中使用的管理Docker和Kubernetes的全栈化容器部署与管理平台。

参考：https://rancher.com/docs/rancher/v1.6/zh/



### 前端Vue

获取DOM元素

- 救命稻草，前端框架就是为了减少DOM操作，但是特定情况下，也给你留了后门

- 在指定的元素上，添加 `ref="名称A"`

- 在获取的地方加入 `this.$refs.名称A`  

  - 如果ref放在了原生DOM元素上，获取的数据就是原生DOM对象，可以直接操作
  - 如果ref放在了组件对象上，获取的就是组件对象
  - 获取到DOM对象，通过`this.$refs.sub.$el`，进行操作
  - 对应的事件
    - created 完成了数据的初始化，此时还未生成DOM，无法操作DOM
    - mounted 数据已经装载到了DOM之上，可以操作DOM

- [统一封装 Axios 接口与异常处理](https://blog.csdn.net/qq_40128367/article/details/82735310)

- 实例：

  - 在组件内（xxx.vue）中的this
  - new Vue()
  - 事件
    - this.$on(事件名,回调函数(参数))
    - this.$emit(事件名,数据)
    - this.$once(事件名,回调函数(参数)) 就触发一次
    - this.$off(事件名); 取消事件
  - 实例属性
    - $props,$parent,$children,$refs

- 全局函数

  - Vue.use(param) 安装插件 param需要实现install函数 接受一个Vue，可以在Vue的原型上挂载属性，后期组件内通过this.就可以拿到该数据，在所有组件中使用
  - 单文件 Vue.component(名称,组件对象)
  - 引包 Vue.component(名称,options)
  - Vue.filter(过滤器名,function(value){ return value; } )

- 生僻指令

  - :key 当DOM列表中删除某一个元素 ，更优化的方案是直接删除这一个DOM元素
  - Vue就需要辨识你删除的数组中的元素与DOM中那个元素的对应关系
    - 如果不指定key，vue也会去计算，把对象计算出一个唯一标识，相对损耗性能
    - 我们来通过key告知vue，这个元素的标识就是 obj.id index，可以很好的提升性能
  - v-on:事件  @事件=
  - v-bind:属性 :属性=

- 全局

  - Vue.component('组件名',组件对象)  在哪里都可以使用

- 组件传值

  - 父传子: 属性作为参数
  - 常量 title="xxx"   子组件声明接收参数 props:['xxx']
  - 变量 :title="num"  子组件声明接收参数 props:['xxx']
  - 子传父: vuebus（只能是同一辆车）
  - 先停车到父组件，On一下
  - 再开车到子组件，如果需要的话，emit一下，触发上述时间的回调函数
  - 父子组件之间通信规则不太清楚  
  - 父向子 -> 自定义指令给属性传值  <my-div xxx="{{name}}"
  - 子向父 -> 通过事件触发 -> 只能是同一个对象的事件监听和触发 $emit
  - vue bus 同一辆车在不同的地方使用($on/$emit)

- render: c => c(App)这是啥，babel->语法转换器，转换ES6/7、react  

- options: {presets: ['es2015'], plugins: ['transform-runtime'] }  

- 路由使用

  - 使用步骤

    1. 下载
    2. 引入对象
    3. 安装插件
    4. 创建路由对象配置路由规则
    5. 配置进vue实例对象的options中
    6. 留坑 `<router-view></router-view>`
       1. 去哪里 `<router-link :to="{name:'xxx'}"></router-link>`
       2. 导航 `{name:'xxx', path:'/xxx', component:Home}`
       3. 去了以后干什么

    - 在created函数中，发请求
    - 获取路由参数`this.$route.params|query.xxx;`

  - 套路

    1. 去哪里 `<router-link :to="{name:'bj'}"></router-link>`
    2. 导航（配置路由规则）`{name:'bj',path:'/beijing',组件A}`
    3. 去了干嘛（在组件A内干什么）
       - 在created事件函数中，获取路由参数
       - 发起请求，把数据挂载上去
    4. 参数
       - 查询字符串（#/beijing?id=1&age=2）
         1. 去哪里 `<router-link :to="{name:'bj',query:{id:1,age:2}}"></router-link>`
         2. 导航（配置路由规则） `{name:'bj',path:'/beijing',组件A}`
         3. 去了干嘛（在组件A内干什么）  
            `this.$route.query.id||age`
       - path(#/beijing/1/2)
         1. 去哪里 `<router-link :to="{name:'bj',params:{id:1,age:2}}"></router-link>`
         2. 导航（配置路由规则） `{name:'bj',path:'/beijing/:id/:age',组件A}`
         3. 去了干嘛（在组件A内干什么）`this.$route.params.id||age`
    5. 编程导航
       - 一个获取信息的只读对象($route)
       - 一个具备功能函数的对象($router)
       - 根据浏览器历史记录前进和后台 `this.$router.go(1|-1);`
       - 跳转到指定路由 `this.$router.push({name:'bj'});`
    6. 嵌套路由
       - 让变化的视图(router-view)产生包含关系(router-view)
       - 让路由与router-view关联，并且也产生父子关系
    7. 多视图
       - 让视图更为灵活，以前一个一放，现在可以放多个，通过配置可以去修改

- axios:

  - 开始:

    - 跨域 + 默认的头是因为你的数据是对象，所以content-type:application/json
    - 有OPTIONS预检请求（浏览器自动发起）

  - 最终:

    - 当我们调整为字符串数据，引起content-type变为了www键值对
    - 没有那个OPTIONS预检请求

  - 总结：跨域 + application/json 会引起OPTIONS预检请求，并且自定义一个头（提示服务器，这次的content-type较为特殊），content-type的值

  - 服务器认为这个是一次请求，而没有允许content-type的头，

  - 浏览器就认为服务器不一定能处理掉这个特殊的头的数据

  - 抛出异常

  - 在node服务器`response.setHeader("Access-Control-Allow-Headers","content-type,多个");`

  - formdata的样子: key=value&key=value

  - axios属性关系

    - options: headers、baseURL、params
    - 默认全局设置（大家都是这么用）`Axios.defaults-> options对象`
    - 针对个别请求来附加options
    - axios.get(url,options)
    - axios.post(url,data,options)

  - 独立构建：引包的方式

  - 运行时构建：单文件方式

  - 单文件方式引入bootstrap

    ```javascript
    new webpack.ProvidePlugin({
      $: 'jquery',
      jQuery: 'jquery',
      'window.jQuery': 'jquery',
      'window.$': 'jquery',
    }),
    ```

    以上方式是将jquery声明成全局变量。供bootstrap使用

  - 使用代理跨域

    ```javascript
    devServer: {
      proxy: {
        '/v2/*': {
          target: 'https://api.douban.com/',
          changeOrigin: true,
        }
      }
    ```

  - 合并请求

    - axios.all([请求1,请求2])
    - 分发响应  axios.spread(fn)
    - fn:对应参数(res)和请求的顺序一致
    - 应用场景：必须保证两次请求都成功，比如，分头获取省、市的数据
    - 执行特点：只要有一次失败就算失败，否则成功

  - 拦截器

    - 过滤，在每一次请求与响应中、添油加醋
    - axios.interceptors.request.use(fn)  在请求之前
    - function(config){ config.headers = { xxx }}   config 相当于options对象
    - 默认设置 defaults 范围广、权利小
    - 单个请求的设置options get(url,options)  范围小、权利中
    - 拦截器 范围广、权利大

  - token（扩展）

    - cookie 和session的机制，cookie自动带一个字符串
    - cookie只在浏览器
    - 移动端原生应用，也可以使用http协议，1:可以加自定义的头、原生应用没有cookie
    - 对于三端来讲，token可以作为类似cookie的使用，并且可以通用
    - 拦截器可以用在添加token上

  - 拦截器操作loadding

  - 在请求发起前open，在响应回来后close

- 视口

  ```html
  <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
  ```

- 相关环境总结

  - webpack.config.js -> 入口和出口，解决文件的解析loader
  - index.html -> SPA
  - main 程序入口
  - app.vue 主体组件文件
  - components -> 各种功能页面的组件
  - static
    - 全局css
    - img图片
    - vender -> mui

- npm命令

  - npm i(install) 包名 -S(--save)-D(--save-dev) 安装包
  - 全部或者生产恢复包: npm i(install) --production(只恢复生产依赖dependencies)

- yarn命令

  - yarn add||remove 包名 -S(--save)-D(--save-dev) 安装包
  - 全部或者生产恢复包: yarn i(install) --production(只恢复生产依赖dependencies)

- 相关命令

  ```bat
  npm i mint-ui vue-preview axios vue-router monent vue - S;
  npm i webpack html - webpack - plugin css - loader style - loader less less - loader autoprefixer - loader babel - loader babel - core babel - preset - es2015 babel - plugin - transform - runtime url - loader file - loader vue - loader vue - template - compiler webpack-dev-server - D
  ```

## 部署

(1) 绝对路径改成相对路径

![x](./Resource/34.png)

![x](./Resource/35.png)

(2) npm run build

## 多页面

1、创建文件

![x](./Resource/36.png)

2、添加多入口

![x](./Resource/37.png)

3、开发环境修改

![x](./Resource/38.png)

对编译环境进行配置：

![x](./Resource/39.png)

配置生产环境，每个页面都要配置一个chunks，不然会加载所有页面的资源。

![x](./Resource/40.png)

## iview组件表格render函数的使用

如果要在标签中加入属性，例如img中src属性，a标签中href属性。此时要用attrs来加入而不是props。

## 浅谈$mount()

Vue 的 `$mount()` 为手动挂载，在项目中可用于延时挂载（例如在挂载之前要进行一些其他操作、判断等），之后要手动挂载上。new Vue时，el 和 `$mount` 并没有本质上的不同。

顺便附上vue渲染机制流程图：

![x](./Resource/41.png)

## 生成条形码和二维码

### 条形码

1. 命令：`npm install jsbarcode --save`

2. 引入：

   ```html
   <script src="https://www.jq22.com/jquery/vue.min.js"></script>
   <script src='js/JsBarcode.all.min.js'></script>）
   ```

   （安装了依赖可不引入）

3. 声明：

   ```js
   var JsBarcode = require('jsbarcode')
   ```

4. 简单例子：

```html
<svg id="barcode"></svg>

<!-- 在HTML元素中定义值和选项 -->
<svg class="barcode"
     jsbarcode-format="CODE128"
     :jsbarcode-value= obj.id
     jsbarcode-textmargin="0"
     jsbarcode-fontoptions="bold">
</svg>

<script>
JsBarcode("#barcode", "Hi world!");

// 配置
JsBarcode("#barcode", "1234", {
　format: "pharmacode",
　lineColor: "#0aa",
　width: 4,
　height: 40,
　displayValue: false
});

// 在HTML元素中定义值和选项
JsBarcode(".barcode").init();

// 高级
JsBarcode("#barcode")
  .options({font: "OCR-B"}) // 会影响所有条形码
  .EAN13("1234567890128", {fontSize: 18, textMargin: 0})
  .blank(20) // 在条形码之间创建空间
  .EAN5("12345", {height: 85, textPosition: "top", fontSize: 16, marginTop: 15})
  .render();
</script>
```

支持的条形码：

- CODE128
  - CODE128（自动模式切换）
  - CODE128 A / B / C（强制模式）
- EAN
  - EAN-13
  - EAN-8
  - EAN-5
  - EAN-2
  - UPC（A）
  - UPC（E）
- CODE39
- ITF-14
- MSI
  - MSI10
  - MSI11
  - MSI1010
  - MSI1110
- Pharmacode
- Codabar

## 组件重新加载

1. 利用v-if控制router-view，在根组件APP.vue中实现一个刷新方法，这种方法可以实现任意组件的刷新。

   ```html
   <template>
     <router-view v-if="isRouterAlive"/>
   </template>
   <script>
     export default {
       data () {
         return {
           isRouterAlive: true
         }
       },
       methods: {
         reload () {
           this.isRouterAlive = false
           this.$nextTick(() => (this.isRouterAlive = true))
         }
       }
     }
   
     // 然后其它任何想刷新自己的路由页面，都可以这样：
     this.reload()
   </script>
   ```

2. 路由替换

   ```js
   // replace another route (with different component or a dead route) at first
   // 先进入一个空路由
   vm.$router.replace({
     path: '/_empty',
   })
   // then replace your route (with same component)
   vm.$router.replace({
     path: '/student/report',
     query: {
       'paperId':paperId
    }
   })
   ```

## 问题

- 错误：<i style="color:red">无法加载文件 C:\Users\gxf\AppData\Roaming\npm\nodemon.ps1，因为在此系统上禁止运行脚本。</i>

  原因：笔记本禁止运行脚本

  解决方法：

  ```sh
  1.管理员身份打开powerShell
  2.输入 set-ExecutionPolicy RemoteSigned
  3.选择 Y 或者 A，就好了
  ```

- 错误：<i style="color:red">Vue项目启动出现 Error:Cannot find module 'array-includes'</i>

  解决方法：

  ```sh
  1. 删掉项目中的node_modules文件夹，
  2 .执行 npm cache clean 或者  cnpm cache clean 命令清除掉cache缓存，
  3.然后cnpm install 和npm run dev就可以在这台电脑运行你的项目
  ```

FormValidatoe使用时，Prop和Model要同名，只能更改Model属性，不能为了省力将Model对象设置为{}对象，Prop会判断错误！

### 事件总线



**参考：**

- https://www.zhihu.com/question/421925754