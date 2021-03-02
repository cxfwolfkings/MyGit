package com.colin.study.demo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Scanner;

public class SaveInfo extends Thread {
	public void start() {
		final LinkedList<String> list = new LinkedList<String>();
		try {
			PrintWriter out = new PrintWriter(
					new FileOutputStream("info.txt"));
			final Thread writer = new Thread() {
				@Override
				public void run() {
					while (true) {
						if (list.isEmpty()) {
							try {
								out.flush();
								Thread.sleep(5000);
							} catch (InterruptedException e) {
							}
							continue;
						}
						String str = list.removeFirst();
						out.println(str);
						out.flush();
					}
				}
			};
			writer.setDaemon(true);
			Thread reader = new Thread() {
				@Override
				public void run() {
					Scanner sc = new Scanner(System.in);
					while (true) {
						String info = sc.nextLine();
						if (info.equals("exit")) {
							writer.interrupt();
							break;
						}
						list.addLast(info);
					}
				}
			};
			reader.start();
			writer.start();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}