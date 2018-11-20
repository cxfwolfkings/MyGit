package com.colin.common.async;

/**
 * 当对一个复杂对象进行某种操作时，从操作开始到操作结束，被操作的对象往往会经历若干非法的中间状态。
 * 这跟外科医生做手术有点像，尽管手术的目的是改善患者的健康，但医生把手术过程分成了几个步骤，每个步骤如果不是完全结束的话，都会严重损害患者的健康。想想看，
 * 如果一个医生切开患者的胸腔后要休三周假会怎么样？
 * 与此类似，调用一个函数（假设该函数是正确的）操作某对象常常会使该对象暂时陷入不可用的状态（通常称为不稳定状态），等到操作完全结束，
 * 该对象才会重新回到完全可用的状态。
 * 如果其他线程企图访问一个处于不可用状态的对象，该对象将不能正确响应从而产生无法预料的结果，如何避免这种情况发生是线程安全性的核心问题。
 * 
 * 一个类在可以被多个线程安全调用时就是线程安全的。
 * 正确性与安全性之间的关系非常类似于在描述ACID（原子性、一致性、独立性和持久性）事务时使用的一致性与独立性之间的关系
 * 
 * 必须使用一种方法的结果作为另一种方法的输入条件的样式，它就是一个状态依赖，就必须保证至少在调用这两种方法期间元素的状态没有改变。
 * 
 * 下面分别描述了线程安全性的五种类别： 1）不可变：不可变的对象一定是线程安全的，并且永远也不需要额外的同步。Java
 * 类库中大多数基本数值类如Integer、String 和BigInteger 都是不可变的。
 * 2）线程安全：由类的规格说明所规定的约束在对象被多个线程访问时仍然有效，不管运行时环境如何排列，线程都不需要任何额外的同步。这种线程安全性保证是很严格的。
 * 许多类，如Hashtable 或者 Vector 都不能满足这种严格的定义。
 * 3）有条件的线程安全：有条件的线程安全类对于单独的操作可以是线程安全的，但是某些操作序列可能需要外部同步。 条件线程安全的最常见的例子是遍历由
 * Hashtable 或者 Vector 返回的迭代器。由这些类返回的 fail-fast 迭代器假定在迭代器进行遍历的时候底层集合不会有变化。
 * 4）线程兼容：线程兼容类不是线程安全的，但是可以通过正确使用同步而在并发环境中安全地使用。 许 多常见的类是线程兼容的， 如集合类 ArrayList 和
 * HashMap、java.text.SimpleDateFormat 或者 JDBC 类 Connection 和 ResultSet。
 * 5）线程对立：线程对立类是那些不管是否调用了外部同步都不能在并发使用时安全地呈现的类。
 * 
 * Servlet的线程安全性： Servlet 体系结构是建立在Java 多线程机制之上的，它的生命周期是由Web
 * 容器负责的。当客户端第一次请求某个Servlet 时，Servlet 容器将会根据web.xml 配置文件实例化这个Servlet 类。
 * 当有新的客户端请求该Servlet 时，一般不会再实例化该Servlet 类，也就是有多个线程在使用这个实例。Servlet
 * 容器会自动使用线程池等技术来支持系统的运行。
 * 这样，当两个或多个线程同时访问同一个Servlet时，可能会发生多个线程同时访问同一资源的情况，数据可能会变得不一致。所以在用Servlet 构建的Web
 * 应用时如果不注意线程安全的问题，会使所写的Servlet 程序有难以发现的错误。 解决方法：无状态Servlet，没有实例变量，全部使用方法局部变量。
 * 
 * 当两个线程需要使用同一个对象时，存在交叉操作而破坏数据的可能性。这种潜在的干扰动作在术语上被称作临界区（critical section）。
 * 通过同步（Synchronize）对临界区的访问可以避免这种线程干扰。
 * 某些动作操作对象之前，必须先获得这个对象的锁。获取待操作对象上的锁可以阻止其他对象获取这个锁，直至这个锁的持有者释放它为止。这样，多线程就不会同时执行那些会互相干扰的动作。
 * 同步是围绕被称为内在锁（intrinsic lock）或者监视器锁（monitor lock）的内部实体构建的，强制对对象状态的独占访问，以及建立可见性所需的发生前关系。
 * 每个对象都具有与其关联的内在锁，按照约定，需要对对象的字段进行独占和一致性访问的线程，在进行访问之前，必须获得这个对象的内在锁，访问操作完成之后必须释放内在锁。
 * 在从获得锁到释放锁的时间段内，线程被称为拥有内在锁。只要有线程拥有内在锁，其他线程就不能获得同一个锁，试图获得锁的其他线程将被阻塞。
 * Java 提供了synchronized 关键字来支持内在锁。Synchronized 关键字可以放在方法的前面、对象的前面、类的前面。
 * 1. 同步方法中的锁
 *    当线程调用同步方法时，它自动获得这个方法所在对象的内在锁，并且方法返回时释放锁，如果发生未捕获的异常，也会释放锁。
 *    当调用静态同步方法时，因为静态方法和类相关联，线程获得和这个类关联的 Class对象的内在锁。
 * 2. 同步语句
 * 	     同步语句必须指定提供内在锁的对象
 * 3. 同步类
 *    把 synchronized 关键字放在类的前面，这个类中的所有方法都是同步方法。
 * 4. 可重入同步
 *    线程可以获得它已经拥有的锁，运行线程多次获得同一个锁，就是可重入（reentrant）同步。
 *    这种情况通常是同步代码直接或者间接的调用也包含了同步代码的方法，并且两个代码集都使用同一个锁。如果没有可重入同步，那么同步代码就必须采取很多额外的预防措施避免线程阻塞自己。
 * 
 * 
 * @author Colin Chen
 * @date 2018年9月11日 下午8:34:21
 */
public class ThreadSafe {
	public static void main(String[] args) {
		try {
			// 线程干扰示例
			BankAccount.Demo();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/**
 * 银行账号
 * @author Colin Chen
 * @date 2018年9月11日 下午9:08:04
 */
class BankAccount {
	private int number;

	private int balance;

	public BankAccount(int number, int balance) {
		this.number = number;
		this.balance = balance;
	}

	/**
	 * 使用内在锁后，把 deposit 方法和 withdraw 方法修改为同步方法，就可以避免线程干扰。
	 * @return
	 */
	public synchronized int getBalance() {
		return balance;
	}

	public void deposit(int amount) {
		// 同步语句必须指定提供内在锁的对象
		synchronized (this) {
			balance = balance + amount;
		}
	}

	public void withdraw(int amount) {
		balance = balance - amount;
	}

	/**
	 * 假设线程 t1 执行deposit 操作时，线程t2 几乎同时执行 withdraw 操作，帐户的初始值为1000，
	 * 那么当存款的初始化值为1000 时，取款的初始值也为1000，存款操作的结果可能覆盖取款操作的结果，balance变为1100。10 万次操作后，就会形成比较严重的误差。
	 * @throws InterruptedException
	 */
	public static void Demo() throws InterruptedException {
		BankAccount a = new BankAccount(1, 1000);
		Thread t1 = new Thread(new Depositor(a, 100), "depositor");
		Thread t2 = new Thread(new Withdrawer(a, 100), "withdraw");
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		System.out.println(a.getBalance());
	}

	static class Depositor implements Runnable {
		BankAccount account;
		int amount;

		public Depositor(BankAccount account, int amount) {
			this.account = account;
			this.amount = amount;
		}

		@Override
		public void run() {
			for (int i = 0; i < 100000; i++)
				account.deposit(amount);
		}
	}

	static class Withdrawer implements Runnable {
		BankAccount account;
		int amount;

		public Withdrawer(BankAccount account, int amount) {
			this.account = account;
			this.amount = amount;
		}

		@Override
		public void run() {
			for (int i = 0; i < 100000; i++)
				account.withdraw(amount);
		}
	}

}
