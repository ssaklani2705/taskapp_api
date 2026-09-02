package com.webelement.taskapp.controller;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.LoginRequest;
import com.webelement.taskapp.dto.LoginResponse;
import com.webelement.taskapp.service.AuthService;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc" }, allowCredentials = "true")
public class AuthController {
	
	
	@Autowired
	private AuthService authService;
	
	private Boolean isManager = false;

	@GetMapping("/test")
	public String test() {
		return "20-08-2026 10:53";
	}

	@GetMapping("/captcha")
	public ResponseEntity<ResponseApi<String>> generateCaptcha(HttpSession session, HttpServletRequest httpRequest) {
		int captcha = 100000 + new Random().nextInt(900000);
		session.setAttribute("captcha", String.valueOf(captcha));
		ResponseApi<String> response = new ResponseApi<>(true, "CAPTCHA Generated", String.valueOf(captcha));
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseApi<LoginResponse>> checkLogin(@RequestBody LoginRequest request, HttpSession session,
			HttpServletRequest httpRequest, HttpServletResponse response) throws Exception {
		ResponseEntity<ResponseApi<LoginResponse>> loginResponse = authService.login(request, session, httpRequest);
		return loginResponse;
	}

	@PostMapping("/forgotpasswordMail")
	public ResponseEntity<ResponseApi<String>> forgotpasswordMail(@RequestParam String emailId,
			HttpServletRequest httpRequest) throws Exception {
		 isManager = false;
		return authService.forgotpasswordMail(emailId, httpRequest,isManager);
	}

	@PostMapping("/forgot_password_manager_login")
	public ResponseEntity<ResponseApi<String>> forgotpasswordManagerLoginMail(@RequestParam String emailId,HttpServletRequest httpRequest) throws Exception {
		 isManager = true;
		return authService.forgotpasswordMail(emailId, httpRequest,isManager);
	}

	@PostMapping("/forgotpassword")
	public ResponseEntity<ResponseApi<String>> forgotpassword(@RequestParam String emailId, @RequestParam String userId,
			@RequestParam String password,@RequestParam String isManager) throws Exception {
		return authService.forgotpassword(emailId, userId, password,isManager);
	}

}
