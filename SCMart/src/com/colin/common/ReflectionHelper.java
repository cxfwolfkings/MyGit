package com.colin.common;

import java.util.Date;

/**
 * 反射
 * 1) 反射作用
 *   可以通过反射机制发现对象类型，发现类型的方法、属性、构造器
 *   可以创建对象并访问任意对象方法和属性等
 * 2) Class加载
 *   类加载到内存：JAVA将磁盘类文件加载到内存中，为一个对象（Class的实例）
 * 3) Class实例代表JAVA中类型
 *   Class cls = String.class;
 *   Class cls = Class.forName("java.lang.String"); // 懒加载，内存中发现该类已加载，直接返回
 *   Class cls = "".getClass();
 * 反射技术是JAVA底层JVM运行程序的机制
 * newInstance()方法，利用默认（无参）构造器创建类实例（实例对象）
 * 
 * Java程序可以加载一个运行时才得知名称的class，获悉其完整构造（但不包括methods定义），并生成其对象实体、或对其fields设值、或唤起其methods。
 * 这种“看透class”的能力（the ability of the program to examine itself）被称为introspection（内省、内观、反省）。Reflection和introspection是常被并提的两个术语。
 * 在JDK中，主要由以下类来实现Java反射机制，这些类都位于java.lang.reflect包中
 *   –Class类：代表一个类。
 *   –Field类：代表类的成员变量（成员变量也称为类的属性）。
 *   –Method类：代表类的方法。
 *   –Constructor类：代表类的构造方法。
 *   –Array类：提供了动态创建数组，以及访问数组的元素的静态方法
 * Java中，无论生成某个类的多少个对象，这些对象都会对应于同一个Class对象。要想使用反射，首先需要获得待处理类或对象所对应的Class对象。
 * 获取某个类或某个对象所对应的Class对象的常用的3种方式：
 * a) 使用Class类的静态方法forName：Class.forName("java.lang.String");
 * b) 使用类的.class语法：String.class;
 * c) 使用对象的getClass()方法：String s = "aa"; Class<?> clazz = s.getClass();
 * 若想通过类的不带参数的构造方法来生成对象，我们有两种方式：
 * a) 先获得Class对象，然后通过该Class对象的newInstance()方法直接生成即可：
 *   Class<?> classType = String.class;
 *   Object obj = classType.newInstance();
 * b) 先获得Class对象，然后通过该对象获得对应的Constructor对象，再通过该Constructor对象的newInstance()方法生成：
 *   Class<?> classType = Customer.class; 
 *   Constructor cons = classType.getConstructor(new Class[]{});
 *   Object obj = cons.newInstance(new Object[]{});
 * 若想通过类的带参数的构造方法生成对象，只能使用下面这一种方式：
 *   Class<?> classType = Customer.class;
 *   Constructor cons = classType.getConstructor(new Class[]{String.class, int.class});
 *   Object obj = cons.newInstance(new Object[]{"hello", 3});
 *   
 * 动态创建多维数组
 * //创建一个设值数组维度的数组
 * int[] dims = new int[] { 5, 10, 15 };
 * //利用Array.newInstance创建一个数组对象，第一个参数指定数组的类型，第二个参数设置数组的维度，下面是创建一个长宽高为：5,10,15的三维数组
 * Object array = Array.newInstance(Integer.TYPE, dims);
 * System.out.println(array instanceof int[][][]);
 * //获取三维数组的索引为3的一个二维数组
 * Object arrayObj = Array.get(array, 3);
 * //获取二维数组的索引为5的一个一维数组
 * arrayObj = Array.get(arrayObj, 5);
 * //设一维数组arrayObj下标为10的值设为37
 * Array.setInt(arrayObj, 10, 37);
 * int[][][] arrayCast = (int[][][])array;
 * System.out.println(arrayCast[3][5][10]);
 * 
 * 利用反射访问类的私有方法：
 * Private p = new Private();
 * Class<?> classType = p.getClass();
 * Method method = classType.getDeclaredMethod("sayHello", new Class[] { String.class });
 * method.setAccessible(true);//压制Java的访问控制检查，使允许访问private方法
 * String str = (String)method.invoke(p, new Object[]{"zhangsan"});
 * System.out.println(str);
 * 
 * 利用反射访问类的私有变量：
 * Private2 p = new Private2();
 * Class<?> classType = p.getClass();
 * Field field = classType.getDeclaredField("name");
 * field.setAccessible(true);//压制Java对访问修饰符的检查
 * field.set(p, "lisi");
 * System.out.println(p.getName());
 * 
 * Proxy
 * 代理模式的作用是：为其他对象提供一种代理以控制对这个对象的访问。在某些情况下，一个客户不想或者不能直接引用另一个对象，而代理对象可以在客户端和目标对象之间起到中介的作用
 * 代理模式一般涉及到的角色有
 *   抽象角色：声明真实对象和代理对象的共同接口
 *   代理角色：代理对象角色内部含有对真实对象的引用，从而可以操作真实对象，同时代理对象提供与真实对象相同的接口以便在任何时刻都能代替真实对象。同时，代理对象可以在执行真实对象操作时，附加其他的操作，相当于对真实对象进行封装
 *   真实角色：代理角色所代表的真实对象，是我们最终要引用的对象
 * Java动态代理类位于java.lang.reflect包下，一般主要涉及到以下两个类：
 * (1)Interface InvocationHandler：该接口中仅定义了一个方法
 *   public object invoke(Object obj, Method method, Object[] args)
 *   在实际使用时，第一个参数obj一般是指代理类，method是被代理的方法，如上例中的request()，args为该方法的参数数组。这个抽象方法在代理类中动态实现。
 * (2)Proxy：该类即为动态代理类，作用类似于上例中的ProxySubject，其中主要包含以下内容
 *   protected Proxy(InvocationHandler h)：构造函数，用于给内部的h赋值。
 *   static Class getProxyClass(ClassLoader loader, Class[] interfaces)：
 *     获得一个代理类，其中loader是类装载器，interfaces是真实类所拥有的全部接口的数组。
 *   static Object newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h)：
 *     返回代理类的一个实例，返回后的代理类可以当作被代理类使用（可使用被代理类的在Subject接口中声明过的方法）
 * 所谓Dynamic Proxy（动态代理）是这样一种class：它是在运行时生成的class，在生成它时你必须提供一组interface给它，
 * 然后该class就宣称它实现了这些interface。你当然可以把该class的实例当作这些interface中的任何一个来用。
 * 当然，这个Dynamic Proxy其实就是一个Proxy，它不会替你作实质性的工作，在生成它的实例时你必须提供一个handler，由它接管实际的工作
 * 在使用动态代理类时，我们必须实现InvocationHandler接口。通过代理的方式，被代理的对象(RealSubject)可以在运行时动态改变，
 * 需要控制的接口(Subject接口)可以在运行时改变，控制的方式(DynamicSubject类)也可以动态改变，从而实现了非常灵活的动态代理关系。
 * 动态代理是指客户通过代理类来调用其它对象的方法
 * 
 * 动态代理使用场合：调试、远程方法调用(RMI)
 * 动态代理的步骤：
 *   1.创建一个实现接口InvocationHandler的类，它必须实现invoke方法 
 *   2.创建被代理的类以及接口
 *   3.通过Proxy的静态方法
 *     newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h) 创建一个代理
 *   4.通过代理调用方法
 *   
 * RMI本地对象之间相互调用的正常途径
 * 服务器端对于每一次远程方法调用请求都执行以下的操作：
 *   1.反调度参数（明确调用的方法和提取参数）
 *   2.定位被调度的对象
 *   3.调用所期待的方法
 *   4.捕捉和调度返回值或这次调度抛出的异常。
 *   5.发送结果数据报到客户端的stub对象。
 *   
 * 创建RMI实例
 * 开发RMI应用一般可以下按如下步骤完成：
 *   1.定义用于远程对象的接口。这个接口定义了客户机能够远程调用的方法。
 *   2.编写实现远程接口的服务器类。
 *   3.编写在服务器上运行的主程序。这个程序必须实例化一个或多个步骤2中编写的服务器对象。然后，将远程对象注册到 RMI名称注册表，这样客户机就能够找到对象。
 *   4.使用JDK中提供的程序rmic为服务器类生成用于生成用于自动通讯的stub和seketlon类。
 *   5.编写客户端程序。
 *   6.启动RMI注册表。
 *   7.运行服务器主程序。
 *   8.启动客户端程序。
 * 几个额外的规定：
 *   1．远程接口必须直接或间接的扩展(extends)自接口java.rmi.Remote。 
 *   2．远程接口必须为public。
 *   3．除了与应用程序本身可能抛出的Exception外，远程接口中的每一个方法都必须在自己的throws子句中声明java.rmi.RemoteException异常。
 *   4．所有传递的参数或返回值都必须是基本数据类型（int，double等）或实现了java.io.Serializable的类。
 * 
 * @author  Colin Chen
 * @create  2018年11月21日 上午6:44:33
 * @modify  2018年11月21日 上午6:44:33
 * @version A.1
 */
public class ReflectionHelper {

}

/**
 * 远程接口
 * 实现远程接口的服务器类 ，要遵守如下一些必要的编程规定：
 * （1）所有的服务器类都必须直接或间接的继承类java.rmi.server.RemoteServer。
 * 在实际开发中，我们一般继承的是RemoteServer的子类UniCastRemoteObject，下图展示了RMI中一些重要的类之间的继承关系。
 *   Object
 *   --RemoteObject
 *   ----Remote
 *   ----RemoteStub
 *   ----RemoteServer
 *   ------UnicastRemoteObject
 * （2）所有的服务器类都必须有一个无参数的构造函数。
 * 虽然在Java中，如果用户没有为某个类定义任何的构造函数的话，编译器会自动地为该类添加一个无参数的构造函数，但在此我们还是推荐大家显式地定义自己的无参数构造函数。
 * 
 * 服务器主程序和RMI注册表 
 * 服务器主程序完成的工作主要有：
 * （1）创建并安装一个安全管理器(SecurityManager)。创建和安装的过程可以使用：
 *     System.setSecurityManager(new RMISecurityManager());
 * （2）创建一个或多个服务器对象的实例。
 * （3）向RMI注册表注册创建的服务器对象
 *     在Windows环境下可以通过以下的命令完成: start rmiregistry
 *     在Linux中，可以使用以下的命令完成：rmiregistry & 提供远程调用的网络连接 
 * rmic的命令格式为：rmic SomeRemoteObject
 * 当rmic命令执行完后，JDK自动为用户生成两个新的类：
 * SomeRemoteObject_Stub.class
 * SomeRemoteObject_Skel.class
 * 
 * 编写客户端程序 
 * 在客户端应用程序要做工作主要包括：
 * （1）建立并安装安全管理器(SecurityManager)。使用RMI的客户程序，必须安装安全管理器以控制动态装入的stub的活动。
 * （2）查找并从服务器获得远程接口。
 * 
 * 部署和运行RMI应用 
 * 在默认情况下RMI安全管理程序RMISecurityManager会禁止程序中的任何代码建立网络联系，
 * 所以为了访问RMI注册表，并调用远端对象的方法，必须为客户端程序提供一个安全策略文件（policy文件）。
 * 
 * 
 * @author  Colin Chen
 * @create  2018年11月21日 上午11:57:02
 * @modify  2018年11月21日 上午11:57:02
 * @version A.1
 */
interface RemoteDate extends java.rmi.Remote {
	/**
	 * 用于获取远端服务器系统时间的方法 
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	public Date getRemoteDate() throws java.rmi.RemoteException;
}

