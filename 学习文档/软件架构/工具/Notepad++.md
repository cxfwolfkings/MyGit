# Notepad++

### 环境配置

#### Java

1. 安装JDK

   为了能够在控制台窗口中方便的使用JDK中的工具，需要在Windows系统环境变量PATH中追加JDK二进制（bin）文件所在的路径，在控制台窗口中执行如下命令，更新PATH环境变量。

   ```sh
   set path=.;%path%;E:/Program Files/Java/jdk1.6.0_02/bin
   ```

2. 安装Notepad++

   Notepad++是运行在Windows操作系统下的一款代码编辑器软件，能够对绝大多数的编程语言进行语法着色，为自由软件，遵循GPL。

3. 配置Notepad++

   3.1 单词自动补全功能配置

   (何为单词自动补全：你的源文件以前输入过的单词，即token，会缓存下来， 当你输入一个字符后会显示出来，按回车选中) Notepad++提供了一系列编程相关的功能，如自动识别源代码类型，支持自动缩进，语法着色，支持单词/函数名称自动补全功能等。Notepad++ 默认设置支持了大部分代码编写功能，下面将使Notepad++支持“单词自动补全”功能。

   点击“设置”菜单，选择“首选项”菜单项，弹出“首选项对话框”，选中“备份与自动完成”，在此标签页底部中选中“所有输入均启用自动完成”复选框，并选择“单词自动完成”。

   3.2 Notepad++的插件NppExec实现Console Dialog，此插件可以完成Windows控制台基本功能，如可以在此窗口中进行编译、运行Java程序；Notepad++还能够运行外部程序，通过 菜单项与外部程序建立映射关系，支持为外部程序输入参数等。

   3.3 Console Dialog作为Java开发环境

   显示Console Dialog对话框

   点击“插件”主菜单，在其下拉菜单中选中“NppExec”，在二级菜单中选中“Show Console Dialog”。

   默认在编辑器的底部出现Console Dialog。

   编辑Java源文件

   新建一个Java源文件HelloNpp.java，在编辑器中输入如下内容：

   ```java
   public class HelloNpp{
       public static void main(String[] args){
           System.out.println("Hello Notepad++!");
       }
   }
   
   3.4 编译运行Java程序
   
   notepad有两个运行代码的功能，一个是Run(F5)，另一个是Plugins -> NppExec(F6),用前者实现比较困难，所以选择后者，他带的命令行模拟非常好用。
   
   首先按F6，会弹出执行命令的对话框，在Command(s)中输入下列三行：
   
   ​```sh
   npp_save //转载注：保存文件
   cd "$(CURRENT_DIRECTORY)"
   D:/jdk1.6.0_10/bin/Javac.exe "$(FILE_NAME)"// 转载注：java PATH已经设置好了，可以直接使用:Javac.exe "$(FILE_NAME)"
   D:/jdk1.6.0_10/bin/java.exe "$(NAME_PART)"// java.exe "$(NAME_PART)"
   javaRunWin:
   cd "$(CURRENT_DIRECTORY)"
   jar -cvfe "$(NAME_PART)".jar "$(NAME_PART)" *.class
   java与package:
   npp_save
   cd "$(CURRENT_DIRECTORY)"
   Javac -d . "$(FILE_NAME)"
   ```

   这里解释一下notepad++的环境变量，CURRENT_DIRECTORY表示当前路径，就是当前编辑文件的路径。然后另存为BuildAndRun脚本，表示编译然后运行。以后F6，可以直接选择该脚本，而不必输入命令了。

   在Console Dialog中编译器输出结果如下面的清单，绿色字体表示用户输入的命令和系统提示，黑色字体为Console Dialog的输出信息，与Windows 命令行程序相比多输出一些信息，表示程序执行的开始与结束，最后输出的信息表示Console Dialog处于等待新的命令状态。

   ```sh
   CD: D:/Java
   Current directory: D:/Java
   D:/jdk1.6.0_10/bin/Javac.exe "HelloWorld.java"
   Process started >>>
   <<< Process finished.
   D:/jdk1.6.0_10/bin/java.exe "HelloWorld"
   Process started >>>
   Hello World!
   b=true
   l=2000
   f=1.2
   <<< Process finished.
   ================ READY ================
   ```

4. 配置Java外部工具

   4.1 Notepad++环境变量

   表1为Notepad++定义的这些环境，可以作为参数传递给外部工具

   | 变量名称          | 含义                   | 例子                  |
   | ----------------- | ---------------------- | --------------------- |
   | FULL_CURRENT_PATH | 文件路径名称           | E:/java/HelloNpp.java |
   | CURRENT_DIRECTORY | 文件目录（不含文件名） | E:/java/              |
   | FILE_NAME         | 文件全名称             | HelloNpp.java         |
   | NAME_PART         | 文件名称（不含ext）    | HelloNpp              |
   | EXT_PART          | 文件扩展名             | java                  |

   4.2 创建外部工具

   这两个外部工具为javac和java，增加了暂停功能，可以在编译或运行Java程序时通过控制台窗口显示输出信息。

   4.2.1 javacnpp.bat：编译当前Java源文件，需要一个指定Java源文件作为参数，运行后屏幕处于暂停状态，并显示编译程序的执行结果。以下为javacnpp.bat代码：

   ```sh
   @echo on
   javac %1
   pause
   ```

   4.2.2 Javanpp.bat：运行Java的class二进制文件，需要指定两个参数，第一个参数为class文件所在的目录；第二个参数为Java程序名称。运行后屏幕处于暂停状态，显示程序执行的结果。以下为javanpp.bat代码：

   ```sh
   @echo on
   java -cp %1 %2
   pause
   ```

   4.3 创建javac菜单

   此菜单项用来编译Java源代码，生成class文件。选择主菜单“运行”，在下拉菜单中选择“运行...”或使用快捷键F5，显示“运行”对话框,输入如下运行程序名

   "E:/Program Files/Notepad++/javacnpp.bat" $(FULL_CURRENT_PATH)

   javacnpp.bat为上述创建的外部工具，$(FULL_CURRENT_PATH)为当前编辑的Java源代码文件。

   点击“保存”按钮，在名称编辑框中输入此外部工具名称，如javac；在快捷方式对话框中选择执行此外部程序对应的快捷键，选中(Control+Shift+J)作为编译Java源代码的快捷方式。

   4.4 创建java菜单

   java菜单用来运行java程序，创建的方式与javac菜单相同，但各自使用不同的外部工具，在“运行”对话框中输入运行程序名称：

   "E:/Program Files/Notepad++/javanpp.bat" $(CURRENT_DIRECTORY) $(NAME_PART)

   javanpp.bat为上述创建的外部工具，$(CURRENT_DIRECTORY)为Java class文件所在的目录，$(NAME_PART)为文件名称（不包含扩展名）。

   点击“保存”按钮，在名称编辑框中输入此外部工具名称，如java；在快捷方式对话框中选择执行此外部程序对应的快捷键，选中(Control+Shift+X)作为运行Java程序的快捷方式。

   建立这两个外部工具后，在“运行”菜单中会增加两个菜单项：javac和java，分别用来编译Java源代码和运行Java程序。在代码编辑区完成编写 工作后，执行javac命令（或使用快捷键Ctrl+Shift+J）编译当前Java源代码文件；编译通过后再执行java(或使用快捷键 Ctrl+Shift+X）运行编译后的Java程序。

5. 小结

本文介绍了如何使用Notepad++与JDK集成作为一个简单的Java IDE，适合与Java初学者。文中将所有的Java源文件都放置到缺省包（package）中，没有使用自定义包(package），如果使用 import/package关键字，按照本文描述的配置执行javac或java，则会在运行时会出现“ java.lang.NoClassDefFoundError:”错误，建议采用Console Dialog中执行

#### C

1. 打开菜单栏中的运行，点击其中的运行，或者按F5。弹出设置对话框。

2. 在弹出的设置对话框中填准确信息：

   cmd /k gcc -o "D:\study\$(NAME_PART).exe" "$(FULL_CURRENT_PATH)" & PAUSE & EXIT然后名字为编译c，快捷键自己设置。

   同样第二个：cmd /k "D:\study\$(NAME_PART)" & PAUSE & EXIT名字为运行c，快捷键自己设置。

   设置好以后关闭对话框即可。

3. 然后在软件简单地测试程序，检验是否通过。示例：

   ```C
   #include <stdio.h>
   int main(void)
   {
       int i1=1,i2=2;
       int sum=i1+i2;
       printf("%d\n",sum);
       return 0;
   }
   ```

4. 然后依次点击编译c和运行c，最终测试通过。恭喜大家可以使用这个编译器编程了。

   注意：D:\study为程序输出位置，这个需要自己设置。

   我自己设置的快捷键（如果快捷键无效，一般是因为快捷键冲突）：

   编译C：Ctrl+Shift+O

   运行C：Ctrl+Shift+G