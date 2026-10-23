package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.Exceptions.ApiException;
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.DesignationDTO;
import com.webelement.taskapp.entity.DesignationEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.DesignationRepository;
import com.webelement.taskapp.repo.UserLoginRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DesignationService {
	private final DesignationRepository designationRepository;
	private final CommonFunction commonFunction;
	private final UserLoginRepository userLoginRepository;
	public ApiResponse<DesignationDTO> addOrUpdate(DesignationDTO dto, HttpServletRequest httpRequest) {
		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
		String name = dto.getName() != null ? dto.getName().trim() : "";
		boolean isNew = dto.getDesigmationId() == null || dto.getDesigmationId() == 0;
		if (isNew) {
			if (designationRepository.existsByNameIgnoreCaseAndStatusNot(name, 3)) {

				return new ApiResponse<>(false, "Designation name already exists", null);
			}
		} else {

			boolean duplicateName = designationRepository.existsByNameIgnoreCaseAndDesignationIdNotAndStatusNot(name,
					dto.getDesigmationId(), 3);
			if (duplicateName) {
				return new ApiResponse<>(false, "Designation name already exists", null);
			}
		}

		DesignationEntity entity;
		if (!isNew) {
			// UPDATE
			entity = designationRepository.findById(dto.getDesigmationId())
					.orElseThrow(() -> new RuntimeException("Designation Record not found"));
			entity.setName(name);
			entity.setSequence(dto.getSequence() != null ? dto.getSequence() : 0);
			entity.setUserId(dto.getUserId());
			if (dto.getStatus() != null) {
				entity.setStatus(dto.getStatus());
			}
			entity.setModdate(timestamp);
		} else {
			entity = new DesignationEntity();
			entity.setName(name);
			entity.setSequence(dto.getSequence() != null ? dto.getSequence() : 0);
			entity.setUserId(dto.getUserId());
			entity.setStatus(1);
			entity.setRegdate(timestamp);
		}

		DesignationEntity saved = designationRepository.save(entity);
		DesignationDTO responseDTO = new DesignationDTO();
		responseDTO.setDesigmationId(saved.getDesignationId());
		responseDTO.setName(saved.getName());
		responseDTO.setSequence(saved.getSequence());
		responseDTO.setUserId(saved.getUserId());
		responseDTO.setStatus(saved.getStatus());
		responseDTO.setRegdate(saved.getRegdate());
		responseDTO.setModdate(saved.getModdate());
		String action = isNew ? "Designation Added" : "Designation Updated";
		commonFunction.createHistoryAccess(dto.getUserId(), commonFunction.resolveClientIp(httpRequest),commonFunction.getLocalIp(), action, 3, saved.getDesignationId(), -1);
		return new ApiResponse<>(true, "Designation saved successfully", responseDTO);
	}

	public ApiResponse<DesignationDTO> getById(Integer id) {
		DesignationEntity entity = designationRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Designation Record not found"));
		DesignationDTO dto = new DesignationDTO();
		dto.setDesigmationId(entity.getDesignationId());
		dto.setName(entity.getName());
		dto.setSequence(entity.getSequence());
		dto.setUserId(entity.getUserId());
		dto.setStatus(entity.getStatus());
		dto.setRegdate(entity.getRegdate());
		dto.setModdate(entity.getModdate());
		List<TransactionEntity> history = commonFunction.getTransactionLogs(3, id);
		if (history != null && !history.isEmpty()) {
			dto.setTransactionHistory(history);
		}
		return new ApiResponse<>(true, "Designation fetched successfully", dto);
	}

	public Page<DesignationDTO> findDesigmationDetails(int page, int size, int statusIndex, String search) {
		return designationRepository.findDesignationDetails(PageRequest.of(page, size), statusIndex, search);
	}

	public ResponseEntity<ApiResponse<String>> deleteDesigmation(Integer desigmationId, Integer userId,
			HttpServletRequest httpRequest) {
		Optional<DesignationEntity> existing = designationRepository.findById(desigmationId);
		if (!existing.isPresent()) {
			throw ApiException.notFound("Designation not found");
		}
		boolean isDesignationInUse = userLoginRepository.existsByDesignationIdAndStatusNot(desigmationId, 3);
		if (isDesignationInUse) {
			
		throw ApiException.conflictedException("Designation is assigned to one or more users and cannot be deleted");
		}
		int updatedRows = designationRepository.softDelete(3, desigmationId);
		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Designation Deleted", 3, desigmationId, -1);
			return ResponseEntity.ok(new ApiResponse<>(true, "Designation deleted successfully", null));
		}
		throw ApiException.internal("Failed to delete designation",null);
	}

	public List<DesignationDTO> getActiveDesigmations() {
		List<DesignationEntity> list = designationRepository.findByStatus(1);
		return list.stream().map(d -> new DesignationDTO(d.getDesignationId(), d.getName()))
				.collect(Collectors.toList());
	}
}
