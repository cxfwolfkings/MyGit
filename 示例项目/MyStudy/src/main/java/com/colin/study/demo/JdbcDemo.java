package demo;

import utiles.ConnectionUtils;

import java.sql.*;
import java.util.Calendar;

/**
 * @author 23907
 */
public class JdbcDemo {
    public static void main(String[] args) {
        textSelect();
    }

    private static void textSelect() {
        // TODO Auto-generated method stub
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String url = "jdbc:oracle:thin:@192.168.10.205:1521:tarena";
            String dbUser = "openlab";
            String dbPwd = "open123";
            try {
                Connection conn = DriverManager.getConnection(url, dbUser,
                        dbPwd);
                String sql = "select * from student_lzh";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);
                    System.out.println(id + " " + name);
                }
                rs.close();
                stmt.close();
                conn.close();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void getDate(String page,int pagesize){

        int intpage = 1;
        try{
            intpage = Integer.parseInt(page);
        }catch(Exception ignored){

        }
        if(intpage < 1){
            intpage = 1;
        }
        int totalpage = getTotalPage(pagesize);
        if(intpage>totalpage){
            intpage = totalpage;
        }
        getDate(intpage,pagesize);
    }

    private static int getTotalPage(int pagesize) {
        // TODO Auto-generated method stub
        int totalpage = 0;
        int totalcount = getTotalCount();
        if(totalcount%pagesize==0){
            totalpage = totalcount/pagesize;
        }else {
            totalpage = totalcount/pagesize+1;
        }
        return totalpage;
    }

    private static int getTotalCount() {
        // TODO Auto-generated method stub
        int totalcount = 0;
        String sql = "select count(*) from my_temp_luzhen";
        Connection conn = ConnectionUtils.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                totalcount = rs.getInt(1);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            ConnectionUtils.close(rs);
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }

        return totalcount;
    }

    private static void getDate(int page, int pagesize) {
        // TODO Auto-generated method stub
        String sql = "select * from my_temp_luzhen";
        int begin = (page - 1)*pagesize+1;
        Connection conn = ConnectionUtils.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(sql);
            rs.absolute(begin);
            for(int i=0;i<pagesize;i++){
                System.out.println(rs.getInt(1));
                if(!rs.next()){
                    break;
                }
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            ConnectionUtils.close(rs);
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
    }

    public static void demo1() {
        Calendar c = Calendar.getInstance();
        c.set(2012, Calendar.JUNE, 2);
        Date d = new Date(c.getTimeInMillis());
        addEmp(1236, "lhhu", "man", d);
    }

    private static void addEmp(int empno, String ename, String job,
                               Date hiredate) {
        // TODO Auto-generated method stub
        Connection conn = ConnectionUtils.getConnection();
        PreparedStatement stmt = null;
        try {
            String sql = "insert into emp_xie(empno,ename,job,hiredate) values(?,?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empno);
            stmt.setString(2, ename);
            stmt.setString(3, job);
            stmt.setDate(4, hiredate);
            stmt.executeUpdate();
            System.out.println("nnn");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
    }

    private static boolean changePwd(int id, String pwd) {
        // TODO Auto-generated method stub
        String sql = "update luzz" + " set pwd = " + pwd + " where id = " + id;
        // System.out.println(sql);
        boolean flag = false;
        Connection conn = ConnectionUtils.getConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            int n = stmt.executeUpdate(sql);// ******
            if (n == 1) {// ?
                flag = true;
            }
        } catch (SQLException e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
        return flag;
    }

    public static boolean deleteUser(int id) {
        String sql = "delete from luzz" + " where id = " + id;
        boolean flag = false;
        Connection conn = ConnectionUtils.getConnection();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            int n = stmt.executeUpdate(sql);
            if (n == 1) {
                flag = true;
            }
        } catch (SQLException e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
        return flag;

    }

    public static boolean createUser(int id, String name, String pwd,
                                     String phone, String email) {
        // String sql = "insert into user_lz values(" + id + ",'"+name+"','"+
        // pwd +"','"+phone+"','"+email+"')";
        String sql = "insert into luzz values(?,?,?,?,?)";
        boolean flag = false;
        Connection conn = ConnectionUtils.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setString(3, pwd);
            stmt.setString(4, phone);
            stmt.setString(5, email);
            int n = stmt.executeUpdate();
            if (n == 1) {
                flag = true;
            }
        } catch (SQLException e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
        return flag;
    }

    public static void batch(int i,String name){
        String sql = "insert into my_temp_luzhen values(?,?)";
        Connection conn = ConnectionUtils.getConnection();
        PreparedStatement stmt = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            for(i=200;i<=305;i++){
                stmt.setInt(1, i);
                stmt.setString(2, name);
                stmt.addBatch();
                if(i%10==0){
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
            }
            System.out.print(i+"\t");
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
    }

    public static boolean login(int sid,int cid) {
        boolean flag = false;
        Connection conn = ConnectionUtils.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "select * from stu_cour_lzh "+"where sid = "+sid+"and cid = "+cid;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                flag = true;
                System.out.println(rs.getInt("cid"));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            ConnectionUtils.close(rs);
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
        return flag;
    }

    public static void listAll(){
        Connection conn = ConnectionUtils.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "select * from stu_cour_lzh";
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                int sid = rs.getInt(1);
                int cid = rs.getInt(2);
                int score = rs.getInt("score");
                System.out.println(sid+","+cid+","+score);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            ConnectionUtils.close(rs);
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
    }

    public static void addUser(int sid,int cid,int score){
        Connection conn = ConnectionUtils.getConnection();
        PreparedStatement stmt = null;
        String sql = "insert into stu_cour_lzh values(?,?,?)";
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1,sid);
            stmt.setInt(2,cid);
            stmt.setInt(3,score);
            int n = stmt.executeUpdate();
            if(n==1){
                System.out.println("ok");
            }else{
                System.out.println("no");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            ConnectionUtils.close(stmt);
            ConnectionUtils.close(conn);
        }
    }
}
