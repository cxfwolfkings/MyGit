package com.colin.service;

import java.util.List;

import com.colin.entity.Product;

public interface SelectService {

	List<Product> select(String name, String type);

}
