package com.webelement.taskapp.controller;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.CaptchaDto;
import com.webelement.taskapp.dto.ChangePassWordDTO;
import com.webelement.taskapp.dto.LoginRequest;
import com.webelement.taskapp.dto.LoginResponse;

import com.webelement.taskapp.service.AuthService;
import com.webelement.taskapp.service.CaptchaService;
import com.webelement.taskapp.service.impl.ChangePasswordImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final CaptchaService captchaService;
	private final ChangePasswordImpl changePasswordService;
	
	private Boolean isManager = false;


	@GetMapping("/captcha")
	public ResponseEntity<ResponseApi<Map<String, String>>> generateCaptcha() throws IOException {
	    return ResponseEntity.ok(new ResponseApi<>(true, "CAPTCHA Generated", captchaService.create()));

	}

	@PostMapping("/login")
	public ResponseEntity<ResponseApi<LoginResponse>> checkLogin(@RequestBody LoginRequest request,
			HttpServletRequest httpRequest, HttpServletResponse response) throws Exception {
		ResponseEntity<ResponseApi<LoginResponse>> loginResponse = authService.login(request, httpRequest);
		return loginResponse;
	}

	@PostMapping("/forgotpasswordMail")
	public ResponseEntity<ResponseApi<String>> forgotpasswordMail(@RequestParam String emailId,
			@RequestParam String loginType, HttpServletRequest httpRequest) throws Exception {
		isManager = false;
		return authService.forgotpasswordMail(emailId, httpRequest, isManager, loginType);
	}

	@PostMapping("/forgotpassword")
	public ResponseEntity<ResponseApi<String>> forgotpassword(@RequestParam String emailId, @RequestParam String userId,
			@RequestParam String password, @RequestParam(required = false) String isManagerLogin) throws Exception {
		return authService.forgotpassword(emailId, userId, password, isManagerLogin);
	}

	@PostMapping("/change_password")
	public ResponseEntity<ResponseApi<String>> changePassword(@RequestBody ChangePassWordDTO request) {

		String message = changePasswordService.changePassword(request);
		return ResponseEntity.ok(new ResponseApi<String>(true, message, null));
	}
}
