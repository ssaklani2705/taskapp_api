package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.Exceptions.DuplicateTaskTitleException;
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.GenericRecurringDTO;
import com.webelement.taskapp.entity.GenericRecurringEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.GenericRecurringRepository;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class GenericRecurringService {

	@Autowired
	private GenericRecurringRepository genericRecurringRepository;

	@Autowired
	private CommonFunction commonFunction;

	@Autowired
	private UserLoginRepository userLoginRepository;

	   // For AddOrUpdate
    public ApiResponse<GenericRecurringEntity> addOrUpdateGenericRecurring(GenericRecurringDTO dto, Integer userId,
            HttpServletRequest httpRequest) {


        boolean isNew = dto.getGenericRecurringId() == null || dto.getGenericRecurringId() == 0;


        if (dto.getType() == null || dto.getType() < 1 || dto.getType() > 4) {
            throw new RuntimeException("Invalid recurring type.");
        }


        if (dto.getPriority() == null || dto.getPriority() < 1 || dto.getPriority() > 3) {
            throw new RuntimeException("Invalid priority.");
        }


        if (dto.getTime() == null) {
            throw new RuntimeException("Please select a time.");
        }


        validateGenericRecurringType(dto);


        String title = dto.getTitle() == null ? "" : dto.getTitle().trim();


        if (title.isEmpty()) {
            throw new RuntimeException("Please enter a title.");
        }


        List<Short> activeStatuses = Arrays.asList((short) 1, (short) 2);


        boolean duplicateTitle;


        if (isNew) {
            duplicateTitle = genericRecurringRepository.existsByTitleIgnoreCaseAndStatusIn(title, activeStatuses);
        } else {
            duplicateTitle = genericRecurringRepository.existsByTitleIgnoreCaseAndStatusInAndGenericRecurringIdNot(
                    title, activeStatuses, dto.getGenericRecurringId());
        }


        if (duplicateTitle) {
            throw new DuplicateTaskTitleException("A Generic Recurring record with this title already exists.");
        }


        GenericRecurringEntity recurring;


        if (isNew) {
            recurring = new GenericRecurringEntity();
            recurring.setRegDate(Timestamp.from(Instant.now()));
            recurring.setStatus((short) 1);
        } else {
            Optional<GenericRecurringEntity> existing = genericRecurringRepository
                    .findById(dto.getGenericRecurringId());


            if (existing.isEmpty()) {
                throw new RuntimeException("Generic Recurring record not found.");
            }


            recurring = existing.get();
            recurring.setModDate(Timestamp.from(Instant.now()));


            if (dto.getStatus() != null) {
                recurring.setStatus(dto.getStatus());
            }
        }


        recurring.setPriority(dto.getPriority());
        recurring.setForValue(dto.getForValue());
        recurring.setTitle(dto.getTitle().trim());
        recurring.setDescription(dto.getDescription());
        recurring.setType(dto.getType());
        recurring.setDate(dto.getDate());
        recurring.setDay(dto.getDay());
        recurring.setMonth(dto.getMonth());
        recurring.setTime(dto.getTime());
        recurring.setTaskCatId(dto.getTaskCatId());


        GenericRecurringEntity recurringEntity = genericRecurringRepository.save(recurring);


        String action = isNew ? "Generic Recurring Added" : "Generic Recurring Updated";


        commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
                commonFunction.getLocalIp(), action, 14, recurringEntity.getGenericRecurringId(), -1);


        return new ApiResponse<>(true,
                isNew ? "Generic Recurring added successfully" : "Generic Recurring updated successfully",
                recurringEntity);
    }



	private void validateGenericRecurringType(GenericRecurringDTO dto) {
		Short type = dto.getType();

		if (type == 1) {
			dto.setDay(null);
			dto.setDate(null);
			dto.setMonth(null);
		} else if (type == 2) {
			if (dto.getDay() == null || dto.getDay() < 1 || dto.getDay() > 7) {
				throw new RuntimeException("For weekly generic recurring, day must be between 1 and 7.");
			}

			dto.setDate(null);
			dto.setMonth(null);
		} else if (type == 3) {
			if (dto.getDate() == null || dto.getDate() < -2 || dto.getDate() > 28) {
				throw new RuntimeException("For monthly generic recurring, date must be between 1 and 28.");
			}

			dto.setDay(null);
			dto.setMonth(null);
		} else if (type == 4) {
			if (dto.getDate() == null || dto.getDate() < 1 || dto.getDate() > 28) {
				throw new RuntimeException("For yearly generic recurring, date must be between 1 and 28.");
			}

			if (dto.getMonth() == null || dto.getMonth() < 1 || dto.getMonth() > 12) {
				throw new RuntimeException("For yearly generic recurring, month must be between 1 and 12.");
			}

			dto.setDay(null);
		}
	}

	// For edit
	@Transactional(readOnly = true)
	public GenericRecurringDTO getGenericRecurringById(Integer genericRecurringId, Integer userId) {

		Optional<GenericRecurringEntity> existing = genericRecurringRepository.findById(genericRecurringId);

		if (existing.isEmpty()) {
			throw new RuntimeException("Generic Recurring record not found.");
		}

		GenericRecurringEntity recurring = existing.get();

		return new GenericRecurringDTO(recurring.getGenericRecurringId(), recurring.getForValue(), recurring.getDate(),
				recurring.getDay(), recurring.getDescription(), recurring.getMonth(), recurring.getStatus(),
				recurring.getTaskCatId(), recurring.getTitle(), recurring.getType(), recurring.getPriority(),
				recurring.getTime());
	}

	public Page<GenericRecurringDTO> findGenericRecurringDetails(Pageable pageable, Short status, String search,
			Short type, Integer taskCatId, Integer forValue, Integer userId, String isAdmin, String loginType,
			Integer priority) {

		Short statusFilter = (status != null && status == 0) ? null : status;

		String searchFilter = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

		Short typeFilter = (type != null && type == 0) ? null : type;

		Integer taskCatFilter = (taskCatId != null && taskCatId == 0) ? null : taskCatId;

		Integer forValueFilter = (forValue != null && forValue == 0) ? null : forValue;

		return genericRecurringRepository.findGenericRecurringDetails(pageable, statusFilter, searchFilter, typeFilter,
				taskCatFilter,  forValueFilter, isAdmin, loginType, userId, priority);
	}

	// For View
	@Transactional(readOnly = true)
	public GenericRecurringDTO getViewGenericRecurringById(Integer genericRecurringId, Integer userId) {

		GenericRecurringDTO recurring = genericRecurringRepository.getGenericRecurringById(genericRecurringId);

		if (recurring == null) {
			throw new RuntimeException("Generic Recurring record not found.");
		}

		List<TransactionEntity> history = getTransactionLogs(15, genericRecurringId);

		recurring.setTransactionHistory(history);

		return recurring;
	}

	// For Delete
	@Transactional
	public ResponseEntity<ResponseApi<String>> deleteGenericRecurring(Integer genericRecurringId, Integer userId,
			HttpServletRequest httpRequest) {

		int updatedRows = genericRecurringRepository.deleteGenericRecurring((short) 3, genericRecurringId);

		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Generic Recurring Deleted", 15, genericRecurringId, -1);

			return ResponseEntity.ok(new ResponseApi<>(true, "Generic Recurring deleted successfully", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseApi<>(false, "Failed to delete Recurring", null));
		}
	}

	// For transaction history
	public List<TransactionEntity> getTransactionLogs(int moduleId, Integer recordId) {
		List<Object[]> results = userLoginRepository.getTransactionLogs(moduleId, recordId);

		return results.stream().map(obj -> {
			TransactionEntity dto = new TransactionEntity();

			dto.setEntryDate((String) obj[0]);
			dto.setName((String) obj[1]);
			dto.setAction((String) obj[2]);
			dto.setUserId(obj[3] != null ? ((Number) obj[3]).intValue() : null);
			dto.setFlag((String) obj[4]);

			return dto;
		}).collect(Collectors.toList());
	}
}
