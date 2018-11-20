package com.colin.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 常用接口：
 * java.sql.Connection: 连接，会话
 * java.sql.Statement: 语句
 * java.sql.ResultSet: 结果集，查询语句(Select)的返回结果
 * 
 * 常用类：
 * java.sql.DriverManager: 驱动管理器，通过参数：数据库的位置/用户名/密码/驱动类，获得连接(Connection)
 * 前提：Oracle的驱动包(ojdbc14_11g.jar)加入到项目的build path（构建路径）中
 * 
 * 连接数据库
 * 1. 注册驱动  
 * 2. DriverManager通过参数获得连接(Connection)
 *    url: 连接字符串  jdbc:oracle:thin:@ip:1521:sid
 *    oracle格式: jdbc:oracle:thin:@ip:port:sid
 *    url = "jdbc:oracle:thin:@192.168.10.205:1521:tarena";
 * 3. Statement传送sql语句给数据库，并接收结果。
 *    if：Select语句，结果是ResultSet 
 *    if：DML语句(insert/update/delete)，结果是int数据
 * 执行增删改查(CRUD)
 * C:create,新增;
 * R:Retrive:取出，select
 * U:Update:更新
 * D:Delete:删除
 * 4.工具类:关闭资源
 * 
 * 预编译PreparedStatement
 * 1.sql语句的写法
 *   String sql = "insert into t_users values(?,?,?,?,?)";//必须传递进所有列
 *   String sql = "insert into t_users(id,name) values(?,?)"; // 按照自定义的列名传递数据   
 *   String sql = "insert into t_users(id,name) values(sql_users.nextval,?)";// 按照序列之外有多少列，传入多少列的数据
 * 2.注意传递sql语句的时机
 *   PreparedStatement stmt = conn.prepareStatement(sql);
 * 3.sql语句中的问号赋值
 *   第一个参数是问号的位置，不是列的位置
 *   stmt.setInt(1, 1001);//要求id是int类型
 *   stmt.setString(2, name);//name是String类型
 *   stmt.setDouble(3, 10000.5);
 *   stmt.setDate(4, new Date());
 *   新增日期类型的数据:
 *   Connection conn = ConnectionUtils.getConnection();
 *   PreparedStatement stmt = null;
 *   String sql = "insert into emp (id,name,salary,hiredate) values(?,?,?,?)";
 *   stmt = conn.prepareStatement(sql);
 *   stmt.setInt(1, 1001);
 *   stmt.setString(2, "zhangsan");
 *   stmt.setDouble(3, 10000.8);
 *   java.sql.Date d = new Date(System.currentTimeMillis());
 *   stmt.setDate(4, d);
 *   close();
 *   
 * 事务处理
 * 事务要遵循的ISO/IEC所制定的ACID原则：
 * ACID是原子性(atomicity)、一致性(consistency)、隔离性(isolation)和持久性(durability)的缩写。
 * 1.事务的原子性表示事务执行过程中的任何失败都将导致事务所做的任何修改失效。
 * 2.一致性表示当事务执行失败时，所有被该事务影响的数据都应该恢复到事务执行前的状态。
 * 3.隔离性表示在事务执行过程中对数据的修改，在事务提交之前对其他事务不可见。
 * 4.持久性表示已提交的数据在事务执行失败时，数据的状态都应该正确。
 * 怎样在编程中实现对事务的管理呢？
 * 首先我们没有必要显示地启动一个事务，可以通过调用Connection.setAutoCommit(false)隐式地实现。
 * 然后调用Connection.commit()方法来提交一个事务，而且如果考虑到代码的安全性，还要包括一个回滚Connection.rollback()。
 * 实现事务
 * 默认自动提交，JDBC中修改为手动提交：conn.setAutoCommit(false) 
 * SQLPlus：set autocommit on|off
 * 转账: 甲转账500元到已账户
 * create table account_ning(
 *   id char(4) primary key,
 *   money number(7,2)
 * );
 * insert into account_ning values('jia',1000);
 * insert into account_ning values('yi', 300);
 * commit;
 * update account set money = money - 500 where id = 'jia';
 * update account set money = money + 500 where id = 'yi';
 * 
 * 批处理
 * 简单示例：
 * conn.setAutoCommit(false);
 * for(i = 0; i < 1000005; i++){
 *   sql = "insert into temp values(i)";
 *   stmt.addBatch(sql);
 *   if (i % 1000 == 0){
 *     stmt.executeBatch();
 *     stmt.clearBatch();
 *   }
 * }
 * stmt.executeBatch();
 * conn.commit();
 * 注意：JDBC的批处理不能加入select语句，否则会抛异常：
 * java.sql.BatchUpdateException: Can not issue SELECT via executeUpdate(). 
 * at com.mysql.jdbc.StatementImpl.executeBatch(StatementImpl.java:1007)
 * 
 * 分页策略
 * 1000条符合条件的记录，每页20条(pageSize)，50页
 * select * from product;
 * 第一页:1-20
 * 第二页:21-40
 * ....
 * 第n页: (n - 1) * paseSize + 1 至 n * pageSize
 * 1.基于缓存的分页策略
 * 把记录全部取出，放在缓存中。第一次比较慢，以后每页都很快。缺点：内存压力；优点：不用频繁访问数据库，只访问一次
 * 要求结果集有指针移动（向前和向后）的能力
 * stmt = conn.createStatement(arg1, arg2);
 * rs = stmt.executeQuery(sql);
 * 可滚动的结果集的常用方法:
 * rs.next();
 * rs.previous();
 * rs.absolute(20);
 * rs.relative(-3);
 * rs.first()
 * rs.last()
 * 如何定位到指定页？
 * 第n页: begin = (n - 1) * pageSize + 1
 * rs.absolute(begin);
 * 如何判断超过最大页数？  200, 10 
 * int totalCount = 205;  //表的记录总数,205
 * int pageSize = 10;
 * int totalPage; //总页数
 * if (totalCount % pageSize == 0) {
 *   totalPage = totalCount / pageSize;
 * } else {
 *   totalPage = totalCount / pageSize + 1;
 * }
 * 2.基于查询的分页策略
 * 只取一页的数据，20条；取所有页的时间都差不多；换页需要频繁访问数据库；内存无压力
 * rownum只能从1开始计数
 * -- 用行内视图解决rownum只能从1开始的问题
 * select * from (select rownum rn, id from emp) where rn between 4 and 6;
 * create view v_emp
 * as
 * select rownum rn, id, name, salary from emp；
 * -- oracle中的实现方式
 * select * from (select rownum rn, id from temp_ning) 
 * where rn between 21 and 30;
 * -- mysql
 * select * from temp_ning limit 21, 10;
 * -- 把薪水排序后再取4-6条.
 * -- 取到4-6条后排序，失败
 * select * from (
 *   select rownum rn, id, name, salary from emp order by salary desc)
 *   where rn between 4 and 6;
 * -- 排序后再分页的实现方式(oracle):
 * select * from (
 *   select rownum rn, id, name,salary from (select * from emp order by salary desc))
 *   where rn between 4 and 6;
 * -- 排序后再分页的实现方式(mysql)
 * select * from emp order by salary desc limit 4, 3;
 * 
 * 存储过程
 * JDBC允许用户在应用程序中调用存储过程。首先需要创建一个CallableStatement对象。CallableStatement对象包含一个存储过程调用，而不是包含存储过程本身，存储过程调用要写在{}中，并用""引起来。
 * 
 * @author  Colin Chen
 * @create  2018年11月18日 下午8:03:15
 * @modify  2018年11月18日 下午8:03:15
 * @version A.1
 */
public class JdbcHelper {
	public static void main(String[] args) { 
        exeBatchStaticSQL(); 
	} 

	/** 
	 * 批量执行预定义模式的SQL 
	 */ 
	public static void exeBatchParparedSQL() { 
	    Connection conn = null; 
	    try { 
	        conn = ConnectionManager.getConnection(); 
	        String sql = "insert into testdb.book (kind, name) values (?,?)"; 
	        PreparedStatement pstmt = conn.prepareStatement(sql); 
	        pstmt.setString(1, "java"); 
	        pstmt.setString(2, "jjjj"); 
	        pstmt.addBatch();//添加一次预定义参数 
	        pstmt.setString(1, "ccc"); 
	        pstmt.setString(2, "dddd"); 
	        pstmt.addBatch();//再添加一次预定义参数 
	        //批量执行预定义SQL 
	        pstmt.executeBatch(); 
	    } catch (SQLException e) { 
	        e.printStackTrace(); 
	    } finally { 
	    	ConnectionManager.closeConnection(conn);
	    } 
	} 

	/** 
	 * 批量执行混合模式的SQL、有预定义的，还有静态的 
	 */ 
	public static void exeBatchMixedSQL() { 
	    Connection conn = null; 
	    try { 
	        conn = ConnectionManager.getConnection(); 
	        String sql = "insert into testdb.book (kind, name) values (?,?)"; 
	        PreparedStatement pstmt = conn.prepareStatement(sql); 
	        pstmt.setString(1, "java"); 
	        pstmt.setString(2, "jjjj"); 
	        pstmt.addBatch();    //添加一次预定义参数 
	        pstmt.setString(1, "ccc"); 
	        pstmt.setString(2, "dddd"); 
	        pstmt.addBatch();    //再添加一次预定义参数 
	        //添加一次静态SQL 
	        pstmt.addBatch("update testdb.book set kind = 'JAVA' where kind='java'"); 
	        //批量执行预定义SQL 
	        pstmt.executeBatch(); 
	    } catch (SQLException e) { 
	        e.printStackTrace(); 
	    } finally { 
	    	ConnectionManager.closeConnection(conn);
	    } 
	} 

	/** 
	 * 执行批量静态的SQL 
	 */ 
	public static void exeBatchStaticSQL() { 
	    Connection conn = null; 
	    try { 
	        conn = ConnectionManager.getConnection();
	        Statement stmt = conn.createStatement(); 
	        //连续添加多条静态SQL 
	        stmt.addBatch("insert into testdb.book (kind, name) values ('java', 'java in aciton')"); 
	        stmt.addBatch("insert into testdb.book (kind, name) values ('c', 'c in aciton')"); 
	        stmt.addBatch("delete from testdb.book where kind ='C#'"); 
	        stmt.addBatch("update testdb.book set kind = 'JAVA' where kind='java'"); 
	        //stmt.addBatch("select count(*) from testdb.book");                //批量执行不支持Select语句 
	        //执行批量执行 
	        stmt.executeBatch(); 
	    } catch (SQLException e) { 
	        e.printStackTrace(); 
	    } finally { 
	        ConnectionManager.closeConnection(conn);
	    } 
	} 

}
