package com.webelement.taskapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.PlanDTO;
import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.entity.PlanEntity;
import com.webelement.taskapp.entity.StateEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.PlanRepo;
import com.webelement.taskapp.repo.StateRepo;
import com.webelement.taskapp.service.PlanService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

	private final PlanRepo planRepo;
	private final CommonFunction commonFunction;
	private final HttpServletRequest httpRequest;

	@Override
	public ApiResponse<PlanDTO> addOrUpdate(PlanDTO request) {
		LocalDateTime now = LocalDateTime.now();

		String name = request.getName() != null ? request.getName().trim() : "";

		boolean isNew = request.getPlanId() == null || request.getPlanId() == 0;

		if (isNew) {

			if (planRepo.existsByNameIgnoreCaseAndStatusNot(name, (short) 3)) {
				return new ApiResponse<>(false, "A plan with this name already exists", null);
			}

		} else {

			List<PlanEntity> duplicates = planRepo.findByNameIgnoreCase(name);

			boolean exists = duplicates.stream().anyMatch(
					duplicate -> !duplicate.getPlanId().equals(request.getPlanId()) && duplicate.getStatus() != 3);

			if (exists) {
				return new ApiResponse<>(false, "A plan with this name already exists", null);
			}
		}

		// Build entity (new or updated)
		PlanEntity entity;

		if (!isNew) {

			entity = planRepo.findById(request.getPlanId())
					.orElseThrow(() -> new RuntimeException("Plan record not found"));

			entity.setName(name);
			entity.setRate(request.getRate());
			entity.setDescription(request.getDescription());
			entity.setStatus(request.getStatus());
			entity.setModificationDate(now);

		} else {

			entity = new PlanEntity();
			entity.setName(name);
			entity.setRate(request.getRate());
			entity.setDescription(request.getDescription());
			entity.setStatus(request.getStatus() != null ? request.getStatus() : (short) 1);
			entity.setRegistrationDate(now);
		}

		PlanEntity saved = planRepo.save(entity);

		String action = isNew ? "Plan Added" : "Plan Updated";
		commonFunction.createHistoryAccess(request.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, 9, saved.getPlanId(), -1);
		return new ApiResponse<>(true, "Plan saved successfully", null);
	}

	@Override
	public ApiResponse<PlanDTO> delete(PlanDTO request) {
		Optional<PlanEntity> existing = planRepo.findById(request.getPlanId());
		if (!existing.isPresent()) {
			return new ApiResponse<>(false, "Plan not found", null);
		}
		int updatedRows = planRepo.softDelete(request.getPlanId());
		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(request.getUserId(), commonFunction.getLocalIp(),
					commonFunction.getLocalIp(), "Plan Deleted", 9, request.getPlanId(), -1);
			return new ApiResponse<>(true, "Plan deleted successfully", null);
		}
		return new ApiResponse<>(false, "Plan delete failed", null);
	}

	@Override
	public ApiResponse<PlanDTO> getById(Integer planId) {
		PlanEntity entity = planRepo.findById(planId).orElse(null);
		if (entity == null) {
			return new ApiResponse<>(false, "Plan record not found", null);
		}
		PlanDTO dto = PlanDTO.builder().planId(entity.getPlanId()).name(entity.getName()).rate(entity.getRate())
				.description(entity.getDescription()).status(entity.getStatus()).build();
		List<TransactionEntity> history = commonFunction.getTransactionLogs(9, planId);
		if (history != null && !history.isEmpty()) {
			dto.setTransactionHistory(history);
		}
		return new ApiResponse<>(true, "Plan fetched successfully", dto);
	}

	@Override
	public Page<PlanDTO> findPlanList(int page, int size, int statusIndex, String search) {
		return planRepo.findPlanDetails(PageRequest.of(page, size), statusIndex, search);
	}

}
