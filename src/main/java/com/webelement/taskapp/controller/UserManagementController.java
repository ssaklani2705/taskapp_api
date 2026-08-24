package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.UserActiveDTO;
import com.webelement.taskapp.dto.UserInfo;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.service.UserManagementService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc"})
public class UserManagementController {
	
	@Autowired
	private UserManagementService userService;

	@GetMapping("/getUserManagementDetails")
	public Map<String, Object> findBasicUserInfo(@RequestParam() int page, @RequestParam() int size,
			@RequestParam() int statusIndex, @RequestParam() String search) {
		
		Page<UserInfo> userPage = userService.findBasicUserInfo(page, size, statusIndex, search);
		Map<String, Object> response = new HashMap<>();
		response.put("data", userPage.getContent());
		response.put("totalElements", userPage.getTotalElements());
		return response;
	}
	
	@GetMapping("/getUserManagementDetails/{userId}")
	public ResponseEntity<?> getUserById(@PathVariable int userId) {
		UserLoginEntity user = userService.getUserById(userId);
		if (user != null) {
			return ResponseEntity.ok(user);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/saveUserDetail")
	public ResponseEntity<?> saveUserDetail(@RequestBody UserLoginEntity userRequest, HttpServletRequest httpRequest)
			throws Exception {
		return userService.saveUserDetail(userRequest, httpRequest);
	}

	@PostMapping("/deleteUser")
	public ResponseEntity<ResponseApi<String>> deleteUser(@RequestParam int userId, @RequestParam int createdBy,
			HttpServletRequest httpRequest) throws Exception {
		return userService.deleteUser(userId, createdBy, httpRequest);
	}

	@GetMapping("/active")
	public ResponseEntity<List<UserActiveDTO>> getActiveUsers() {
		return ResponseEntity.ok(userService.getActiveUsers());
	}

}
