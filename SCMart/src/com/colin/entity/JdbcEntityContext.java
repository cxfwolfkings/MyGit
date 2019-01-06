package com.colin.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.colin.common.ConnectionManager;

public class JdbcEntityContext {

	public static List<Product> findProductbyType(String type) {
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from product where type=?";
		List<Product> list = new ArrayList<Product>();
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, type);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Product pro = new Product();
				pro.setId(rs.getInt("id"));
				pro.setName(rs.getString("name"));
				pro.setType(rs.getString("type"));
				pro.setCost(rs.getDouble("cost"));
				list.add(pro);
			}
			ConnectionManager.close(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	public static List<Product> findProductbyName(String name) {
		List<Product> list = new ArrayList<Product>();
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select *from product where name=?";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Product pro = new Product();
				pro.setId(rs.getInt("id"));
				pro.setName(rs.getString("name"));
				pro.setType(rs.getString("type"));
				pro.setCost(rs.getDouble("cost"));
				list.add(pro);
			}
		} catch (Exception e) {
		}
		return list;

	}

	public static List<Product> findProductbyNameandType(String type,
			String name) {
		List<Product> list = new ArrayList<Product>();
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select *from product where type=? and name=?";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, type);
			stmt.setString(2, name);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Product pro = new Product();
				pro.setId(rs.getInt("id"));
				pro.setName(rs.getString("name"));
				pro.setType(rs.getString("type"));
				pro.setCost(rs.getDouble("cost"));
				list.add(pro);
			}
		} catch (Exception e) {
		}
		return list;

	}

}
