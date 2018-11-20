package com.colin.common;

/**
 * java正则表达式通过java.util.regex包下的Pattern类与Matcher类实现。共分三步：	
 * （1）构造一个模式：Pattern p=Pattern.compile("[a-z]*");
 * （2）建造一个匹配器：Matcher m = p.matcher(str);
 * （3）进行判断得到结果：boolean b = m.matches();
 * Matcher类提供三个匹配操作方法，三个方法均返回 boolean类型，当匹配到时返回true，没匹配到则返回false
 * （1）m.matches()：matches()对整个字符串进行匹配，只有整个字符串都匹配了才返回true
 * （2）m.lookingAt()：lookingAt()对前面的字符串进行匹配，只有匹配到的字符串在最前面才返回true
 * （3）m.find()：find()对字符串进行匹配，匹配到的字符串可以在任何位置
 * Matcher类的其他方法
 *   int groupcount()：返回此匹配器模式中的捕获组数。
 *   String replaceAll(String replacement)：用给定的 replacement 全部替代匹配的部分
 *   String repalceFirst(String replacement)：用给定的 replacement 替代第一次匹配的部分
 *   appendReplacement(StringBuffer sb, String replacement)：根据模式用replacement替换相应内容，并将匹配的结果添加到sb当前位置之后
 *   StringBuffer appendTail(StringBuffer sb)：将输入序列中匹配之后的末尾字串添加到sb 当前位置之后.
 *   group(n)：0代表永远都是匹配整个表达式的字符串的那部分 n<>0 时代表第n组匹配的部分
 * 
 * @author Colin Chen
 * @date   2018年11月9日 下午9:32:22
 *
 */
public class RegexHelper {

	public static void Demo(){
		
	}
	
}
