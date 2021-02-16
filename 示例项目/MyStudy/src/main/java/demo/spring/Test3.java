package demo.spring;


import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test3 {
	private static final String[] CONFIGS=new String[]{"tarena/demo3/applicationContext.xml"};
	public static void main(String[] args) {
		//Ĭ������� MyBean�� Spring��������ʱ�ͻᴴ��
		//ApplicationContext ac=new ClassPathXmlApplicationContext(CONFIGS);
		//�ӳٴ���ʱ���ڵ���ʱ����
		//MyBean bean1=(MyBean)ac.getBean("mybean");
		//MyBean bean2=(MyBean)ac.getBean("mybean");
		//Spring����Ĭ����ͨ������ģʽ����Bean�����,���̲߳�������ʱ���з���
		//System.out.println(bean1==bean2);//true
		AbstractApplicationContext ac=new ClassPathXmlApplicationContext(CONFIGS);
		MyBean bean=(MyBean)ac.getBean("mybean");//�����������ó�ʼ������
		ac.close();//Spring�����رպ����ٶ���,ֻ�Ե���ģʽ��Ч
	}
}
