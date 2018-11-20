package com.colin.common.async;

/**
 * 1、多线程编程概念
 *   Java语言的优势之一就是线程处理较为简单。
 *   一个进程内部可能包含多个顺序执行流，每个顺序执行流就是一个线程。
 *   程序：指令 + 数据 的 byte 序列
 *   进程：正在运行的程序，是程序动态的执行过程（运行于内存中）
 *   线程：在进程内部，并发运行的过程
 *   并发：进程是并发运行的，OS将时间划分为很多时间片段（时间片），尽可能均匀分配给正在运行的程序，微观上进程走走停停，宏观上都在运行，这种都运行的现象叫并发，但是不是绝对意义上的“同时发生”。
 * 2、创建线程
 *   1) 继承Thread类
 *   2) 覆盖run方法
 * 3、线程的状态
 *   New：新建；start()之前
 *   Runnable：可运行（就绪）；start()之后
 *   Running：（正在）运行；经过线程调度该线程获取了CPU，执行run()，系统分配的时间片结束后（或调用yield()方法），回到Runnable状态
 *   Block：阻塞（挂起）状态；发生情况：sleep()主动放弃、调用阻塞式(IO)方法。阻塞结束，进入Runnable状态
 *   Dead：死亡；run()结束，死亡线程不能再start()
 * 4、线程状态管理
 *   1)	Thread.yield() 让出CPU
 *     当前线程让出处理器，离开Running状态，进入Runnable状态
 *   2)	Thread.sleep(times) 休眠
 *     使当前线程从Running放弃处理器进入Block状态，休眠times毫秒，再返回到Runnable，如果其它线程打断(Thread.interrupt())当前线程的Block(sleep)，就会发生InterruptedException
 * 5、线程的常用属性及方法
 *   1)	线程优先级（资源紧张时，尽可能优先）
 *     t3.setPriority(Thread.MAX_PRIORITY); 设置为最高优先级
 *     默认有10优先级，默认优先级5；优先级高的线程获得执行（进入Running状态）的机会多
 *   2)	后台线程（守护线程，精灵线程）
 *     t1.setDaemon(true);
 *     Java进程的结束：当前所有前台线程结束时，Java进程结束；当前台线程结束时，不管后台线程是否结束，都要被停掉！
 *   3)	获得线程名字：getName()
 *   4)	获得当前线程：Thread main = Thread.currentThread();
 * 6、线程并发安全问题
 *   多个线程并发读写同一个临界资源会发生“线程并发安全问题”，如果保证多线程同步访问临界资源，就可以解决。
 *   常见临界资源：多线程共享实例变量、静态公共变量
 *   如何同步：
 *   synchronized(同步监视器) { }
 *   同步监视器是一个任意对象实例，多线程要使用同一个“监视器”，实现互斥
 *   如果方法的全部过程需要同步，可以使用synchronized修饰方法，相当于整个方法的synchronized(this)
 *   尽量减少同步范围，提高并发效率
 * 7、ThreadLocal是什么
 *   ThreadLocal为解决多线程程序的并发问题提供了一种新的思路。使用这个工具类可以很简洁地编写出优美的多线程程序。
 *   当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
 *   从线程的角度看，目标变量就象是线程的本地变量，这也是类名中"Local"所要表达的意思。
 *   所以，在Java中编写线程局部变量的代码相对来说要笨拙一些，因此造成线程局部变量没有在Java开发者中得到很好的普及。
 *   ThreadLocal类接口很简单，只有4个方法，我们先来了解一下：
 *     1）void set(Object value)：设置当前线程的局部变量的值。
 *     2）public Object get()：该方法返回当前线程所对应的局部变量。
 *     3）public void remove()：将当前线程局部变量的值删除，目的是为了减少内存的占用，该方法是JDK 5.0新增的方法。需要指出的是，当线程结束后，对应该线程的局部变量将自动被垃圾回收，所以显式调用该方法清除线程的局部变量并不是必须的操作，但它可以加快内存回收的速度。
 *     4）protected Object initialValue()：返回该线程局部变量的初始值，该方法是一个protected的方法，显然是为了让子类覆盖而设计的。这个方法是一个延迟调用方法，在线程第1次调用get()或set(Object)时才执行，并且仅执行1次。ThreadLocal中的缺省实现直接返回一个null。
 *   值得一提的是，在JDK5.0中，ThreadLocal已经支持泛型，该类的类名已经变为ThreadLocal<T>。API方法也相应进行了调整，新版本的API方法分别是void set(T value)、T get()以及T initialValue()。
 *   ThreadLocal是如何做到为每一个线程维护变量的副本的呢？其实实现的思路很简单：在ThreadLocal类中有一个Map，用于存储每一个线程的变量副本，Map中元素的键为线程对象，而值对应线程的变量副本。我们自己就可以提供一个简单的实现版本
 *   
 * 
 * @author  Colin Chen
 * @create  2018年11月11日 下午8:21:29
 * @modify  2018年11月11日 下午8:21:29
 * @version A.1
 */
public class ThreadHelper {

}

/**
 * 通常我们通过匿名内部类的方式定义ThreadLocal的子类，提供初始的变量值。
 * ThreadLocal和线程同步机制相比有什么优势呢？
 *   ThreadLocal和线程同步机制都是为了解决多线程中相同变量的访问冲突问题。
 *   在同步机制中，通过对象的锁机制保证同一时间只有一个线程访问变量。这时该变量是多个线程共享的，使用同步机制要求程序慎密地分析什么时候对变量进行读写，什么时候需要锁定某个对象，什么时候释放对象锁等繁杂的问题，程序设计和编写难度相对较大。
 *   而 ThreadLocal则从另一个角度来解决多线程的并发访问。ThreadLocal会为每一个线程提供一个独立的变量副本，从而隔离了多个线程对数据的访问冲突。因为每一个线程都拥有自己的变量副本，从而也就没有必要对该变量进行同步了。
 *   ThreadLocal提供了线程安全的共享对象，在编写多线程代码时，可以把不安全的变量封装进ThreadLocal。
 *   概括起来说，对于多线程资源共享的问题，同步机制采用了“以时间换空间”的方式，而ThreadLocal采用了“以空间换时间”的方式。
 *   前者仅提供一份变量，让不同的线程排队访问，而后者为每一个线程都提供了一份变量，因此可以同时访问而互不影响。
 *   需要注意的是 ThreadLocal对象是一个本质上存在风险的工具，应该在完全理解将要使用的线程模型之后，再去使用ThreadLocal对象。
 *   这就引出了线程池(thread pooling)的问题，线程池是一种线程重用技术，有了线程池就不必为每个任务创建新的线程，一个线程可能会多次使用。
 *   用于这种环境的任何ThreadLocal对象包含的都是最后使用该线程的代码所设置的状态，而不是在开始执行新线程时所具有的未被初始化的状态。
 *   那么 ThreadLocal是如何实现为每个线程保存独立的变量的副本的呢？通过查看它的源代码，我们会发现，是通过把当前“线程对象”当作键，变量作为值存储在一个Map中。
 * 小结
 *   ThreadLocal是解决线程安全问题一个很好的思路，它通过为每个线程提供一个独立的变量副本解决了变量并发访问的冲突问题。在很多情况下，ThreadLocal比直接使用synchronized同步机制解决线程安全问题更简单，更方便，且结果程序拥有更高的并发性。
 *   
 *   
 * @author  Colin Chen
 * @create  2018年11月11日 下午8:29:07
 * @modify  2018年11月11日 下午8:29:07
 * @version A.1
 */
class TestNum {
    // 1、通过匿名内部类覆盖ThreadLocal的initialValue()方法，指定初始值
    private static ThreadLocal<Integer> seqNum = new ThreadLocal<Integer>() {
        public Integer initialValue() {
            return 0;
        }
    };

    // 2、获取下一个序列值
    public int getNextNum() {
        seqNum.set(seqNum.get() + 1);
        return seqNum.get();
    }

    public static void main(String[] args) {
        TestNum sn = new TestNum();
        // 3、3个线程共享sn，各自产生序列号
        TestClient t1 = new TestClient(sn);
        TestClient t2 = new TestClient(sn);
        TestClient t3 = new TestClient(sn);
        t1.start();
        t2.start();
        t3.start();
    }

    private static class TestClient extends Thread {
        private TestNum sn;

        public TestClient(TestNum sn) {
            this.sn = sn;
        }

        public void run() {
            for (int i = 0; i < 3; i++) {
                // 4、每个线程打出3个序列值
                System.out.println("thread[" + Thread.currentThread().getName() + "] --> sn[" + sn.getNextNum() + "]");
            }
        }
    }
}



