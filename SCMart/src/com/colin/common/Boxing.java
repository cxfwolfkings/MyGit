package com.colin.common;

/**
 * 1、包装类可以把基本类型包装为对象类型
 * 2、有八种包装类
 *   int        Integer
 *   Long       Long
 *   byte       Byte
 *   short      Short
 *   Float      Float
 *   double     Double
 *   boolean    Boolean
 *   char       Character
 * 3、包装类提供了对应数据类型的工具方法
 *   Integer.toHexString()
 *   Integer.toString(int)
 *   Integer.toBinaryString()
 *   Integer.parseInt(String)
 *   "3f"->int
 *   Integer.parstInt(String)
 *   "2.718" -> 2.718 
 *   Double.parseDouble(String str);
 *   自动包装(auto boxing / unboxing)（java5 以后可以）：
 *   Integer i = 2; // i = new Integer(2);
 *   Object o = 3.5;
 *   System.out.println(o instanceof Double); // true
 *   int a = i + 1; // a = i.intValue() + 1;
 * 注意： 
 *   1、包装类是final的类
 *   2、包装类对象是不变的，与字符串类似（不变模式）
 *     Integer a = 1;
 *     Integer b = 2;
 *     a = a + b; // a = new Integer(a.intValue() + b.intValue())
 *   3、包装类覆盖了 toString、equals、hashCode、compareTo
 *   
 * 
 * @author  Colin Chen
 * @create  2018年11月10日 下午9:37:24
 * @modify  2018年11月10日 下午9:37:24
 * @version A.1
 */
public class Boxing {

}
