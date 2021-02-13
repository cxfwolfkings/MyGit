MSBuild 是 Microsoft 和 Visual Studio的生成系统。它不仅仅是一个构造工具，应该称之为拥有相当强大扩展能力的自动化平台。MSBuild平台的主要涉及到三部分：执行引擎、构造工程、任务。其中最核心的就是执行引擎，它包括定义构造工程的规范，解释构造工程，执行“构造动作”；构造工程是用来描述构造任务的，大多数情况下我们使用MSBuild就是遵循规范，编写一个构造工程；MSBuild引擎执行的每一个“构造动作”就是通过任务实现的，任务就是MSBuild的扩展机制，通过编写新的任务就能够不断扩充MSBuild的执行能力。所以这三部分分别代表了引擎、脚本和扩展能力。

**构造工程（脚本文件）**
先说说构造工程，只要通过Notepad打开任何一个Visual Studio下的C#工程（csproj）文件，就知道构造工程到底是怎么回事了。

```xml
<?xml version="1.0" encoding="utf-8"?>
<Project xmlns="http://schemas.microsoft.com/developer/msbuild/2003">
  <PropertyGroup>
    <Root>$(MSBuildStartupDirectory)</Root>
  </PropertyGroup>
  <Target Name="Build">
    <!-- Compile -->
    <ItemGroup> 
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Common\Gimela.Common.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Infrastructure\Gimela.Infrastructure.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Management\Gimela.Management.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Security\Gimela.Security.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Tasks\Gimela.Tasks.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Text\Gimela.Text.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Net\Gimela.Net.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\ServiceModel\Gimela.ServiceModel.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Data\Gimela.Data.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Presentation\Gimela.Presentation.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Media\Gimela.Media.sln" />
      <ProjectToBuild Include="$(Root)\..\src\Foundation\Streaming\Gimela.Streaming.sln" />   
      <ProjectToBuild Include="$(Root)\..\src\Crust\Gimela.Crust.sln" />      
    </ItemGroup>
    <MSBuild Projects="@(ProjectToBuild)" Targets="Build" Properties="Configuration=Debug;">
      <Output TaskParameter="TargetOutputs" ItemName="AssembliesBuiltByChildProjects" />
    </MSBuild>
    
  </Target>
</Project>
```

在构造工程中我们可以定义和使用变量（通过Property/PropertyGourp/Item/ItemGroup等元素），可以使用条件分支（通过Choose/When/Otherwise等元素）、能够在运行时给变量赋值（通过执行任务，获取其返回类型参数的方式）、能够定义执行块（通过Target元素，相当于函数）、能够进行异常处理（通过OnError元素）、还可以复用已有工程定义的内容（通过Import元素）。拥有这些能力和高级语言已经相差无几了，所以笔者认为构造工程不是描述性语言，而是脚本语言。

这里还需要强调一点的是，项目级元素（Property）可以在元素下定义，也可以在构造过程中作为外部参数传入,这是一个非常有用的特性，一般编译时选择配置项（Debug或者Release）就是利用这个特性实现的。

**Project元素**
这是每一个项目文件的最外层元素，它表示了一个项目的范围。如果缺少了这一元素，MSBuild会报错称Target元素无法识别或不被支持。
Project元素拥有多个属性，其中最常用到的是DefaultTargets属性。我们都知道，在一个项目的生成过程中可能需要完成几项不同的任务（比如编译、单元测试、check-in到源代码控制服务器中等），其中每一项任务都可以用Target来表示。对于拥有多个Target的项目，你可以通过设置Project的DefaultTargets（注意是复数）属性来指定需要运行哪（几）个Target，如果没有这个设置，MSBuild将只运行排在最前面的那个Target。

**Property元素**

在项目中你肯定需要经常访问一些信息，例如需要创建的路径名、最终生成的程序集名称等。以name/value的形式添加进Property，随后就可以以$(PropertyName)的形式访问。这样你就无须为了改动一个文件名称而让整个项目文件伤筋动骨了。比如上面代码中的Bin就是将要创建的路径名称，而AssemblyName则是最终要生成的程序集名称。这些属性的名称不是固定的，你完全可以按自己的习惯来进行命名。在使用时，你需要把属性名称放在”$(“和”)”对内（不包括引号），以表示这里将被替换成一个Property元素的值。
另外，如果Property元素数量比较多，你还可以把它们分门别类地放在不同的PropertyGroup里，以提高代码的可阅读性。这对Property本身没有任何影响。

```xml
 <PropertyGroup>
    <Configuration Condition=" '$(Configuration)' == '' ">Debug</Configuration>
    <Platform Condition=" '$(Platform)' == '' ">AnyCPU</Platform>
    <ProductVersion>8.0.30703</ProductVersion>
    <SchemaVersion>2.0</SchemaVersion>
    <ProjectGuid>{6C2561FB-4405-408F-B41B-ACE5E519A26E}</ProjectGuid>
    <OutputType>Library</OutputType>
    <AppDesignerFolder>Properties</AppDesignerFolder>
    <RootNamespace>Gimela.Infrastructure.Patterns</RootNamespace>
    <AssemblyName>Gimela.Infrastructure.Patterns</AssemblyName>
    <TargetFrameworkVersion>v4.0</TargetFrameworkVersion>
    <FileAlignment>512</FileAlignment>
  </PropertyGroup>
```

**Item元素**
在整个项目文件中你肯定要提供一些可被引用的输入性资源(inputs)信息，比如源代码文件、引用的程序集名称、需要嵌入的图标资源等。它们应该被放在Item里，以便随时引用。语法是：`<Item` `Type="TheType" Include="NameOrPath" />`

其中Type属性可以被看作是资源的类别名称，比如对于.cs源文件，你可以把它们的Type都设置为Source，对于引用的程序集把Type都设置为Reference，这样在随后想引用这一类别的资源时只要引用这个Type就可以了，方法是@(TypeName)。可千万别和Property的引用方法弄混了。
既然Type是资源的类名，那么Include就是具体的资源名称了，比如在上面的示例代码中，Include引用的就是C#源代码文件的名称。你也可以用使用通配符*来扩大引用范围。比如下面这行代码就指定了当前目录下的所有C#文件都可以通过@(Source)来引用：

```xml
<Item Type="Source" Include="*.cs" />
```

另外，你也可以通过与PropertyGroup类似的方法把相关的Item放在ItemGroup里。

```xml
  <ItemGroup>
    <Reference Include="System" />
    <Reference Include="System.Core" />
    <Reference Include="System.Data" />
    <Reference Include="System.ServiceModel" />
    <Reference Include="System.Xml" />
  </ItemGroup>
  <ItemGroup>
    <Compile Include="Commands\CommandBase.cs" />
    <Compile Include="Commands\DuplexCommandBase.cs" />
    <Compile Include="Commands\ICommand.cs" />
    <Compile Include="Commands\IDuplexCommand.cs" />
    <Compile Include="Extensions\BitConverterExtensions.cs" />
    <Compile Include="Extensions\ConcurrentDictionaryExtensions.cs" />
    <Compile Include="Extensions\StopwatchExtensions.cs" />
    <Compile Include="Extensions\TimeSpanExtensions.cs" />
    <Compile Include="Flyweight\FlyweightObjectPool.cs" />
    <Compile Include="Singleton\StaticSingleton.cs" />
    <Compile Include="Properties\AssemblyInfo.cs" />
    <Compile Include="SmartQueue\ISmartQueueMapper.cs" />
    <Compile Include="SmartQueue\SmartQueue.cs" />
    <Compile Include="SmartQueue\SmartQueueBase.cs" />
    <Compile Include="SmartQueue\SmartQueueMapper.cs" />
    <Compile Include="UnitOfWork\IUnitOfWork.cs" />
    <Compile Include="UnitOfWork\IUnitOfWorkFactory.cs" />
    <Compile Include="UnitOfWork\UnitOfWork.cs" />
    <Compile Include="WeakActions\IWeakActionExecuteWithObject.cs" />
    <Compile Include="WeakActions\WeakAction.cs" />
    <Compile Include="WeakActions\WeakActionGeneric.cs" />
    <Compile Include="WeakFuncs\IWeakFuncExecuteWithObjectAndResult.cs" />
    <Compile Include="WeakFuncs\WeakFunc.cs" />
    <Compile Include="WeakFuncs\WeakFuncGeneric.cs" />
  </ItemGroup>
```

**Target元素**
Target表示一个需要完成的虚拟的任务单元。每个Project可以包括一个或多个Target，从而完成一系列定制的任务。你需要给每个Target设置一个Name属性（同一Project下的两个Target不能拥有同样的Name）以便引用和区别。

举例来说，在你的项目生成过程中可能需要完成三个阶段的任务：首先check-out源代码，接下来编译这些代码并执行单元测试，最后把它们check-in。那么通常情况下你可以创建三个不同的Target以清晰划分三个不同的阶段：

```xml
<Target Name="CheckOut"></Target>
<Target Name="Build" DependsOnTargets="CheckOut">
    <Task Name="Build" .../>
    <Task Name="UnitTest" ... />
</Target>
<Target Name="CheckIn" DependsOnTargets="CheckOut;Build"></Target>
```

这样，你就可以非常清晰地控制整个生成过程。为了反应不同Target之间的依赖关系（只有Check-in后才能编译，只有编译完成才可能Check-out……），你需要设置Target的DependsOnTargets属性（注意是复数），以表示仅当这些Target执行完成之后才能执行当前的Target。当MSBuild引擎开始执行某项Target时（别忘了Project的DefaultTargets属性），会自动检测它所依赖的那些Target是否已经执行完成，从而避免因为某个生成环节缺失而导致整个生成过程发生意外。
你可以通过Project的DefaultTargets属性指定MSBuild引擎从哪（几）个Target开始执行，也可以在调用MSBuild.exe时使用t开关来手动指定将要运行的Target，方法如下：
MSBuild /t:CheckOut 这样，只有CheckOut（以及它所依赖的Target，在上文中没有）会被执行。

**Task元素**
这可能是整个项目文件中最重要的，因为它才是真正可执行的部分（这也是为什么我在上面说Target是虚拟的）。你可以在Target下面放置多个Task来顺序地执行相应的任务。

 

相关文档

- [MSBuild入门](http://www.cnblogs.com/linianhui/archive/2012/08/30/2662648.html)
- [MSBuild入门（续）](http://www.cnblogs.com/linianhui/archive/2012/09/01/2666104.html)
- [Introduction to MSBuild - Part 1](http://www.codeproject.com/Articles/465087/Introduction-to-MSBuild-Part-1)
- [Working with MSBuild - Part 2](http://www.codeproject.com/Articles/468855/Working-with-MSBuild-Part-2)



**参考：**

- [张善友博客](https://www.cnblogs.com/shanyou/)