package com.colin.common.async;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时器java.util.Timer功能：
 * 1) 可以安排自动的计划任务的类，每个人物都是一个线程
 * 2) 创建Timer实例
 * 3) 为Timer实例增加计划任务，计划任务是一个接口
 * 4) 使用cancel()清除timer对象上所有计划任务
 * 
 * @author  Colin Chen
 * @create  2018年11月13日 下午9:54:19
 * @modify  2018年11月13日 下午9:54:19
 * @version A.1
 */
public class TimerHelper {
	public static void main(String[] args) {
		final Timer timer = new Timer();
		long start = System.currentTimeMillis();
		final long end = start + 6 * 1000;
		TimerTask task = new TimerTask() {
			public void run() {
				long show = end - System.currentTimeMillis();
				long h = show / 1000 / 60 / 60;
				long m = show / 1000 / 60 % 60;
				long s = show / 1000 % 60;
				System.out.println(h + ":" + m + ":" + s);
			}
		};
		timer.schedule(task, 0, 1000);
		TimerTask task2 = new TimerTask() {
			public void run() {
				timer.cancel();
			}
		};
		timer.schedule(task2, new Date(end));
	}
}
