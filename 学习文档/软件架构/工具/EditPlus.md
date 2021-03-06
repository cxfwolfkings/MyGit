# EditPlus

### EditPlus帮助手册

步骤：【工具】-->【配置用户工具】-->【添加工具】，选择php的chm帮助文件。  

这样在php相应的函数上按ctrl+1（或者你定义的数字键）就可以跳到相应的函数解释，就像VC中在函数上按F1跳到MSDN上一样。

![x](../../Resources/EditPlus1.png)

### EditPlus调试工具

#### php

- 选择：【工具】->【配置用户工具】，在弹出的窗口中选择：【添加工具】->【程序】，在【菜单文本】中输入"Debug PHP"。

- 点击【命令】右边的按钮，找到你的php.exe所在的路径，我的电脑上是"D:\wamp\bin\php\php5.2.6\php.exe "。

- 再点击【参数】右边的下拉按钮选择【文件路径】，再点击【起始目录】右边的下拉按钮选择【文件目录】，最后再把【捕捉输出】前面的复选框选上，这样就可以开始调试PHP程序了。

  ![x](../../Resources/EditPlus2.png)

- 点击【输出模式】按钮，会弹出一个定义输出模式的窗体，把【使用默认输出方式】前面的复选框去掉，在【规则表达式】这一项的文本框中输入`^.+ in (.+) line ([0-9]+)`（不包括引号）。

- 然后，在下面的【文件名称】下拉菜单中选择【表达式标记1】，即上边正则表达式中的第一个参数，【行】下拉菜单项选择【表达式标记2】，【列】下拉项保持为空。然后保存设置。

- 这一步设置好后，当你调试PHP时输出窗口报错时，你只要双击报错信息，就能找到出错的PHP代码了，非常方便。

  ![x](../../Resources/EditPlus3.png)

- 确定后查看【工具】菜单，如下图，红色的为新添加的工具，后面对应的是快捷键：

  ![x](../../Resources/EditPlus4.png)

#### python

![x](../../Resources/EditPlus7.png)

### 函数自动完成

去[http://editplus.com/html.html](http://editplus.com/html.html)，下载你需要的自动补全插件。推荐下载这个：[http://www.editplus.com/dn.cgi?php_acp3.zip](http://www.editplus.com/dn.cgi?php_acp3.zip)

说明：acp就是自动补全，stx是语法文件。

打开【工具】->【参数】，选择【设置与语法】->【文件类型】选择【PHP】，点击【自动完成】后面的【...】按钮，弹出文件选择框，将下好的php.acp复制到该目录下，并选中php.acp然后点击【打开】。

如果你不想使用Editplus的自动完成功能，你只要勾选禁用自动完成功能即可。最后重启下Editplus即可使用了。

使用Editplus的PHP自动完成功能时，注意在PHP文件中务必输入完整的PHP语句或PHP函数，然后按下空格即可启动自动完成功能，如果只输入了一部分，按空格是启动不了Editplus的PHP自动完成功能的

### 添加模板

在参数设置里建一个php的模板，使得在“文件-->新建”出现php模板，当然也可以继续点击“文件-->新建-->其它-->php”，来实现新建一个空白php模板。其它模板文件就是一个代码片段。

 首先打开Editplus，点击“文件-->新建-->其它-->php”，然后写代码片段后命名为template.php，保存在Editplus的根目录下。接下来我们开始在“工具-->参数-->模板-->添加”，选择刚才你保存的template.php文件。在“菜单文本”输入"php"，点击“载入”，然后“确定”。效果如图所示。

![x](../../Resources/EditPlus5.png)

### 代码美化

PHP代码美化就是方便那些不按照合理的格式写代码的童鞋们，或者是看别人写的乱七八糟的代码的时候直接格式美化后看就舒服多了。配置过程和第二步相似，不同的是代码美化需要下载一个应用程序（phpCB.exe文件），然后设置动作为“运行为文本过滤器(替换)”即可。不得不说这一版的PHP代码美化已经比之前的好看了，看来还是自己先写的标准一点的好。配置如图所示。

![x](../../Resources/EditPlus6.png)

### 函数提醒

这一步其实只是作为使用Editplus调试PHP时，函数自动完成的补充功能，当你记不清具体的函数名时，函数提醒功能是非常有必要的。

首先，请下载素材文件，即php.ctl文件，然后将解压的php.ctl文件放到Editplus安装目录下，然后选中左侧的素材文件，右键刷新即可。当你要使用Editplus的PHP函数提醒功能时，只要选择相应的PHP素材文件，在PHP文件中输入函数名的一部分，然后按F2，就会模糊列出相应的函数，最后选择你需要使用的PHP函数即可。

OK，至此配置Editplus调试PHP程序的所有步骤就介绍完了，经过上述步骤的配置，就可以很好的使用Editplus编写和调试PHP了。当然你如果有一定的基础，你也可以整理php.ctl文件，或者根据自己的使用习惯整理PHP的自动完成规则文件acp。总的来说配置 Editplus非常灵活，作为轻量级的PHP开发工具优势还是很明显的。 