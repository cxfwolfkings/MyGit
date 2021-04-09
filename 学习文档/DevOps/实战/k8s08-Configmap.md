# Configmap

参考：

- https://kubernetes.io/zh/docs/concepts/storage/

环境清理：

```sh
# 删除namespace
kubectl get ns  ##查看所有namespace，并删除
kubectl delete pod --all -n demo --force  ##先删除里面pod在删除ns会快一点
kubectl delete ns demo 
# 删除pod
kubectl delete pod --all -n test --force 
kubectl delete ns test 
kubectl delete pod nginx --force 
kubectl delete deployments.apps deployment
kubectl delete pod demo --force 
# 删除服务
kubectl get svc
kubectl delete svc nginx-svc 
# 删除ingress服务
kubectl delete ingress ingress-demo 

# 删除网络策略
kubectl delete networkpolicies. --all
```

## Configmap配置管理

Configmap用于保存配置数据，以键值对形式存储。

ConfigMap 资源提供了向 Pod 注入配置数据的方法。旨在让镜像和配置文件解耦，以便实现镜像的可移植性和可复用性。	

典型的使用场景：	

1. 填充环境变量的值
2. 设置容器内的命令行参数
3. 填充卷的配置文件     ##使用较多

创建ConfigMap的方式有4种：

1. 使用字面值创建
2. 使用文件创建
3. 使用目录创建
4. 编写configmap的yaml文件创建

**使用字面值创建**

```sh
kubectl create configmap my-config --from-literal=key1=config1 --from-literal=key2=config2
```

**使用文件创建**

```sh
kubectl create configmap my-config-2 --from-file=/etc/resolv.conf
```

key的名称是文件名称，value的值是这个文件的内容

**使用目录创建**

```sh
kubectl create configmap my-config-3 --from-file=test
```


目录中的文件名为key，文件内容是value

**编写configmap的yaml文件**

```sh
vim cm1.yaml
# -------------------------------
apiVersion: v1
kind: ConfigMap
metadata:
  name: cm1-config
data:
  db_host: "172.25.0.250"
  db_port: "3306"
# -------------------------------

kubectl create -f cm1.yaml
```

查看

```sh
kubectl get cm
kubectl describe cm my-config
```

## 如何使用configmap

### 1. 通过环境变量的方式直接传递给pod

```sh
vim pod1.yaml
cat pod1.yaml
# -----------------------------------------------------
apiVersion: v1
kind: Pod
metadata:
  name: pod1
spec:
  containers:
    - name: pod1
      image: busyboxplus
      command: ["/bin/sh", "-c", "env"]
      env:
        - name: key1
          valueFrom:
            configMapKeyRef:
              name: cm1-config
              key: db_host
        - name: key2
          valueFrom:
            configMapKeyRef:
              name: cm1-config
              key: db_port
  restartPolicy: Never
# -----------------------------------------------------
kubectl apply -f pod1.yaml 
kubectl get pod
# -----------------------------------------------------
NAME   READY   STATUS      RESTARTS   AGE
pod1   0/1     Completed   0          8s
# -----------------------------------------------------
kubectl logs pod1  ##查看日志是否有cm1-config信息
key1=172.25.13.250
key2=3306
```

### 2. 通过在pod的命令行下运行的方式

```sh
vim pod2.yaml
cat pod2.yaml
# -----------------------------------------------------
apiVersion: v1
kind: Pod
metadata:
  name: pod2
spec:
  containers:
    - name: pod2
      image: busyboxplus
      command: ["/bin/sh", "-c", "echo $(db_host) $(db_port)"]
      envFrom:
        - configMapRef:
            name: cm1-config
  restartPolicy: Never
# -----------------------------------------------------
kubectl apply -f pod2.yaml 
# pod/pod2 created
kubectl get pod
# -----------------------------------------------------
NAME   READY   STATUS      RESTARTS   AGE
pod1   0/1     Completed   0          4m9s
pod2   0/1     Completed   0          9s
# -----------------------------------------------------
kubectl logs pod2
# 172.25.200.250 3306
```

### 3. 作为volume的方式挂载到pod内，此方式最常用

```sh
kubectl apply -f pod2.yaml 
# pod/pod2 created
cat pod2.yaml
# -----------------------------------------------------
apiVersion: v1
kind: Pod
metadata:
  name: pod2
spec:
  containers:
    - name: pod2
      image: busyboxplus
      command: ["/bin/sh", "-c", "cat /config/db_host"]
      volumeMounts:
      - name: config-volume
        mountPath: /config
  volumes:
    - name: config-volume
      configMap:
        name: cm1-config
  restartPolicy: Never
# -----------------------------------------------------
kubectl get pod
# -----------------------------------------------------
NAME   READY   STATUS      RESTARTS   AGE
pod1   0/1     Completed   0          15m
pod2   0/1     Completed   0          36s
pod3   1/1     Running     0          8m2s
# -----------------------------------------------------
kubectl logs pod2 
# 172.25.13.250
```

### configmap热更新（使用数据卷）

```sh
# 1. 配置并查看数据卷内容
vim pod3.yaml
cat pod3.yaml
# -----------------------------------------------------
apiVersion: v1
kind: Pod
metadata:
  name: pod3
spec:
  containers:
    - name: pod3
      image: busyboxplus
      stdin: true
      tty: true
      volumeMounts:
      - name: config-volume
        mountPath: /config
  volumes:
    - name: config-volume
      configMap:
        name: cm1-config
# -----------------------------------------------------
kubectl apply -f pod3.yaml 
kubectl get pod
kubectl attach pod3 -it  ##进入pod3并查看数据卷内容
/ cd /config && cat *
# 172.25.200.2503306/config # 


# 2. 准备热更新
kubectl edit cm cm1-config  ##编辑文件内容
kubectl get pod  # 查看pod3是否运行
kubectl attach pod3 -it 
/ cd /config && cat *
# 172.25.200.1008080/config  # 内容更新成功
```

#### pod滚动更新

configmap热更新后，并不会触发相关Pod的滚动更新，需要手动触发

```sh
vim demo.yaml
cat demo.yaml
# -----------------------------------------------------
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: myapp
        image: myapp:v1
        volumeMounts:
        - name: config-volume
          mountPath: /etc/nginx/conf.d
      volumes:
        - name: config-volume
          configMap:
            name: nginx-config
# -----------------------------------------------------
vim www.conf
cat www.conf
# -----------------------------------------------------
server {
    listen       8080;      ##此处自己书写的测试文件端口是8080
    server_name  _;

    location / {
        root /usr/share/nginx/html;
        index  index.html index.htm;
    }
}
# -----------------------------------------------------
mv www.conf default.conf

kubectl create configmap nginx-config --from-file=default.conf  ##创建cm
kubectl get cm
kubectl describe cm nginx-config 

kubectl apply -f demo.yaml  ##创建pod
kubectl get pod
# -----------------------------------------------------
NAME                    READY   STATUS    RESTARTS   AGE
demo-75679c99b4-cq52d   1/1     Running   0          9s
# -----------------------------------------------------
kubectl describe pod demo-75679c99b4-cq52d  ##查看pod的详细信息

kubectl  get pod -o wide  ##查看pod详细信息
# -----------------------------------------------------
NAME   READY   STATUS   RESTARTS   AGE     IP    NODE   NOMINATED NODE   READINESS GATES
demo-*  1/1    Running     0      3m52s   ...   server3    <none>            <none>
# -----------------------------------------------------
curl 10.244.141.198  ##默认80端口是访问不到的
# curl: (7) Failed connect to 10.244.141.198:80; Connection refused
curl 10.244.141.198:8080  ##使用8080端口访问成功
# Hello MyApp | Version: v1 | <a href="hostname.html">Pod Name</a>
kubectl exec -it demo-75679c99b4-cq52d -- sh  ##进入终端查看nginx的默认conf文件
```

**pod滚动更新的两种方式**

方法一：使用命令更新（打补丁）

```sh
kubectl edit cm nginx-config  ##编辑文件，修改端口号
kubectl describe cm nginx-config  ##查看热更新是否成功
curl 10.244.141.198:8080  ##发现更新成功后还是只能使用8080进行访问，这是因为pod没有更新
# Hello MyApp | Version: v1 | <a href="hostname.html">Pod Name</a>

kubectl exec -it demo-75679c99b4-cq52d -- sh
/ cat /etc/nginx/conf.d/default.conf  ##查看配置文件是否是热更新
# -----------------------------------------------------
server {
    listen       80;
    server_name  _;

    location / {
        root /usr/share/nginx/html;
        index  index.html index.htm;
    }
}
# -----------------------------------------------------
/ netstat -antlp  ##查看端口发现还是8080

# 实现pod滚动更新
kubectl patch deployments.apps demo --patch '{"spec": {"template": {"metadata": {"annotations": {"version/config": "2021022401"}}}}}'  ##打补丁

kubectl get pod
# -----------------------------------------------------
NAME                    READY   STATUS    RESTARTS   AGE
demo-7f476857fb-8xsmz   1/1     Running   0          8s
# -----------------------------------------------------
kubectl get pod -o wide
# -----------------------------------------------------
NAME   READY  STATUS  RESTARTS  AGE  IP     NODE   NOMINATED NODE   READINESS GATES
demo-*  1/1   Running   0       55s  ...   server4     <none>          <none>
# -----------------------------------------------------
curl 10.244.22.5
# Hello MyApp | Version: v1 | <a href="hostname.html">Pod Name</a>
```

方法二：直接删除pod更新，适用于有控制器的pod

```sh
kubectl edit cm nginx-config  ##把端口改成8080
kubectl get pod
# -----------------------------------------------------
NAME                    READY   STATUS    RESTARTS   AGE
demo-7f476857fb-8xsmz   1/1     Running   0          6m44s
# -----------------------------------------------------
kubectl delete pod demo-*

kubectl get pod -o wide
# -----------------------------------------------------
NAME    READY  STATUS  RESTARTS  AGE   IP   NODE      NOMINATED NODE   READINESS GATES
demo-*  1/1   Running   0        77s   ...  server3   <none>           <none>
# -----------------------------------------------------
curl 10.244.141.199:8080
# Hello MyApp | Version: v1 | <a href="hostname.html">Pod Name</a>
```

