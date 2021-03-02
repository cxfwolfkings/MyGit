package com.colin.study.demo.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TalkClient {
	public static void main(String args[]) {
		try{
			Socket socket=new Socket("127.0.0.1",4700);
			// �򱾻���4700�˿ڷ����ͻ�����
			BufferedReader sin=new BufferedReader(new InputStreamReader(System.in));
			// ��ϵͳ��׼�����豸����BufferedReader����
			PrintWriter os=new PrintWriter(socket.getOutputStream());
			// ��Socket����õ��������������PrintWriter����
			BufferedReader is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// ��Socket����õ�����������������Ӧ��BufferedReader����
			String readline;
			readline=sin.readLine(); // ��ϵͳ��׼�������һ�ַ���
			while(!readline.equals("bye")){
				// ���ӱ�׼���������ַ���Ϊ "bye"��ֹͣѭ��
				os.println(readline);
				// ����ϵͳ��׼���������ַ��������Server
				os.flush();
				// ˢ���������ʹServer�����յ����ַ���
				System.out.println("Client:"+readline);
				// ��ϵͳ��׼����ϴ�ӡ������ַ���
				System.out.println("Server:"+is.readLine());
				// ��Server����һ�ַ���������ӡ����׼�����
				readline=sin.readLine(); // ��ϵͳ��׼�������һ�ַ���
			} // ����ѭ��
			os.close(); // �ر�Socket�����
			is.close(); // �ر�Socket������
			socket.close(); // �ر�Socket
		}catch(Exception e) {
			System.out.println("Error"+e); // �������ӡ������Ϣ
		}
	}
}
