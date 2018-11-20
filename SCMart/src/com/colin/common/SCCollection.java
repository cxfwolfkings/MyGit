package com.colin.common;

/**
 * Collection
 * --Set
 * ----HashSet
 * ----LinkedHashSet
 * ----SorteSet
 * ------TreeSet
 * --List
 * ----ArrayList
 * ----LinkedList
 * 
 * Map：key-value对，key不能重复
 * --HashMap
 * --SortedMap
 * ----TreeMap
 * 
 * StringBuilder：char[] + 操作（增删改查） 
 * ArrayList：Object[] + 线性表操作（增删改查）；ArrayList（1.2以后新的）是使用变长数组算法实现的List（线性表方法），读取修改很快，删除较慢，非线程安全
 * Vector(1.0)：使用变长数组算法实现，是List，矢量向量，线程安全
 * LinkedList：采用双向循环链表实现的List，接口中定义的方法；在头尾插入/修改速度很快，读取比较慢
 * 
 * 1、散列表 Map   
 *   1) 容量：散列表中散列数组大小
 *   2) 散列运算：key->散列值（散列数组下标）的算法，如："mm".hashCode()%10->8
 *   3) 散列桶：散列值相同的元素的“线性集合”
 *   4) 加载因子：就是散列数组加载率，一般小于75%性能比较理想。就是：元素数量/散列数组大小，如: 7/10=70%
 *   5) 散列查找：根据Key计算散列值，根据散列值（下标）找到散列桶，在散列桶中顺序比较Key，如果一样，就返回value
 *   6) 散列表中Key不同，Value可以重复
 * 2、HashMap（关键字：值），关键字key是唯一不重复的，查找表
 *   1) key可以是任何对象，Value可以任何对象
 *   2) key:value成对的放置到集合中
 *   3) 重复的key算一个，重复添加是替换操作
 *   4) 根据key的散列值计算散列表，元素按照散列值（不可见）排序
 *   5) 默认的容量：16；默认加载因子（加载率）：0.75
 *   6) 根据key检索查找value值
 *   7) 用于查找场合，可以提高根据key查找效率
 *   8) HashMap VS Hashtable
 *      A、HashMap 新，非线程安全，不检查锁，快
 *      B、Hashtable 旧（1.2以前），线程安全，检查锁，慢一点
 *      
 * 包装类
 * 集合框架 （Collection and Map，集合与映射，容器类(cpp)）
 *   1) List 元素有先后次序的集合，元素有index位置，元素可以重复，继承Collection接口，实现类：ArrayList、Vector、LinkedList
 *   2) Set 元素无序，不能重复添加，是数学意义上的集合，继承Collection接口，实现类：HashSet（是一个只有Key的HashMap）
 *   3) Collection 集概念，没有说明元素是否重复和有序，使用集合的根接口，很少直接使用，其他集合都是实现类：ArrayList、HashSet
 *   4) Map描述了key:value成对放置的集合，key不重复，Value可以重复，key重复算一个。实现类：HashMap（散列表算法实现）。TreeMap（二叉排序树实现，利用Key排序）。Map适合检查查找
 * 集合的迭代（遍历算法）
 *   1) java使用Iterator 接口描述了迭代模式操作
 *     Iterator 中的方法模式化设计，专门配合while循环操作
 *   2) Iterator 的实例可以从集合对象获得，是这个集合的一个元素序列视图，默认包含一个操作游标。在第一个元素之前，hasNext()方法可以检查游标是否有下一个元素，next()方法移动游标到下一个元素，并且返回这个元素引用。使用while配合这个两个方法，可以迭代处理集合的所有元素
 *   3) 迭代时候可以使用迭代器remove()方法删除刚刚迭代的元素，在迭代过程中，不能使用集合方法(add, remove, set)更改集合元素
 * 比较大小
 *   1) Comparable可以比较的，用于类实现，实现这个接口表示：这个类的实例可以比较大小，可以进行自然排序。Comparable的实现必须与equals()的结果一致，就是相等的对象比较结果一定是0！
 *   2) Comparator比较工具，用于临时定义比较规则，不是默认比较规则
 * 集合复制：默认的复制规则是浅表（浅层）复制
 *   A、clone()方法
 *   B、使用复制构造器！
 *     Map map = new HashMap();
 *     Map map2 = new HashMap(map);
 *     List list1 = new ArrayList();
 *     List list2 = new LinkedList(list1);
 * 同步化（线程安全的）
 *   List list = new ArrayList();
 *   // synchronizedList可以将非线程安全的list包装为线程安全的
 *   list = Collections.synchronizedList(list); 转换以后就相当于 Vector
 *   HashMap map = new HashMap(); 
 *   // synchronizedMap可以将非线程安全的map包装为线程安全的 
 *   map = Collections.synchronizedMap(map);
 * 数组与集合的转换
 *   a、数组转List（只读的） 
 *     String[] names = {"A","B","C"};
 *     List list = Arrays.asList(names); // 只读list
 *     list = new ArrayList(list); // 复制为全功能List
 *     Set set = new HashSet(list); // 复制为Set集合
 *   b、集合转数组
 *     Object[] ary1 = list.toArray(); // 集合转Object[]
 *     String[] ary2 = (String[])list.toArray(new String[]{}); // 集合转制定类型数组
 * Collection 与 Collections
 *   Collection 抽象的集合概念
 *   Collections 集合静态工具类，包含集合的工具方法
 * 
 * @author  Colin Chen
 * @create  2018年11月10日 上午8:08:57
 * @modify  2018年11月10日 上午8:08:57
 * @version A.1
 */
public class SCCollection {

}
