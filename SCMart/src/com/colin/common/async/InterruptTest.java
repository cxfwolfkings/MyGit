package com.colin.common.async;

/**
 * 使用中断(Interrupt)取消线程。我们可以通过中断（Thread.interrupt）线程来请求取消，并且让线程来监视并响应中断。
 * 中断请求通常是用户希望能够终止线程的执行，但并不会强制终止线程，但是它会中断线程的睡眠状态，比如调用sleep 和wait 方法后。
 * 线程自己检查中断状态并终止线程比直接调用 stop()放要安全很多，因为线程可以保存自己的状态。并且stop()方法已经不推荐使用了。
 * 线程的中断状态只能有线程自己清除，当线程侦测到自己被中断时，经常需要在响应中断之前做某些清除工作，
 * 这些清除工作可能涉及那些在线程仍然保持中断状态时会受到影响的操作。 如果被中断的线程正在执行 sleep，或者wait
 * 方法，就会抛出InterruptedException 异常。 大体上任何执行阻塞操作的方法，都应该通过 Interrupt 来取消阻塞操作。
 * 
 * @author colin.chen
 * @version 1.0.0
 * @date 2018年9月11日 上午9:20:56
 */
public class InterruptTest extends Thread {
	static int result = 0;

	public InterruptTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		System.out.println("主线程执行");
		Thread t = new InterruptTest("计算线程");
		t.start();
		System.out.println("result：" + result);
		try {
			long start = System.nanoTime();
			t.join(2000);
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
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			System.out.println(this.getName() + "被中断,结束");
			return;
		}
		result = (int) (Math.random() * 10000);
		System.out.println(this.getName() + "结束计算");
	}
}
