package com.webelement.taskapp.service;

import java.util.List;

import org.springframework.data.domain.Page;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.PlanDTO;
import com.webelement.taskapp.entity.PlanEntity;

public interface PlanService {
	public ApiResponse<PlanDTO> addOrUpdate(PlanDTO request);

	public ApiResponse<PlanDTO> delete(PlanDTO request);

	public ApiResponse<PlanDTO> getById(Integer planId);

	public Page<PlanDTO> findPlanList(int page, int size, int statusIndex, String search);
	
	public List<PlanEntity> getPlan();
}
