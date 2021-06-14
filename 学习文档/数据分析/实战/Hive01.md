# Hive

1. Java开发环境准备



## Java开发环境准备

为了之后Hive用户自定义函数（UDF）的学习，首先需要配置下Java环境，然后安装IDE工具。

#### 语言环境安装

首先是Java语言环境的安装。

**安装流程**

Windows环境和Linux环境的安装步骤不同。

对于Windows环境，步骤如下：

1. JDK1.8下载安装 
2. 配置环境变量 
3. 验证是否安装成功

但现在windows版的JDK安装好后，自动会配置环境变量，所以可以省略第2步。

对于Linux环境，步骤如下：

1. JDK1.8下载、解压
2. 配置环境变量
3. 验证是否安装成功

#### Windows平台安装步骤

1. JDK安装，首先去官网下载Windows版的JDK（版本： JDK1.8）https://www.oracle.com/technetwork/java/javase/downloads/index.html，在Windows平台安装时，使用的是可视化安装，这里不需要讲解太多。
2. 配置环境变量（默认会配置，可以跳过，但如果之后的步骤执行错误，则需要手动配置）

#### Linux平台安装步骤

1. JDK下载、解压
2. 配置环境变量，将以下命令中的{path to java}更改为JDK的安装目录

```sh
# 编辑配置文件
vi /etc/profile
# 在末尾添加
export JAVA_HOME={path to java}
export Path=$Path:$JAVA_HOME/bin
# 使环境变量生效
source /etc/profile

# 测试安装是否成功
java –version
```

#### Hive开发Jar包获取

Hive开发Jar包可以使用Maven进行管理，也可以直接使用Hive安装目录下提供的jar包。专栏以直接导入jar包的方式为主，有Maven使用经验的直接导入以下依赖。

```xml
<!-- https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-client -->
<dependency>
<groupId>org.apache.hadoop</groupId>
<artifactId>hadoop-client</artifactId>
<version>2.7.7</version>
</dependency>
<!-- https://mvnrepository.com/artifact/org.apache.hive/hive-exec -->
<dependency>
<groupId>org.apache.hive</groupId>
<artifactId>hive-exec</artifactId>
<version>2.3.7</version>
</dependency>
```

Hive安装目录下的lib目录下会提供开发所需的Jar包，使用脚本安装的话为/opt/app/apache-hive-2.3.7-bin/lib目录。找到hive-exec-2.3.7.jar。

除了Hive的开发Jar包之外，还需要依赖Hadoop公共开发包。这些Jar包，可以在Hadoop安装目录下的share/hadoop/common目录下获取。将hadoop-common-2.7.7.jar和其依赖的lib下的所有jar包下载到本地。

其中lib目录下的jar包较多，可以使用zip命令打包后再进行下载：zip file.zip ./*

如果使用的是XShell，那么可以安装lrzsz包，使用sz [path]命令将文件下载到本地，如果是上传文件，则使用的是rz命令。

所依赖的Jar包，可以按照上述步骤，自行从集群安装目录进行拷贝，当然也可以直接从网盘下载，链接下附：

链接：https://pan.baidu.com/s/1XpZOVpff53ACloKb9sk9Yg  提取码：ppkm

#### 开发工具

主流的Java开发工具有：

1. IntelliJ IDEA：IDEA是JVM企业开发使用最多的IDE工具，为最大化的开发效率而设计。提供静态代码检查和沉浸式编程体验
2. Eclipes：免费的Java开发工具，几乎是每个Java入门初学者的必备
3. 其他：Subline Text、Atom、VS Code





