package com.colin.common.async;

/**
 * 线程组
 * @author Colin Chen
 * @date   2018年9月11日 上午6:15:35
 */
public class MyThreadGroup extends Thread {
	public static int flag = 1;
	ThreadGroup tgA;
	ThreadGroup tgB;

	public static void Demo() {
		MyThreadGroup dt = new MyThreadGroup();
		dt.tgA = new ThreadGroup("A");
		dt.tgB = new ThreadGroup("B");
		for (int i = 1; i < 3; i++)
			new ThreadMember(dt.tgA, i * 1000, "one" + i);
		for (int i = 1; i < 3; i++)
			new ThreadMember(dt.tgB, 1000, "two" + i);
		dt.start();
	}

	public void run() {
		try {
			sleep(5000);
			this.tgB.checkAccess();
			this.tgB.destroy();
			System.out.println("**************tgB stop!***********************");
			sleep(1000);
			this.tgA.checkAccess();
			this.tgA.destroy();;
			System.out.println("**************tgA stop!***********************");
		} catch (SecurityException es) {
			System.out.println("**" + es);
		} catch (Exception e) {
			System.out.println("::" + e);
		}
	}
}

class ThreadMember extends Thread{
	int pauseTime;
	String name;

	public ThreadMember(ThreadGroup g, int x, String n) {
		super(g, n);
		pauseTime = x;
		name = n;
		start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				System.out.print(name + "::::");
				this.getThreadGroup().list();
				Thread.sleep(pauseTime);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
