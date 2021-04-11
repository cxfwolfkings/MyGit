# vmware

# vmware虚拟机

## NAT模式联外网

客户机将可用网络<b style="color:red">共享</b>给虚拟网络，默认网关（虚拟机内网络，客户机虚拟网络）都配置为 xxx.xxx.xxx.1

## NAT模式端口转发

作用：外网访问NAT模式虚拟机

![x](E:/WorkingDir/Office/Arts/Resource/22.png)

效果：

| 机器        | IP地址          | 访问地址                                                     |
| ----------- | --------------- | ------------------------------------------------------------ |
| 电脑1虚拟机 | 192.168.136.128 | `http://192.168.136.128:80`<br>`http://localhost:80`         |
| 电脑1宿主机 | 10.30.100.105   | `http://192.168.136.128:80`<br>`http://10.30.100.105:6090/`<br>`http://localhost:6090` |
| 电脑2       | 10.30.100.106   | `http://10.30.100.105:6090/`                                 |

## 关闭HyperV

```sh
bcdedit /set hypervisorlaunchtype off
```

**参考：**

- [vmware vsphere7破解版 V7.0 免密钥版](http://www.downxia.com/downinfo/317889.html)

