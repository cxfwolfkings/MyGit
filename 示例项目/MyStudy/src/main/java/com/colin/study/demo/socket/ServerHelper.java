package demo.socket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerHelper {

	/**
	 * 
	 * @param port
	 * @throws IOException
	 */
	public static void startServerPort(int port) throws IOException {
		// Ϊ�˼���������е��쳣��Ϣ��������
		// ����һ��ServerSocket�����ڶ˿�port��
		ServerSocket server = new ServerSocket(port);
		// server���Խ�������Socket����������server��accept����������ʽ��
		Socket socket = server.accept();
		// ���ͻ��˽���������֮�����ǾͿ��Ի�ȡsocket��InputStream�������ж�ȡ�ͻ��˷���������Ϣ�ˡ�
		Reader reader = new InputStreamReader(socket.getInputStream());
		char chars[] = new char[64];
		int len;
		StringBuilder sb = new StringBuilder();
		while ((len = reader.read(chars)) != -1) {
			sb.append(new String(chars, 0, len));
		}
		System.out.println("from client: " + sb);
		reader.close();
		socket.close();
		server.close();
	}

	/**
	 * �������������Ǵ��������ж�ȡ�ͻ��˷��͹��������ݣ�
	 * �����������������������д�����ݸ��ͻ��ˣ��������رն�Ӧ����Դ�ļ���
	 * ��ʵ��������������ܲ����ᰴ������Ԥ������ķ�ʽ���У�
	 * ��Ϊ���������ж�ȡ������һ������ʽ��������������whileѭ���е��������ݵ�ʱ��ͻ�ִ��ѭ���壬
	 * ����ͻ����������������д��������Զ��ִ�в����ˡ�
	 * ���ǿͻ��˶�Ӧ��Socket�ر��������Ż�ֹͣ��whileѭ��Ҳ��������
	 * ������ֿ�����Զ�޷�ִ����ȥ������Ľ��������whileѭ����Ҫ����������������������
	 * �ݹ��������룬�ڲ��ϱ仯��Ҳֻ��ȡ���ĳ���len�Ͷ����������ˣ�len�Ѿ��ǲ����õ��ˣ�
	 * Ψһ���õľ��Ƕ����������ˡ�������������ͨ�����Ƕ���Լ��һ��������ǣ�
	 * ���ͻ��˷��͹��������ݰ���ĳ���������ʱ��˵����ǰ�������Ѿ���������ˣ�
	 * ���ʱ�����ǾͿ��Խ���ѭ���������ˡ�������˶�ȡ���ͻ��˷��͵Ľ�����ǣ�
	 * ����eof��ʱ�ͻ�������ݵĽ��գ���ֹѭ�������������Ĵ����ֿ��Լ��������ˡ� 
	 * @param port
	 * @throws IOException
	 */
	public static void startServer(int port) throws IOException {
		// Ϊ�˼���������е��쳣��Ϣ��������
		// int port = 8899;
		// ����һ��ServerSocket�����ڶ˿�8899��
		ServerSocket server = new ServerSocket(port);
		// server���Խ�������Socket����������server��accept����������ʽ��
		Socket socket = server.accept();
		// ���ͻ��˽���������֮�����ǾͿ��Ի�ȡsocket��InputStream�������ж�ȡ�ͻ��˷���������Ϣ�ˡ�
		Reader reader = new InputStreamReader(socket.getInputStream());
		char chars[] = new char[64];
		int len;
		StringBuilder sb = new StringBuilder();
		String temp;  
		int index; 
		while ((len = reader.read(chars)) != -1) {
			temp = new String(chars, 0, len);  
			if ((index = temp.indexOf("eof")) != -1) {//����eofʱ�ͽ�������  
				sb.append(temp.substring(0, index));  
				break;  
			}  
			sb.append(temp);
		}
		System.out.println("from client: " + sb);
		// �����дһ��
		Writer writer = new OutputStreamWriter(socket.getOutputStream());
		writer.write("Hello Client.");
		writer.flush();
		writer.close();
		reader.close();
		socket.close();
		server.close();
	}

}
