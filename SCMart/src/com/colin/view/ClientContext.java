package com.colin.view;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.colin.entity.Product;
import com.colin.service.SelectService;

public class ClientContext {
	private SelectFrame selectFrame;
	private SelectService selectService;

	public void setSelectFrame(SelectFrame selectFrame) {
		this.selectFrame = selectFrame;
	}

	public SelectService getSelectService() {
		return selectService;
	}

	public void setSelectService(SelectService selectService) {
		this.selectService = selectService;
	}

	public void select() {
		// TODO Auto-generated method stub
		// selectFrame.updaterArea();
		String name = selectFrame.getProductname();
		System.out.println(name);
		String type = selectFrame.getProducttype();
		// System.out.println(type);
		List<Product> list = selectService.select(name, type);
		// selectFrame.setVisible(false);
		selectFrame.setRsarea(list);
		// System.out.println(list);
	}

	public void exit(JFrame source) {
		// TODO Auto-generated method stub
		int val = JOptionPane.showConfirmDialog(source, "Really?");
		if (val == JOptionPane.YES_OPTION) {
			source.setVisible(false);
			System.exit(0);
		}

	}

}
