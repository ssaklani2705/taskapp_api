package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.DepartmentDTO;
import com.webelement.taskapp.service.DepartmentService;


@RestController
@RequestMapping("/admin/department")
@CrossOrigin(origins = {
		"http://localhost:4500",
		"https://app.webelement.cc",
		"https://13.202.30.190"
})
public class DepartmentController {

	@Autowired
	private DepartmentService departmentService;

	// =========================================================
	// PAGINATION + SEARCH
	// =========================================================

	@GetMapping("/getDepartmentDetails")
	public Map<String, Object> findDepartmentDetails(
			@RequestParam int page,
			@RequestParam int size,
			@RequestParam int statusIndex,
			@RequestParam(required = false) String search) {

		Page<DepartmentDTO> departmentPage =
				departmentService.findDepartmentDetails(
						page,
						size,
						statusIndex,
						search);

		Map<String, Object> response =
				new HashMap<>();

		response.put(
				"data",
				departmentPage.getContent());

		response.put(
				"totalElements",
				departmentPage.getTotalElements());

		return response;
	}

	// =========================================================
	// ADD / UPDATE
	// =========================================================

	@PostMapping("/saveDepartment")
	public ResponseEntity<ApiResponse<DepartmentDTO>> saveDepartment(
			@RequestBody DepartmentDTO dto,
			HttpServletRequest httpRequest) {

		try {

			ApiResponse<DepartmentDTO> response =
					departmentService.addOrUpdate(
							dto,
							httpRequest);

			return ResponseEntity.ok(
					response);

		} catch (Exception e) {

			return ResponseEntity.ok(
					new ApiResponse<>(
							false,
							e.getMessage(),
							null));
		}
	}

	// =========================================================
	// GET BY ID
	// =========================================================

	@GetMapping("/{departmentId}")
	public ApiResponse<DepartmentDTO> getDepartmentById(
			@PathVariable Integer departmentId) {

		try {

			return departmentService
					.getById(
							departmentId);

		} catch (Exception e) {

			return new ApiResponse<>(
					false,
					e.getMessage(),
					null);
		}
	}

	// =========================================================
	// SOFT DELETE
	// =========================================================

	@PostMapping("/deleteDepartment")
	public ResponseEntity<ApiResponse<String>> deleteDepartment(
			@RequestBody DepartmentDTO dto,
			HttpServletRequest httpRequest) {

		return departmentService.deleteDepartment(
				dto.getDepartmentId(),
				dto.getUserId(),
				httpRequest);
	}

	// =========================================================
	// ACTIVE DEPARTMENTS
	// =========================================================

	@GetMapping("/active")
	public List<DepartmentDTO> getActiveDepartments() {

		return departmentService
				.getActiveDepartments();
	}
}

