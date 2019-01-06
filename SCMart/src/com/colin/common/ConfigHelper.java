package com.colin.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 配置文件帮助类
 * @author  Colin Chen
 * @create  2018年12月1日 上午5:17:09
 * @modify  2018年12月1日 上午5:17:09
 * @version A.1
 */
public class ConfigHelper {
	private Properties table = new Properties();

	public ConfigHelper(String file) {
		try {
			table.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public int getInt(String key) {
		return Integer.parseInt(table.getProperty(key));
	}

	public String getString(String key) {
		return table.getProperty(key);
	}

	public double getDouble(String key) {
		return Double.parseDouble(table.getProperty(key));
	}
}
