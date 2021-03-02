package com.colin.study.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.colin.study.model.User;

@RestController
public class UserController {
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public List<User> query(
			@RequestParam(name = "username", required = false, defaultValue = "colin") String username) {
		System.out.println(username);
		List<User> users = new ArrayList<>();
		users.add(new User());
		users.add(new User());
		users.add(new User());
		return users;
	}
}
