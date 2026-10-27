package com.webelement.taskapp.service.impl;

import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.CompanyDTO;
import com.webelement.taskapp.entity.CompanyEntity;
import com.webelement.taskapp.entity.PlanEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.CompanyRepo;
import com.webelement.taskapp.service.CompanyService;

import lombok.RequiredArgsConstructor;
import com.webelement.taskapp.Exceptions.ApiException;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

	private final CompanyRepo companyRepo;
	private final CommonFunction commonFunction;
	private HttpServletRequest httpRequest;

	@Override
	@Transactional
	public ApiResponse<CompanyDTO> addOrUpdate(CompanyDTO request) {
		LocalDateTime now = LocalDateTime.now();
		String name = request.getCompanyName() != null ? request.getCompanyName().trim() : "";
		boolean isNew = request.getCompanyId() == null || request.getCompanyId() == 0;

		if (isNew) {
			if (companyRepo.existsByCompanyNameIgnoreCaseAndStatusNot(name, (short) 3)) {
				throw ApiException.badRequest("A company with this name already exists");
			}
		} else {
			List<CompanyEntity> duplicates = companyRepo.findByCompanyNameIgnoreCase(name);
			boolean exists = duplicates.stream()
					.anyMatch(duplicate -> !duplicate.getCompanyId().equals(request.getCompanyId())
							&& duplicate.getStatus() != 3);
			if (exists) {
				throw ApiException.badRequest("A company with this name already exists");
			}
		}

		CompanyEntity entity;
		if (!isNew) {
			entity = companyRepo.findById(request.getCompanyId())
					.orElseThrow(() -> ApiException.badRequest("Company record not found"));
			entity.setModificationDate(now);
			entity.setStatus(request.getStatus());
		} else {
			entity = new CompanyEntity();
			entity.setRegistrationDate(now);
			entity.setStatus(request.getStatus() != null ? request.getStatus() : (short) 1);
		}

		entity.setCompanyName(name);
		entity.setCompanySiteOffice(request.getCompanySiteOffice());
		entity.setAddressLine1(request.getAddressLine1());
		entity.setAddressLine2(request.getAddressLine2());
		entity.setCity(request.getCity());
		entity.setPincode(request.getPincode());
		entity.setLocation(request.getLocation());
		entity.setStateId(request.getStateId());
		entity.setGstNo(request.getGstNo());
		entity.setPan(request.getPan());

		CompanyEntity saved = companyRepo.save(entity);

		String action = isNew ? "Company Added" : "Company Updated";
		commonFunction.createHistoryAccess(request.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, 16, saved.getCompanyId(), -1);

		return new ApiResponse<>(true, "Company saved successfully", null);
	}

	@Override
	@Transactional
	public ApiResponse<CompanyDTO> delete(CompanyDTO request) {
		Integer companyId = request.getCompanyId();

		Integer companyExistsInClient = companyRepo.existsByCompanyId(companyId);
		if (companyExistsInClient != null && companyExistsInClient > 0) {
			throw ApiException.badRequest("Company is assigned to one or more clients and cannot be deleted");
		}

		Optional<CompanyEntity> existing = companyRepo.findById(companyId);
		if (!existing.isPresent()) {
			throw ApiException.badRequest("Company not found");
		}

		int updatedRows = companyRepo.softDelete(companyId);
		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(request.getUserId(), commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Company Deleted", 16, companyId, -1);
			return new ApiResponse<>(true, "Company deleted successfully", null);
		}
		throw ApiException.badRequest("Company delete failed");
	}

	@Override
	public ApiResponse<CompanyDTO> getById(Integer companyId) {
		Optional<CompanyEntity> optional = companyRepo.findById(companyId);

		if (!optional.isPresent() || Short.valueOf((short) 3).equals(optional.get().getStatus())) {
			return new ApiResponse<>(false, "Company not found", null);
		}

		CompanyEntity company = optional.get();

		CompanyDTO dto = toDto(company);

		// Fetch transaction history
		List<TransactionEntity> history = commonFunction.getTransactionLogs(16, companyId);

		if (history != null && !history.isEmpty()) {
			dto.setTransactionHistory(history);
		}

		return new ApiResponse<>(true, "Company found", dto);

	}

	@Override
	public Page<CompanyDTO> findCompanyList(int page, int size, int statusIndex, String search,Integer stateId) {
		return companyRepo.findCompanyDetails(PageRequest.of(page, size), statusIndex, search,stateId).map(this::toDto);
	}

	private CompanyDTO toDto(CompanyEntity e) {
		CompanyDTO dto = new CompanyDTO();
		dto.setCompanyId(e.getCompanyId());
		dto.setCompanyName(e.getCompanyName());
		dto.setCompanySiteOffice(e.getCompanySiteOffice());
		dto.setAddressLine1(e.getAddressLine1());
		dto.setAddressLine2(e.getAddressLine2());
		dto.setCity(e.getCity());
		dto.setPincode(e.getPincode());
		dto.setLocation(e.getLocation());
		dto.setStateId(e.getStateId());
		dto.setStateName(e.getState() != null ? e.getState().getName() : null);
		dto.setGstNo(e.getGstNo());
		dto.setPan(e.getPan());
		dto.setStatus(e.getStatus());
		return dto;
	}

	@Override
	public ResponseEntity<List<CompanyEntity>> findActiveCompany() {
		  return ResponseEntity.ok(companyRepo.activeCompany());
	}
}
