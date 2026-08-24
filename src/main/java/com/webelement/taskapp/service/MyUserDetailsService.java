package com.webelement.taskapp.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.LoginRequest;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.UserLoginRepository;


@Service
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	private UserLoginRepository loginRepository;

	LoginRequest loginReq = new LoginRequest();

	HashMap<String, String> users = new HashMap<String, String>();

	public void setUser(LoginRequest req) {
		this.loginReq = req;

	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserLoginEntity user = loginRepository.findByEmailAndStatus(username,1)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
				new java.util.ArrayList<>());
	}

}