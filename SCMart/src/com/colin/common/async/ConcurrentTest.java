package com.colin.common.async;

/**
 * JDK5.0 以后的版本都引入了高级并发特性，并且新的版本在不断的补充和完善。大多数的特性在java.util.concurrent 包中实现，Java 集合框架中也有新的并发数据结构。
 * 主要增加的高级并发对象有：Lock 对象，执行器，并发集合、原子变量和同步器。
 * 1）Lock 对象
 *    前面介绍的同步代码依靠简单类型的可重入锁，即内部锁（隐式锁）。这种类型的锁易于使用，但是有很多局限性。新的Lock 对象支持更加复杂的锁定语法。
 *    和隐式锁类似，每一时刻只有一个线程能够拥有Lock 对象，通过与其相关联的Condition对象，Lock 对象也支持wait 和notify 机制。Lock 对象的最大优势在于能够阻挡获得锁的企图。
 *    如果锁不能立即可用或者在超时时间到期之前可用，tryLock 方法就会阻挡，如果另一个线程在获得锁之前发送中断，lockInterruptibly 方法就会阻挡。
 * 2）执行器
 *    前面例子，线程完成的任务（Runnable 对象）和线程对象（Thread）之间紧密相连。适用于小型程序，在大型应用程序中，把线程管理和创建工作与应用程序的其余部分分离开更有意义。
 *    封装线程管理和创建的对象被称为执行器（Executor）。JDK 中定义了3 个执行器接口：Executor，ExecutorService 和ScheduledExecutorService。
 * 3）并发集合
 *    并发集合是原有集合框架的补充， 为多线程并发程序提供了支持。主要有：BlockingQueue，ConcurrentMap，ConcurrentNavigableMap。
 * 4）原子变量
 *    定义了支持对单一变量执行原子操作的类。所有类都有 get 和set 方法，工作方法和对volatile 变量的读取和写入一样。
 * 5）同步器
 *    提供了一些帮助在线程间协调的类，包 括 semaphores, mutexes, barriers, latches, exchangers 等。
 * 
 * @author  colin.chen
 * @version 1.0.0
 * @date    2018年9月12日 下午2:09:52
 */
public class ConcurrentTest {

}
