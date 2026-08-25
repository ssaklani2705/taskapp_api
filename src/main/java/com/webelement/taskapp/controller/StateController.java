package com.webelement.taskapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.entity.StateEntity;
import com.webelement.taskapp.service.StateService;

@RestController
@RequestMapping("/state")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
public class StateController {

	@Autowired
	private StateService stateService;

	@GetMapping("/getStates")
	public ApiResponse<List<StateEntity>> getStates() {

		List<StateEntity> states = stateService.getStates();

		return new ApiResponse<>(true, "States fetched successfully", states);
	}
}