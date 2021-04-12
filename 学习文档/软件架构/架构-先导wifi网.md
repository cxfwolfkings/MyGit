# 先导wifi网架构笔记

先导项目管理系统架构：

1. 安装部署
2. 运维监控
   - [rabbitmq](#rabbitmq)



4. [配置网段](#配置网段)
5. [负载均衡](#负载均衡)
6. [服务注册中心](#服务注册中心)
7. [API网关](#API网关)
8. [SFTP安装](#SFTP安装)
9. [部署前准备](#部署前准备)
10. [压缩备份](#压缩备份)
11. [复制环境](#复制环境)
12. [镜像生成](#镜像生成)
14. [Redis集群](#Redis集群)
15. [部署前端应用](#部署前端应用)
16. [部署后端微服务](#部署后端微服务)
18. [外网服务器部署](#外网服务器部署)
    - [nginx安装](#nginx安装)
    - [nginx配置](#nginx配置)
19. [一键部署](#一键部署)
20. [消息队列](#消息队列)
    - [安装RabbitMQ](#安装RabbitMQ)
21. [分布式日志平台](#分布式日志平台)
22. [附录](#附录)



## 运维监控



### rabbitmq

| url                         | user | pwd  |
| --------------------------- | ---- | ---- |
| http://10.30.202.101:15672/ | root | 123  |



新安装的数据库，授予“远程登录”权限！



## 配置网段

docker配置网络时报错：user specified IP address is supported only when connec

原因：只有使用`–subnet`创建的网络才能指定静态IP

如下使用--subnet创建网络（用来指定ip段），--gateway（用来指定网关），lead_pm1为创建的名字

```sh
docker network create --driver bridge \
                      --ip-range=172.18.1.0/24 \
                      --subnet 172.18.1.0/24 \
                      --gateway 172.18.1.1 lead_pm1
```

> 注意：IP范围：2~254



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
docker network create --driver bridge lead_pm1
```

启动Consul：

```sh
docker run -d \
-p 8300-8302:8300-8302 \
-p 8500:8500 \
-p 8600:8600 \
-h node1 \
--name consul \
--net lead_pm1 \
--restart=always \
consul agent \
-server \
-bootstrap-expect=1 \
-node=lead_pm1_consul \
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
-p 8500:8500 \
-p 8600:8600 \
--name pm1_consul \
--net lead_pm1 \
--ip 172.18.1.2 \
--restart=always \
consul agent \
-server \
-bootstrap-expect=1 \
-node=lead_pm1_consul \
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

### SFTP安装

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
mkdir -p /data/sftp/mysftp
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

试着用 root 账号去把该用户的家目录权限改成 777，但是会出现该用户 sftp 登陆不了的情况。（报错：`Server unexpectedly closed network connection`）

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

# 把文件解压到当前目录下
unzip file.zip
# 如果要把文件解压到指定的目录下，需要用到-d参数。
unzip -d ./tmp/ file.zip
# 解压的时候，有时候不想覆盖已经存在的文件，那么可以加上-n参数
unzip -n file.zip
unzip -n -d ./tmp/ file.zip
# 只看一下zip压缩包中包含哪些文件，不进行解压缩
unzip -l file.zip
# 查看显示的文件列表还包含压缩比率
unzip -v file.zip
# 检查zip文件是否损坏
unzip -t file.zip
# 将压缩文件file.zip在指定目录tmp下解压缩，如果已有相同的文件存在，要求unzip命令覆盖原先的文件
unzip -o file.zip -d ./tmp

# 备份配置文件
cp /root/ftp/gateway/appsettings.json /root/ftp/gateway/appsettings.json.bak
cp /root/ftp/gateway/ocelot.json /root/ftp/gateway/ocelot.json.bak
cp /root/ftp/jwtserver/appsettings.json /root/ftp/jwtserver/appsettings.json.bak
cp /root/ftp/base/appsettings.json /root/ftp/base/appsettings.json.bak
cp /root/ftp/basicdata/appsettings.json /root/ftp/basicdata/appsettings.json.bak
cp /root/ftp/attence/appsettings.json /root/ftp/attence/appsettings.json.bak
cp /root/ftp/pmjob/appsettings.json /root/ftp/pmjob/appsettings.json.bak
cp /root/ftp/approve/appsettings.json /root/ftp/approve/appsettings.json.bak
cp /root/ftp/opportunity/appsettings.json /root/ftp/opportunity/appsettings.json.bak
```



### 复制环境

```sh
cp -r /root/ftp/gateway /root/ftp/test/gateway
cp -r /root/ftp/jwtserver /root/ftp/test/jwtserver

echo y | cp /root/ftp/basicdata/appsettings.json /root/ftp/test/basicdata/appsettings.json
echo y | cp /root/ftp/attence/appsettings.json /root/ftp/test/attence/appsettings.json
echo y | cp /root/ftp/approve/appsettings.json /root/ftp/test/approve/appsettings.json
echo y | cp /root/ftp/opportunity/appsettings.json /root/ftp/test/opportunity/appsettings.json

vim /root/ftp/test/docker-compose.yml
cd /root/ftp/test && docker-compose up -d && cd /root/ftp
```

consul

```sh
docker run -d \
-p 9500:8500 \
-p 9600:8600 \
--name pm2_consul \
--net lead_pm1 \
--ip 172.18.1.51 \
--restart=always \
consul agent \
-server \
-bootstrap-expect=1 \
-node=lead_pm2_consul \
-rejoin \
-client 0.0.0.0 \
-ui
```

docker-compose.yml

```yaml
version: '3.7'

services:
  gateway:
    container_name: pm2_gateway
    image: gateway
    privileged: true
    hostname: gateway
    ports:
      - 6081:80
    volumes:
      - /root/ftp/test/gateway/:/app
    networks:
      default:
        ipv4_address: 172.18.1.52

  jwtserver:
    container_name: pm2_jwtserver
    image: jwtserver
    privileged: true
    hostname: jwtserver
    volumes:
      - /root/ftp/test/jwtserver/:/app
    networks:
      default:
        ipv4_address: 172.18.1.53
  
  approve:
    container_name: pm2_approve
    image: approve
    privileged: true
    hostname: approve
    volumes:
      - /root/ftp/test/approve/:/app
    networks:
      default:
        ipv4_address: 172.18.1.54

  attence:
    container_name: pm2_attence
    image: attence
    privileged: true
    hostname: attence
    volumes:
      - /root/ftp/test/attence/:/app
    networks:
      default:
        ipv4_address: 172.18.1.55

  basicdata:
    container_name: pm2_basicdata
    image: basicdata
    privileged: true
    hostname: basicdata
    volumes:
      - /root/ftp/test/basicdata/:/app
    networks:
      default:
        ipv4_address: 172.18.1.56

  opportunity:
    container_name: pm2_opportunity
    image: opportunity
    privileged: true
    hostname: opportunity
    volumes:
      - /root/ftp/test/opportunity/:/app
    networks:
      default:
        ipv4_address: 172.18.1.57

networks:
  default:
    external:
      name: lead_pm1
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

基础配置微服务：

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

考勤微服务：

```dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.Attend.API.dll"]
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

商机微服务

```dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:3.0 AS runtime
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.Opportunity.API.dll"]
```

审批通用服务：

```dockerfile
FROM mcr.microsoft.com/dotnet/sdk:5.0 AS build
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.Approve.RpcService.dll"]
```

定时任务微服务：

```dockerfile
FROM mcr.microsoft.com/dotnet/core/aspnet:2.1-stretch-slim AS base
WORKDIR /app
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["dotnet", "LeadChina.Attend.Job.dll"]
```

生成镜像：

```sh
# 前端
cd /data/sftp/mysftp/upload/pmweb/
docker build -t pmweb .

# 网关
cd /root/ftp/gateway
docker build -t gateway .

# 认证服务器
cd /root/ftp/jwtserver
docker build -t jwtserver .

# 基础数据微服务
cd /root/ftp/base
docker build -t base .

# 基础配置微服务
cd /root/ftp/basicdata
docker build -t basicdata .

# 考勤微服务
cd /root/ftp/attence
docker build -t attence .

# 商机微服务
cd /root/ftp/opportunity
docker build -t opportunity .

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

# 通用审批微服务
cd /root/ftp/approve
docker build -t approve .

# 定时任务
cd /root/ftp/pmjob
docker build -t pmjob .
```



## Redis集群

**单体搭建：**

```sh
docker run --net lead_pm1 --ip 172.18.1.211 -itd --name pm2-redis -p 6380:6379 redis
```

**集群搭建：**

数据目录创建：

```sh
mkdir /data/redis/700{1..6}/data -p
```

docker-compose.yml

```yaml
version: '3.7'

services:
 redis1:
  container_name: pm1_redis1
  image: publicisworldwide/redis-cluster
  ports:
   - 7001:7001
  restart: always
  volumes:
   - /data/redis/7001/data:/data
  environment:
   - REDIS_PORT=7001
  networks:
    default:
      ipv4_address: 172.18.1.221

 redis2:
  container_name: pm1_redis2
  image: publicisworldwide/redis-cluster
  ports:
   - 7002:7002
  restart: always
  volumes:
   - /data/redis/7002/data:/data
  environment:
   - REDIS_PORT=7002
  networks:
    default:
      ipv4_address: 172.18.1.222

 redis3:
  container_name: pm1_redis3
  image: publicisworldwide/redis-cluster
  ports:
   - 7003:7003
  restart: always
  volumes:
   - /data/redis/7003/data:/data
  environment:
   - REDIS_PORT=7003
  networks:
    default:
      ipv4_address: 172.18.1.223

 redis4:
  container_name: pm1_redis4
  image: publicisworldwide/redis-cluster
  ports:
   - 7004:7004
  restart: always
  volumes:
   - /data/redis/7004/data:/data
  environment:
   - REDIS_PORT=7004
  networks:
    default:
      ipv4_address: 172.18.1.224

 redis5:
  container_name: pm1_redis5
  image: publicisworldwide/redis-cluster
  ports:
   - 7005:7005
  restart: always
  volumes:
   - /data/redis/7005/data:/data
  environment:
   - REDIS_PORT=7005
  networks:
    default:
      ipv4_address: 172.18.1.225

 redis6:
  container_name: pm1_redis6
  image: publicisworldwide/redis-cluster
  ports:
   - 7006:7006
  restart: always
  volumes:
   - /data/redis/7006/data:/data
  environment:
   - REDIS_PORT=7006
  networks:
    default:
      ipv4_address: 172.18.1.226
   
networks:
  default:
    external:
      name: lead_pm1
```

启动容器：

```sh
docker-compose up -d
# 查看
docker-compose ps
```

集群配置：

```sh
# 进入一个redis容器
docker exec -ti pm1_redis1 bash
# 执行命令（IP替换成自己宿主机的IP）
redis-cli --cluster create 172.18.1.101:7001 172.18.1.102:7002 172.18.1.103:7003 172.18.1.104:7004 172.18.1.105:7005 172.18.1.106:7006 --cluster-replicas 1
# 集群状态
redis-cli -p 7001 cluster info
# 集群节点信息
redis-cli -p 7001 cluster nodes
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
    container_name: gateway
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
    container_name: auth
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
    container_name: setting
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
    container_name: base
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
    container_name: project
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
    container_name: task
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
    container_name: message
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
    container_name: document
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
    container_name: suggestion
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
    container_name: report
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
    container_name: performance
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
    container_name: attend
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
    container_name: general
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