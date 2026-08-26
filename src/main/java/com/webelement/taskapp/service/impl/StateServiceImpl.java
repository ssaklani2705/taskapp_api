package com.webelement.taskapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.DesignationDTO;
import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.entity.DesignationEntity;
import com.webelement.taskapp.entity.StateEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.StateRepo;
import com.webelement.taskapp.service.StateService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StateServiceImpl implements StateService  {

	   private final StateRepo stateRepository;
	   private final CommonFunction commonFunction;
	   
	   
	   private final HttpServletRequest httpRequest;

	   @Override
	   public ApiResponse<StateDTO> addOrUpdate(StateDTO dto) {

	       LocalDateTime now = LocalDateTime.now();
	       String name = dto.getName() != null ? dto.getName().trim() : "";
	       boolean isNew = dto.getStateId() == null || dto.getStateId() == 0;

	       if (isNew) {
	           if (stateRepository.existsByNameIgnoreCaseAndStatusNot(name, (short) 3)) {
	               return new ApiResponse<>(false, "State name already exists", null);
	           }
	       } else {
	    	   List<StateEntity> duplicates =
	    		        stateRepository.findByNameIgnoreCase(name);

	    		boolean exists = duplicates.stream()
	    		        .anyMatch(duplicate ->
	    		                !duplicate.getStateId().equals(dto.getStateId())
	    		                && duplicate.getStatus() != 3);

	    		if (exists) {
	    		    return new ApiResponse<>(
	    		            false,
	    		            "State name already exists",
	    		            null);
	    		}
	       }

	       // Build entity (new or updated)
	       StateEntity entity;

	       if (!isNew) {

	           entity = stateRepository.findById(dto.getStateId())
	                   .orElseThrow(() -> new RuntimeException("State record not found"));

	           entity.setName(name);
	           entity.setCode(dto.getCode());
	           entity.setStatus(dto.getStatus());
	           entity.setUserId(dto.getUserId());
	           entity.setModificationDate(now);

	       } else {

	           entity = new StateEntity();
	           entity.setName(name);
	           entity.setCode(dto.getCode());
	           entity.setStatus(dto.getStatus() != null ? dto.getStatus() : (short) 1);
	           entity.setUserId(dto.getUserId());
	           entity.setRegistrationDate(now);
	       }

	       StateEntity saved = stateRepository.save(entity);
	       String action =
					isNew
							? "State Added"
							: "State Updated";
	       commonFunction.createHistoryAccess(
	                dto.getUserId(),
	                commonFunction.resolveClientIp(httpRequest),
	                commonFunction.getLocalIp(),
	                action,
	                4,
	                saved.getStateId(),
	                -1);

	       // Build response DTO
	       StateDTO responseDto = new StateDTO();
	       responseDto.setStateId(saved.getStateId());
	       responseDto.setName(saved.getName());
	       responseDto.setCode(saved.getCode());
	       responseDto.setStatus(saved.getStatus());
	       responseDto.setUserId(saved.getUserId());
	       responseDto.setRegistrationDate(saved.getRegistrationDate());
	       responseDto.setModificationDate(saved.getModificationDate());

	       return new ApiResponse<>(true, "State saved successfully", responseDto);
	   }
	   
	   @Override
	   public ApiResponse<StateDTO> getById(Integer stateId) {
		   StateEntity entity = stateRepository.findById(stateId).orElse(null);

		    if (entity == null) {
		        return new ApiResponse<>(false, "State record not found", null);
		    }

		    StateDTO dto = StateDTO.builder()
		            .stateId(entity.getStateId())
		            .name(entity.getName())
		            .code(entity.getCode())
		            .status(entity.getStatus())
		            .userId(entity.getUserId())
		            .build();
		    
		    List<TransactionEntity> history =
		            commonFunction.getTransactionLogs(4, stateId);

		    if (history != null && !history.isEmpty()) {
		        dto.setTransactionHistory(history);
		    }

		    return new ApiResponse<>(true, "State fetched successfully", dto);
	   }

	   @Override
	   public Page<StateDTO> findStateList(int page, int size, int statusIndex, String search) {
		
		   return stateRepository.findStateDetails(
	                PageRequest.of(page, size),
	                statusIndex,
	                search);
	   }

		@Override
		public ApiResponse<StateDTO> delete(StateDTO dto) {

			Optional<StateEntity> existing = stateRepository.findById(dto.getStateId());

			if (!existing.isPresent()) {
				return new ApiResponse<>(false, "State not found", null);
			}

			int updatedRows = stateRepository.softDelete(dto.getStateId());

			if (updatedRows > 0) {

				commonFunction.createHistoryAccess(dto.getUserId(), commonFunction.getLocalIp(),
						commonFunction.getLocalIp(), "State Deleted", 4, dto.getStateId(), -1);

				return new ApiResponse<>(true, "State deleted successfully", null);
			}

			return new ApiResponse<>(false, "State delete failed", null);
		}

	   
	   
	   
	   
		public List<StateEntity> getStates() {
	        return stateRepository.findByStatus((short) 1);
	    }
	


}
