package com.colin.common;

/**
 * 枚举类都是类库中Enum类的子类(java.lang.Enum)，是一个不可以被继承的final类。
 * 枚举值都是public static final的。
 * 构造器只能私有private，绝对不允许有public构造器。这样可以保证外部代码无法新建构造枚举类的实例。
 * 这也是完全符合情理的，因为我们知道枚举值是public static final的常量而已。
 * 但枚举类的方法和数据域可以允许外部访问。
 * 
 * @author Colin Chen
 * @date   2018年11月9日 下午9:12:17
 * 
 */
public class EnumModel {
	public enum Color{
		RED, BLUE, BLACK, YELLOW, GREEN
	}
}
