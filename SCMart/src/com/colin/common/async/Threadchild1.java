package com.colin.common.async;

/**
 * 继承 java.lang.Thread 类创建线程是最简单的一种方法，也最直接。
 * @author Colin Chen
 * @date   2018年9月11日 上午6:08:34
 */
public class Threadchild1 extends Thread {
	boolean runflag = true;
	boolean suspended = true;

	public synchronized void fauxresume() {
		suspended = false;
		notify();
	}

	public void run() {
		while (runflag) {
			System.out.println("I am working..............");
			try {
				// 暂停1s，该方法不会放弃除CPU之外的其它资源。并不保证这些睡眠时间的精确性，因为他们受到系统计时器和调度程序精度和准确性的影响。
				// 另外中断（interrupt）可以终止睡眠时间，在任何情况下，都不能假设调用sleep 就会按照指定的时间精确的挂起线程。
				sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("sleep::" + e);
			}
			synchronized (this) {
				try {
					if (suspended)
						wait();
				} catch (InterruptedException e) {
					System.out.println("wait::" + e);
				}
			}
		}
		System.out.println("thread over...........");
	}
}

/**
 * Join 方法让一个线程等待另一个线程的完成，如果t1，t2 是两个Thread 对象，在t1 中调用t2.join（），会导致t1 线程暂停执行，直到t2 的线程终止。
 * Join 的重载版本允许程序员指定等待的时间，但是和sleep 一样，这个时间是不精确的。
 * 
 */

