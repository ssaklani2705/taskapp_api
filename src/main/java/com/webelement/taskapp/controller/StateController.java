package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.Map;

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

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.repo.StateRepo;
import com.webelement.taskapp.service.impl.StateServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/state")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
@RequiredArgsConstructor
public class StateController {

	private final StateServiceImpl stateService;

	@PostMapping("/addOrUpdate")
	public ResponseEntity<ApiResponse<StateDTO>> addOrUpdate(@RequestBody StateDTO dto) {

		ApiResponse<StateDTO> response = stateService.addOrUpdate(dto);

		if (!response.isSuccess()) {
			return ResponseEntity.badRequest().body(response);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/getStateList")
	public ApiResponse<Map<String, Object>> findStateList(@RequestParam int page, @RequestParam int size,
			@RequestParam int statusIndex, @RequestParam(required = false) String search) {

		Page<StateDTO> pageData = stateService.findStateList(page, size, statusIndex, search);

		Map<String, Object> data = new HashMap<>();
		data.put("data", pageData.getContent());
		data.put("totalElements", pageData.getTotalElements());

		return new ApiResponse<>(true, "State list fetched successfully", data);
	}

	@GetMapping("/getById/{stateId}")
	public ApiResponse<StateDTO> getById(@PathVariable Integer stateId) {

		return stateService.getById(stateId);
	}
}
