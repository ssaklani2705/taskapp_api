package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
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
import com.webelement.taskapp.dto.PlanDTO;
import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.entity.PlanEntity;
import com.webelement.taskapp.entity.StateEntity;
import com.webelement.taskapp.service.impl.PlanServiceImpl;
import com.webelement.taskapp.service.impl.StateServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/plan")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
@RequiredArgsConstructor
public class PlanController {

	private final PlanServiceImpl planService;
	
	@PostMapping("/add_or_Update")
	public ResponseEntity<ApiResponse<PlanDTO>> addOrUpdate(@RequestBody PlanDTO dto) {

		ApiResponse<PlanDTO> response = planService.addOrUpdate(dto);

		if (!response.isSuccess()) {
			return ResponseEntity.badRequest().body(response);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/plan_list")
	public ApiResponse<Map<String, Object>> findStateList(@RequestParam int page, @RequestParam int size,
			@RequestParam int statusIndex, @RequestParam(required = false) String search) {

		Page<PlanDTO> pageData = planService.findPlanList(page, size, statusIndex, search);

		Map<String, Object> data = new HashMap<>();
		data.put("data", pageData.getContent());
		data.put("totalElements", pageData.getTotalElements());

		return new ApiResponse<>(true, "State list fetched successfully", data);
	}

	@GetMapping("/get_by_Id/{planId}")
	public ApiResponse<PlanDTO> getById(@PathVariable Integer planId) {

		return planService.getById(planId);
	}
	
	@PostMapping("/delete")
	public ApiResponse<PlanDTO> delete(@RequestBody PlanDTO dto) {
	    return planService.delete(dto);
	}
	
	@GetMapping("/getPlan")
    public ApiResponse<List<PlanEntity>> getPlan() {

        List<PlanEntity> plan = planService.getPlan();

        return new ApiResponse<>(true, "Plan fetched successfully", plan);
    }
	
}
