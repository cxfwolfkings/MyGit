package demo.spring;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	
	private static ThreadLocal<Session> tl = new ThreadLocal<Session>();
	private static Configuration conf;
	private static SessionFactory sessionFactory;
	
	static {
		// conf����ֻҪ����һ�ξ���
		conf = new Configuration();
		// Ĭ�ϼ���src�µ�hibernate.cfg.xml
		conf.configure();
		// ��ȡSession���󹤳�
		sessionFactory = conf.buildSessionFactory();
	}

	/**
	 * �̵߳���
	 * @return
	 */
	public static Session getSession() {
		//sessionFactory.getCurrentSession();
		Session session = tl.get();
		if(session==null){
			//����Session
			session = sessionFactory.openSession();
			tl.set(session);
		}
		return session;
	}
	
	public static void closeSession(){
		Session session = tl.get();
		if(session!=null){
			session.close();
			tl.set(null);
		}
	}

}
