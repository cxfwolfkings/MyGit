package com.angel.javase;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Scanner;

public class FileDemo {
	public static void main(String[] args) {

	}

	public static void copy() {
		try {
			for(int i = 2; i < 5; i++){
				String fileName = "ProductIcon_03_" + i;
				for(int j = 4; j < 64; j++){
					FileInputStream input = new FileInputStream("C:\\Users\\colin.chen\\Desktop\\" + fileName + ".jpg");
					String copyFileName = "ProductIcon_" + String.format("%02d", j) + "_" + i;
					FileOutputStream output = new FileOutputStream("C:\\Users\\colin.chen\\Desktop\\Copy\\" + copyFileName + ".jpg");
					int in = input.read();
					while (in != -1) {
						output.write(in);
						in = input.read();
					}
					output.close();
					input.close();
				}
			}
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
}

/**
 * 从控制台输入信息，保存到硬盘上，使用异步保存文件
 */
public class SaveInfo extends Thread {
	public static void start() throws FileNotFoundException {
		final LinkedList<String> list = new LinkedList<String>();
		final PrintWriter out = new PrintWriter(
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
	}
}
