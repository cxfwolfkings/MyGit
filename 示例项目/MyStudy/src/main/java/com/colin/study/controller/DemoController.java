package com.colin.study.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DemoController {
	public static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

	@RequestMapping("/index")
	public String index() {
		return "index.html";
	}
}
