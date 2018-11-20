package com.colin.common;

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
 * @author  Colin Chen
 * @create  2018年11月13日 下午9:56:24
 * @modify  2018年11月13日 下午9:56:24
 * @version A.1
 */
public class ReflectHelper {

}
