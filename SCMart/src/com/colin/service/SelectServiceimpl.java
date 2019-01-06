package com.colin.service;

import java.util.ArrayList;
import java.util.List;

import com.colin.entity.JdbcEntityContext;
import com.colin.entity.Product;

public class SelectServiceimpl implements SelectService {

	private JdbcEntityContext jdbcEntityContext;

	public void setJdbcEntityContext(JdbcEntityContext jdbcEntityContext) {
		this.jdbcEntityContext = jdbcEntityContext;
	}

	@Override
	public List<Product> select(String name, String type) {
		// TODO Auto-generated method stub
		List<Product> list = new ArrayList<Product>();
		if (name.equals("")) {
			list = JdbcEntityContext.findProductbyType(type);
		}
		if (type.equals("")) {
			list = JdbcEntityContext.findProductbyName(name);
		}
		if (name.length() != 0 && type.length() != 0) {
			list = JdbcEntityContext.findProductbyNameandType(type, name);
		}
		return list;
	}

}
