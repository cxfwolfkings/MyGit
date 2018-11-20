package com.colin.common.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用线程池创建的多线程程序，100个线程目标对象共享2个线程
 * @author Colin Chen
 * @date   2018年9月11日 上午6:28:12
 */
public class TestThreadPool {
	public static void main(String args[]) throws InterruptedException {
		// 在线程池中创建2个线程
		ExecutorService exec = Executors.newFixedThreadPool(2);
		// 创建100个线程目标对象
		for (int index = 0; index < 100; index++) {
			Runnable run = new Runner(index);
			// 执行线程目标对象
			exec.execute(run);
		}
		// shutdown
		exec.shutdown();
	}
}

/**
 * 1) public static ExecutorService newFixedThreadPool(int nThreads)
 * 创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程，在需要时使用提供的 ThreadFactory 创建新线程。在任意点，在大多数 nThreads 线程会处于处理任务的活动状态。
 * 如果在所有线程处于活动状态时提交附加任务，则在有可用线程之前，附加任务将在队列中等待。如果在关闭前的执行期间由于失败而导致任何线程终止，那么一个新线程将代替它执行后续的任务（如果需要）。
 * 在某个线程被显式地关闭之前，池中的线程将一直存在。
 * 
 * 2) public static ThreadFactory defaultThreadFactory()
 * 返回用于创建新线程的默认线程工厂。此工厂创建同一个线程组(ThreadGroup)中Executor 使用的所有新线程。如果有SecurityManager，则它使用System.getSecurityManager()返回的组，
 * 其他情况则使用调用defaultThreadFactory 方法的组。每个新线程都作为非守护程序而创建，并且具有设置线程优先级为Thread.NORM_PRIORITY 与线程组中允许的最大优先级的较小者。
 * 新线程具有可通过pool-N-thread-M 的 Thread.getName() 来访问的名称，其中N 是此工厂的序列号，M 是此工厂所创建线程的序列号。
 * 
 * 3) public static ExecutorService newCachedThreadPool()
 * 创建一个可根据需要创建新线程的线程池，但是在以前构造的线程可用时将重用它们。对于执行很多短期异步任务的程序而言，这些线程池通常可提高程序性能。调用 execute 将重用以前构造的线程（如果线程可用）。
 * 如果现有线程没有可用的，则创建一个新线程并添加到池中。终止并从缓存中移除那些已有 60 秒钟未被使用的线程。因此，长时间保持空闲的线程池不会使用任何资源。
 * 注意，可以使用 ThreadPoolExecutor 构造方法创建具有类似属性但细节不同（例如超时参数）的线程池。
 * 
 * 4) public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize)
 * 创建一个线程池，它可安排在给定延迟后运行命令或者定期地执行。
 * 
 * 5) void execute(Runnable command)
 * 在未来某个时间执行给定的命令。该命令可能在新的线程、已入池的线程或者正调用的线程中执行，这由 Executor 实现决定。
 * 
 * 6) void shutdown()
 * 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用没有其他作用。
 */

/**
 * 线程目标对象
 * @author Colin Chen
 * @date   2018年9月11日 上午6:33:16
 */
class Runner implements Runnable {
	int index = 0;
	
	public Runner(int index) {
		this.index = index;
	}
	
	@Override
	public void run() {
		long time = (long) (Math.random() * 1000);
		// 输出线程的名字和使用目标对象及休眠的时间
		System.out.println("线程：" + Thread.currentThread().getName() + "(目标对象" + index + ")" + ":Sleeping " + time + "ms");
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}
}
