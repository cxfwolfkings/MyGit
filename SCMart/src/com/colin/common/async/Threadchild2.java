package com.colin.common.async;

/**
 * 为了构建结构清晰线程程序，可以把代码独立出来形成线程目标对象，然后传给Thread对象。通常，实现Runnable 接口的类创建的对象，称作线程的目标对象。
 * @author Colin Chen
 * @date 2018年9月11日 上午6:14:03
 */
public class Threadchild2 implements Runnable {

	public static void main(String[] args) {
		for (int i = 0; i < 5; i++) {
			// 创建线程目标对象
			Runnable r = new Threadchild2();
			// 把目标对象传递给Thread，即虚拟CPU
			new Thread(r, "thread" + i).start();
		}
	}

	@Override
	public void run() {
		for (int i = 0; i < 20; i++) {
			System.out.println(Thread.currentThread().getName() + ":" + i);
		}
	}
}
