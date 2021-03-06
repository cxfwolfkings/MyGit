# 目录

1. 简介
   - [数据管理](#数据管理)
   - [挂载点](#挂载点)
   - [仓库](#仓库)
   - [底层原理](#底层原理)

2. 实战

   - [安装](#安装)
   - [MySQL示例](#MySQL示例)
   - [wordpress示例](#wordpress示例)
   - [用Docker建立一个公用GPU服务器](#用Docker建立一个公用GPU服务器)
   - [编写Dockerfile](#编写Dockerfile)
   - [Portainer管理集群部署](#Portainer管理集群部署)
   - [docker-swarm](#docker-swarm)
   - [docker-machine](#docker-machine)
   - [搭建私有镜像仓库](#搭建私有镜像仓库)
   
3. 总结

   - [常用命令](#常用命令)
     - [容器命令](#容器命令)
   - [常见问题](#常见问题)
   - [Windows容器](#Windows容器)
   - [基于Docker的DevOps方案](#基于Docker的DevOps方案)
   - [容器云平台的构建实践](#容器云平台的构建实践)
   - [Docker生态](#Docker生态)
   - [监控工具](#监控工具)
   - [参考](#参考)

4. 升华



### 数据管理

在容器里直接写入数据是很不好的习惯，那么，如果想在容器里面写入数据，该怎么做呢？

Docker 数据管理主要有两种方式：**数据卷** 和 **数据卷容器**。下面我们会分别展开介绍。

卷：提供一个容器可以使用的可写文件系统。由于映像只可读取，而多数程序需要写入到文件系统，因此卷在容器映像顶部添加了一个可写层，这样程序就可以访问可写文件系统。 程序并不知道它正在访问的是分层文件系统，此文件系统就是往常的文件系统。卷位于主机系统中，由 Docker 管理。

#### 数据卷（Data Volume）

数据卷的使用其实和 Linux 挂载文件目录是很相似的。简单来说，数据卷就是一个可以供容器使用的特殊目录。

- 创建一个数据卷

  在运行 `Docker run` 命令的时候使用 `-v` 参数为容器挂载一个数据卷：

  ```sh
  docker run -ti --name volume1 -v /myDir ubuntu:16.04 bash
  ```

  可以发现我们的容器里面有一个 myDir 的目录，这个目录就是我们所说的数据卷

- 删除一个数据卷

  数据卷是用来持久化数据的，所以数据卷的生命周期独立于容器。所以在容器结束后数据卷并不会被删除，如果你希望删除数据卷，可以在使用 `docker rm` 命令删除容器的时候加上 `-v` 参数。

  值得注意的是，如果你删除挂载某个数据卷的所有容器的同时没有使用 -v 参数清理这些容器挂载的数据卷，你之后再想清理这些数据卷会很麻烦，所以在你确定某个数据卷没有必要存在的时候，在删除最后一个挂载这个数据卷的容器的时候，使用 `-v` 参数删除这个数据卷。

- 挂载一个主机目录作为数据卷

  当然，你也可以挂载一个主机目录到容器，同样是使用 `-v` 参数。

  ```sh
  docker run -ti --name volume2 -v /home/zsc/Music/:/myShare ubuntu:16.04 bash
  ```

  以上指令会把宿主主机的目录 /home/zsc/Music 挂载到容器的 myShare 目录下，然后你可以发现我们容器内的 myShare 目录就会包含宿主主机对应目录下的文件

  Docker 挂载数据卷的默认权限是读写，你可以通过 :ro 指令为只读：

  ```sh
  docker run -ti --name volume2 -v /home/zsc/Music/:/myShare:ro ubuntu:16.04 bash
  ```

  直接挂载宿主主机目录作为数据卷到容器内的方式在测试的时候很有用，你可以在本地目录放置一些程序，用来测试容器工作是否正确。当然，Docker也可以挂载宿主主机的一个文件到容器，但是这会出现很多问题，所以不推荐这样做。如果你要挂载某个文件，最简单的办法就是挂载它的父目录。

#### 数据卷容器（Data Volume Container）

所谓数据卷容器，其实就是一个普通的容器，只不过这个容器专门作为数据卷供其它容器挂载。

首先，在运行 docker run 指令的时候使用 -v 参数创建一个数据卷容器（这和我们之前创建数据卷的指令是一样的）：

```sh
docker run -ti  -d -v /dataVolume --name v0 ubuntu:16.04
```

然后，创建一个新的容器挂载刚才创建的数据卷容器中的数据卷：使用 `--volumes-from` 参数

```sh
docker run -ti --volumes-from v0 --name v1 ubuntu:16.04 bash
```

然后，我们的新容器里就可以看到数据卷容器的数据卷内容

>注意：  
>1、数据卷容器被挂载的时候不必保持运行！  
>2、如果删除了容器 v0 和 v1，数据卷并不会被删除。如果想要删除数据卷，应该在执行 docker rm 命令的时候使用 -v 参数。



### 挂载点

虽然我们已经通过 Linux 的命名空间解决了进程和网络隔离的问题，在 Docker 进程中我们已经没有办法访问宿主机器上的其他进程并且限制了网络的访问，但是 Docker 容器中的进程仍然能够访问或者修改宿主机器上的其他目录，这是我们不希望看到的。

在新的进程中创建隔离的挂载点命名空间需要在 clone 函数中传入 CLONE_NEWNS，这样子进程就能得到父进程挂载点的拷贝，如果不传入这个参数子进程对文件系统的读写都会同步回父进程以及整个主机的文件系统。

如果一个容器需要启动，那么它一定需要提供一个根文件系统(rootfs)，容器需要使用这个文件系统来创建一个新的进程，所有二进制的执行都必须在这个根文件系统中。

![x](./Resources/docker18.png)

想要正常启动一个容器就需要在 rootfs 中挂载以上的几个特定的目录，除了上述的几个目录需要挂载之外我们还需要建立一些符号链接保证系统 IO 不会出现问题。

![x](./Resources/docker19.png)

为了保证当前的容器进程没有办法访问宿主机器上其他目录，我们在这里还需要通过 libcontainer 提供的 pivot_root 或者 chroot 函数改变进程能够访问个文件目录的根节点。

```go
// pivor_root
put_old = mkdir(...);
pivot_root(rootfs, put_old);
chdir("/");
unmount(put_old, MS_DETACH);
rmdir(put_old);

// chroot
mount(rootfs, "/", NULL, MS_MOVE, NULL);
chroot(".");
chdir("/");
```

到这里我们就将容器需要的目录挂载到了容器中，同时也禁止当前的容器进程访问宿主机器上的其他目录，保证了不同文件系统的隔离。

这一部分的内容是作者在 libcontainer 中的[SPEC.md](https://github.com/opencontainers/runc/blob/master/libcontainer/SPEC.md) 文件中找到的，其中包含了 Docker 使用的文件系统的说明，对于 Docker 是否真的使用 **chroot** 来确保当前的进程无法访问宿主机器的目录，作者其实也没有确切的答案，一是 Docker 项目的代码太庞大，不知道该从何入手，作者尝试通过 Google 查找相关的结果，但是既找到了无人回答的[问题](https://forums.docker.com/t/does-the-docker-engine-use-chroot/25429)，也得到了与 SPEC 中的描述有冲突的[答案](https://www.quora.com/Do-Docker-containers-use-a-chroot-environment) ，如果各位读者有明确的答案可以在博客下面留言，非常感谢。



### 仓库

仓库(Repository)是集中存放镜像文件的场所。有时候会把仓库和仓库注册服务器(Registry)混为一谈，并不严格区分。实际上，仓库注册服务器上往往存放着多个仓库，每个仓库中又包含了多个镜像，每个镜像有不同的标签(tag)。

仓库分为公开仓库(Public)和私有仓库(Private)两种形式。最大的公开仓库是 Docker Hub，存放了数量庞大的镜像供用户下载。国内的公开仓库包括时速云、网易云等，可以提供大陆用户更稳定快速的访问。当然，用户也可以在本地网络内创建一个私有仓库。

当用户创建了自己的镜像之后就可以使用 push 命令将它上传到公有或者私有仓库，这样下次在另外一台机器上使用这个镜像时候，只需要从仓库上 pull 下来就可以了。

Docker 仓库的概念跟 Git 类似，注册服务器可以理解为 GitHub 这样的托管服务。

#### Docker Hub

仓库是集中存放镜像的地方。目前Docker官方仓库维护了一个[公共仓库](https://hub.docker.com)，其中已经包括15000多个的镜像。大部分需求都可以通过在Docker Hub中直接下来镜像来实现。

**登录：**

可以通过执行 `docker login` 命令来输入用户名、密码和邮箱来完成注册登录。

**基本操作：**

用户无需登录可以通过 `docker search` 命令来查找官方仓库中的镜像，并利用 `docker pull` 下载到本地，可以通过 `docker push` 命令将本地镜像推送到 docker hub。

先tag一下复制一个镜像，然后把镜像push到服务器上

```sh
docker images
docker tag <ImageID> <ImageName> #复制镜像
docker push <ImageName> #推送到服务器
```

#### 创建和使用私有仓库

**使用registry镜像创建私有仓库：**

可以通过docker官方提供的registry镜像来搭建一套本地私有仓库。镜像地址：[https://hub.docker.com/_/registry/](https://hub.docker.com/_/registry/)

命令：

```sh
docker run -e SEARCH_BACKEND=sqlalchemy -e SQLALCHEMY_INDEX_DATABASE=sqlite:////tmp/docker-registry.db -d –name registry -p 5000:5000 registry
```

- -e设定环境变量
- -d从后台启动的方式启动镜像
- -name 启动的容器名字
- -p 暴露端口，容器内部的5000绑定到宿主机的5000端口上。

**registry镜像本身：**

SEARCH_BACKEND=sqlalchemy默认索引是可以查询的

参考地址：

- [https://github.com/docker/docker-registry#search-engine-options](https://github.com/docker/docker-registry#search-engine-options)
- [https://hub.docker.com/_/registry/](https://hub.docker.com/_/registry/)

自动下载并启动一个registry容器，创建本地的私有仓库服务。默认仓库创建在/tmp/registry目录下。上传到本地的私有仓库中

```sh
docker tag <ImageId> <IP:port/ImageName>
docker push <IP:port/ImageName>
```

报错了：http:server gave HTTP response to HTTPS client 后面会告诉你如何解决往下看。

docker启动参数配置：

- 环境：centos7解决上边的问题

- 配置文件：/lib/systemd/system/docker.service 修改成：

  ```conf
  #ExecStart=/usr/bin/dockerd
  ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock --insecure-registry 192.168.100.146:5000
  ```

  （此处默认2375为主管理端口，unix:///var/run/docker.sock用于本地管理，7654是备用的端口）

  重启服务，在启动一个私有仓库的容器，然后push到私有仓库中

  ```sh
  systemctl daemon-reload && service docker restart
  ps -ef|grep docker
  docker run -e SEARCH_BACKEND=sqlalchemy -e SQLALCHEMY_INDEX_DATABASE=sqlite:////tmp/docker-registry.db -d –name registry -p 5000:5000 registry
  docker push <IP:port/ImageName>
  ```

参考地址：[https://docs.docker.com/engine/admin/configuring/](https://docs.docker.com/engine/admin/configuring/)

#### 仓库加速服务

加速下载官方镜像：

- 推荐服务：[https://dashboard.daocloud.io/](https://dashboard.daocloud.io/)
- 点击加速器：[https://dashboard.daocloud.io/mirror](https://dashboard.daocloud.io/mirror)

配置Docker加速器：

```sh
curl -sSL https://dashboard.daocloud.io/daotools/set_mirror.sh | sh -s http://b8laace9.m.daocloud.io
```

![x](E:/WorkingDir/Office/devops/Resource/配置Docker加速器.jpg)

#### 仓库管理

Registry Web UI 用于镜像的查询，删除。镜像地址：[https://hub.docker.com/r/atcol/docker-registry-ui/](https://hub.docker.com/r/atcol/docker-registry-ui/)

启动命令：运行下面的命令的时候建议先配置上边讲的加速哦，因为要下载的东西有点多。

```sh
docker run -d –name registry_ui -p 8080:8080 -e REG1=http://172.17.0.2:5000/v1/atcol/docker-registry-ui
```

- 查看端口是否启用：`netstat -nlp|grep 8080`
- 查看logs：`docker logs -f registry_ui`
- 访问地址：`http://IP地址:8080`



### 底层原理

本文主要简单讲解 Docker 底层原理，包括控制组，命名空间和分层存储。

**Docker 究竟做了什么？**

为了理解 Docker 帮助我们做了什么，我们先来看看 Linux 内核做了什么。简单来说，Linux 内核做了下面几件事：

- 对来自硬件的消息作出响应；
- 启动和规划程序的运行；
- 控制和组织存储；
- 在程序之间传递消息；
- 分配资源——内存，CPU，网络等；

Docker 做的也是这些事情。

Docker 是一个 Go 语言开发的程序，它利用了 Linux 内核的一些特性，比如控制组，命名空间等技术来为容器提供隔离，让容器看起来就是一个独立的系统。这些技术并不是 Docker 的原创，在 Docker 之前这些技术就已经存在了，不过除非你是 Linux 专家，否则很难完美地使用这些特性。Docker 的出现让这一切变得优雅又简单，你可以很方便地在自己的电脑使用 Docker 部署容器！

本文接下来的内容会比较详细地介绍一下这些 Docker 背后的技术，了解一下原理有助于大家对 Docker 有一个更加深刻的认识。让我们开始吧！

**Docker 的 C/S 模型**

Docker 采用了 C/S 架构，包括客户端（Client）和服务端（Server），服务端通过 socket 接受来自客户端的请求，这些请求可以是创建镜像，运行容器，终止容器等等。

![x](./Resources/docker36.png)

服务端既可以运行在本地主机，也可以运行在远程服务器或者云端，只要你可以访问 Docker 的服务端，你甚至可以在容器里运行容器。现在，我们来看看在 Docker 容器里运行 Docker 容器的例子：

![x](./Resources/docker37.png)

Docker 官方有一个名为"docker"的镜像，使用这个镜像运行容器的话，就可以在容器里运行 Docker 命令。现在，我们让客户端运行在这个容器里面，服务端运行在宿主主机，所以需要把宿主主机的"/var/run/docker.sock"挂载到容器里的"/var/run/docker.sock"：

```sh
docker run --rm -ti -v /var/run/docker.sock:/var/run/docker.sock docker sh
```

然后，我们可以在这个容器里运行 Docker 命令：

```sh
docker run -ti --rm net:v1.0 bash
```

你可以看到，"net:v1.0"本是我们自定义的镜像，存储在宿主主机里，之所以我们在容器里可以从这个镜像运行容器，是因为我们可以访问宿主主机的 Docker 服务端。所以只要我们可以访问宿主主机的 Docker 服务端，我们就可以从服务端存在的镜像运行容器。

总之，只要理解：**Docker是C/S架构**就可以了！

在深入讲解 Docker 网络原理之前，不得不简单提一下网络有关的知识：

- Ethernet（以太网）：通过有线或者无线传递“帧”(frame)
- IP Layer：在局域网内传递数据包
- Routing（路由）：在不同网络之间传递数据包
- Ports（端口）：寻址一台主机的特定程序，这里指的是某些程序监听某些端口

其实在之前学习 Docker 网络操作部分的时候，我们已经介绍过 Docker 网络的一些原理了。Docker 并不是像变魔术一样直接在容器之间传递包，而是运行的时候会自动在宿主主机上创建一个名为 docker0 的虚拟网桥，它就像软件交换机一样，在挂载到它的网口之间进行消息转发。运行一个 Docker 容器时，会创建 veth 对(Virtual Ethernet Pair)接口，这对接口一端在容器内，另一端挂载到 Docker 的网桥（默认 docker0，或者使用-- net 参数指定网络）。veth 总是成对出现，并且从一端进入的数据会从另一端流出，这样就可以实现挂载到同一网桥的容器间通信。

![x](./Resources/docker38.png)

之前在学习 Docker 端口映射的时候，使用 -p 参数将宿主主机的端口映射到容器内部，这个过程用到了 Linux 的防火墙命令 iptable，iptable 会创建映射规则。现在，我们的主机上没有运行任何容器，让我们看看目前的端口映射规则：

```sh
iptables -n -L -t nat
```

![x](./Resources/docker39.png)

现在，运行一个容器，映射宿主机的8080端口：

```sh
docker run --rm -p 8080:8080 -ti net:v1.0 bash
```

现在，再来看看端口映射情况：

![x](./Resources/docker40.png)

可以从最后一行看到我们的映射规则 `tcp dpt:8080 to 172.17.0.2:8080`。

**进程和控制组**

先来简单描述一下 Linux 进程有关知识。

Linux 的进程都是来自一个父进程，所以进程之间是父子关系。当一个子进程结束的时候，会返回一个退出代码给父进程。在众多进程中，有一个进程是特殊的，它就是初始化进程(init)，进程号为0，这个进程负责启动所有其它进程。

使用 Docker 运行容器时，容器启动的时候也有一个初始化进程，当这个进程终止的时候，对应的容器也就终止了。下面以一个具体例子加深理解：

首先，运行一个容器：

```sh
docker run --name process --rm -ti ubuntu:16.04 bash
```

然后，查看容器进程号：

```sh
docker inspect --format '{{.State.Pid}}' process
```

然后使用 `kill <Pid>` 命令，发现容器退出。

并且，Docker 使用 Linux 控制组（cgroup, control group 来对容器进程进行隔离。cgroup 是 Linux 内核的特性之一，它保证所有在一个控制组内的进程组成一个私密的、隔离的空间。控制组内的进程有自己的进程号，并且无法访问所在控制组之外的进程。所以控制组可以把你的系统中的进程划分为若干相互隔离的区域，并且控制组内的父进程衍生的子进程依旧在这个控制组内。Docker 正是利用这个特性实现容器间进程隔离。同时，控制组还提供了资源限制，资源审计等功能，这些在 Docker 里都有所体现。

**分层存储**



## 实战



### MySQL示例

```sh
# 运行命令
docker run --name colin-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 -itd mysql:5.7
# 进入MySQL容器
docker exec -it colin-mysql /bin/bash
# 进入MySQL
mysql -u root -p
```

docker run是启动容器的命令；  

- --name：指定了容器的名称，方便之后进入容器的命令行  
- -itd：其中，i是交互式操作，t是一个终端，d指的是在后台运行  
- -p：指在本地生成一个随机端口，用来映射mysql的3306端口  
- -e：设置环境变量 `MYSQL_ROOT_PASSWORD=emc123123`：指定了mysql的root密码  
- mysql：指运行mysql镜像

**进行配置，使外部工具可以连接：**

```sql
-- 设置root帐号的密码：
update user set authentication_string = password('1234') where user = 'root';
-- 接着，由于mysql中root执行绑定在了localhost，因此需要对root进行授权
grant all privileges on *.* to 'root'@'%' identified by '1234' with grant option;
flush privileges;
```



### wordpress示例

```sh
# 1. 准备目录
mkdir myblog && cd myblog
# 2. 编辑文件
vi docker-compose.yml
----------------------------------------------------------
version: '2'
services:
   db:
     image: mysql:5.7
     volumes:
       - db_data:/var/lib/mysql
     restart: always
     environment:
       MYSQL_ROOT_PASSWORD: your-mysql-root-password
       MYSQL_DATABASE: wordpress
       MYSQL_USER: wordpress
       MYSQL_PASSWORD: wordpress
   wordpress:
     depends_on:
       - db
     image: wordpress:latest
     volumes:
        - wp_site:/var/www/html
     ports:
       - "80:80"
       - "443:443"
     restart: always
     environment:
       WORDPRESS_DB_HOST: db:3306
       WORDPRESS_DB_USER: wordpress
       WORDPRESS_DB_PASSWORD: wordpress
volumes:
    db_data:
    wp_site:
----------------------------------------------------------
# 3. 执行安装命令
docker-compose up -d
```



### 用Docker建立一个公用GPU服务器

首先声明一下，Docker 本来被设计用来部署应用（一次配置，到处部署），但是在这篇文章里面，我们是把 Docker 当做一个虚拟机来用的，虽然这稍微有悖于 Docker 的使用哲学，但是，作为一个入门教程的结课项目，我们通过这个例子复习之前学到的 Docker 指令还是很好的。

本文我们主要使用容器搭建一个可以供小型团队（10人以下）使用的 GPU 服务器，用来进行 Deep Learning 的开发和学习。如果读者不是深度学习研究方向的也不要担心，本文的内容依旧是讲解 Docker 的使用，不过提供了一个应用场景。另外，本文会涉及到一些之前没有提到过的 Linux 指令，为了方便 Linux 初学者，会提供详细解释或者参考资料。

本文参考了以下资料的解决思路，将 LXC 容器替换成 Docker 容器，并针对实际情况作了改动：https://abcdabcd987.com/setup-shared-gpu-server-for-labs/

**为什么要使用 Docker 来建立服务器？**

深度学习目前火出天际（2017年），我所在的实验室也有相关研究。但是，深度学习模型的训练需要强悍的显卡，由于目前显卡的价格还是比较高的，所以不可能给每个同学都配备几块显卡。因此，公用服务器就成了唯一的选择。但是，公用服务器有一个问题：如果大家都直接操作宿主主机，直接在宿主主机上配置自己的开发环境的话肯定会发生冲突。

实验室一开始就是直接在宿主机配置账号，也允许每个人配置自己需要的开发环境，结果就是慢慢地大家的环境开始发生各种冲突，导致谁都没有办法安安静静地做研究。于是，我决定使用 Docker 把服务器容器化，每个人都直接登录自己的容器，所有开发都在自己的容器内完成，这样就避免了冲突。并且，Docker 容器的额外开销小得可以忽略不计，所以也不会影响服务器性能。

**服务器配置思路**

服务器的配置需要满足一些条件：

- 用户可以方便地登录
- 用户可以自由安装软件
- 普通用户无法操作宿主主机
- 用户可以使用 GPU 资源
- 用户之间互不干扰

我的解决思路是，在服务器安装显卡驱动后，使用 **nvidia-docker** 镜像运行容器。

为什么使用 nvidia-docker 呢？因为 Docker 是平台无关的（也就是说，无论镜像的内容是什么，只要主机安装了 Docker，就可以从镜像运行容器），这带来的问题就是——当需要使用一些专用硬件的时候就会无法运行。

因此，Docker 本身是不支持容器内访问 NVIDIA GPU 资源的。早期解决这个问题的办法是在容器内安装 NVIDIA 显卡驱动，然后映射与 NVIDIA 显卡相关的设备到容器（Linux 哲学：硬件即文件，所以很容易映射）。这种解决办法很脆弱，因为这样做之后就要求容器内的显卡驱动与主机显卡硬件型号完全吻合，否则即使把显卡资源映射到容器也无法使用！所以，使用这种方法，容器显然无法再做到平台无关了。

为了解决这些问题，nvidia-docker 应运而生。nvidia-docker 是专门为需要访问显卡资源的容器量身定制的，它对原始的 Docker 命令作了封装，只要使用 `nvidia-docker run` 命令运行容器，容器就可以访问主机显卡设备（只要主机安装了显卡驱动）。nvidia-docker 的使用规则和 Docker 是一致的，只需要把命令里的"docker"替换为"nvidia-docker"就可以了。

然后，为了方便大家使用，为每个容器做一些合适的端口映射，为了方便开发，我还配置了图形界面显示功能！

最后，为了实时监控服务器的资源使用情况，使用 WeaveScope 平台监控容器运行状况（当然，这部分内容和 Docker 入门使用关系不大，大家随意看一下就好了）。

如果你没有 GPU 服务器，并且自己的电脑显卡也比较差，你可以不用 nvidia-docker，仅仅使用普通的 Docker 就好了。当然，你可能需要根据自己的实际情况对后文提供的 Dockerfile 进行修改。

**宿主主机配置**

首先，服务器主机需要安装显卡驱动，你可以使用 NVIDIA 官网提供的 ".run" 文件安装，也可以图方便使用 apt 安装：

```sh
apt install nvidia-387 nvidia-387-dev
```

接下来，我们安装 [nvidia-docker](#quick-start)：

```sh
# Add the package repositories
curl -s -L https://nvidia.github.io/nvidia-docker/gpgkey |
sudo apt-key add -
curl -s -L https://nvidia.github.io/nvidia-docker/ubuntu16.04/amd64/nvidia-docker.list |
sudo tee /etc/apt/sources.list.d/nvidia-docker.list
sudo apt-get update

# Install nvidia-docker2 and reload the Docker daemon configuration
sudo apt-get install -y nvidia-docker2
```

我们以 "tensorflow/tensorflow:latest-gpu" 为基础镜像定制自己的镜像，所以先 pull 这个镜像：

```sh
docker pull tensorflow/tensorflow:latest-gpu
```

**使用 Dockerfile 定制镜像**

这部分内容参考了[这个项目](https://github.com/fcwu/docker-ubuntu-vnc-desktop)。配置可以在浏览器显示的远程桌面：

```sh
FROM tensorflow/tensorflow:latest-gpu
MAINTAINER Shichao ZHANG <@gmail>

ENV DEBIAN_FRONTEND noninteractive
RUN sed -i 's#http://archive.ubuntu.com/#http://tw.archive.ubuntu.com/#' /etc/apt/sources.list

# built-in packages
RUN apt-get update 
    && apt-get install -y --no-install-recommends software-properties-common curl
    && sh -c "echo 'deb http://download.opensuse.org/repositories/home:/Horst3180/xUbuntu_16.04/ /' >> /etc/apt/sources.list.d/arc-theme.list"
    && curl -SL http://download.opensuse.org/repositories/home:Horst3180/xUbuntu_16.04/Release.key | apt-key add -
    && add-apt-repository ppa:fcwu-tw/ppa 
    && apt-get update 
    && apt-get install -y --no-install-recommends --allow-unauthenticated
    supervisor 
    openssh-server openssh-client pwgen sudo vim-tiny
    net-tools
    lxde x11vnc xvfb
    gtk2-engines-murrine ttf-ubuntu-font-family
    libreoffice firefox
    fonts-wqy-microhei 
    language-pack-zh-hant language-pack-gnome-zh-hant firefox-locale-zh-hant libreoffice-l10n-zh-tw
    nginx
    python-pip python-dev build-essential
    mesa-utils libgl1-mesa-dri
    gnome-themes-standard gtk2-engines-pixbuf gtk2-engines-murrine pinta arc-theme
    dbus-x11 x11-utils
    && rm -rf /var/lib/apt/lists/
    
RUN echo 'root:root' |chpasswd\****

# tini for subreap
ENV TINI_VERSION v0.9.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /bin/tini
RUN chmod +x /bin/tini
ADD image /
RUN pip install setuptools wheel && pip install -r /usr/lib/web/requirements.txt

EXPOSE 80

WORKDIR /root

ENV HOME=/home/ubuntu
SHELL=/bin/bash

ENTRYPOINT ["/startup.sh"]  
```

然后，由此 Dockerfile 构建镜像：

```sh
docker build -t gpu:v0.1 .
```

等待镜像构建完成。现在，从这个镜像运行一个容器：

```sh
nvidia-docker run -d -ti --rm --name gputest -p 9999:80 -e VNC_PASSWORD=1234 gpu:v0.1
```

说明：-e VNC_PASSWORD 设置登录密码。

我的服务器网址是"223.3.43.127"，端口是我们指定的9999，会要求我们输入密码，输入你设置的密码，即可进入桌面环境。

好了，这样的话团队成员就可以方便地使用 GPU 服务器了！

**简易服务器监控网站**

我们使用一个开源项目来监控容器的运行——[Weave Scope](https://www.weave.works/docs/scope/latest/introducing/)。首先，在宿主主机执行以下命令来安装和启动 Weave Scope：

```sh
sudo curl -L git.io/scope -o /usr/local/bin/scope
sudo chmod a+x /usr/local/bin/scope
scope launch
```

然后浏览器打开服务器 IP 地址，端口号4040，就可以实时监控了：

点击对应的容器即可查看容器信息，包括 CPU 占用率，内存占用，端口映射表，开机时间，IP 地址，进程列表，环境变量等等。并且，通过这个监控网站，可以对容器做一些简单操作：停止，重启，attach，exec 等。

这是一个很简单的 Docker 容器监控方案，使用者可以通过这个监控网站直接操作容器，所以无需登录宿主主机来进行相关操作，完美实现资源隔离。

但是，这个方案也有一些缺点，比如每个用户都可以看到其它用户的密码，甚至可以直接进入其他用户的容器！不过，由于我们的使用背景是“实验室或者小团队的私密使用”，作为关系紧密的内部小团体，建立在大家相互信任的基础上，相信这也不是什么大问题。

**服务器管理方案小结**

现在总结一下我们的 GPU 服务器容器化的全部工作：

- 宿主主机配置 Docker 和 nvidia-docker，安装显卡驱动；

- 使用 Dockerfile 定制镜像；

- 为每个用户运行一个容器，注意需要挂载需要的数据卷；

  ```sh
  nvidia-docker run -ti -d --name ZhangShichao -v /home/berry/dockerhub/zsc/:/root/zsc -v /media/zhangzhe/data1:/root/sharedData -p 6012:22 -p 6018:80 -p 6010:6000 -p 6011:6001 -p 6019:8888 -e VNC_PASSWORD=ZhangShichao  gpu:v0.1
  ```

- 使用 WeaveScope 监控容器的运行情况；

在此之后，如果团队成员需要启动新的容器，管理员可以通过宿主主机为用户运行需要的容器。普通用户无法操作宿主主机，完美实现隔离！



## 容器网络机制和多主机网络实践

容器网络不是新技术，它是云计算虚拟化技术互联互通的基础核心技术。一般意义的网络都是主机与主机之间的通信，颗粒度局限在物理层面的网卡接口。随着虚拟化技术的发展，以应用为中心的新网络结构逐渐明朗清晰。容器技术就是让依赖环境可以跟着应用绑定打包，并随需启动并互联。容器技术的特点也对网络技术的发展起到了互推的作用，当网络不在持久化存在的时候，软件定义网络（SDN）技术的能力就会体现的更充分。

### 容器主机网络模型

Docker 内建的网络模型是 Bridge Network。这种网络是基于主机内部模型的网络，设计之初也是为了解决单机模式下容器之间的互联互通问题。如图：

![x](./Resource/47.png)

Veth pair 技术源于 Linux 网络模型的虚拟设备，比如 TAP 设备，方便主机上应用程序接收网络数据而创建。TAP 设备只能监听到网卡接口上的数据流量，如果想连接多个网络命名空间，就需要用到 Veth pair 技术来打通连接。容器网络之间的互通就是通过这个做到的，但是细心的读者可以看到，图上主机网卡和 docker0 网桥是没有连接的，不能数据互联。为了让容器与外界网络相连，首先要保证主机能允许转发 IP 数据包，另外需要让 iptables 能指定特定的 IP 链路。通过系统参数 ip_forward 来调节开关，如：

```sh
sysctl net.ipv4.conf.all.forwarding

  net.ipv4.conf.all.forwarding = 0

sysctl net.ipv4.conf.all.forwarding=1

sysctl net.ipv4.conf.all.forwarding

  net.ipv4.conf.all.forwarding = 1
```

另外，当 Docker 后台程序起来后，会自动添加转发规则到 Docker 过滤链上，如下图：

```sh
$ sudo iptables -t filter -L
Chain INPUT (policy ACCEPT)
target     prot opt source               destination
ACCEPT     tcp  --  anywhere             anywhere             tcp dpt:domain
ACCEPT     udp  --  anywhere             anywhere             udp dpt:domain
ACCEPT     tcp  --  anywhere             anywhere             tcp dpt:bootps
ACCEPT     udp  --  anywhere             anywhere             udp dpt:bootps
Chain FORWARD (policy ACCEPT)
target     prot opt source               destination
DOCKER-ISOLATION  all  --  anywhere             anywhere
DOCKER     all  --  anywhere             anywhere
ACCEPT     all  --  anywhere             anywhere             ctstate RELATED,ESTABLISHED
ACCEPT     all  --  anywhere             anywhere
ACCEPT     all  --  anywhere             anywhere
DOCKER     all  --  anywhere             anywhere
ACCEPT     all  --  anywhere             anywhere             ctstate RELATED,ESTABLISHED
ACCEPT     all  --  anywhere             anywhere
ACCEPT     all  --  anywhere             anywhere
DOCKER     all  --  anywhere             anywhere
ACCEPT     all  --  anywhere             anywhere             ctstate RELATED,ESTABLISHED
ACCEPT     all  --  anywhere             anywhere
ACCEPT     all  --  anywhere             anywhere
ACCEPT     all  --  anywhere             anywhere
DROP       all  --  anywhere             anywhere
Chain OUTPUT (policy ACCEPT)
target     prot opt source               destination
Chain DOCKER (3 references)
target     prot opt source               destination
Chain DOCKER-ISOLATION (1 references)
target     prot opt source               destination
DROP       all  --  anywhere             anywhere
DROP       all  --  anywhere             anywhere
DROP       all  --  anywhere             anywhere
DROP       all  --  anywhere             anywhere
DROP       all  --  anywhere             anywhere
DROP       all  --  anywhere             anywhere
RETURN     all  --  anywhere             anywhere
```

另外衍生出来的问题是，所有 Docker 容器启动时都需要显示指定端口参数，这样做是因为由于需要 iptable 规则来开启端口映射能力。

### 跨越主机的容器网络模型

如果需要让容器网络可以跨越主机访问，最原生的方式是 Macvlan 驱动支持的二层网络模型。VLAN 技术是网络组网的基本技术，在网络环境中很容易获得，所以，由此产生的用户映像是能不能打破主机和容器的网络间隙，把他们放在一个网络控制面上协作。Macvlan 技术就是为了这个需求而设计的，它实现了容器网络和主机网络的原生互联。当然，需要支持 Macvlan 也是需要准备一些基础环境的：

- Docker 版本必须在1.12.0+以上
- Linux kernel v3.9–3.19 and 4.0+才内置支持Macvlan 驱动

Macvlan 技术是一种真实的网络虚拟化技术，比其他Linux Bridge 更加轻量级。相比 Linux Bridge，性能更高。因为它跳过了主机网卡和容器网络直接的转发过程，容器网卡接口直接对接主机网口，可以视作为主机网络的延伸。这样的网络，让外部访问容器变的非常简便，不在需要端口映射，如下图所示：

![x](./Resource/48.png)

为了让容器网络支持多个分组，可以考虑采用802.1q 的 VALN tagging 技术实现。这种技术的好处对于小规模主机网络下容器网络的搭建非常合适。这块通过如下图可以解释清楚：

![x](./Resource/49.png)

### 容器网络标准 CNI

容器网络接口（CNI）是云原生基金会支持项目，属于云计算领域容器行业标准。它包含了定义容器网络插件规范和示范。因为 CNI 仅仅聚焦在容器之间的互联和容器销毁后的网络配置清理，所以它的标准简洁并容易实现。

标准包含两部分，CNI Plugin 旨在配置网络信息，另外定义了 IPAM Plugin 旨在分配 IP，管理 IP。这个接口有更广泛的适用性，适应多种容器标准。如图：

![x](./Resource/50.png)

网络插件是独立的可执行文件，被上层的容器管理平台调用。网络插件只有两件事情要做：把容器加入到网络以及把容器从网络中删除。

调用插件的数据通过两种方式传递：环境变量和标准输入。

一般插件需要三种类型的数据：容器相关的信息，比如 ns 的文件、容器 id 等；网络配置的信息，包括网段、网关、DNS 以及插件额外的信息等；还有就是 CNI 本身的信息，比如 CNI 插件的位置、添加网络还是删除网络等。

### 把容器加入到网络

调用插件的时候，这些参数会通过环境变量进行传递：

- CNI_COMMAND：要执行的操作，可以是 ADD（把容器加入到某个网络）、DEL（把容器从某个网络中删除）、VERSION
- CNI_CONTAINERID：容器的 ID，比如 ipam 会把容器 ID 和分配的 IP 地址保存下来。可选的参数，但是推荐传递过去。需要保证在管理平台上是唯一的，如果容器被删除后可以循环使用
- CNI_NETNS：容器的 network namespace 文件，访问这个文件可以在容器的网络 namespace 中操作
- CNI_IFNAME：要配置的 interface 名字，比如 eth0
- CNI_ARGS：额外的参数，是由分号;分割的键值对，比如 “FOO=BAR;ABC=123”
- CNI_PATH：CNI 二进制文件查找的路径列表，多个路径用分隔符 : 分隔

网络信息主要通过标准输入，作为 JSON 字符串传递给插件，必须的参数包括：

- cniVersion：CNI 标准的版本号。因为 CNI 在演化过程中，不同的版本有不同的要求
- name：网络的名字，在集群中应该保持唯一
- type：网络插件的类型，也就是 CNI 可执行文件的名称
- args：额外的信息，类型为字典
- ipMasq：是否在主机上为该网络配置 IP masquerade
- ipam：IP 分配相关的信息，类型为字典
- dns：DNS 相关的信息，类型为字典

CNI 作为一个网络协议标准，它有很强的扩展性和灵活性。如果用户对某个插件有额外的需求，可以通过输入中的 args 和环境变量 CNI_ARGS 传输，然后在插件中实现自定义的功能，这大大增加了它的扩展性；CNI 插件把 main 和 ipam 分开，用户可以自由组合它们，而且一个 CNI 插件也可以直接调用另外一个 CNI 插件，使用起来非常灵活。如果要实现一个继承性的 CNI 插件也不复杂，可以编写自己的 CNI 插件，根据传入的配置调用 main 中已经有的插件，就能让用户自由选择容器的网络。

### 容器网络实践

容器网络的复杂之处在于应用的环境是千变万化的，一招鲜的容器网络模型并不能适用于应用规模的扩张。因为所谓实践，无外乎是在众多网络方案中选择合适自己的网络方案。

一切应用为王，网络性能指标是指导我们选择方案的最佳指南针。主机网络和容器网络互联互通的问题，是首先需要考虑的。当前比较合适的容器网络以 Macvlan/SR-IOV 为主。考虑原因还是尽量在兼容原有网络硬件的集成之上能更方便的集成网络。这块的方案需要软硬件上的支持，如果条件有限制，可能很难实现。比如你的容器网络本来就构建在 Openstack 的虚拟网络中。

退而求其次，当前最普遍的方案就是 Vxlan/overlay 的方案，这种网络方案是虚拟网络，和外界通信需要使用边界网关通信。这块主要的支持者是 Kubernetes 集群。比如常用的 Flannel 方案，主要被用户质疑的地方就是网络效率的损耗。 当然，Vxlan 方案的优秀选择 openswitch，可能是最强有力的支持者。通过 OVS 方便，可以得到一个业界最好的网络通信方案。当遇到生产级瓶颈时，可以考虑使用硬件控制器来代替 OVS 的控制器组件来加速网络。目前 Origin 的方案中选择的就是 OVS 方案，可以认为是当前比较好的选择。

当然，开源的 overlay 方案中有比较优秀的方案比如 Calico 方案，它借用了 BGP 协议作为主机与主机之间边界的路由通信，可以很好的解决小集群模式下的高效网络传输。Calico 的背后公司也是借用此技术在社区中推出商业硬件解决方案。从国内的中小型企业的网络规模来说，此种网络完全可以满足网络需要。

### 展望网络发展趋势

容器网络互联已经不在是棘手的问题，行的实现就在手边。目前用户进一步的使用中，对网络的限流和安全策略有了更多的需求。这也催生了如 cilium 这样的开源项目，旨在利用 Linux 原生的伯克利包过滤（Berkeley Packet Filter，BPF）技术实现网络流量的安全审计和流量导向。如图：

![x](./Resource/51.png)

所以，容器网络的发展正在接近应用生命周期的循环中，从限流，到安全策略，再到可能的虚拟网络 NFV 的构建都有可能改变我们的容器世界。

参考：[容器网络接口标准](https://github.com/containernetworking/cni/blob/master/SPEC.md)

## Docker日志机制与监控实践

日志和监控是容器云平台系统最常见的必备组件，形象一点形容其原理就是咖啡和伴侣一样必须配套使用，让你的应用运行的更贴合用户满意的服务运营目标（SLO）。当容器技术被大量行业采用之后，我们遇到了一个很自然的问题，容器化后应用日志怎么收集，监控报警怎么做。这些问题一直困扰着容器行业的从业者，直到以 Google Borgmon 为理论基础的 Prometheus 开源项目发布，EFK 日志系统的容器化实践落地，得以促成本篇文章的完成。

### EFK 日志系统的容器化实践

日志系统涉及采集、展现和存储三个方面的设计。从采集方面来说，单台容器主机上的采集进程应该是多功能接口的、可以提供插件机制的日志组件才能满足一般采集的需求。那么到了容器这个领域，日志分为控制台日志和应用业务日志两类。对于容器控制台接口，需要通过容器进程开放的接口来采集，如图：

![x](./Resource/52.png)

容器默认采用的是日志驱动为 json-file 模式，采集效率极低还占用大量 IO 读写效能，基本无法适应生产环境需要。在我们生产实践推荐中，偏向于采用系统提供的日志系统 systemd-journal 来接收日志采集，然后通过 fluentd 采集代理进程，把相应的日志按照业务规则分类汇聚，发送到 Elasticsearch 这样的中央日志管理系统。由于业务日志量的规模扩大，日志采集的流量速率会让中央日志系统处理负载过高，导致业务日志处理不过来。所以通常采用流式消息队列服务 Kafka 作为日志存储的异步缓冲，可以极大的缓解日志流量，并高效的解决日志采集的汇聚难题。

CNCF 云原生计算基金会推荐的采集解决方案是 Fluentd，作为行业标杆的托管项目，这个项目的插件是非常丰富的。所以，当你在考虑选择日志采集方案的时候，Fluentd 是当前一站式解决容器日志采集方案的首选，如下图：

![x](./Resource/53.png)

因为 Fluentd 是一套 ruby 编写的日志采集框架，很难让人信服其海量的日志处理能力。所以在今年早些时候推出了基于 C 语言编写的高性能日志转发工具 fluentbit，可以完美承上输入层，起下输出层，如图：

![x](./Resource/54.png)

日志收集到之后，会通过相应的过滤插件汇聚清洗日志条目并聚合到日志中心系统，系统用户通过可视化界面可以检索自己需要的日志信息。

随着 CNCF 在全球范围内吸收了业界主流云计算厂商，导致日志收集又遇到另一个需要解决的问题，那就是 Kubernetes 集群的日志收集问题。所以，我需要逐步按照收集的纬度给予介绍分析。首先，最基本的是 Pod 的日志信息，注意它并不等同于 Docker 容器的控制台日志。

例如 Pod 任务[counter-pod.yaml](https://raw.githubusercontent.com/kubernetes/website/master/docs/tasks/debug-application-cluster/counter-pod.yaml)：

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: counter
spec:
  containers:
  - name: count
    image: busybox
    args: [/bin/sh, -c,
            'i=0; while true; do echo "$i: $(date)"; i=$((i+1)); sleep 1; done']
```

发布这个 Pod 到集群中：

```sh
kubectl create -f https://k8s.io/docs/tasks/debug-application-cluster/counter-pod.yaml
--pod "counter" created
```

查看日志：

```sh
$ kubectl logs counter
0: Mon Jan  1 00:00:00 UTC 2001
1: Mon Jan  1 00:00:01 UTC 2001
2: Mon Jan  1 00:00:02 UTC 2001
...
```

Kubernetes 默认使用容器的 json-file 驱动来写入日志文件，并使用 logrotate 来收敛日志大小。

![x](./Resource/55.png)

除了 Pod 之外，我们还需要考虑 Kubernetes 系统组件的日志收集工作。例如这样的场景：

- Scheduler 和 kube-proxy 是容器化运行
- Kubelet 和 Docker 是非容器化运行

对于容器化的系统组件，他们都是采用 [glog](https://godoc.org/github.com/golang/glog) 来写入日志的并存入 /var/log 目录下，可以采用logrotate 来按大小分割日志。对于非容器化的系统组件，直接采用系统内建的 systemd-journal 收集即可。

当然对于分布式系统的日志收集，还可以通过发布日志采集容器组件的方式来采集日志。最好的方式是采用 sidecar 的方式，每个 Pod 中加入一个日志采集器，方便日志的采集流式进入日志系统中。

![x](./Resource/56.png)

当应用日志需要落盘的时候，这种 sidecar 模式的日志采集方式尤其灵活，值得推荐采用。

### 容器监控实践

容器监控需要关心的指标范畴主要集中在主机、集群、容器、应用以及报警规则和报警推送。监控的指标也大多放在了 CPU、RAM、NETWORK 三个纬度上面。当然业务应用如果是 Java 系统，还有收集 JMX 的需求存在，从容器角度来讲仅需要暴露 JMX 端口即可。很多开始做容器监控的从业者会考虑使用现有基础监控设施 Zabbix 来做容器监控。但是从业界发展趋势上来说，采用 Prometheus 的解决方案会是主流方案。首先，我们可以通过 Prometheus 的架构来了解监控的流程架构图如下：

![x](./Resource/57.png)

它采用 Pull 模式来主动收集监控信息，并可以采用 Grafana 定制出需要的监控大屏面板。从收集探针角度，Prometheus 有很多[输出指标的插件](https://prometheus.io/docs/instrumenting/exporters/)可以使用。注意插件 exporter 的工作目的是能把监控数据缓存起来，供 Prometheus 服务器来主动抓取数据。从生产级别 HA 的需求来看，目前 Prometheus 并没有提供。所有我们需要给 Prometheus Server 和 AlertManager 两个组件提供 HA 的解决方案。

#### HA Prometheus

当前可以实施的方案是建立两套一模一样配置的Prometheus 服务，各自独立配置并本地存储监控数据并独立报警。因为上面介绍了 PULL 的拉取采集方式，对于两个独立的 Prometheus 服务来说是完全可行的，不需要在客户端配置两份监控服务地址。记住两套 Prometheus Server 必须独立，保证一台当机不会影响另外一台 Server 的使用。

#### HA AlertManager

AlertManager 的 HA 配置是复杂的，毕竟有两个Prometheus Server 会同时触发报警给 AlertManager，用户被报警两遍并不是一个好主意。当前 HA 还在开发过程中，采用了[Mesh技术](https://github.com/prometheus/alertmanager#high-availability)帮助 AlertManager 能协调出哪一个接受者可以报告这次警告。

另外，通过 PromSQL 的 DSL 语法，可以定制出任何关心的监控指标：如图：

![x](./Resource/58.png)

定义报警规则的例子如下：

```sh
task:requests:rate10s =
  rate(requests{job="web"}[10s])
```

同时我们还关注到当前 Prometheus 2.0 即将发布 GA，从 RC 版本透露新特性是时间序列数据存储的自定义实现，参考了 Facebook 的 Gorilla（[Facebook's "Gorilla" paper](http://www.vldb.org/pvldb/vol8/p1816-teller.pdf)），有兴趣的可以关注一下。

另外，Prometheus 还有一个痛点就是系统部署比较麻烦，现在推荐的方式是采用 Operator 的模式发布到K8S 集群中提供服务（[Prometheus Operator](https://coreos.com/operators/prometheus/docs/latest)），效率高并且云原生架构实现。

**总结：**

Docker 日志机制已经没有什么技巧可以优化。这个也证明了容器技术的成熟度已经瓜熟蒂落，并且在日常应用运维中可以很好的实施完成。主要的实践重点在于日志体系的灵活性和日志数据处理能力方面的不断磨合和升级，这是容器技术本身无法支撑的，还需要用户结合自身情况选择发展路线。

对于监控系统，时间序列数据库的性能尤为重要。老版本的 Prometheus 基本都是在采集性能上得不到有效的发挥，这次2.0版本完全重写了一遍 tsdb，经过评测发现比老版本性能提升3-4倍，让人刮目相看。期待正式版本的推出，可以让这套云原生的监控系统得到更好的发展。

**参考：**

- [Kubernetes Logging Architecture](https://kubernetes.io/docs/concepts/cluster-administration/logging/)
- [HA AlertManager setup (slide)](http://calcotestudios.com/talks/slides-understanding-and-extending-prometheus-alertmanager.html#/1/9)
- [https://fabxc.org/tsdb/](https://fabxc.org/tsdb/)



17、Dockerfile详解
原文：https://idig8.com/2018/07/29/docker-zhongji-17/
一般的，Dockerfile 分为四部分：基础镜像信息、维护者信息、镜像操作指令和容器启动时执行指令。

18、镜像的发布
原文：https://idig8.com/2018/07/29/docker-zhongji-18/
19、Dockerfile实战
原文：https://idig8.com/2018/07/29/docker-zhongji-19/
20、容器的操作
原文：https://idig8.com/2018/07/29/docker-zhongji-20/
21、Dockerfile实战CMD和ENTRTYPOINT的配合
原文：https://idig8.com/2018/07/29/docker-zhongji-21/
22、容器的资源限制
原文：https://idig8.com/2018/07/29/docker-zhongji-22/
23、docker网络
原文：https://idig8.com/2018/07/29/docker-zhongji-23/
24、docker学习必会网络基础
原文：https://idig8.com/2018/07/29/docker-zhongji-24/
25、Linux网络命名空间
原文：https://idig8.com/2018/07/29/docker-zhongji-25/
26、Docker Bridge详解
原文：https://idig8.com/2018/07/29/docker-zhongji-26/
27、容器之间的Link
原文：https://idig8.com/2018/07/29/docker-zhongji-27/
28、容器的端口映射
原文：https://idig8.com/2018/07/29/docker-zhongji-28/
29、容器网络之host和none
原文：https://idig8.com/2018/07/29/docker-zhongji-29/
30、多容器复杂应用的部署
原文：https://idig8.com/2018/07/29/docker-zhongji-30/
31、overlay网络和etcd实现多机的容器通信
原文：https://idig8.com/2018/07/29/docker-zhongji-31/
32、docker的数据持久化存储和数据共享
原文：https://idig8.com/2018/07/29/docker-zhongji-32/
33、windows下vagrant 通过SecureCRT连接centos7
原文：https://idig8.com/2018/07/29/docker-zhongji-33/
34、数据持久化之Data Volume
原文：https://idig8.com/2018/07/29/docker-zhongji-34/
35、数据持久化之bind Mounting
原文：https://idig8.com/2018/07/29/docker-zhongji-35/
36、docker 使用bind Mounting实战
原文：https://idig8.com/2018/07/29/docker-zhongji-36/
37、docker容器安装wordpress
原文：https://idig8.com/2018/07/29/docker-zhongji-37/



### Portainer管理集群部署

之前都是通过命令的方式，管理docker的，其实docker还是有图形界面的。使用图形界面如何管理docker，其实业界很多公司都对docker进行了图形化的封装。之前在初级和中级的时候也有界面marathon。这里说下业界比较出名的portainer。

官网：https://www.portainer.io

Portainer的开发是为了帮助客户采用Docker容器技术，加快交付价值的时间。构建、管理和维护Docker环境从来没有这么容易。Portainer易于使用为软件开发人员和IT操作提供直观界面的软件。Portainer为您提供了Docker环境的详细概述，并允许您管理容器、图像、网络和卷。Portainer很容易部署——您只需要一个Docker命令就可以在任何地方运行Portainer。

写了那么多命令，现在才说有一个开源Portainer，其实我的目的就是先学会走，在学会跑。如果直接用图形界面对docker的运行，理解不深入，网络原理也不理解。通过图形界面运行后，可以透过图形界面，理解后台是如何运行命令的。

**portainer安装**

开放Docker网络管理端口（四台机器都需要执行）

```sh
vim /lib/systemd/system/docker.service#找到 ExecStart行  ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock  systemctl daemon-reload systemctl restart docker  
```

启动容器（四台机器）

```sh
# 66.100机器执行
docker run -d -p 9000:9000 portainer/portainer -H tcp://192.168.66.100:2375
# 66.101机器执行
docker run -d -p 9000:9000 portainer/portainer -H tcp://192.168.66.101:2375
# 66.102机器执行
docker run -d -p 9000:9000 portainer/portainer -H tcp://192.168.66.102:2375
# 66.103机器执行
docker run -d -p 9000:9000 portainer/portainer -H tcp://192.168.66.103:2375
```

功能页面：http://192.168.66.100:9000/#/init/admin

可能设置完密码会崩了容器，重新 `docker start 容器ID`



### docker-swarm

![x](./Resources/docker20.png)

 

为了让学习的知识融汇贯通，目前是把所有的集群都放在了一个虚拟机上，如果这个虚拟机宕机了怎么办？俗话说鸡蛋不要都放在一个篮子里面，把各种集群的节点拆分部署，应该把各种节点分机器部署，多个宿主机，这样部署随便挂哪个主机我们都不担心。

源码：https://github.com/limingios/netFuture/blob/master/docker-swarm/

swarm 是docker的三剑客一员，之前都说过了，可以看中级和高级啊 。

1. docker machine 容器服务

2. docker compose 脚本服务

3. docker swarm 容器集群技术

**去中心化的设计**

- Swarm Manager 也承担worker节点的作用。
- Swarm Worker 运行容器部署项目

![x](./Resources/docker21.png)

Swarm是没有中心节点的，挂掉其中一个其他是不会挂掉的。Swarm Manager 如果master挂了，立马选举一个新的master。

**创建集群环境**

首先机器已经安装了docker环境

```sh
docker swarm init
```

**加入swarm集群**

```sh
# 加入到 manager 中
docker swarm join-token manager
# 加入到 worker 中
docker swarm join-token worker
```

**环境搭建**

![x](./Resources/docker21.png)

一共4个节点，2个manager节点，2个work节点，manager不光是管理，而且也干活，说白了一共4个干活的节点。

**创建 docker swarm 集群**

```sh
docker swarm init
```

报错注意：如果你在新建集群时遇到双网卡情况，可以指定使用哪个 IP，例如上面的例子会有可能遇到下面的错误。

Error response from daemon: could not choose an IP address to advertise since this system has multiple addresses on different interfaces (10.0.2.15 on enp0s3 and 192.168.66.100 on enp0s8) 
\- specify one with --advertise-addr

再次创建 docker swarm 集群 192.168.66.100

```sh
docker swarm init --advertise-addr 192.168.66.100 --listen-addr 192.168.66.100:2377docker swarm join-token manager
```

再次创建 docker swarm 集群192.168.66.101当前节点以manager的身份加入swarm集群

```sh
docker swarm join --token SWMTKN-1-4itumtscktomolcau8a8cte98erjn2420fy2oyj18ujuvxkkzx-9qutkvpzk87chtr4pv8770mcb 192.168.66.100:2377
```

再次创建 docker swarm 集群 192.168.66.102 当前节点以 worker 的身份加入 swarm 集群

```sh
docker swarm join --token SWMTKN-1-4itumtscktomolcau8a8cte98erjn2420fy2oyj18ujuvxkkzx-f2dlt8g3hg86gyc9x6esewtwl 192.168.66.100:2377
```

再次创建 docker swarm 集群 192.168.66.103 当前节点以 worker 的身份加入 swarm 集群

```sh
docker swarm join --token SWMTKN-1-4itumtscktomolcau8a8cte98erjn2420fy2oyj18ujuvxkkzx-f2dlt8g3hg86gyc9x6esewtwl 192.168.66.100:2377
```

**查看swarm集群**

只能在 manager 节点内执行，leader 挂掉后，reachable 就可以管理集群了。

```sh
docker node ls
```

![x](./Resources/docker23.png)

**创建容器间的共享网络**

只能在manager节点内执行

```sh
docker network create -d overlay --attachable swarm_test
docker network ls
```

目前是4台机器，如果想让4台机器内的容器可以进行共享，overlay的网络就可以了，只需要在创建容器的时候 `–net=swarm_test`

![x](./Resources/docker24.png)

**创建5个pxc容器**





### docker-machine

**1、什么是 Docker Machine？**

Docker Machine是一个工具，它可以帮你在虚拟主机安装 docker，并且通过 `docker-machine` 相关命令控制主机。你可以用 docker machine 在 mac、windows、单位的网络、数据中心、云提供商（AWS 或 Digital Ocean）创建 docker 主机。

通过 docker-machine commands，你能启动、进入、停止、重启主机，也可以升级 docker，还可以配置 docker client。

**2、为什么要用 Docker Machine？**

Docker Machine 是当前 docker 运行在 mac 或者 windows 上的唯一方式，并且操作多种不同 linux 系统的 docker 主机的最佳方式。

**3、Docker Machine之安装**

参考：[https://github.com/docker/machine/](https://github.com/docker/machine/)

下载 docker-machine 二进制文件

Mac Or linux

```sh
curl -Lhttps://github.com/docker/machine/releases/download/v0.8.0/docker-machine-`uname\ -s`-`uname -m` > /usr/local/bin/docker-machine \ && chmod +x/usr/local/bin/docker-machine
```

Windows with git bash

```sh
if [[ ! -d"$HOME/bin" ]]; then mkdir -p "$HOME/bin"; fi && \curl -Lhttps://github.com/docker/machine/releases/download/v0.7.0/docker-machine-Windows-x86_64.exe\ "$HOME/bin/docker-machine.exe" && \ chmod +x"$HOME/bin/docker-machine.exe"
```

黑魔法（离线安装）：

下载地址：[https://github.com/docker/machine/releases/](https://github.com/docker/machine/releases/)

直接在csdn下载：[https://download.csdn.net/download/zhugeaming2018/10404327](https://download.csdn.net/download/zhugeaming2018/10404327)

**4、Docker Machine之使用(macor windows)**

使用准备：

安装最新版的 virtualbox([https://www.virtualbox.org/wiki/Downloads](https://www.virtualbox.org/wiki/Downloads))

```sh
cd /etc/yum.repos.d
wget http://download.virtualbox.org/virtualbox/rpm/rhel/virtualbox.repo
yum install -y  VirtualBox-5.2
```

Create a machine

```sh
docker-machine create –driver virtualbox default
```

在上面你会发现这么句话 "error in driver during machine creation: This computer doesn't have VT-X/AMD-v enabled.Enabling it in the BIOS is mandatory" 意思就是说你没有开启虚拟化。

有朋友说创建虚拟主机太慢，我提供一个阿里云加速命令很快很暴力：

```sh
docker-machine create –driver virtualbox –engine-registry-mirror https://xu176fzy.mirror.aliyuncs.com default
```

- Get the environmentcommands for your new VM

  docker-machine env default

- List available machines again to see your newly minted machine

  docker-machine ls

- Connect your shedocker-machinessh defaultll to the new machine

  docker-machine ssh default

- Start and stop machines

  docker-machine stop default

  docker-machine start default

- Docker machine之使用(Iaas)

原文：https://idig8.com/2018/07/29/docker-zhongji-10/

\#查看docker-machine的版本

docker-machine version

如果你不是window10或者是你在mac中已经安装了docker了，但是docker-machine还没安装的话，可以通过官网来进行安装

https://docs.docker.com/machine/install-machine/#install-machine-directly

\#通过docker-machine 创建一个docker的虚拟机

docker-machine create demo

\#查看创建的虚拟机

docker-machine ls

\#进入创建的虚拟机

docker-machine ssh demo

\#查看docker-machine 创建的docker版本

docker --version

\#再创建一个docker-machine

docker-machine create demo1

\#关闭docker-mache demo1

docker-machine stop demo1

\#docker-machine远程server

先关闭本地的server端，点击docker的右下角图标选择-quit docker

\#共享server

#查看本地docker version#查看docker-machine的环境变量导入到本地docker-machine env demo#windows执行命令 @FOR /f "tokens=*" %i IN ('docker-machine env demo') DO @%i#mac下执行命令eval $(docker-machine env demo)#查看新的环境变量docker version

通过上边这种方式，可以远程管理docker-machine！

docker-machine还可以更换driver的方式，具体查看官网吧：https://docs.docker.com/machine/get-started-cloud/

总体来说docker-machine跟咱们的之前说过的vagrant非常的类似，条条大路通罗马

***\*11、\*******\*在linux/mac下通过Docker-Machine在阿里云上的使用\****

原文：https://idig8.com/2018/07/29/docker-zhongji-11/

在第十节说到，在本地通过docker-machine创建虚拟机，在虚拟机安装了咱们使用的docker。通过docker-machine也可以在云上创建虚拟机。

官网直接推荐的：https://docs.docker.com/machine/get-started-cloud/#drivers-for-cloud-providers

使用Docker Machine管理阿里云ECS

https://github.com/AliyunContainerService/docker-machine-driver-aliyunecs

准备工作

下载 https://docker-machine-drivers.oss-cn-beijing.aliyuncs.com/docker-machine-driver-aliyunecs_linux-amd64.tgz)

通过centos虚拟机来进行安装，先进行docker安装，具体可以看『在centos上安装docker』，记住安装docker-machine。也把如何安装docker-machine给大家说下

base=https://github.com/docker/machine/releases/download/v0.14.0 && curl -L $base/docker-machine-$(uname -s)-$(uname -m) >/tmp/docker-machine && sudo install /tmp/docker-machine /usr/local/bin/docker-machine

安装阿里的docker-machine 的第三方

安装步骤

mkdir docker-machine# Download and unzip Aliyun ECS drivercurl -L https://docker-machine-drivers.oss-cn-beijing.aliyuncs.com/docker-machine-driver-aliyunecs_linux-amd64.tgz > driver-aliyunecs.tgz && tar xzvf driver-aliyunecs.tgz -C docker-machine && rm driver-aliyunecs.tgz mv docker-machine/bin/* /usr/local/bin mv /usr/local/bin/docker-machine-driver-aliyunecs.linux-amd64 /usr/local/bin/docker-machine-driver-aliyunecs && chmod +x /usr/local/bin/docker-machine-driver-aliyunecs

查看是否安装成功

docker-machine create -d aliyunecs --help

开始安装

登录阿里云账号控制台https://home.console.aliyun.com/new#/开通『访问控制』、创建用户、授权

最重要的一步保证自己的账户有100以上的钱，这个很重要啊，要不阿里不让你创建docker-machine。记住你比别人就差这110块钱吗？机会面前这110是小钱。

安装

docker-machine create -d aliyunecs --aliyunecs-io-optimized=optimized --aliyunecs-instance-type=ecs.c5.large --aliyunecs-access-key-id=XXXX --aliyunecs-access-key-secret=XXXX --aliyunecs-region=cn-qingdao liming

安装结果

docker-machine ls

登录

docker-machine ssh liming

docker version

本地的docker server没启动，咱们直接连接阿里云的docker server

docker-machine env liming
eval $(docker-machine env liming)

如果不想连接远程的docker server

docker-machine env --unset
eval $(docker-machine env --unset)

钱不是大风刮来的，了解完了记得删除

docker-machine rm liming

记住安装过程中有错误把错误内容输入到：https://error-center.aliyun.com/status/search 就可以看到提示啦！



### docker-swarm

1.什么是Docker Swarm？
    容器集群管理工具。
    通过docker swarm可以将多台机器连接在一起，通过swarm的调度可以实现服务的多台机器的部署，服务的伸缩。
    docker-swarm的场景因为需要多台docker虚拟机，在虚拟机中创建docker-machine会发现一个很重要的问题，无法创建多个docker的虚拟器，虚拟主机报错"Wrapper DockerMachine process exiting due to closed plugin server ..." 该问题是在 Vmware Workstation Pro 14.1.1 & centos10 上出现的，用真实机器测试不会出现。
    所以下面的演示就在Vmware Workstation下演示1台机器。
    docker-machine create –driver virtualbox manager
    docker-machine ssh manager
    docker version
2.Docker Swarm 使用入门
    注意：docker engine版本为1.18.05.0-ce
    docker swarm manager 节点初始化
    docker swarm init --advertise-addr <hostIP>
    说明：init命令初始化后生成结果如下：
To add a worker to this swarm, run the following command:
    docker swarm join --token SWMTKN-1-5t5n2lcqsal12tmhsngww28njm1qcz6917u9bomgmy6bdyw3o0-8gf8jgpb83b22oae92aiamlel 192.168.101.13:2377
To add a manager to this swarm, run 'docker swarm join-token manager' and follow the instructions.
    开启另一台虚拟机，使用上面的命令将docker加入该集群
    查看集群：docker info
    docker node ls
    删除work节点
    docker swarm leave
    通过swarm创建服务
    docker service create –replicas 1 --name helloworld alpine ping docker.com
    查看服务列表
    docker service ls
    查看服务详情
    docker service inspect –pretty helloworld
    服务弹性扩展
    docker service scale =
    Ex:docker service scale helloworld=5
    查看服务列表
    docker service ps
    Ex:docker service ps helloworld
    服务删除
    docker service remove
    Ex:docker service rm helloworld



## 自动化部署分布式容器云平台实践

当前云计算场景中部署一套 Kubernetes 集群系统是最常见的容器需求。在初期阶段，大量的部署经验都依赖于前人设计实现的自动化部署工具之上，比如 Ansible。但是为什么这样的自动化工具并不能彻底解决所有 Kubernetes 集群的安装问题呢，主要的矛盾在于版本的升级更新动作在分布式系统的部署过程中，由于步骤复杂，无法提供统一的自动化框架来支持。

Ansible 需要撰写大量的有状态的情况来覆盖各种可能发生的部署阶段并做出判断。这种二次判断的操作对于 Ansible 这种自动化工具是无法适应的。Ansible 这样的工具期望行为一致性，如果发生可能发生的情况，将无法有效的保证后续的步奏能有效的安装。通过本文分享的 Kubernetes 社区中提供的安装套件可以帮助大家结合实践现在适合自己的部署分布式容器云平台的方法和工具链。

### Kubernetes Operations（kops）

#### 生产级别 k8s 安装、升级和管理

Ansible 部署 k8s 需要投入很多精力来维护集群知识的 roles 和 inventory，在日常分布式系统中会带来很多不确定的异常，很难维护。所以社区提供了 kops，期望能像 kubectl 一样来管理集群部署的问题。目前实现了 AWS 的支持，GCE 支持属于 Beta 阶段，vSphere 处于 alpha 阶段，其他平台属于计划中。对于中国区的 AWS，可以选用 cn-north-1 可用区来支持。

![x](./Resource/59.png)

1、配置 AWS 信息

```sh
AWS Access Key ID [None]:
AWS Secret Access Key [None]:
Default region name [None]:
Default output format [None]:
```

注意需要声明可用区信息

```sh
export AWS_REGION=$(aws configure get region)
```

2、DNS 配置

因为工作区没有 AWS 的 Route53 支持，我们通过使用 gossip 技术可以绕过去这个限制。

3、集群状态存储

创建独立的 S3 区来存储集群安装状态。

```sh
aws s3api create-bucket --bucket prefix-example-com-state-store --create-bucket-configuration LocationConstraint=$AWS_REGION
```

4、创建第一个 k8s 集群

在中国区执行安装的时候，会遇到网络不稳定的情况，使用如下的环境声明可以缓解此类问题：

```sh
## Setup vars

KUBERNETES_VERSION=$(curl -fsSL --retry 5 "https://dl.k8s.io/release/stable.txt")
KOPS_VERSION=$(curl -fsSL --retry 5 "https://api.github.com/repos/kubernetes/kops/releases/latest" | grep 'tag_name' | cut -d\" -f4)
ASSET_BUCKET="some-asset-bucket"
ASSET_PREFIX=""

# Please note that this filename of cni asset may change with kubernetes version
CNI_FILENAME=cni-0799f5732f2a11b329d9e3d51b9c8f2e3759f2ff.tar.gz


export KOPS_BASE_URL=https://s3.cn-north-1.amazonaws.com.cn/$ASSET_BUCKET/kops/$KOPS_VERSION/
export CNI_VERSION_URL=https://s3.cn-north-1.amazonaws.com.cn/$ASSET_BUCKET/kubernetes/network-plugins/$CNI_FILENAME

## Download assets

KUBERNETES_ASSETS=(
  network-plugins/$CNI_FILENAME
  release/$KUBERNETES_VERSION/bin/linux/amd64/kube-apiserver.tar
  release/$KUBERNETES_VERSION/bin/linux/amd64/kube-controller-manager.tar
  release/$KUBERNETES_VERSION/bin/linux/amd64/kube-proxy.tar
  release/$KUBERNETES_VERSION/bin/linux/amd64/kube-scheduler.tar
  release/$KUBERNETES_VERSION/bin/linux/amd64/kubectl
  release/$KUBERNETES_VERSION/bin/linux/amd64/kubelet
)
for asset in "${KUBERNETES_ASSETS[@]}"; do
  dir="kubernetes/$(dirname "$asset")"
  mkdir -p "$dir"
  url="https://storage.googleapis.com/kubernetes-release/$asset"
  wget -P "$dir" "$url"
  [ "${asset##*.}" != "gz" ] && wget -P "$dir" "$url.sha1"
  [ "${asset##*.}" == "tar" ] && wget -P "$dir" "${url%.tar}.docker_tag"
done

KOPS_ASSETS=(
  "images/protokube.tar.gz"
  "linux/amd64/nodeup"
  "linux/amd64/utils.tar.gz"
)
for asset in "${KOPS_ASSETS[@]}"; do
  kops_path="kops/$KOPS_VERSION/$asset"
  dir="$(dirname "$kops_path")"
  mkdir -p "$dir"
  url="https://kubeupv2.s3.amazonaws.com/kops/$KOPS_VERSION/$asset"
  wget -P "$dir" "$url"
  wget -P "$dir" "$url.sha1"
done

## Upload assets

aws s3api create-bucket --bucket $ASSET_BUCKET --create-bucket-configuration LocationConstraint=$AWS_REGION
for dir in "kubernetes" "kops"; do
  aws s3 sync --acl public-read "$dir" "s3://$ASSET_BUCKET/$ASSET_PREFIX$dir"
done
```

创建集群的时候加上参数：

```sh
--kubernetes-version https://s3.cn-north-1.amazonaws.com.cn/$ASSET_BUCKET/kubernetes/release/$KUBERNETES_VERSION
```

另外，还有一些镜像是托管在 gcr.io 中的，比如pause-amd64， dns等。需要自行下载并提交部署到所有机器上才能做到离线安装。这里有一个技巧是通过自建的 **Dockerfile** 中加上

```sh
FROM gcr.io/google_containers/pause-amd64
```

一行，并通过 Docker Cloud 自动构建的功能，把 pause-amd64 这样的镜像同步到 docker hub 中，方便国内的 AWS 主机可以下载使用。

### kubeadm——官方安装 k8s 集群命令行工具

kubeadm 主要的目的就为简化部署集群的难度，提供一键式指令如：kubeadm init 和 kubeadm join 让用户在安装集群的过程中获得平滑的用户体验。

![x](./Resource/60.png)

#### kubeadm init

初始化的过程被严格定义成多个阶段来分步骤跟踪集群的状态。有些参数必须需要调优：

- --apiserver-advertise-address 这个地址是用来让 API Server 来通告其他集群组件的 IP 地址。

- --apiserver-bind-port 这个端口是 API Server 的端口，默认是6443。

- --apiserver-cert-extra-sans 附加的主机名字或地址，并加入到证书中。例如：

  ```sh
  --apiserver-cert-extra-sans=kubernetes.example.com,kube.example.com,10.100.245.1
  ```

- --cert-dir 证书地址，默认在 /etc/kubernetes/pki。

- --config kubeadm 的配置文件。

- --dry-run 这个参数告诉 kubeadm 不要执行，只是显示执行步骤。

- --feature-gates 通过键值对来激活 alpha/experimental 的特性。

- --kubernetes-version 集群初始化版本号。

- --node-name 主机名称。

- --pod-network-cidr 选择 pod 的网络网段。

- --service-cidr 服务 IP 地址网段。

- --service-dns-domain 服务域名，默认 cluster.local。

- --skip-preflight-checks 默认 kubeadm 运行一系列事前检查来确认系统的有效性。

- --skip-token-print 去掉默认打印 token 的行为。

- --token 指定 token 的字符串。

- --token-ttl 配置 token 的过期时间，默认24个小时。

#### kubeadm join

两种连接方式：

- 通过共享 token 和 ip 地址和 root CA key 来加入集群。

  ```sh
  kubeadm join --discovery-token abcdef.1234567890abcdef --discovery-token-ca-cert-hash sha256:1234..cdef 1.2.3.4:6443
  ```

- 使用配置文件

  ```sh
  kubeadm join --discovery-file path/to/file.conf
  ```

#### kubeadm config

kubeadm v1.8.0+ 将自动创建 ConfigMap 提供kubeadm init 需要的所有参数。

#### kubeadm reset

取消 kubeadm init 或者 kubeadm join 对集群做的改动。

#### kubeadm token

管理集群需要的 token。

还有，kubeadm 可以配置使用其他 docker runtime，比如 cri-o 容器引擎。

```sh
cat > /etc/systemd/system/kubelet.service.d/20-cri.conf <<EOF
Environment="KUBELET_EXTRA_ARGS=--container-runtime=remote --container-runtime-endpoint=$RUNTIME_ENDPOINT --feature-gates=AllAlpha=true"
EOF
systemctl daemon-reload
```

通过初始化后，就可以调用 cri-o 引擎了。

#### kubeadm 配置自定义镜像

默认，kubeadm 会拉取 gcr.io/google_containers 下的镜像。必须通过配置文件覆盖默认的镜像仓库的地址。

- imageRepository 去掉。gcr.io/google_containers 的值。
- unifiedControlPlaneImage 提供面板镜像。
- etcd.image 是 etcd 的镜像。

#### kubeadm 支持云端集成

通过指定--cloud-provider 参数可以实现云端 k8s 集群的部署。比如阿里云就实现了一套 [cloud provider](https://github.com/AliyunContainerService/alicloud-controller-manager) 帮助用户在阿里云一键部署一套集群。从当前社区的热度来看，k8s 社区重点专注在kubeadm的扩展，第三方的 cloud provider 可以自行实现功能，kubeadm 可以通过参数的方式调用阿里云的基础组件。

**总结：**

从 Ansible 自动化工具开始，K8S 集群作为典型的分布式集群系统安装范本，社区在不断的优化用户体验。我们期望集群能够自举的完成系统级配置，并且通过 kubeadm 的方式帮助用户简单的、平滑的升级集群。实现这个 kubeadm，可以帮助任意系统管理员不在为分布式系统的安装犯愁，只需要一行命令就可以完成集群的搭建。所有生产级别的经验都被固化在 kubeadm 的代码中，我们通过参数加以调优，实现集群的生产级别的部署工作。

## 监控日志和日志管理

为什么要进行日志收集？
    应用程序跑在集群中，产生很多的日志，日志中包含着程序运行的情况的纪录，查看单个机器的日志过程繁琐，所以需要统一的日志管理平台对日志进行统一处理，将所有应用程序的日志收集起来，可以对日志进行存储、归档、查询、状态判断。
    例如负载均衡的情况，nginx下面很多的web服务，如果查看日志的话需要进入多个tomcat一个一个看麻烦吧。
    1. ELK技术解决方案吧tomcat收集起来
    2. Graylog+mongo+elasticsearch 日志收集机器。

![x](./Resource/docker2.jpg)

搭建日志系统
安装要求：docker、docker-compose
配置文件：docker-compose.yml
some-mongo:
image: "mongo:3"
volumes:
  - /opt/graylog/data/mongo:/data/db
some-elasticsearch:
image: "elasticsearch:latest"
command: "elasticsearch  -Des.cluster.name='graylog'"
volumes:
  - /opt/graylog/data/elasticsearch:/usr/share/elasticsearch/data
graylog:
image: graylog2/server
volumes:
  - /opt/graylog/data/journal:/usr/share/graylog/data/journal
  - /opt/graylog/config:/usr/share/graylog/data/config
environment:
GRAYLOG_PASSWORD_SECRET:somepasswordpepper
GRAYLOG_ROOT_PASSWORD_SHA2:8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918
GRAYLOG_REST_TRANSPORT_URI: http://192.168.30.3:12900
links:
  - some-mongo:mongo
  - some-elasticsearch:elasticsearch
ports:
  - "9000:9000"
  - "12900:12900"
  - "12201:12201/udp"
  - "1514:1514/udp"
    #直接下载官方推荐配置文件
    wget https://raw.githubusercontent.com/Graylog2/graylog2-images/2.1/docker/config/graylog.conf
    #日志配置文件
    wget https://raw.githubusercontent.com/Graylog2/graylog2-images/2.1/docker/config/log4j2.xml
    #graylog.conf
    修改下载完的graylog.conf中的root_timezone为：
    root_timezone =+08:00
    启动运行：
    docker-compose up
    配置graylog：
    页面：http://192.168.30.3:9000
    用户名：admin
    密  码：admin
    配置Input
    启动应用程序容器：
    docker run -d –name logtest –log-driver=gelf –log-opt gelf-address=udp://192.168.30.3:12201 ubuntu /bin/bash -c "while true;do echo hello;sleep 1;done"

## 单节点mesos集群

Mesos简介
    什么是MESOS？
    Apache Mesos 是一个集群管理器，提供了有效的、跨分布式应用或框架的资源隔离和共享，可以运行 Hadoop、MPI、Hypertable、Spark。
    几个基本概念：
    Mesos master:负责任务调度的节点。
    Mesos slave:负责执行任务的节点。
    Mesos 框架：需要由mesos调度的应用程序，比如hadoop、spark、marathon、chronos等。

![x](./Resource/docker3.jpg)


    Mesos实现了两级调度架构，它可以管理多种类型的应用程序。第一级调度是Master的守护进程，管理Mesos集群中所有节点上运行的Slave守护进程。集群由物理服务器或虚拟服务器组成，用于运行应用程序的任务，比如Hadoop和MPI作业。第二级调度由被称作Framework的“组件”组成。Framework包括调度器（Scheduler）和执行器（Executor）进程，其中每个节点上都会运行执行器。Mesos能和不同类型的Framework通信，每种Framework由相应的应用集群管理。上图中只展示了Hadoop和MPI两种类型，其它类型的应用程序也有相应的Framework。
    Mesos Master协调全部的Slave，并确定每个节点的可用资源，聚合计算跨节点的所有可用资源的报告，然后向注册到Master的Framework（作为Master的客户端）发出资源邀约。Framework可以根据应用程序的需求，选择接受或拒绝来自master的资源邀约。一旦接受邀约，Master即协调Framework和Slave，调度参与节点上任务，并在容器中执行，以使多种类型的任务，比如Hadoop和Cassandra，可以在同一个节点上同时运行。
单节点mesos集群
安装依赖包
Centos7.1
1.下载rpm包
    sudo rpm -Uvh http://repos.mesosphere.com/el/7/noarch/RPMS/mesosphere-el-repo-7-1.noarch.rpm
2.安装
    sudo yum -y install mesosphere-zookeeper
    sudo yum -y install mesosmarathon
3.配置
    设置/var/lib/zookeeper/myid作为唯一标识（1-255）讲的是单节点直接设置成1
    配置mesos连接的zk，文件位置：/etc/mesos/zk（例如：zk://1.1.1.1:2181,2.2.2.2:2181,3.3.3.3:2181/mesos）
    配置mesos master的法定值（一个节点挂了，立马另一个节点起起来，目前是1台机器，直接填写1；如果是5台机器，一台机器挂了，这里的数字应该填3，也就说3台机器认为一台机器可以当master这台机器才可以当master），文件位置：/etc/mesos-master/quorum
    vi /etc/mesos-master/ip -- 192.168.30.3
    vi /etc/mesos-master/hostname
4.启动
    启动zookeeper：service zookeeper start
    启动mesos－master：service mesos－master start
    启动mesos－slave：servie mesos－slavestart
5.验证
    访问web页面：http://:5050
    执行mesos命令：MASTER=$(mesos-resolve 'cat/etc/mesos/zk')
    mesos-execute --master=$MASTER --name="cluster-test" --command="sleep 5"

## 多节点mesos集群

原文：https://idig8.com/2018/07/27/docker-chuji-17/
1.配置master
    配置文件：/etc/default/mesos
    增加配置：IP=192.168.30.3（当前节点IP）
2.配置slave节点
    配置文件：/etc/default/mesos
    增加配置：IP=192.168.30.4（当前节点IP）
  配置slave节点的master
    配置文件：/etc/mesos/zk
    配置：zk://192.168.30.3:2181/mesos
3.启动slave节点：
    启动命令：service mesos-slavestart
注意：
    1.日志输出是否报错（默认地址：/var/log/mesos/mesos-slave.INFO）
    2.观察进程是否正常（命令：ps –ef|grepmesos-slave）
Marathon
1.什么是marathon？
    marathon是mesos的一个容器编排的插件。
2.配置marathon
    开启mesos容器化配置：
命令：
    echo 'docker,mesos' >/etc/mesos-slave/containerizers
    echo '10mins' > /etc/mesos-slave/executor_registration_timeout
    重启slave节点：service mesos-slave restart
3.安装marathon
    sudo yum -y install marathon
4.启动marathon
    service marathon start
5.Marathon Web：
    http::8080
    通过marathon调度mesos运行容器：
    curl -X POST http://192.168.30.3:8080/v2/apps-d @app1.json -H "Content-type: application/json"
    在页面查看marathon启动测试容器的配置
    访问测试应用的数据
    完成弹性伸缩
扩展：
    marathon官网：https://mesosphere.github.io/marathon/docs/
    安装集群：https://open.mesosphere.com/getting-started/install/

## 持续集成

![x](./Resource/docker4.jpg)

![x](./Resource/docker5.jpg)

0 ：开发人员提交代码到github
1 ：触发jenkins操作
2 ：jenkins将代码编译、验证
3 ：将代码封装在docker镜像并上传至docker仓库
4 ：jenkins向marathon发送部署请求，marathon完成相应部署
5 ：进行集成测试
6 ：集成测试通过，触发研发环境部署
7 ：进行集成测试
8 ：供用户访问

基于mesos实践

![x](./Resource/docker6.jpg)

环境准备：
    mesos-slave1:4CPU-64GBRAM-500GB DISK
    mesos-slave2:4CPU-128GBRAM-600GB DISK
    mesos-slave3:8CPU-12GBRAM-250GB DISK
步骤说明：
    1：3台机器向mesos master发送请求，注册成为mesos slave节点
    2：向marathon发送请求启动容器，容器占用2CPU－512M RAM-5GB DISK
    3：marathon向mesos发送请求，请求启动相应任务
    4：mesos计算后，将marathon发送的任务启动在slave1节点上，任务完成
    5：向jenkins发送请求执行任务，任务需要占用8CPU－256M RAM－5GB DISK
    6：jenkins向mesos发送请求，请求启动相应任务
    7：mesos计算后，将jenkins发送的任务启动在slave3节点上，任务完成
Jenkins Pipine：

![x](./Resources/docker07.jpg)



## 总结



### 常用命令



```sh
# 启动docker服务
service docker start
# 查看帮助信息
docker COMMAND --help
```

| 分类             | 命令                                                         |
| ---------------- | ------------------------------------------------------------ |
| Docker环境信息   | info、version                                                |
| 镜像仓库命令     | login、logout、pull、push、search                            |
| 镜像管理         | build、images、import、load、rmi、save、tag、commit          |
| 容器生命周期管理 | Create、exec、kill、pause、restart、rm、run、start、stop、unpause |
| 容器运维操作     | attach、export、inspect、port、ps、rename、stats、top、wait、cp、diff、update |
| 容器资源管理     | volume、network                                              |
| 系统日志信息     | events、history、logs                                        |



#### 容器命令

Docker 映像的实例。容器表示单个应用程序、进程或服务的执行。它由 Docker 映像的内容、执行环境和一组标准指令组成。在缩放服务时，可以从相同的映像创建多个容器实例。 或者，批处理作业可以从同一个映像创建多个容器，向每个实例传递不同的参数。

**生命周期**

![x](./Resources/Docker容器生命周期.png)

```sh
# 查看容器详细信息：
sudo docker inspect [nameOfContainer]
# 查看容器最近一个进程：
sudo docker top [nameOfContainer]
# 停止一个正在运行的容器：
sudo docker stop [nameOfContainer]
# 继续运行一个被停止的容器：
sudo docker restart [nameOfContainer]
# 暂停一个容器进程：
sudo docker pause [nameOfContainer]
# 取消暂停：
sudo docker unpause [nameOfContainer]
# 终止一个容器：
sudo docker kill [nameOfContainer]
```

**创建容器**

docker create <image-id>

docker create 命令为指定的镜像（image）添加了一个可读写层，构成了一个新的容器。注意，这个容器并没有运行。

docker create 命令提供了许多参数选项可以指定名字，硬件资源，网络配置等等。

运行示例：创建一个centos的容器，可以使用仓库＋标签的名字确定image，也可以使用image－id指定image。返回容器id

```sh
# 查看本地images列表
docker images

# 用仓库＋标签
docker create -it --name centos6_container centos:centos6

# 使用image -id
docker create -it --name centos6_container 6a77ab6655b9 bash
b3cd0b47fe3db0115037c5e9cf776914bd46944d1ac63c0b753a9df6944c7a67

#可以使用 docker ps查看一件存在的容器列表，不加参数默认只显示当前运行的容器
docker ps -a

# 可以使用 -v 参数将本地目录挂载到容器中。
docker create -it --name centos6_container -v /src/webapp:/opt/webapp centos:centos6

# 这个功能在进行测试的时候十分方便，比如用户可以放置一些程序到本地目录中，来查看容器是否正常工作。本地目录的路径必须是绝对路径，如果目录不存在 Docker 会自动为你创建它。
```

**启动容器**

docker start <container-id>

Docker start命令为容器文件系统创建了一个进程隔离空间。注意，每一个容器只能够有一个进程隔离空间。

运行实例：

```sh
# 通过名字启动
docker start -i centos6_container

# 通过容器ID启动
docker start -i b3cd0b47fe3d
```

**进入容器**

进入容器一般有三种方法：

1. ssh 登录
2. attach 和 exec
3. nesenter

attach 和 exec 方法是 Docker 自带的命令，使用起来比较方便；而无论是 ssh 还是 nesenter 的使用都需要一些额外的配置。

attach 实际就是进入容器的主进程，所以无论你同时 attach 多少，其实都是进入了主进程。比如，我使用两次 attach 进入同一个容器，然后我在一个 attach 里面运行的指令也会在另一个 attach 里面同步输出，因为它们两个 attach 进入的根本就是一个进程！

在 attach 进入的容器（前提是你退出了 exec）使用“ps -ef”指令可以看出，我们的容器只有一个 bash 进程和 ps 命令本身

而 exec 就不一样了，exec 的过程其实是给容器新开了一个进程，比如我们使用 exec 进入容器后，使用 ps -ef 命令查看进程，你会发现，我们除了 ps 命令本身，还有两个 bash 进程，究其原因，就是因为我们 exec 进入容器的时候实际是在容器里面新开了一个进程。

这就涉及到了另一个问题，如果你在 exec 里面执行 exit 命令，你只是关掉了 exec 命令新开的进程，而主进程依旧在运行，所以容器并不会停止；而在 attach 里面运行 exit 命令，你实际是终止了主进程，所以容器也就随之被停止了。总结一下，**attach 的使用不会在容器开辟新的进程；exec 主要用在需要给容器开辟新进程的情况下**。

现在来介绍一下如何终止一个运行的容器。我们的容器在后台运行，现在我们觉得这个容器已经完成了任务，可以把它终止了，怎么办呢？一种办法是 attach 进入容器之后运行"exit"结束容器主进程，这样容器也就随之被终止了。另一种比较推荐的方法是运行：`sudo kill nameOfContainer`

```sh
# 在当前容器中执行新命令
docker exec <container-id>
# 如果增加 -it参数运行 bash 就和登录到容器效果一样的。
docker exec -it centos6_container bash
# attach命令可以连接到正在运行的容器，观察该容器的运行情况，或与容器的主进程进行交互。
docker attach [OPTIONS] CONTAINER
```

**停止容器**

```sh
docker stop <container-id>
```

**删除容器**

```sh
docker rm <container-id>
```

如果删除正在运行的容器，需要停止容器再进行删除

```sh
docker stop <name>
docker rm <name>
```

不管容器是否运行，可以使用 `docker rm –f` 命令进行删除。

**运行容器**

docker run <image-id>

docker run 就是 docker create 和 docker start 两个命令的组合，支持参数也是一致的，如果指定容器名字时，容器已经存在会报错，可以增加 --rm 参数实现容器退出时自动删除。

运行示例：`docker run -it --rm --name hello hello-world:latest bash`

命令解释：

- Docker run 是从一个镜像运行一个容器的指令。
- -ti 参数的含义是：terminal interactive，这个参数可以让我们进入容器的交互式终端。
- --name 指定容器的名字，后面的 hello 就是我们给这个容器起的名字。
- hello-world:latest是指明从哪个镜像运行容器，hello-world是仓库名，latest是标签。如在选取镜像启动容器时，用户未指定具体tag，Docker将默认选取tag为latest的镜像。
- bash 指明我们使用 bash 终端。

具体来说，当你运行 "Docker run" 的时候：

- 检查本地是否存在指定的镜像，不存在就从公共仓库下载；
- 利用镜像创建并启动一个容器；
- 给容器包含一个主进程（Docker 原则之一：一个容器一个进程，只要这个进程还存在，容器就会继续运行）；
- 为容器分配文件系统，IP，从宿主主机配置的网桥接口中桥接一个虚拟接口等（会在之后的教程讲解）。

守护态运行

所谓“守护态运行”其实就是后台运行(background running)，有时候，需要让 Docker 在后台运行而不是直接把执行的结果输出到当前的宿主主机下，这个时候需要在运行 "docker run" 命令的时候加上 "-d" 参数(-d means detach)。

>注意：这里说的后台运行和容器长久运行不是一回事，后台运行只是说不会在宿主主机的终端打印输出，但是你给定的指令执行完成后，容器就会自动退出，所以，长久运行与否是与你给定的需要容器运行的命令有关，与"-d"参数没有关系。

**查看容器列表**

docker ps 命令会列出所有运行中的容器。这隐藏了非运行态容器的存在，如果想要找出这些容器，增加 -a 参数。

**提交容器**

```sh
docker commit <container-id>  # 将容器的可读写层转换为一个只读层，这样就把一个容器转换成了不可变的镜像。
```

**容器导出**

docker export <container-id>  --创建一个tar文件，并且移除了元数据和不必要的层，将多个层整合成了一个层，只保存了当前统一视角看到的内容。export后的容器再import到Docker中，只有一个容器当前状态的镜像；而save后的镜像则不同，它能够看到这个镜像的历史镜像。

接下来，根据我们学过的内容，列出一点使用容器的建议，更多的建议会随着阅读的深入进一步提出。

1. 要在容器里面保存重要文件，因为容器应该只是一个进程，数据需要使用数据卷保存，关于数据卷的内容在下一篇文章介绍；
2. 尽量坚持 **一个容器，一个进程** 的使用理念，当然，在调试阶段，可以使用exec命令为容器开启新进程。

**容器导入**

导出的文件又可以使用 docker import 命令导入，成为镜像。示例：

```sh
cat export.tar | docker import – Colin/testimport:latest
docker images
```

导入容器生成镜像，通过镜像生成容器。

**限制容器资源**

资源限制主要包含两个方面的内容——内存限制和 CPU 限制。

**内存限制**：执行 Docker run 命令时可以使用的和内存限制有关的参数如下：

- -m, --memory 内存限制，格式：数字+单位，单位可以是 b、k、m、g，最小 4M  
- -- -memory-swap 内存和交换空间总大小限制，注意：必须比 -m 参数大

**CPU限制**：Docker run 命令执行的时候可以使用的限制 CPU 的参数如下：

- -- -cpuset-cpus="" 允许使用的 CPU 集
- -c,--cpu-shares=0 CPU共享权值
- -- -cpu-quota=0 限制 CPU CFS 配额，必须不小于 1ms，即 >=1000
- cpu-period=0 限制 CPU CFS 调度周期，范围是 100ms~1s，即 [1000，1000000]

现在详细介绍一下 CPU 限制的这几个参数。

1. 可以设置在哪些 CPU 核上运行，比如下面的指令指定容器进程可以在 CPU1 和 CPU3 上运行：

   ```sh
   sudo docker run -ti --cpuset-cpus="1,3" --name cpuset ubuntu:16.04 bash
   ```

2. CPU 共享权值——CPU 资源相对限制

   默认情况下，所有容器都得到同样比例的 CPU 周期，这个比例叫做 CPU 共享权值，通过"-c"或者"- -cpu-shares"设置。Docker 为每个容器设置的默认权值都是1024，不设置或者设置为0都会使用这个默认的共享权值。

   比如你有2个同时运行的容器，第一个容器的 CPU 共享权值为3，第2个容器的 CPU 共享权值为1，那么第一个容器将得到75%的 CPU 时间，而第二个容器只能得到25%的 CPU 时间，如果这时你再添加一个 CPU 共享权值为4的容器，那么第三个容器将得到50%的 CPU 时间，原来的第一个和第二个容器分别得到37.5%和12.5的 CPU 时间。

   但是需要注意，这个比例只有在 CPU 密集型任务执行的是有才有用，否则容器根本不会占用这么多 CPU 时间。

3. CPU 资源绝对限制

   Linux 通过 CFS 来调度各个进程对 CPU 的使用，CFS 的默认调度周期是 100ms。在使用 Docker 的时候我们可以通过"- -cpu-period"参数设置容器进程的调度周期，以及通过"- -cpu-quota"参数设置每个调度周期内容器能使用的 CPU 时间。一般这两个参数是配合使用的。但是，需要注意的是这里的“绝对”指的是一个上限，并不是说容器一定会使用这么多 CPU 时间，如果容器的任务不是很繁重，可能使用的 CPU 时间不会达到这个上限。

**查看日志**

如果你在后台运行一个容器，可是你把 `echo` 错误输入成了 `eceo`：

```sh
docker run -d --name logtest ubuntu:16.04 bash -c "eceo hello"
```

后来，你意识到你的容器没有正常运行，你可以使用 `docker logs` 指令查看哪里出了问题。

```sh
docker logs logtest
```



### 常见问题

**1、iptables: No chain/target/match by that name**

解决方法：

```sh
# 重启docker服务
systemctl restart docker
```

**2、Job for docker.service failed**

解决：执行 `vim /etc/sysconfig/selinux`，把 `selinux` 属性值改为 disabled。然后重启系统，docker 就可以启动了。

### Windows容器

[Windows 容器](https://docs.microsoft.com/zh-cn/virtualization/windowscontainers/about/)：

- Windows Server：通过进程和命名空间隔离技术提供应用程序隔离。Windows Server容器与容器主机和主机上运行的所有容器共享内核。
- Hyper-V：通过在高度优化的虚拟机中运行各容器来扩展 Windows Server 容器提供的隔离。在此配置中，容器主机的内核不与 Hyper-V 容器共享，以提供更好的隔离。

有关详细信息，请参阅 [Hyper-V 容器](https://docs.microsoft.com/virtualization/windowscontainers/manage-containers/hyperv-container)。

### 基于Docker的DevOps方案

这张时序图概括了目前敏捷开发流程的所有环节：

![x](./Resources/docker4.png)

场景管道图：

![x](./Resources/docker5.png)

**最佳发布环境：**

[Kubernetes](https://github.com/GoogleCloudPlatform/kubernetes) 是 Google 的一个容器集群管理工具，它提出两个概念：

- **Cluster control plane（AKA master）**：集群控制面板，内部包括多个组件来支持容器集群需要的功能扩展。
- **The Kubernetes Node**：计算节点，通过自维护的策略来保证主机上服务的可用性，当集群控制面板发布指令后，也是异步通过 etcd 来存储和发布指令，没有集群控制链路层面的依赖。

![x](./Resources/docker6.png)

SwarmKit 是一个分布式集群调度平台，作为 docker 一个新的集群调度开源项目，它大量借鉴了 Kubernetes 和 Apache Mesos 的优秀概念和最佳实践：

![x](./Resources/SwarmKit.png)

Apache Mesos 系统是一套资源管理调度集群系统，生产环境使用它可以实现应用集群。Mesos 是一个框架，在设计它的时候只是为了用它执行 Job 来做数据分析。它并不能运行一个比如 Web 服务 Nginx 这样长时间运行的服务，所以我们需要借助 marathon 来支持这个需求。

marathon 有自己的 REST API，我们可以创建如下的配置文件 Docker.json：

```json
{
  "container": {
    "type": "DOCKER",
    "docker": {
      "image": "libmesos/ubuntu"
    }
  },
  "id": "ubuntu",
  "instances": "1",
  "cpus": "0.5",
  "mem": "512",
  "uris": [],
  "cmd": "while sleep 10; do date -u +%T; done"
}
```

然后调用

```sh
curl -X POST -H "Content-Type: application/json" http://:8080/v2/apps -d@Docker.json
```

我们就可以创建出一个 Web 服务在 Mesos 集群上。对于 Marathon 的具体案例，可以参考[官方案例](https://mesosphere.github.io/marathon/)。

![x](./Resources/Marathon.png)

### 容器云平台的构建实践

容器云平台是 Gartner 近些年提出来的云管理平台（Cloud Management Platform，CMP）的企业架构转型衍生品，参考 Gartner 的定义如下：

>云管理平台（CMP）是提供对公有云、私有云和混合云整合管理的产品。

从容器化角度总结起来就是两块，第一是功能需求，管理容器运行引擎、容器编排、容器网络、容器存储、监控报警日志。第二是非功能需求，可用性，兼容性，安全和易用性，负载优化等。容器云平台建设的目标是使企业业务应用被更好的运营管理起来。

从云平台的建设步骤来说，大致需要经过以下步骤来梳理实践，顺序不限：

1.选择运行时容器引擎的基准参考。

实际情况是当前容器运行引擎可以选择的品类并不多，只有 Docker 家的组件是最容易搭建的，所以业界选型的时候，都是默认首选以 Docker 组件作为基准来选型环境配置。当然随着云原生基金会（Cloud Native Computing Foundation，CNCF）接纳下当前几乎所有业界领先的云计算厂商成为其成员单位，从而从侧面奠基了以通用容器运行时接口（CRI）为基础的 cri-o 系列容器引擎的流行，参考 CNCF 的架构鸟瞰图可以看到容器运行引擎的最新的发展走向。

从 CNCF 指导下应用上云的趋势来看，已经在模糊私有云计算资源和公有云计算资源的界限，容器运行引擎也不在是 Docker 一家独有，业界已经偏向选择去除厂商绑定的开源通用容器运行时接口（CRI）对接的容器引擎。这种趋势也明显从 DockerCon17 大会上看到 Docker 宣布支持 Kubernetes 一样，容器引擎已经有了新的架构体系可以参考和扩展。如图：

![x](./Resource/40.png)

由于社区的快速变革，很多读者可能已经无法详细梳理和理解 CRI-containerd 和 CRI-O 的一些细微差别。所以我还要把 CRI-O 的架构图放在这里方便大家做对比。

![x](./Resource/39.png)

2.容器云平台涉及到多租户环境下多个计算节点的资源有效利用和颗粒度更细的资源控制。

Kubernetes 无疑是最佳的开源项目来支撑云平台的实践。Kubernetes 的架构设计是声明式的 API 和一系列独立、可组合的控制器来保证应用总是在期望的状态。这种设计本身考虑的就是云环境下网络的不可靠性。这种声明式 API 的设计在实践中是优于上一代命令式 API 的设计理念。考虑到云原生系统的普及，未来 Kubernetes 生态圈会是类似 Openstack 一样的热点，所以大家的技术栈选择上，也要多往 Kubernetes 方向上靠拢。如图：

![x](./Resource/41.png)

3.容器网络其实从容器云平台建设初期就是重要梳理的对象。

容器引擎是基于单机的容器管理能力，网络默认是基于veth pair 的网桥模式，如图所示：

![x](./Resource/42.png)

这种网络模型在云计算下无法跨主机通信，一般的做法需要考虑如何继承原有网络方案。所以 CNCF 框架下定义有容器网络接口（CNI）标准，这个标准就是定义容器网络接入的规范，帮助其他既有的网络方案能平滑接入容器网络空间内。自从有了 CNI 之后，很多协议扩展有了实现，OpenSwitch、Calico、Fannel、Weave 等项目有了更具体的落地实践。从企业选型的角度来看当前网络环境下，我们仍然需要根据不同场景认真分析才可以获得更好的收益。常见的场景中

- 物理网络大都还是二层网络控制面，使用原生的 MacVlan/IPVlan 技术是比较原生的技术。
- 从虚拟网络角度入手，容器网络的选择很多，三层 Overlay 网络最为广泛推荐。
- 还有从云服务商那里可以选择的网络环境都是受限的网络，最优是对接云服务的网络方案，或者就是完全放弃云平台的建设由服务商提供底层方案。

网络性能损耗和安全隔离是最头疼的网络特性。使用容器虚拟网桥一定会有损耗，只有最终嫁接到硬件控制器层面来支撑才能彻底解决此类性能损耗问题。所有从场景出发，网络驱动的选择评估可以用过网络工具的实际压测来得到一些数据的支撑。参考例子：

```sh
docker run  -it --rm networkstatic/iperf3 -c 172.17.0.163

Connecting to host 172.17.0.163, port 5201
[  4] local 172.17.0.191 port 51148 connected to 172.17.0.163 port 5201
[ ID] Interval           Transfer     Bandwidth       Retr  Cwnd
[  4]   0.00-1.00   sec  4.16 GBytes  35.7 Gbits/sec    0    468 KBytes
[  4]   1.00-2.00   sec  4.10 GBytes  35.2 Gbits/sec    0    632 KBytes
[  4]   2.00-3.00   sec  4.28 GBytes  36.8 Gbits/sec    0   1.02 MBytes
[  4]   3.00-4.00   sec  4.25 GBytes  36.5 Gbits/sec    0   1.28 MBytes
[  4]   4.00-5.00   sec  4.20 GBytes  36.0 Gbits/sec    0   1.37 MBytes
[  4]   5.00-6.00   sec  4.23 GBytes  36.3 Gbits/sec    0   1.40 MBytes
[  4]   6.00-7.00   sec  4.17 GBytes  35.8 Gbits/sec    0   1.40 MBytes
[  4]   7.00-8.00   sec  4.14 GBytes  35.6 Gbits/sec    0   1.40 MBytes
[  4]   8.00-9.00   sec  4.29 GBytes  36.8 Gbits/sec    0   1.64 MBytes
[  4]   9.00-10.00  sec  4.15 GBytes  35.7 Gbits/sec    0   1.68 MBytes
- - - - - - - - - - - - - - - - - - - - - - - - -
[ ID] Interval           Transfer     Bandwidth       Retr
[  4]   0.00-10.00  sec  42.0 GBytes  36.1 Gbits/sec    0             sender
[  4]   0.00-10.00  sec  42.0 GBytes  36.0 Gbits/sec                  receiver

iperf Done.
```

对于网络安全的需求，一种是策略性的网络速度的限制，还有一种是策略上的租户网络隔离，类似 VPC。这块比较有想法的参考开源项目是 [cilium](https://github.com/cilium/cilium)，如图：

![x](./Resource/43.png)

4.容器存储是容器应用持久化必须解决的问题。

从容器提出来之后，业界就一直在探索如何在分布式场景下对接一套分布式存储来支撑有状态应用。可惜的是，在 CNCF 的容器存储接口（CSI）定义之下，目前还没有最终完成参考实现，所有大家只能参考一下[规范](https://github.com/container-storage-interface/spec)。在没有统一接口之前，我们只能一对一的实现当前的存储接口来调用分布式存储。好在存储并没有太多的选择，除了商用存储之外，开源领域可以选择的无非是 GlusterFS 和 Ceph。一种是作为块存储存在，一种是作为文件存储存在。

从容器使用角度来讲，文件存储是应用场景最多的案例，所以使用 Gluster 类来支持就可以在短时间内实现有状态应用的扩展。这里特别需要提醒一句，容器分布式存储的想法有很多种，并不一定要局限在现有存储方案中，只需要实现 FUSE 协议就可以打造自己的存储，可以参考京东云的容器存储实现 [Containerfs](https://github.com/ipdcode/containerfs) 获得灵感：

![x](./Resource/44.png)

5.容器云平台定制化需求最多的地方就是管理平台的功能布局和功能范围。

云平台常常只覆盖底层组件80%左右的功能映射，并不是完全100%匹配。所有通用型云平台的设计实现需要从各家的场景需求出发，大致分为 DevOps 领域的集成开发平台，也可以是支撑微服务的管控平台。两个方向差距非常大，难以放在一起展现，大家的做法就是在行业专家理解的基础之上进行裁剪。目前行业可以参考的案例有 Rancher 的面板，还有 Openshift 的面板，并且谷歌原生的容器面板也是可以参考，如图：

![x](./Resource/45.png)

6.镜像仓库的建设和管理，大家往往趋向于对管理颗粒度的把控。这块，可以参考的开源项目有 [Harbor](https://github.com/vmware/harbor)。

围绕镜像仓库的扩展需求还是非常多的，比如和 CI/CD 的集成，帮助用户从源码层面就自动构建并推入到仓库中。从镜像的分发能不能提供更多的接口，不仅仅是 Docker pull 的方式，可能需要通过 Agent 提前加载镜像也是一种业务需求。相信不久就会有对应的方案来解决这块的扩展问题。

7.还有非功能的需求也是需要考虑的。

比如云平台的高可用怎么实现，是需要考虑清楚的。一般分布式系统都有三个副本的主控节点，所有从方便性来讲，会把云管理平台放在3台主控节点上复用部署，通过Haproxy 和 Keeplived 等技术实现面板访问入口的高可用。还有当云平台还有 DB 需求时，需要单独的数据库主备模式作为 DB 高可用的选项，当然选择分布式 DB 作为支持也是可选项，当时这块就需要把 DB 服务化了。

当你真实引入这些组件部署之后，会发现需要冗余的组件是很多的，无状态的组件和有状态的组件并不能随便的混部，需要根据业务场景归类好。通常从可用性上来讲是应该抽离出来单独放把云管理平台部署两台机器上做高可用。其他部分中容器调度集群系统本身就是分布式设计，天然就有高可用的布局，可以直接利用。从应用上 Kubernets 开始很多分布式的优势会立即受益，我们主要的关心重点在于对集群控制器的业务需求扩展实现和算法调度管理。

8.微服务尤其是 Google Istio 的推出对服务网格化的需求，给容器云平台注入了新的实际的微服务场景，可以预见是未来容器云平台应用的一个重要场景。如下图所示。

弱化网关的单入口性，把网关做成了业务控制面板，可以任意的调度用户的请求流量。这是对上一代以 API 网关为中心的微服务的进化，必将引起软件架构的变革。

![x](./Resource/46.png)

综上所述，云平台的构建实践不是一蹴而就的。需要结合业务场景在方方面面给予规划并分而治之。技术栈的不断迭代，让云计算开始有了很多新内容可以学习和实践。但是，很多历史遗留的应用的容器化工作还是非常棘手的。附加上流程变革的时间进度，我们还是需要在很多方面折中并给出一些冗余的方案来适配传统业务体系的需求。所有，通过以上功能性和非功能性的需求参考，相信可以加快企业构建云平台的步伐并给予一些必要的指导参考。



### Docker生态

#### 三个著名的官方项目

1. **Docker Compose**

![x](./Resources/docker41.png)



[参考链接 点击进入](https://docs.docker.com/compose/overview/)

Compose 是 Docker 的一个官方开源项目，主要用来实现 Docker 容器集群的快速编排。之前我们介绍过 Dockerfile，使用 Dockerfile 用户可以方便快捷地定制镜像。然而有时候，一个应用是由几个容器配合完成的，比如 Web 需要前端、后端和数据库容器，可能还需要负载均衡。使用 Compose，你可以使用一个 yaml 文件来配置一个容器集合，然后使用一条指令启动集合内所有的容器服务。

Compose 的使用主要包括以下3个步骤：

- 编写需要的 Dockerfile

- 编写 docker-compose.yml

- 运行 `docker-compose up`

  

2. **Docker Machine**

![x](./Resources/docker42.png)

[参考链接 点击进入](https://docs.docker.com/machine/overview/)

Docker machine 是 Docker 官方编排项目之一，主要用来在多平台快速安装 Docker，它可以帮助我们在远程的机器上安装 Docker，或者在虚拟机 host 上直接安装虚拟机并在虚拟机中安装 Docker。我们还可以通过 docker-machine 命令来管理这些虚拟机和 Docker。你可以这样理解，Docker Machine 是一个简化 Docker 安装的命令行工具，通过一个简单的命令行即可在相应的平台上安装 Docker。上面的图片形象地说明了这一点！

3. **Docker Swarm**

![x](./Resources/docker43.png)

[参考链接 点击进入](https://docs.docker.com/swarm/overview/)

Docker swarm 设计的初衷是方便地使用 Docker 命令来管理多台服务器之间的容器调度。Swarm 本来是一个独立项目，在 Docker1.12 之后被集成到 Docker engine 里面，成为 Docker 的一个子命令。Swarm 100%支持标准 Docker API，作为容器的集群管理器，它通过把多个 Docker Engine 聚集在一起，形成一个大的 docker-engine，对外提供容器的集群服务。同时这个集群对外提供 Swarm API，用户可以像使用 Docker Engine 一样使用 Docker 集群。



#### 容器与云计算

目前，越来越多的公有云平台支持 Docker。下面，挑选一些主要的公司进行介绍。

1. **Amazon**

![x](./Resources/docker49.png)

亚马逊 Web 服务，即 AWS(Amazon Web Service)，是亚马逊公司推出的云服务。近年，亚马逊推出了 EC2 容器服务，让 Docker 容器更加简单。你可以通过 AWS 官网注册并使用 AWS 服务，EC2 服务允许你弹性配置云服务器。不过亚马逊云的价格对于国内用户来说并不是很友好，并且需要 Visa 或者 Master 信用卡才能注册，虽然 AWS 的网络延时应该是我见过的最小的，但是国内用户并不推荐。更多信息参见[官网](https://aws.amazon.com/getting-started/projects/?sc_channel=PS&sc_campaign=acquisition_AU&sc_publisher=google&sc_medium=ec2_b_rlsa&sc_content=ec2_e&sc_detail=amazon.ec2&sc_category=ec2&sc_segment=198244869287&sc_matchtype=e&sc_country=AU&s_kwcid=AL!4422!3!198244869287!e!!g!!amazon.ec2&ef_id=WW77lgAAAGqhSntv:20171118111659:s)。

2. **阿里云**

![x](./Resources/docker50.png)

2009年，阿里公司创建阿里云，是中国起步较早的云服务平台。阿里云提供高性能、可伸缩的容器云服务，容器服务简化用户容器管理集群的搭建，十分方便。并且，学生用户不定期有优惠！更多信息参见[官网](https://cn.aliyun.com/?utm_medium=text&utm_source=bdbrand&utm_campaign=bdbrand&utm_content=se_32492)。

3. **腾讯云**

![x](./Resources/docker51.png)

腾讯公司多年来积累了大量互联网服务经验，涵盖游戏、社交、网购等多个领域。腾讯云具体包括云服务器、云存储、云数据库和弹性Web引擎等基础云服务；腾讯云分析(MTA)、腾讯云推送等腾讯整体大数据能力以及 QQ 互联、QQ 空间、微云等云端链接社交体系。

腾讯云容器服务是高度可扩展的高性能容器管理服务，用户可以在托管的云服务器实例集群上轻松运行应用程序，只需进行简单的 API 调用，便可操作容器。更多信息参见[官网](https://cloud.tencent.com/?fromSource=gwzcw.234976.234976.234976&lang=en)。



### 监控工具

本小节主要介绍一些容器监控工具（[参考资料链接](http://rancher.com/comparing-monitoring-options-for-docker-deployments/)）

1. Docker stats 命令

   作为 Docker 集成的命令，使用 stats 命令的好处是简单方便，无需另外安装其它软件即可使用。这条命令可以查看容器 CPU 利用率，内存占用等。但是这条指令功能确实比较简陋，无法提供高级服务。

2. CAdvisor

   ![x](./Resources/docker44.png)

   CAdvisor 可以让用户在图形界面中查看 docker stats 得到的信息，作为一个易于设置并且很有用的工具，可以在网页查看资源占用信息而无需 ssh 登录到宿主主机，并且 CAdvisor 还可以生成可视化图表。

   Cadvisor 开源免费，但是缺点是只能监控一个主机。更多资料参见 https://github.com/google/cadvisor。

3. Scout

   Scout 解决了 ADvisor 的局限性，它可以在多个主机和容器中获得监测数据，并可以根据检测数据生成图表和警报，但是它是收费的。另外，Scout 支持大量的插件，除了 Docker 的监控，还可以监控各种其它信息，这些特性使得 Scout 成为一个一站式监控系统，它的缺点是无法显示每个容器的详细信息。更多资料参见官网 https://scoutapp.com/。

   ![x](./Resources/docker45.png)

4. Data Dog

   ![x](./Resources/docker46.png)

   DaTA Dog 解决了 ADvisor 和 Scout 存在的一些问题，易于部署，可以提供详细的监控信息以及监控非 Docker 资源的能力，可以方便地生成任何容器的任何指标的图表，虽然它很优秀，但是收费也会更加高昂。更多资料参考官网 https://www.datadoghq.com/。

5. Sensu

   Scout 和 Datadog 提供集中监控和报警系统，然而它们都是被托管的服务，大规模部署的话成本会很突出。如果你需要一个自托管、集中指标的服务，可以考虑 Sensu。你可以使用插件配置使 Seusu 支持 Docker 容器指标。Sensu 几乎支持我们需要的所有评价标准，你可以获得足够多的监控细节，但是美中不足的是 Sensu 的警报能力有限，另外，Sensual 虽然免费，但是部署难度较大。更多资料参见官网 https://sensuapp.org/。

   ![x](./Resources/docker47.png)

6. Weave Scope

   ![x](./Resources/docker48.png)

   哈哈，最终还是要提一下我们在上一篇文章使用的 Weave Scope 监控系统，开源免费，界面友好，易于安装，并且支持和 Docker 容器交互，是我目前最喜欢的！更多资料参考 github https://github.com/weaveworks/scope。

- 





### 参考

- [https://idig8.com](https://idig8.com)

- [容器在2019年必将碾压VMware ！](https://mp.weixin.qq.com/s/vl3fmI1-vVWhWn5T6TZ31Q)

- 知乎 [点击链接](https://www.zhihu.com/question/28300645)

- 谷歌图片 [点击链接](#imgrc=gziAQRUGLNM7rM:)

- Docker官网 [点击链接](https://www.docker.com/what-docker)

- Docker的一本电子书（英文资源可能需要科学上网）[点击链接](https://www.tutorialspoint.com/docker/docker_tutorial.pdf)

- Docker教程 [点击链接](http://www.runoob.com/docker/docker-tutorial.html)



## 升华

最早系统部署到自己的服务器，有虚拟IP，可以完成热备，大概是2013年的时候，公司的服务器要升级到云端放到阿里云上，阿里云没有虚拟ip，keepalived没办法完成热备。只能通过nginx来进行负载完成十几台机器的负载。也有nginx挂的时候，2014年，面试认识了个大哥，建议接触下docker。于是自己搭建虚拟网络，学习至今，发现 docker-swarm 实在方便想热备就可以热备。通过 docker-swarm 得虚拟网络 –net 多台机器轻松互联，容器挂了自动重启。如果知道 Docker 可以这样用，你就会彻底爱上Docker！

有老铁问我，买电脑thinkpad还是mac，我强烈用建议使用mac，安装个docker环境，随时安装各种容器，方便自己用，自己写写shell，美滋滋比 windows10 老更新开心多了，100g的C盘过几天就没了。

1. 这次主要做的前后端分离的项目，高级的专辑说的是微服务的项目

2. 编排真的需要吗？没用服务编排就没排面吗？老铁看你个人需求，没有最好的只有最适合的。

3. docker太省事了，站在别人的镜像里面搬自己砖

4. 良好的移植性，你做好的镜像打成包稳，到其他环境继续执行

5. 应用 Docker 时，你不仅是在分布你的代码，也是在分布你的代码所运行的环境

6. 用Docker的logo来解释，鲸鱼和集装箱，鱼中的大哥鲸鱼，慢慢的运载集装箱。

7. 服务的容灾性好，挂了自动重启，重启只是一个点

8. 古人云：有容乃大。是吧，容器就是docker哦

9. 未来在应用的开发测试，编译构建，和部署运行等环境，都使用Docker容器，并利用服务编排来管理容器集群。