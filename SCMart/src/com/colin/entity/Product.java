package com.colin.entity;

/**
 * 
 * @author  Colin Chen
 * @create  2018年11月30日 下午9:58:08
 * @modify  2018年11月30日 下午9:58:08
 * @version A.1
 */
public class Product {
	private int id;
	private String name;
	private String type;
	private double cost;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + ", type=" + type
				+ ", cost=" + cost + "]";
	}

}
