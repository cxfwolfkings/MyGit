package demo.spring;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JDKProxyFactory implements InvocationHandler{
	
	private Object target;
	//����ģʽ
	private JDKProxyFactory(){}
	public static JDKProxyFactory getInstance(){
		return new JDKProxyFactory();
	}
	/**
	 * @param clazz Ŀ���������
	 * @return      �������
	 */
	public Object getProxy(Class clazz) throws Exception{
		//��ȡĿ�����͵�ʵ������
		target=clazz.newInstance();
		//����Ŀ�����Ͷ��󴴽��������
		Object proxy=Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),this);
		return proxy;
	}
	/**
	 * ÿ��ͨ������������Ŀ�귽��ʱ��ִ�и÷���
	 */
	public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
		Object retVal=null;
		try {
			System.out.println("����ǰ��֪ͨ");
			//����Ŀ�������
			retVal=method.invoke(target,params);
			System.out.println("���ú���֪ͨ");
		} catch (Exception e) {
			System.out.println("�����쳣֪ͨ");
		}finally{
			System.out.println("��������֪ͨ");
		}
		return retVal;
	}
	
}
