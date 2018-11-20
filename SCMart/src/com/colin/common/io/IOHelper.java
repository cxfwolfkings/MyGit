package com.colin.common.io;

/**
 * 流
 * --字符流
 * ----Reader
 * ------BufferedReader
 * ------CharArrayReader
 * ------FilterReader
 * ------StringReader
 * ------PipedReader
 * ------InputStreamReader
 * --------FileReader
 * ----Writer
 * ------BufferedWriter
 * ------OutputStreamWriter
 * --------FileWriter
 * --字节流
 * ----InputStream
 * ------FileInputStream
 * ------ByteArrayInputStream
 * ------StringBufferInputStream
 * ------PipedInputStream
 * ------SequenceInputStream
 * ------FilterInputStream
 * --------BufferedInputStream
 * --------LineNumberInputStream
 * --------PushbackInputStream
 * --------DataInputStream
 * ----OutputStream
 * ------FileOutputStream
 * ------FilterOutputStream
 * --------BufferedOutputStream
 * 
 * java.io.File
 *   用于表示文件（目录），通过这个类可以在程序中操作硬盘上的文件和目录。
 * 回调模式和FileFilter
 *   FileFilter是对操作文件的过滤，API方法：
 *   File[] listFile(FileFilter)
 *   listFiles()方法会将dir中每个文件交给accept()方法检测，如果返回true，就作为方法的返回结果元素。accept()方法的调用属于回调模式
 * RandomAccessFile
 *   java提供的功能丰富的文件内容访问类，它提供了众多方法来访问文件内容，既可以读取文件内容，也可以向文件输出数据，RandomAccessFile支持“随机访问”方式，可以访问文件的任意位置。
 *   1) java文件模型：在硬盘上文件是byte byte byte存储的，是数据的集合
 *   2) 打开文件
 *     有两种模式"rw"（读写）、"r"（只读）
 *     RandomAccessFile raf = new RandomAccessFile(file, "rw");
 *     打开文件时，默认文件指针在开头 pointer=0
 *   3)	写入方法
 *     raf.write(int)可以整数的“低八位”写入到文件中，同时指针自动移动到下一个位置，准备再次写入。
 *     注意：文件名的扩展名要明确指定，没有默认扩展名现象！
 *     RandomAccessFile raf = new randomAccessFile("Hello.java", "rw");
 *   4)	读取文件
 *     int b = raf.read() 从文件中读取一个byte（8位）填充到int的低八位，高24位为0，返回值范围正数：0~255，如果返回-1，表示读取到了文件末尾！每次读取后自动移动文件指针，准备下次读取。
 * 序列化和反序列化
 *   将类型int转换为4 byte，或其它数据类型（如long->8 byte）的过程，即将数据转换为n个byte序列叫序列化（数据->n byte）
 *   反序列化，将n byte转换为一个数据的过程（n byte->数据），如：[7f,ff,ff,ff]->0x7fffffff
 *   RandomAccessFile提供基本类型的读写方法，可以将基本类型数据序列化到文件或者将文件内容反序列化为数据
 * 流
 *   可从中读出一系列字节的对象称为“输入流”(InputStream)；而能向其中写入一系列字节的对象则称为“输出流”(OutputStream)。
 *   Java中的IO流是实现输入输出的基础
 *   1）InputStream、OutputStream都是抽象类
 *     InputStream抽象了应用程序读取数据的方式
 *     OutputStream抽象了应用程序写出数据的方式
 *   2）EOF = End of File = -1
 *   3）输入流基本方法
 *     int b = in.read()  读取一个byte无符号填充到int低8位，-1是EOF
 *     in.read(byte[] buf)  读取数据填充到buf中
 *     in.read(byte[] buf, int start, int size)  读取数据填充到buf中
 *     in.skip(long n)
 *     in.close();
 *   4）	输出流的基本方法：
 *     out.write(int b)  写出一个byte到流b的低8位写出
 *     out.write(byte[] buf)  将缓冲区buf都写入到流
 *     out.write(byte[] buf, int start, int size)  将buf的一部分写到流中
 *     out.flush()  清理缓冲
 *     out.close()
 *   注：输入流、输出流是相对应用程序而言的，应用程序是参照物
 * FileInputStream和FileOutputStream
 *   分别实现了InputStream抽象类和OutStream抽象类，具体实现了在文件上读取数据和写入数据
 *   文件输出流(FileOutputStream)的构造器，如果没有文件，会自动的创建文件！
 *   输出时默认是覆盖这个文件内容，如果需要追加内容，需要使用新的构造器
 *   boolean append = true;
 *   new FileOutputStream(file,append);
 *   out.flush( )方法表示清理缓存，在写代码中尽量加上，能保证可靠写入。
 * DateOutputStream和DateInputStream
 *   是对“流”功能的扩展，可以更方便地读取和写入字符类型数据
 *   DataOutputStream对基本输出流功能扩展，提供了基本数据类型的输出方法，也就是基本类型是序列化方法：
 *   writeInt()
 *   writeDouble()
 *   writeUTF()
 *   DataOutputStream可以被理解为“过滤器”
 *   应用程序 -> DataOutputStream（过滤器）-> FileOutputStream（输出流）-> 文件(Byte)
 *   DataInputStream是过滤器，只有功能扩展，不能直接读取文件。
 *   DataInputStream提供的方法有
 *   readInt()
 *   readDouble()
 *   ...
 *   文件(Byte) -> FileInputStream（输入流）-> DataInputStream（过滤器）->应用程序
 *   
 * BufferedOutputStream和BufferedInputStream
 *   BufferedInputStream && BufferedOutputStream为IO操作提供了缓冲区，一般打开文件进行写入或读取操作时，都加上缓冲流，这种流模式是为了提高IO（输入输出）的性能。
 *   应用程序 -> DataOutputStream（过滤器）->BufferedOutputStream（缓冲区）-> FileOutputStream（输出流）-> 文件(Byte)
 *   我们想从应用程序中把数据放入文件，相当于将一缸水倒入另一个缸中：
 *   仅使用FOS的write()方法，想当于一滴水一滴水的“转移”
 *   使用DOS的writeXxx()方法方便些，相当于一瓢一瓢的“转移”
 *   使用BOS的writeXxx()方法更方便，相当于从DOS一瓢一瓢放入桶(BOS)中，再从桶(BOS)中倒入另一个缸，性能提高了。
 *   
 * 文件复制实现与优化
 *   文件 -> IS（输入流） -> 应用程序 -> OS（输出流） -> 文件
 *   
 * 字符串的序列化
 *   1）String字符串本质上是char[]
 *     将char[]转换成byte序列，就是字符串的编码，就是字符串的序列化问题。char类型是16位无符号整数，值是unicode编码
 *   2）UTF-16BE编码方案
 *     UTF-16BE编码方案，是将16位char从中间切开位2个byte
 *     UTF-16BE是将unicode编码的char[]序列化为byte[]的编码方案
 *     如：char[] = ['A','B','中']
 *     byte[] = [00,41,00,00,42,4e,2d]
 *     UTF-16BE编码能够支持65535个字符编码
 *   3）UTF-8编码方案
 *     采用变长编码1~N方案，其中英文占1个byte，中文占3个byte
 *   4）较常用的编码
 *     GBK：中国国标，支持20000+中日英韩字符，英文1位编码，中文2位
 *     与Unicode编码不兼容，需要码表转换
 *     GB2312 简体中文编码
 *     ISO8859-1：西欧常用字符
 *     UTF-8：Unicode的一种变长字符编码，又称“万国码”
 *     
 * 认识文本和文本文件
 *   文本文件是文本(char)序列按照某种编码方案(utf8)序列化为byte的存储结果
 *   
 * 对象的序列化
 *   Object -> byte
 *   1）序列化流 ObjectOutputStream 过滤流
 *     ObjectOutputStream writeObject(Object) 序列化对象
 *     ObjectInputStream readObject() 反序列化对象
 *   2）序列化接口 Serializable 空接口，只是一个标识
 *   3）可以通过序列化和反序列化实现深层复制
 *   
 * 浅层复制和深层复制
 *   1）java的默认复制规则是浅层复制，性能好，但隔离性差，浅层复制现象只复制第一层对象
 *   2）利用序列化可以实现深层复制
 *   
 * 总结
 *   A．分类
 *     数据格式分类：
 *       字节流（传输的为byte）：以Stream结尾的流，继承自InputStream和OutputStream（抽象类）
 *       字符流（传输的为unicode字符）：以Writer和Reader结尾的流，继承自Writer和Reader（抽象类）
 *     功能分类
 *       节点流：基本的流的实现，流开始和结束的地方
 *       过滤流：对节点流进行了功能的升华，进行了扩展
 *       还有其他的分类方式：缓冲区、输入输出等
 *   B．特殊流的功能：
 *     1. 可以读写基本类型数据和字符串的流，都实现自DataInput/DataOutput接口
 *       RandomAccessFile
 *       DataInputStream/DataOutputStream 
 *       ObjectInputStream/ObjectOutputStream 
 *     2. 带用缓冲区，提高效率
 *       BufferedInputStream/BufferedOutputStream
 *       BufferedReader/BufferedWriter中提供了readLine()方法
 *     3. 从字节流到字符流之间的桥梁
 *       InputStreamReader/OutputStreamWriter
 *     4. 读写对象类型
 *       ObjectInputStream/ObjectOutputStream 使用时注意实现对象的系列化接口
 *     5. PrintStream
 *       println()方法
 *     6. 即可以读也可以写的io类
 *       RandomAccessFile
 *   C．使用流需要注意的：
 *     1. 使用输入输出流在结束的时候需要调用close()方法关闭，flush方法刷出
 *     2. 文件的路径，默认路径为项目文件夹
 *   D．对象的序列化
 *     java.io.Serializable 实现序列化在网络上的应用，便于操作
 *   E.编码问题：文本的序列化
 *     中文             英文
 *     GBK     1byte    2byte
 *     UTF-8   1byte    3byte
 * 
 * @author  Colin Chen
 * @create  2018年11月10日 下午9:44:52
 * @modify  2018年11月10日 下午9:44:52
 * @version A.1
 */
public class IOHelper {

}
