package com.webelement.taskapp.service;
import org.springframework.data.domain.Page;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.PlanDTO;

public interface PlanService {
	ApiResponse<PlanDTO> addOrUpdate(PlanDTO request);
	ApiResponse<PlanDTO> delete(PlanDTO request);
	ApiResponse<PlanDTO> getById(Integer planId);
	Page<PlanDTO> findPlanList(int page, int size, int statusIndex, String search);
}
