package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.GenericRecurringDTO;
import com.webelement.taskapp.entity.GenericRecurringEntity;
import com.webelement.taskapp.service.GenericRecurringService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class GenericRecurringController {

	private final GenericRecurringService genericRecurringService;

	// For AddOrUpdate
	@PostMapping("/addOrUpdateGenericRecurring")
	public ResponseEntity<ApiResponse<GenericRecurringEntity>> addOrUpdateGenericRecurring(
			@RequestBody GenericRecurringDTO dto, @RequestParam("userId") Integer userId,
			HttpServletRequest httpRequest) {

		ApiResponse<GenericRecurringEntity> response = genericRecurringService.addOrUpdateGenericRecurring(dto, userId,
				httpRequest);

		return ResponseEntity.ok(response);

	}

	// For edit
	@GetMapping("/getGenericRecurringById/{genericRecurringId}")
	public ResponseEntity<ApiResponse<GenericRecurringDTO>> getRecurringById(@PathVariable Integer genericRecurringId,
			@RequestParam("userId") Integer userId) {

		GenericRecurringDTO recurring = genericRecurringService.getGenericRecurringById(genericRecurringId, userId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Recurring details fetched successfully", recurring));
	}

	private static final Map<String, String> RECURRING_SORT_MAP = Map.of("title", "r.title", "for", "r.forValue",
			"description", "r.description", "type", "r.type", "taskCategory", "tc.name", "status", "r.status",
			"priority", "r.priority", "regDate", "r.regDate");

	// For Index
	@GetMapping("/getGenericRecurringDetails")
	public Map<String, Object> getGenericRecurringDetails(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "0") Short status,
			@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") Short type,
			@RequestParam(defaultValue = "0") Integer taskCatId, @RequestParam(defaultValue = "0") Integer forValue,
			@RequestParam(defaultValue = "title") String sortColumn,
			@RequestParam(defaultValue = "asc") String sortDirection, @RequestParam Integer userId,
			@RequestParam("isAdmin") String isAdmin, @RequestParam("loginType") String loginType,
			@RequestParam(defaultValue = "0") Integer priority) {

		String sortBy = RECURRING_SORT_MAP.getOrDefault(sortColumn, "title");

		Sort.Direction direction = sortDirection.trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC
				: Sort.Direction.ASC;

		sortBy = sortBy.replace("r.", "").replace("c.", "").replace("tc.", "");

		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

		Page<GenericRecurringDTO> recurringPage = genericRecurringService.findGenericRecurringDetails(pageable, status,
				search, type, taskCatId, forValue, userId, isAdmin, loginType, priority);

		Map<String, Object> response = new HashMap<>();

		response.put("data", recurringPage.getContent());
		response.put("totalElements", recurringPage.getTotalElements());
		response.put("totalPages", recurringPage.getTotalPages());
		response.put("currentPage", recurringPage.getNumber());
		response.put("pageSize", recurringPage.getSize());

		return response;
	}

	// For View
	@GetMapping("/genricRecurringDetails/{genericRecurringId}")
	public ResponseEntity<?> getRecurringDetailsById(@PathVariable Integer genericRecurringId,
			@RequestParam("userId") Integer userId) {

		GenericRecurringDTO recurring = genericRecurringService.getViewGenericRecurringById(genericRecurringId, userId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Generic Recurring details fetched successfully", recurring));
	}

	// For Delete
	@PostMapping("/deleteGenericRecurring")
	public ResponseEntity<ResponseApi<String>> deleteGenericRecurring(@RequestParam int genericRecurringId,
			@RequestParam int userId, HttpServletRequest httpRequest) throws Exception {

		return genericRecurringService.deleteGenericRecurring(genericRecurringId, userId, httpRequest);
	}
}
