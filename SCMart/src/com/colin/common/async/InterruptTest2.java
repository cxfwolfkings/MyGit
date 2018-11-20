package com.colin.common.async;

/**
 * 如果一个线程长时间没有调用能够抛出 InterruptedException 异常的方法，那么线程就必须定期的调用Thread.interrupted方法，如果接收到中断就返回true，然后就可以退出线程。 
 * 
 * 使用 Stop 终止线程：
 * 在 Thread 类中提供了Stop 方法了强迫线程停止执行。但是现在已经过时了。
 * 该方法具有固有的不安全性。用 Thread.stop 来终止线程将释放它已经锁定的所有监视 器（作为沿堆栈向上传播的未检查ThreadDeath异常的一个自然后果）。
 * 如果以前受这些监视器保护的任何对象都处于一种不一致的状态，则损坏的对象将对其他线程可见，这有可能 导致任意的行为。
 * stop的许多使用方式都应由只修改某些变量以指示目标线程应该停止运行的代码来取代。目标线程应定期检查该变量，并且如果该变量指示它要停止运行，则从其运行方法依次返回。
 * 如果目标线程等待很长时间（例如基于一个条件变量），则应使用 interrupt 方法来中断该等待。
 * 无论该线程在做些什么，它所代表的线程都被迫异常停止，并抛出一个新创建的ThreadDeath 对象作为异常。
 * 应用程序通常不应试图捕获 ThreadDeath，除非它必须执行某些异常的清除操作（注意，抛出ThreadDeath 将导致try 语句的finally 子句在线程正式终止前执行）。
 * 如果catch 子句捕获了一个ThreadDeath 对象，则重新抛出该对象很重要，因为这样该线程才会真正终止。
 * 
 * 结束程序的执行：
 * 线程一般分为两种：用户线程和守护线程。用户线程的存在可以使应用程序保持运行状态，而守护线程则不会。当最后一个用户线程结束时，所有守护线程都会被终止，应用程序也随之结束。
 * 守护线程的终止，很像调用destroy 所产生的终止，事发突然，没有机会做任何清除，所以应该考虑清楚，用守护线程执行哪种类型的任务。
 * 使用Thread.setDaemon(true)可以把线程标记为守护线程。默认情况下，线程的守护状态继承自创建它的线程。
 * 我们可以通过调用 System，或者Runtime 的exit 方法来强制应用程序结束，这个方法将终止Java 虚拟机的当前执行过程。
 * 许多类会隐式的在应用程序中创建线程，比如图形用户界面，并创建了特殊的线程来处理事件。有些是守护线程，有些不是。如果没有更好的办法，那么就可以用exit 方法。
 * 
 * @author colin.chen
 * @version 1.0.0
 * @date 2018年9月11日 上午9:25:17
 */
public class InterruptTest2 extends Thread {
	static int result = 0;

	public InterruptTest2(String name) {
		super(name);
	}

	public static void main(String[] args) {
		System.out.println("主线程执行");
		Thread t = new InterruptTest2("计算线程");
		t.start();
		System.out.println("result：" + result);
		try {
			long start = System.nanoTime();
			t.join(10);
			long end = System.nanoTime();
			t.interrupt();
			System.out.println((end - start) / 1000000 + "毫秒后:" + result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println(this.getName() + "开始计算...");
		for (int i = 0; i < 100000; i++) {
			result++;
			if (Thread.interrupted()) {
				System.out.println(this.getName() + "被中断");
				// 在更加复杂的应用程序中，当线程收到中断信号后，抛出InterruptedException
				// 异常可能更有意义。把中断处理代码集中在catch 子句中。
				try {
					throw new InterruptedException();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println(this.getName() + "结束计算");
	}
}