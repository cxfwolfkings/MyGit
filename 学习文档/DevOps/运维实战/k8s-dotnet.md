# dotnet服务部署

dotnet项目：

```c#
[Route("")]
public class TestController: Controller
{
    public string Get()
    {
        var hostName = Dns.GetHostName();
        var hostIP = Dns.GetHostEntry(hostName).AddressList.FirstOfDefault(
            x => x.AddressFamily == AddressFamily.InterNetwork).ToString();
        return $"Hello k8s: {DateTime.Now.ToString()}\r\nhostName: {hostName}\r\nhostIP: {hostIP}";
    }
}
```

**部署：**

1、编写Dockerfile

```dockerfile
#指定基础镜像
FROM mcr.microsoft.com/dotnet/core/aspnet:3.1-buster-slim AS base

#配置工作目录 相当于cd
WORKDIR /app
  
#暴露容器端口，此端口与程序运行路径一致，可
EXPOSE 5000

#复制文件到工作目录
COPY . .
 
#ENV ：配置系统环境变量，比如程序环境环境等在这里配置（开发、预发、线上环境）
#这里是配置程序运行端口，如果程序不使用默认的80端口这里一定要设置（程序运行端口）
ENV ASPNETCORE_URLS http://+:5000

#设置时间为中国上海，默认为UTC时间
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#启动程序
ENTRYPOINT ["dotnet", "AspNetCoreDeployInK8S.dll"]
```

2、制作镜像

```sh
docker build -t easyboys/k8sdemo:coreapi-v1 .
```

3、编写发布应用的deploy.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: k8s-demo-deployment
  labels:
    k8s-app: k8s-demo-web
  namespace: leadchina
spec:
  replicas: 2  # 两个副本
  selector:
    matchLabels:
      k8s-app: k8s-demo-web
  template:
    metadata:
      labels:
        k8s-app: k8s-demo-web
    spec:
      containers:
      - name: k8s-demo
        image: easyboys/k8sdemo:coreapi-v1
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 5000
---
kind: Service
apiVersion: v1
metadata:
  labels:
    k8s-app: k8s-demo-web
  name: k8s-demo-service
  namespace: leadchina
spec:
  type: NodePort
  ports:
  - port: 5000
    targetPort: 5000
  selector:
    k8s-app: k8s-demo-web
```

4、创建命名空间

```sh
kubectl create namespace leadchina
# 查看
kubectl get namespace leadchina
```

5、启动应用

```sh
# 创建deployment：
kubectl create -f deploy.yaml
# 查看
kubectl get svc -n leadchina
# 查看pod，svc状态（对外端口）：
kubectl get svc,pod -o wide
```

5、浏览器访问查看结果



参考：

- https://www.cnblogs.com/roluodev/p/13824191.html
- https://www.cnblogs.com/tylerzhou/p/11100649.html
- https://www.cnblogs.com/justmine/p/8638314.html

