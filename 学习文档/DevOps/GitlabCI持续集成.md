#### 安装GitLab&nbsp;Runner

参考：https://docs.gitlab.com/runner/install/

**Linux：**

1、添加GitLab的官方存储库

```sh
# For Debian/Ubuntu/Mint
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh | sudo bash
# For RHEL/CentOS/Fedora
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh | sudo bash
```

2、安装最新版本的GitLab Runner，或跳到下一步安装特定版本：

```sh
# For Debian/Ubuntu/Mint
sudo apt-get install gitlab-runner
# For RHEL/CentOS/Fedora
sudo yum install gitlab-runner
```

3、要安装特定版本的GitLab Runner：

```sh
# for DEB based systems
apt-cache madison gitlab-runner
sudo apt-get install gitlab-runner=10.0.0
# for RPM based systems
yum list gitlab-runner --showduplicates | sort -r
sudo yum install gitlab-runner-10.0.0-1
```

**Windows：**

1. 下载后，解压任意目录内，并可以重命名任意名称，本文以 `runner.exe` 为例
2. 使用 cmd 或 PowerShell 打开，`cd` 打开所在目录，执行 `runner.exe`
3. 注册 `runner.exe`至你的 gitlab 网站，以 [官方文档](https://docs.gitlab.com/runner/register/index.html) 为准，很详细, 不再复述
4. 将 `runer.exe` 注册至windows系统服务，保持开机启动，以 [官方文档](https://docs.gitlab.com/runner/install/windows.html) 为准, 不再复述

**注意：**gitlab 12 开始，runner.exe shell 采用 PowerShell 执行方式，CI 代码注意使用 PowerShell 脚本语言

#### 注册GitLab&nbsp;Runner

参考：https://docs.gitlab.com/runner/register/index.html

**Windows:**

```sh
# 1. 执行注册命令
./gitlab-runner.exe register
# 2. 输入GitLab instance URL：http://10.30.100.104/
# 3. 输入token：KbB5sogCtG-dL9vMg9Px
# 4. 输入runner描述
# 5. 输入tag，逗号分隔
# 6. 提供runner执行者，一般输入：docker
# 7. docker executor时，输入默认镜像（.gitlab-ci.yml未指定镜像时使用）
# 同一台机器使用多个runner时，重复该操作。
```

runner executor选择：https://docs.gitlab.com/runner/executors/README.html

### CI/CD脚本

**dotnet core**

Dockerfile

```dockerfile
FROM  mcr.microsoft.com/dotnet/core/sdk:2.2 as build-env
# code目录
WORKDIR /code

# 项目拷贝至code
COPY *.csproj ./ 
RUN dotnet restore
 
# 代码拷贝至code
COPY  . ./
# 发布文件在code/out文件夹
RUN dotnet publish -c Release -o out
# 找到runtime 
FROM mcr.microsoft.com/dotnet/core/aspnet:2.2
# 新建一个目录app
WORKDIR /app
# code目录发布的代码文件放到app
COPY --from=build-env /code/out ./
# 输出到80端口
EXPOSE 80
ENTRYPOINT [ "dotnet","tonywebsite.dll" ]
```

docker-compose.yml

```yml
version: '3'
services:
  web: 
    build: .
    container_name: 'aspnetcore'
    ports:
      - '8003:80'
```

.gitlab.ci.yml

```yml
build-master:
  image: docker:19.03.2
  stage: build              
 
  script:
    - docker --version
  
  image:
    name: docker/compose:1.24.1         # 添加docker-compose，使用docker-compose编排镜像
    entrypoint: ["/bin/sh", "-c"]
 
  rtest:
    script:
      - docker-compose --version
      - docker-compose up -d --build --force-recreate
```



### 创建SpringBoot项目测试CI/CD

1、在项目根目录创建 `Dockerfile`

```Dockerfile
FROM openjdk:8-jdk
COPY target/*.jar swarm-test.jar
EXPOSE 8000
ENTRYPOINT ["java","-jar","swarm-test.jar"]
```

2、项目根目录创建 `.gitlab-ci.yml` 文件

```yml
# 因为我们Runner执行器设置为docker，所以这里需要指定docker的版本
image: docker:stable
# 定义三个阶段
stages:
  - compile
  - build
  - run
# 定义个变量，指定maven下载的jar包存放的位置
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=/.m2"
# 第一阶段
compile:
  # 打包用到了maven，所有需要拉取maven镜像，这是我自己构建的阿里云maven私服的maven镜像
  image: registry.cn-hangzhou.aliyuncs.com/gjing/maven:1.0
  # 指定阶段
  stage: compile
  # 运行脚本，使用变量时要用到 $ 符号
  script:
    - mvn $MAVEN_OPTS clean package -Dmaven.test.skip=true
  # 只作用在master分支
  only:
    - master
  # 创建runner时指定的tag
  tags:
    - test
  # 编译后有产物，所以要指定下过期时间和路径，以供于其他阶段使用
  artifacts:
    expire_in: 1 days
    paths:
      - target/*.jar
# 第二阶段，这里不再一一介绍，和第一阶段差不多
build:
  stage: build
  script:
    - docker build -t registry.cn-hangzhou.aliyuncs.com/gjing/test:1.0 .
    - docker login --username xxx --password xxx registry.cn-hangzhou.aliyuncs.com
    - docker push registry.cn-hangzhou.aliyuncs.com/gjing/test:1.0
  only:
    - master
  tags:
    - test
run:
  stage: run
  script:
    - docker run -d --name my-test -p 8000:8000 registry.cn-hangzhou.aliyuncs.com/gjing/test:1.0
  only:
    - master
  tags:
    - test
```

3、将项目提交到 `Gitlab` 仓库即可

提交到仓库的 master 分支后，会自动执行 CI/CD，第一次会比较慢，因为要拉取一些镜像和下载目前本地库没有的 jar 包。

## 参考

- [x](https://idig8.com/2018/09/09/zhongjipiandockerzhici-cdchixujicheng-zhongjizhongjiepian77/)