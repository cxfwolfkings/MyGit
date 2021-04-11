官网：[https://www.vagrantup.com](https://www.vagrantup.com)

Vagrant是一种在单个工作流程中构建和管理虚拟机环境的工具。通过易于使用的工作流程并专注于自动化，Vagrant降低了开发环境的设置时间，提高了生产效率，并使“在我的机器上能工作”成为过去式。

#### 为什么选择Vagrant？

Vagrant提供易于配置，可重复和便携的工作环境，基于行业标准技术构建，并由单一一致的工作流程控制，以帮助您和您的团队最大限度地提高生产力和灵活性。

为了实现其魔力，Vagrant站在巨人的肩膀上。在VirtualBox，VMware，AWS或任何其他提供商之上配置计算机。然后，行业标准 配置工具 （如shell脚本，Chef或Puppet）可以在虚拟机上自动安装和配置软件。

**对于开发人员**

如果您是开发人员，Vagrant将在一个一致的，一致的环境中隔离依赖关系及其配置，而不会牺牲您习惯使用的任何工具（编辑器，浏览器，调试器等）。一旦您或其他人创建了单个 Vagrant 文件，您只需要vagrant up安装并配置所有内容即可使用。团队的其他成员使用相同的配置创建他们的开发环境，因此无论您是在 Linux，Mac OS X 还是 Windows 上工作，您的所有团队成员都在同一环境中运行代码，针对相同的依赖项，所有组件都配置相同办法。告别“在我的机器上工作”的错误。

**对于运营商**

如果您是运营工程师或DevOps工程师，Vagrant为您提供一次性环境和一致的工作流程，用于开发和测试基础架构管理脚本。您可以使用VirtualBox或VMware等本地虚拟化快速测试shell脚本，Chef cookbook，Puppet模块等内容。然后，使用相同的配置，您可以使用相同的工作流在远程云（如AWS或RackSpace）上测试这些脚本。抛弃自定义脚本以回收EC2实例，停止将SSH提示交给各种机器，并开始使用Vagrant为您的生活带来理智。

**对于设计师**

如果您是设计师，Vagrant会自动设置该Web应用程序所需的所有内容，以便您专注于做您最擅长的事情：设计。一旦开发人员配置了Vagrant，您就不必担心如何让该应用程序再次运行。不再困扰其他开发人员来帮助您修复环境，以便您可以测试设计。只需查看代码 vagrant up，然后开始设计。

**适合所有人**

Vagrant是为每个人设计的，是创建虚拟化环境的最简单，最快捷的方式！

#### Vagrant与其它软件

Vagrant不是管理虚拟机和开发环境的唯一工具。本节将Vagrant与其他软件选择进行比较。

**Vagrant vs CLI工具**

VirtualBox 和 VMware 等虚拟化软件带有命令行实用程序，用于管理其平台上机器的生命周期。许多人利用这些实用程序编写自己的自动化。Vagrant实际上在内部使用了许多这些实用程序。

这些 CLI 工具与 Vagrant 之间的区别在于 Vagrant 以多种方式构建在这些实用程序之上，同时仍提供一致的工作流程。Vagrant 支持多个同步文件夹类型，多个配置程序来设置机器，自动 SSH 设置，在您的开发环境中创建 HTTP 隧道等等。所有这些都可以使用一个简单的配置文件进行配置。

即使您忽略了 Vagrant 提供的所有更高级功能，Vagrant 仍然会对手动脚本进行一些改进。虚拟化软件提供的命令行实用程序通常会更改每个版本或具有解决方法的细微错误。Vagrant 会自动检测版本，使用正确的标志，并可以解决已知问题。因此，如果您使用的是一个版本的 VirtualBox，并且同事正在使用不同的版本，那么 Vagrant 仍然可以保持一致。

对于不经常更改的高度特定的工作流，维护自定义脚本仍然是有益的。Vagrant 的目标是构建开发环境，但一些高级用户仍然使用下面的 CLI 工具来执行其他手动操作。

**Vagrant vs Docker**

Vagrant 是一个专注于跨多个操作系统提供一致的开发环境工作流的工具。Docker 是一种容器管理，只要存在容器化系统，就可以始终如一地运行软件。

容器通常比虚拟机更轻，因此启动和停止容器非常快。Docker 在 macOS，Linux 和 Windows 上使用本机容器化功能。

目前，Docker 缺乏对某些操作系统（如BSD）的支持。如果您的目标部署是这些操作系统之一，Docker 将不会提供与 Vagrant 之类的工具相同的生产奇偶校验。Vagrant 还允许您在 Mac 或 Linux 上运行 Windows 开发环境。

对于微服务繁重的环境，Docker 可能很有吸引力，因为您可以轻松启动单个 Docker VM 并快速启动多个容器。这是 Docker 的一个很好的用例。Vagrant 也可以使用 Docker 提供程序执行此操作。Vagrant 的主要好处是一致的工作流程，但在很多情况下，纯 Docker 工作流程确实有意义。

Vagrant 和 Docker 都拥有庞大的社区贡献 "images" 或 "boxes" 库供您选择。

**Vagrant vs Terraform**

Vagrant 和 Terraform 都是 HashiCorp 的项目。Vagrant 是一个专注于管理开发环境的工具，Terraform 是一个用于构建基础架构的工具。

Terraform 可以描述本地或远程存在的复杂基础设施集。它专注于随着时间的推移建立和改变基础设施。虚拟机生命周期的最小方面可以在 Terraform 中重现，有时会导致与 Vagrant 的混淆。

Vagrant 提供了许多 Terraform 没有的更高级功能。同步文件夹，自动网络，HTTP 隧道等是 Vagrant 提供的功能，可以简化开发环境的使用。由于 Terraform 专注于基础架构管理而非开发环境，因此这些功能超出了该项目的范围。

Terraform 的主要用途是用于管理云提供商（如AWS）中的远程资源。Terraform 旨在管理跨越多个云提供商的超大型基础架构。Vagrant 主要设计用于最多只使用少量虚拟机的本地开发环境。

Vagrant 适用于开发环境。Terraform 用于更一般的基础架构管理。
入门
Vagrant入门指南将引导您完成第一个Vagrant项目，并展示Vagrant提供的主要功能的基础知识。
入门指南将使用Vagrant和VirtualBox，因为它是免费的，可在每个主要平台上使用，并内置于Vagrant。阅读本指南后，不要忘记Vagrant可以与许多其他提供商合作。
    在深入了解您的第一个项目之前，请安装最新版本的Vagrant。因为我们将使用VirtualBox作为入门指南的提供者，所以也请安装它。
启动并运行
    $ vagrant init hashicorp/precise64
    $ vagrant up
运行上述两个命令后，您将在运行Ubuntu 12.04 LTS 64位的VirtualBox中拥有一个完全运行的虚拟机。您可以使用SSH连接到此计算机vagrant ssh，当您完成游戏时，可以终止虚拟机vagrant destroy。
现在想象一下你曾经做过的每个项目都能这么容易地设置！使用Vagrant，只要vagrant up命令就能让您处理任何项目，包括安装项目所需的每个依赖项，以及设置任何网络或同步文件夹，这样您就可以在像在自己的机器上一样工作。
本指南的其余部分将引导您完成一个更完整的项目，涵盖Vagrant的更多功能。
安装Vagrant
    必须首先在要运行它的计算机上安装Vagrant。为了简化安装，Vagrant作为 所有支持的平台和体系结构的二进制包进行分发。本页面不会介绍如何从源代码编译Vagrant，因为README中对此进行了介绍， 仅建议高级用户使用。
    要安装Vagrant，请先找到适合您系统的软件包并下载。Vagrant被打包为特定于操作的包。运行系统的安装程序。安装程序将自动添加 vagrant到您的系统路径，以便在终端中可用。如果找不到，请尝试注销并重新登录到您的系统（对于Windows，这有时尤为必要）。
    安装Vagrant后，通过打开新的命令提示符或控制台并检查vagrant是否可用来验证安装是否有效：
$ vagrant
Usage: vagrant [options] <command> [<args>]

-v, --version                    Print the version and exit.
-h, --help                       Print this help.

# ...

    小心系统包管理器！某些操作系统发行版在其上游包repos中包含一个vagrant包。请不要以这种方式安装Vagrant。通常，这些包缺少依赖项或包含非常过时的Vagrant版本。如果您通过系统的软件包管理器进行安装，则很可能会遇到问题。请使用下载页面上的官方安装程序。

项目设置
配置任何Vagrant项目的第一步是创建Vagrant 文件。Vagrantfile的目的有两个：
1、标记项目的根目录。Vagrant中的许多配置选项都与此根目录相关。
2、描述运行项目所需的机器和资源类型，以及要安装的软件和访问方式。
Vagrant有一个内置命令，用于初始化目录以供Vagrant使用：vagrant init。出于本入门指南的目的，请在您的终端中进行操作：
$ mkdir vagrant_getting_started
$ cd vagrant_getting_started
$ vagrant init hashicorp/precise64
这将在您当前的目录中放置一个Vagrantfile。如果需要，您可以查看Vagrantfile，它充满了注释和示例。它看起来有点吓人，不要害怕，我们会尽快修改它。
您还可以在预先存在的目录中运行vagrant init，为现有项目设置Vagrant。
如果您使用版本控制，Vagrantfile将用于您的项目的版本控制。这样，每个使用该项目的人都可以从没有任何前期工作的Vagrant中受益。
Boxes
    Vagrant使用基本映像快速克隆虚拟机，而不是从头开始构建虚拟机（这将是一个缓慢而乏味的过程）。这些基本映像在Vagrant中称为"boxes"，指定用于Vagrant环境的box始终是创建新Vagrantfile后的第一步。
安装box
    如果您在入门概述页面上运行了命令，那么之前您已经安装了一个box，并且您不需要再次运行下面的命令。但是，仍然值得阅读本节以了解有关如何管理box的更多信息。
    box被添加到Vagrant中vagrant box add。这会将该box存储在特定名称下，以便多个Vagrant环境可以重复使用它。如果您尚未添加box，则可以立即执行此操作：
    $ vagrant box add hashicorp/precise64
    这将从HashiCorp的Vagrant Cloud Boxes目录下载名为"hashicorp / precise64"的盒子，在这里您可以找到并托管box。虽然最容易从HashiCorp的Vagrant Cloud下载box，但您也可以从本地文件，自定义URL等添加box。
    为当前用户全局存储box。每个项目都使用一个box作为初始image进行克隆，并且永远不会修改实际的基本image。这意味着如果您有两个项目都使用hashicorp/precise64 我们刚刚添加的box，则在一台客户机中添加文件将对另一台机器没有影响。
    在上面的命令中，您会注意到box是命名空间。box被分为两部分 - 用户名和box名称 - 用斜杠分隔。在上面的示例中，用户名是"hashicorp"，box是"precise64"。您还可以通过URL或本地文件路径指定box，但入门指南中不会介绍这些box。
    命名空间不保证规范的盒子！一个常见的误解是像"ubuntu"这样的命名空间代表了Ubuntu盒子的规范空间。这是不真实的。例如，Vagrant Cloud上的命名空间与GitHub上的命名空间的行为非常相似。正如GitHub的支持团队无法协助某人的存储库中的问题一样，HashiCorp的支持团队无法协助第三方发布的box。
使用box
    现在该box已添加到Vagrant，我们需要配置我们的项目以将其用作基础。打开Vagrantfile并将内容更改为以下内容：
Vagrant.configure("2") do |config|
  config.vm.box = "hashicorp/precise64"
end
    在这种情况下，"hashicorp / precise64"必须与您用于添加上面框的名称相匹配。这就是Vagrant知道使用什么box的方式。如果之前未添加该box，Vagrant将自动下载并在运行时添加该box。
    您可以通过指定config.vm.box_version 示例来指定box的显式版本：
Vagrant.configure("2") do |config|
  config.vm.box = "hashicorp/precise64"
  config.vm.box_version = "1.1.0"
end
    您还可以使用config.vm.box_url以下命令直接指定box的URL ：
Vagrant.configure("2") do |config|
  config.vm.box = "hashicorp/precise64"
  config.vm.box_url = "https://vagrantcloud.com/hashicorp/precise64"
end
寻找更多Box
    对于本入门指南的其余部分，我们将仅使用之前添加的"hashicorp/precise64"框。但是在完成这个入门指南后不久，你可能会遇到的第一个问题是“我在哪里可以找到更多的盒子？”
    找到更多盒子的最佳位置是HashiCorp的Vagrant Cloud box目录。HashiCorp的Vagrant Cloud有一个提供免费的可以运行各种平台和技术的box的公共目录。HashiCorp的Vagrant Cloud还有一个很棒的搜索功能，可以让你找到你关心的box。
    除了寻找免费box外，如果您打算为自己的组织创建box，HashiCorp的Vagrant Cloud可让您托管自己的box以及私人box。