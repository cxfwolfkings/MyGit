package demo.spring;


import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CGLIBProxyFactory implements MethodInterceptor{
	private Object target;
	private CGLIBProxyFactory(){}
	public static CGLIBProxyFactory getInstance(){
		return new CGLIBProxyFactory();
	}
	public Object getProxy(Class clazz) throws Exception{
		target=clazz.newInstance();
		Enhancer enhancer=new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(this);
		return enhancer.create();
	}
	public Object intercept(Object proxy, Method method, Object[] params,MethodProxy methodProxy) throws Throwable {
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
