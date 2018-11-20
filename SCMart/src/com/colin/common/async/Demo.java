package com.colin.common.async;

/**
 * 在Object.java中，定义了wait(), notify()和notifyAll()等接口。
 * wait()的作用是让当前线程进入等待状态，同时，wait()也会让当前线程释放它所持有的锁。
 * 而notify()和notifyAll()的作用，则是唤醒当前对象上的等待线程；notify()是唤醒单个线程，而notifyAll()是唤醒所有的线程。
 * 
 * Object中的wait(), notify()等函数，和synchronized一样，会对“对象的同步锁”进行操作。
 * wait()会使“当前线程”等待，因为线程进入等待状态，所以线程应该释放它锁持有的“同步锁”，否则其它线程获取不到该“同步锁”而无法运行！
 * OK，线程调用wait()之后，会释放它锁持有的“同步锁”；而且，根据前面的介绍，我们知道：等待线程可以被notify()或notifyAll()唤醒。
 * 现在，请思考一个问题：notify()是依据什么唤醒等待线程的？或者说，wait()等待线程和notify()之间是通过什么关联起来的？答案是：依据“对象的同步锁”。
 * 负责唤醒等待线程的那个线程(我们称为“唤醒线程”)，它只有在获取“该对象的同步锁”(这里的同步锁必须和等待线程的同步锁是同一个)，并且调用notify()或notifyAll()方法之后，才能唤醒等待线程。
 * 虽然，等待线程被唤醒；但是，它不能立刻执行，因为唤醒线程还持有“该对象的同步锁”。必须等到唤醒线程释放了“对象的同步锁”之后，等待线程才能获取到“对象的同步锁”进而继续运行。
 * 总之，notify(), wait()依赖于“同步锁”，而“同步锁”是对象所持有，并且每个对象有且仅有一个！这就是为什么notify(), wait()等函数定义在Object类，而不是Thread类中的原因。
 * 
 * yield()的作用是让步。它能让当前线程由“运行状态”进入到“就绪状态”，从而让其它具有相同优先级的等待线程获取执行权；
 * 但是，并不能保证在当前线程调用yield()之后，其它具有相同优先级的线程就一定能获得执行权；也有可能是当前线程又进入到“运行状态”继续运行！
 * 
 * 我们知道，wait()的作用是让当前线程由“运行状态”进入“等待(阻塞)状态”的同时，也会释放同步锁。而yield()的作用是让步，它也会让当前线程离开“运行状态”。它们的区别是：
 * 1、wait()是让线程由“运行状态”进入到“等待(阻塞)状态”，而yield()是让线程由“运行状态”进入到“就绪状态”。
 * 2、wait()会让线程释放它所持有对象的同步锁，而yield()方法不会释放锁。
 * 
 * sleep()定义在Thread.java中。作用是让当前线程休眠，即当前线程会从“运行状态”进入到“休眠(阻塞)状态”。
 * sleep()会指定休眠时间，线程休眠的时间会大于/等于该休眠时间；在线程重新被唤醒时，它会由“阻塞状态”变成“就绪状态”，从而等待cpu的调度执行。
 * 
 * wait()的作用是让当前线程由“运行状态”进入“等待(阻塞)状态”的同时，也会释放同步锁。而sleep()的作用是也是让当前线程由“运行状态”进入到“休眠(阻塞)状态”。
 * 但是，wait()会释放对象的同步锁，而sleep()则不会释放锁。
 * 
 * @author Colin Chen
 * @date   2018年9月11日 下午8:28:40
 */
public class Demo {
	public static void main(String[] main) {
		new Demo();
	}

	Demo() {
		try {
			Threadchild1 td = new Threadchild1();
			td.start();
			Thread.sleep(500);
			System.out.println("interrupt child thread");
			td.interrupt();

			System.out.println("let child thread wait!");
			td.wait();
			Thread.sleep(1000);

			System.out.println("let child thread working");
			td.fauxresume();
			// td.resume();
			Thread.sleep(1000);
			td.runflag = false;
			System.out.println("main over..............");
		} catch (InterruptedException ie) {
			System.out.println("inter main::" + ie);
		} catch (Exception e) {
			System.out.println("main::" + e);
		}
	}

}

