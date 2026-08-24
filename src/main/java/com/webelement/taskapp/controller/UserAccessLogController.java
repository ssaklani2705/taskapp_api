package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.UserAccessLogDTO;
import com.webelement.taskapp.service.UserAccessLogService;


@RestController
@RequestMapping("/useraccesslog")
@CrossOrigin(origins = { "http://localhost:4200", "https://www.iba.org.in"})
public class UserAccessLogController {

	@Autowired
	private UserAccessLogService accessLogService;

	@GetMapping("/getUserAccessDetails")
	public Map<String, Object> getUserAccessDetails(@RequestParam int page, @RequestParam int size,
			@RequestParam String search) {
		Page<UserAccessLogDTO> userAccessDetailsPage = accessLogService.getUserAccessDetails(page, size, search);
		Map<String, Object> response = new HashMap<>();
		response.put("data", userAccessDetailsPage.getContent());
		response.put("totalElements", userAccessDetailsPage.getTotalElements());
		return response;
	}

}
