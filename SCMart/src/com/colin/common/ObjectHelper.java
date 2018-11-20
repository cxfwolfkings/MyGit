package com.colin.common;

/**
 * Object：
 * 1）Java类默认继承于java.lang.Object! -> 啥都是东西，继承了Object的属性和方法
 * 2）toString()方法，经常有系统默认调用，当前对象的文本描述，Object默认返回: 全限定名@hashCode
 *    建议覆盖为：当前对象的文本描述，（看着办）
 * 3）equals是用来比较两个对象是否相等的方法。
 *    区别：引用相等 与 对象相等
 *    引用值相等： 使用 "=="
 *    对象的内容相等： .equals() 方法
 *    equals在Object类中声明，默认的比较规则是：比较引用
 *    建议覆盖，实现对象的比较（比较对象的状态，就是比较对象的数据）。
 *    覆盖规则：
 *    a、自反性：对于任何非空引用值 x，x.equals(x)都应返回 true。
 *    b、对称性：对于任何非空引用值 x 和 y，当且仅当 y.equals(x)返回 true时，x.equals(y)才应返回 true。
 *    c、传递性：对于任何非空引用值 x、y 和 z，如果 x.equals(y)返回 true，并且 y.equals(z)返回 true，那么 x.equals(z) 应返回 true。
 *    d、一致性：对于任何非空引用值 x 和 y，多次调用 x.equals(y)始终返回 true 或始终返回 false，前提是对象上 equals比较中所用的信息没有被修改。
 *    e、对于任何非空引用值 x，x.equals(null)都应返回 false。
 * 4）hashCode() 
 *    A、hashCode()方法要与equals方法一同覆盖
 *      a、当两个对象equals比较为true时候，这两个对象应该具有相同的hashCode()值
 *      b、当两个对象equals比较为false时候，这两个对象应该具有尽可能不相同的hashCode()值
 *      c、hashCode()值要稳定（一致性），一个对象创建以后就不应该再变化
 *    B、默认的hashCode()值是当前堆对象地址转换的一个整数，这个整数不是内存地址。
 *    C、一般使用对象的OID值作为hashCode的值。OID是对象的唯一编号，在工程项目中一般采用数据库来生成OID，也就是数据库中的“主键”
 * 
 * @author  Colin Chen
 * @create  2018年11月10日 上午7:53:14
 * @modify  2018年11月10日 上午7:53:14
 * @version A.1
 */
public class ObjectHelper {

}
