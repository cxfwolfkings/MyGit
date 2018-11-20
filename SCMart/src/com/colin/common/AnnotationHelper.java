package com.colin.common;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * 自定义annotation示例
 * @Override：只能用在方法之上的，用来告诉别人这一个方法是改写父类的。 
 * @Deprecated：建议别人不要使用旧的API的时候用的，编译的时候会用产生警告信息，可以设定在程序里的所有的元素上。
 * @SuppressWarnings：这一个类型可以来暂时把一些警告信息消息关闭
 * 
 * @author  Colin Chen
 * @create  2018年11月18日 下午7:58:36
 * @modify  2018年11月18日 下午7:58:36
 * @version A.1
 */
public class AnnotationHelper {

}

@Description("javaeye，做最棒的软件开发交流社区")
class Demo_Annotation {
	@Name(originate="创始人:robbin",community="javaEye") 
	public String getName() { 
	    return null; 
	} 

    @Name(originate="创始人:江南白衣",community="springside") 
	public String getName2() { 
	    return "借用两位的id一用，写这一个例子，请见谅！"; 
	} 
}

@Target(ElementType.TYPE) 
@Retention(RetentionPolicy.RUNTIME) 
@Documented
@interface Description { 
    String value(); 
} 

/**
 * 注意这里的@Target与@Description里的不同，参数成员也不同 
 */
@Target(ElementType.METHOD) 
@Retention(RetentionPolicy.RUNTIME) 
@Documented 
@interface Name { 
    String originate(); 
    String community(); 
} 

class TestAnnotation { 
	/** 
	　* author lighter 
	　* 说明：具体关天Annotation的API的用法请参见javaDoc文档 
	　*/ 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws Exception { 
	    String CLASS_NAME = "com.angel.javase.Demo_Annotation"; 
	    Class test = Class.forName(CLASS_NAME); 
	    Method[] method = test.getMethods(); 
	    boolean flag = test.isAnnotationPresent(Description.class); 
	    if(flag) { 
	        Description des = (Description)test.getAnnotation(Description.class); 
	        System.out.println("描述:"+des.value()); 
	        System.out.println("-----------------"); 
	    } 
	    // 把JavaEyer这一类有利用到@Name的全部方法保存到Set中去 
	    Set<Method> set = new HashSet<Method>(); 
	    for(int i=0;i<method.length;i++) { 
	    	boolean otherFlag = method[i].isAnnotationPresent(Name.class); 
	    	if(otherFlag) set.add(method[i]); 
	    } 
	    for(Method m: set) { 
	    	Name name = m.getAnnotation(Name.class); 
	    	System.out.println(name.originate()); 
	    	System.out.println("创建的社区:" + name.community());
	    } 
	} 
} 
