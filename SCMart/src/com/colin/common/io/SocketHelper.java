package com.colin.common.io;

/**
 * 1、什么是Socket？
 *   Socket（套接字）是一种抽象层，应用程序通过它来发送和接收数据，就像应用程序打开了一个文件句柄，将数据读写到稳定的存储器上一样。
 *   使用Socket可以将应用程序添加到网络中，并与处于同一网络中的其他应用程序进行通信。一台计算机上的应用程序向socket写入的信息能够被另一台计算机上的另一个应用程序读取，反之依然。
 *   根据不同的的底层协议实现，也会有很多种不同的Socket。本课当中只覆盖了TCP/IP协议族的内容，在这个协议族当中主要的Socket类型为流套接字(stream socket)和数据报套接字(datagram socket)。
 *   流套接字将TCP作为其端对端协议，提供了一个可信赖的字节流服务。数据报套接字使用UDP协议，提供可一个“尽力而为”的数据报服务，应用程序可以通过它发送最长65500字节的个人信息。
 * 2、使用基于TCP协议的Socket
 *   一个客户端要发起一次通信，首先必须知道运行服务器端的主机IP地址。然后由网络基础设施利用目标地址，将客户端发送的信息传递到正确的主机上，在Java中，地址可以由一个字符串来定义，这个字符串可以使数字型的地址（比如192.168.1.1），也可以是主机名（example.com）。
 *   在Java当中InetAddress类代表了一个网络目标地址，包括主机名和数字类型的地址信息。下面为大家介绍一下基于TCP协议操作Socket的API：
 *   ServerSocket：这个类是实现了一个服务器端的Socket，利用这个类可以监听来自网络的请求。
 *     a) 创建ServerSocket的方法：
 *       ServerSocket(Int localPort)
 *       ServerSocket(int localport,int queueLimit)
 *       ServerSocket(int localport,int queueLimit,InetAddress localAddr)
 *       创建一个ServerSocket必须指定一个端口，以便客户端能够向该端口号发送连接请求。端口的有效范围是0-65535
 *     b) ServerSocket操作
 *       Socket accept()
 *       void close
 *       accept()方法为下一个传入的连接请求创建Socket实例，并将已成功连接的Socket实例返回给服务器套接字，如果没有连接请求，accept()方法将阻塞等待；
 *       close方法用于关闭套接字
 *   Socket：
 *     a) 创建Socket的方法：
 *       Socket(InetAddress remoteAddress,int remotePort)
 *       利用Socket的构造函数，可以创建一个TCP套接字后，先连接到指定的远程地址和端口号。
 *     b) 操作Socket的方法
 *       InputStream getInputStream()
 *       OutputStream getOutputStream()
 *       void close()
 * 3、使用基于UDP的Socket
 *   a) 创建DatagramPacket
 *     DatagramSocket(byte [] data,int offset,int length,InetAddress remoteAddr,int remotePort)
 *     该构造函数创建一个数据报文对象，数据包含在第一个参数当中
 *   b)	创建DatagramSocket创建
 *     DatagramSocket(int localPort)
 *     以上构造函数将创建一个UDP套接字；
 *   c)	DatagramSocket：发送和接受
 *     void send(DatagramPacket packet)
 *     void receive(DatagramPacket packet)
 *     send()方法用来发送DatagramPacket实例。一旦创建连接，数据报将发送到该套接字所连接的地址；
 *     receive()方法将阻塞等待，知道接收到数据报文，并将报文中的数据复制到指定的DatagramPacket实例中
 * 4、java socket编程流程
 *   1）创建ServerSocket实例绑定一个服务端口（Socket套接字，端口号）
 *   2）开始ServerSocket实例的监听，等待客户端的连接
 *   3）如果有客户连接进来，就获得了客户的套接字(Socket)实例，客户的套接字(Socket)实例中包括与客户端建立的连接流
 *   4）为这个客户(Socket)创建一个服务线程，提供服务（run()方法）
 *   5）继续等待下一个连接，返回步骤2
 *   6）服务线程完成通讯服务过程
 *   7）端口号范围是0 ~ 65535，1K以下(0~1024)是留给系统使用的
 *   
 * @author  Colin Chen
 * @create  2018年11月11日 下午7:48:16
 * @modify  2018年11月11日 下午7:48:16
 * @version A.1
 */
public class SocketHelper {

}
