package com.colin.view;

import com.colin.entity.JdbcEntityContext;
import com.colin.service.SelectServiceimpl;

public class Main {
	
	public static void main(String[] args) {
		SelectFrame selectFrame = new SelectFrame();
		ClientContext clientContext = new ClientContext();
		SelectServiceimpl selectService = new SelectServiceimpl();
		selectFrame.setClientContext(clientContext);
		clientContext.setSelectFrame(selectFrame);
		clientContext.setSelectService(selectService);
		JdbcEntityContext jdbcEntityContext = new JdbcEntityContext();
		selectService.setJdbcEntityContext(jdbcEntityContext);
		selectFrame.setVisible(true);
	}

}
