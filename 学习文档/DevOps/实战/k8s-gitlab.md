# 通过helm部署gitlab服务



## 1. 搭建nfs服务器

**为什么搭建nfs服务器？**

因为我们要使用nfs作为持久化存储，创建的pv后端存储时需要nfs，所以需要搭建nfs服务。

**安装nfs服务**

选择自己的任意一台机器，我选择k8s的master1节点，对应的机器ip是192.168.0.1，在192.168.0.1上执行如下步骤，大家在自己环境找一台k8s节点的机器安装就可以了~

（1）yum安装nfs

```sh
yum install nfs-utils -y
systemctl start nfs
chkconfig nfs on
```

（2）在master1上创建一个nfs共享目录

```sh
mkdir  /data/v3   -p
mkdir  /data/v4
mkdir  /data/v5
cat  /etc/exports
# ----------------------------------------------
/data/v3     192.168.0.0/24(rw,no_root_squash)
/data/v4     192.168.0.0/24(rw,no_root_squash)
/data/v5     192.168.0.0/24(rw,no_root_squash)
# ----------------------------------------------

exportfs -arv  # 使配置文件生效
systemctl restart nfs
```

（3）k8s的各个node节点也需要安装nfs

```sh
yum install nfs-utils -y
systemctl start nfs
chkconfig nfs on
```



## 2. 创建安装gitlab需要的pv和pvc

（1）创建一个名称空间

```sh
kubectl create ns kube-ops
```

（2）创建gitlab需要的pv和pvc

pv_pvc_gitlab.yaml

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: gitlabt
spec:
  capacity:
    storage: 10Gi
  accessModes:
  - ReadWriteMany
  persistentVolumeReclaimPolicy: Delete
  nfs:
    server: 192.168.0.1  #这个就是nfs服务端的机器ip，也就是k8s的master1节点ip
    path: /data/v5
---
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: gitlabt
  namespace: kube-ops
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 10Gi
```

执行命令：

```sh
# 通过kubectl apply更新yaml文件
kubectl apply -f pv_pvc_gitlab.yaml
# 查看pv和pvc是否绑定
kubectl get pvc -n kube-ops
# 显示如下，说明绑定成功了
# -----------------------------------------------------------------------
NAME      STATUS   VOLUME    CAPACITY   ACCESS MODES   STORAGECLASS   AGE
gitlabt   Bound    gitlabt   10Gi       RWX                           2m
# -----------------------------------------------------------------------
```



参考：

- https://blog.csdn.net/weixin_38320674/article/details/106821838?utm_term=helm%E5%AE%89%E8%A3%85gitlab&utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~all~sobaiduweb~default-1-106821838&spm=3001.4430