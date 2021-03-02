package com.colin.study.demo.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerSocketDemo {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8000);
		System.out.println("����˴�����ϣ��ȴ��ͻ�������...");
		final Socket socket = serverSocket.accept();
		System.out.println("�ͻ������ӳɹ���׼��ͨ��...");
		Thread getInfo = new Thread() {
			public void run() {
				try {
					InputStream in = socket.getInputStream();
					InputStreamReader reader = new InputStreamReader(in);
					BufferedReader br = new BufferedReader(reader);
					String info;
					while ((info = br.readLine()) != null) {
						System.out.println("�ͻ���˵:" + info);
					}
				} catch (IOException e) {
					System.out.println("ͨ�����!");
				}
			}
		};
		getInfo.start();
		OutputStream out = socket.getOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(out);
		PrintWriter pw = new PrintWriter(writer);
		Scanner console = new Scanner(System.in);
		String str;
		while ((str = console.nextLine()) != null) {
			pw.println(str);
			pw.flush();
		}
	}
}
