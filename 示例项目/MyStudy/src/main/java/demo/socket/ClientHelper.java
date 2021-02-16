package demo.socket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientHelper {

	/**
	 * 
	 * @param host
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static void startClientPort(String host,int port) throws UnknownHostException, IOException {
		// Ϊ�˼���������е��쳣��ֱ��������
		// String host = "127.0.0.1"; // Ҫ���ӵķ����IP��ַ
		// int port = 8899; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�
		// �����˽�������
		Socket client = new Socket(host, port);
		// �������Ӻ�Ϳ����������д������
		Writer writer = new OutputStreamWriter(client.getOutputStream());
		writer.write("Hello Server.");
		writer.flush();// д���Ҫ�ǵ�flush
		writer.close();
		client.close();
	}
	
	/**
	 * �������������Ǹ�����˷�����һ�����ݣ�֮���ȡ����˷����������ݣ�
	 * ��֮ǰ�ķ����һ���ڶ��Ĺ������п��ܵ��³���һֱ���������Զ������whileѭ����
	 * ��δ�����Ϸ���˵ĵ�һ�δ�������������Ƿ����������Զ������������ݣ�
	 * ��Զ������whileѭ����Ҳ��û��֮��ķ���˷������ݸ��ͻ��ˣ�
	 * �ͻ���Ҳ�Ͳ����ܽ��յ�����˷��ص����ݡ�������������˵ڶ��δ�����ʾ��
	 * �ڿͻ��˷���������Ϻ������������д�������Ǹ��߷���������Ѿ���������ˣ�
	 * ͬ������˷���������Ϻ�Ҳ��һ����Ǹ��߿ͻ��ˡ�
	 * �����ճ�ʹ�õıȽ϶�Ķ������ֿͻ��˷������ݸ�����ˣ�
	 * ����˽������ݺ��ٷ�����Ӧ�Ľ�����ͻ���������ʽ��
	 * ֻ�ǿͻ��˺ͷ����֮�䲻��������һ��һ�Ĺ�ϵ
	 * @param host
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static void startClient(String host, int port)
			throws UnknownHostException, IOException {
		// Ϊ�˼���������е��쳣��ֱ��������
		// String host = "127.0.0.1"; // Ҫ���ӵķ����IP��ַ
		// int port = 8899; // Ҫ���ӵķ���˶�Ӧ�ļ����˿�
		// �����˽�������
		Socket client = new Socket(host, port);
		// �������Ӻ�Ϳ����������д������
		Writer writer = new OutputStreamWriter(client.getOutputStream());
		writer.write("Hello Server.");
		writer.write("eof");  
		writer.flush();
		// д���Ժ���ж�����
		Reader reader = new InputStreamReader(client.getInputStream());
		char chars[] = new char[64];
		int len;
		StringBuffer sb = new StringBuffer();
		String temp;
		int index;  
		while ((len = reader.read(chars)) != -1) {
			temp = new String(chars, 0, len);  
			if ((index = temp.indexOf("eof")) != -1) {  
				sb.append(temp.substring(0, index));  
				break;  
			} 
			sb.append(temp);
		}
		System.out.println("from server: " + sb);
		writer.close();
		reader.close();
		client.close();
	}  

}
