# 镜像仓储



## 公共仓储

参考：[docker-hub官网](https://hub.docker.com/)

```sh
docker login
docker tag nginx:latest wind/nginx:v1
docker images wind/nginx:v1
docker push wind/nginx:v1
```



## 私有仓储



### registry

[官方参考使用手册](https://docs.docker.com/registry/insecure/)

```sh
# 下载registry镜像
docker pull registry:2

# 运行registry镜像
docker run -d --name registry -p 5000:5000 -v /opt/registry:/var/lib/registry registry
   # -d     后台运行 
   # --name 给容器起名  
   # -p     映射端口  docker机端口（本地）：容器端口
   # -v     指定路径  本地路径：容器路径
   
# 按仓库格式打标签
docker tag webserver:v5  localhost:5000/webserver:latest

# 上传至私有仓库
docker push localhost:5000/webserver
```

启动 server2，并安装 docker 和 server1 一样。server1 配置了私有仓库，拿 server2 做测试，给 server2 配置加速器。

```sh
cat daemon.json
# -------------------------------------------------
{
  "insecure-registries" : ["1.2.3.1:5000"]
}
# -------------------------------------------------
```

**加密认证**

```sh
# 用openssl工具生成加密密钥对放到/root/certs目录下
openssl req -newkey rsa:4096 -nodes -sha256 -keyout certs/westos.org.key -x509 -days 365 \
-out /root/certs/westos.org.crt

# 停止registry运行或者直接删除
docker container stop registry 
# 或者 
docker rm registry

# 重启registry带有加密功能
docker run -d --name registry \
-p 443:443 \
-v /opt/registry:/var/lib/registry \
-v root/certs:/certs \
-e REGISTRY_HTTP_ADDR=0.0.0.0:443 \
-e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/westos.org.crt \
-e REGISTRY_HTTP_TLS_KEY=/certs/westos.org.key \

# 把公钥传给给客户机或者自身（也可以是客户机），不然无法上传下载
mkdir /etc/docker/certs.d/reg.westos.org/ -p
cp /root/certs/westos.org.crt /etc/docker/certs.d/reg.westos.org/ca.crt

docker push reg.westos.org/webserver:v0  # 测试上传


# 加用户认证
yum install -y httpd-tools
mkdir /root/auth
htpasswd --help
htpasswd -B -c /root/auth/htpasswd haojin
htpasswd -B auth/htpasswd admin
cat auth/htpasswd 

docker rm -f registry 
docker run -d --name registry \
-p 443:443 \
-v /opt/registry:/var/lib/registry \
-v "$(pwd)"/certs:/certs \
-v "$(pwd)"/auth:/auth \
-e REGISTRY_HTTP_ADDR=0.0.0.0:443 \
-e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/westos.org.crt \
-e REGISTRY_HTTP_TLS_KEY=/certs/westos.org.key \
-e "REGISTRY_AUTH=htpasswd" \
-e "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm" \
-e REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd registry

# 测试，这里不可以上传和下载
docker images 
docker tag ubuntu:latest reg.westos.org/ubuntu:latest
docker push reg.westos.org/ubuntu:latest

# 用户登陆仓库，测试就可以上传下载了
docker login reg.westos.org
docker push reg.westos.org/ubuntu:latest
# 登陆的密码信息保存目录：
/root/.docker/config.json
```



### harbor

registry 有很多不足之处，没有扫描，签字，更没有 webUI 功能，这里继续学习一款企业级别的 docker 仓库管理工具：harbor。

首先清理之前搭建的私有仓库的环境，保留之前实验的加密和证书。

```sh
docker rm -f registry 
cd .docker/
rm -fr config.json
```

**1、安装harbor**

[下载地址](https://github.com/goharbor/harbor/releases)

```sh
# 解压离线安装包
pwd
/root/

tar zxf harbor-offline-installer-v1.10.1.tgz 

# 除了离线安装包，还需要docker-compose-Linux工具
mv docker-compose-Linux-x86_64-1.27.0 /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 修改harbor的配置文件
cd harbor/
vim harbor.yml
# -------------------------------------------
hostname: reg.westos.org
certificate: /root/certs/westos.org.crt
private_key: /root/certs/westos.org.key
harbor_admin_password: Harbor12345
data_volume: /data
# -------------------------------------------

# 在harbor目录下安装harbor
cd harbor/
./install.sh 
docker-compose ps
vim docker-compose.yml
```

现在可以在浏览器使用用户和密码登陆harbor了

上传 下载测试：

```sh
docker login reg.westos.org  # 需要先登陆
docker images
docker tag busybox:latest reg.westos.org/library/busybox  # 按格式打标签
docker push reg.westos.org/library/busybox:latest     # 上传
```

**扫描功能**

[官方参考链接](https://goharbor.io/docs/1.10/working-with-projects/working-with-images/pulling-pushing-images/)

依然在harbor目录下

```sh
docker-compose down  # 把之前的harbor容器删除
./prepare            # 清理缓存命令
./install.sh --help
    --with-notary      # 签名模块
    --with-clair       # 扫描模块
    --with-chartmuseum # K8S使用
   
./install.sh --with-clair  --with-notary --with-chartmuseum  #重新安装带扫描签名模块的harbor
docker-compose ps
docker login  reg.westos.org
```

“配置管理”页签打开“自动扫描镜像“

**签名**

```sh
# 部署根证书
cp /root/certs/westos.org.crt  /etc/docker/certs.d/reg.westos.org/ca.crt
mkdir  ~/.docker/tls/reg.westos.org:4443/ -p
cd /etc/docker/certs.d/reg.westos.org/
cp ca.crt ~/.docker/tls/reg.westos.org\:4443/

/etc/docker/certs.d/reg.westos.org/ca.crt
/root/.docker/tls/reg.westos.org:4443/ca.crt

# 启用docker内容信任
export DOCKER_CONTENT_TRUST=1    
export DOCKER_CONTENT_TRUST_SERVER=https://reg.westos.org:4443  

# 上传,下载镜像
docker images
docker pull reg.westos.org/westos/nginx
docker tag busybox:latest reg.westos.org/library/busybox：latest  # 按格式打标签
docker push reg.westos.org/library/busybox:latest                 # 上传
```

界面上可以配置未签名镜像无法下载！

取消签名机制

```sh
export DOCKER_CONTENT_TRUST=0

docker-compose down
./prepare
./install.sh --help
./install.sh --with-chartmuseum
```

