package demo.spring;


public class A {
	private IB b;
	private String name;
	/**
	 * ���췽ʽע�룬�����������ļ��г��֣��������
	 * @param b
	 */
//	public A(IB b,String name){
//		this.b=b;
//		this.name=name;
//	}
	
	public void print(){
		System.out.println("----��ӡ���----");
		b.show();
		System.out.println("���֣�"+name);
	}
	/**
	 * set��ʽע�룬ʹ�õĽ϶�
	 * @param b
	 */
	public void setB(IB b) {
		this.b = b;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
