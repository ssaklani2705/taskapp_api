package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.time.Instant;
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

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.RecurringDTO;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.entity.RecurringEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.RecurringRepository;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class RecurringService {

	@Autowired
	private RecurringRepository recurringRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private CommonFunction commonFunction;

	@Autowired
	private UserLoginRepository userLoginRepository;

	// For AddOrUpdate
	@Transactional
	public ApiResponse<RecurringEntity> addOrUpdateRecurring(RecurringDTO dto, Integer userId,
			HttpServletRequest httpRequest) {

		boolean isNew = dto.getRecurringId() == null || dto.getRecurringId() == 0;

		if (dto.getClientId() == null || dto.getClientId() <= 0) {

			throw new RuntimeException("Please select a client.");
		}

		Optional<ClientEntity> clientOptional = clientRepository.findByClientIdAndManagerIdAndStatus(dto.getClientId(),
				userId, (short) 1);

		if (clientOptional.isEmpty()) {

			throw new RuntimeException("You are not authorized to add recurring for this client.");
		}

		if (dto.getType() == null || dto.getType() < 1 || dto.getType() > 4) {

			throw new RuntimeException("Invalid recurring type.");
		}

		validateRecurringType(dto);

		RecurringEntity recurring;

		if (isNew) {

			recurring = new RecurringEntity();

			recurring.setRegDate(Timestamp.from(Instant.now()));

			recurring.setStatus((short) 1);

		}

		else {

			Optional<RecurringEntity> existing = recurringRepository.findById(dto.getRecurringId());

			if (existing.isEmpty()) {

				throw new RuntimeException("Recurring record not found.");
			}

			recurring = existing.get();

			Optional<ClientEntity> existingClient = clientRepository.findByClientIdAndManagerId(recurring.getClientId(),
					userId);

			if (existingClient.isEmpty()) {

				throw new RuntimeException("You are not authorized to update this recurring record.");
			}

			recurring.setModDate(Timestamp.from(Instant.now()));

			if (dto.getStatus() != null) {

				recurring.setStatus(dto.getStatus());
			}
		}

		recurring.setClientId(dto.getClientId());
		recurring.setTitle(dto.getTitle());
		recurring.setDescription(dto.getDescription());
		recurring.setType(dto.getType());
		recurring.setDate(dto.getDate());
		recurring.setDay(dto.getDay());
		recurring.setMonth(dto.getMonth());
		recurring.setTaskCatId(dto.getTaskCatId());

		RecurringEntity recurringEntity = recurringRepository.save(recurring);

		String action = isNew ? "Recurring Added" : "Recurring Updated";

		commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, 11, recurringEntity.getRecurringId(), -1);

		return new ApiResponse<>(true, isNew ? "Recurring added successfully" : "Recurring updated successfully",
				recurringEntity);
	}

	private void validateRecurringType(RecurringDTO dto) {
		Short type = dto.getType();

		if (type == 1) {
			dto.setDay(null);
			dto.setDate(null);
			dto.setMonth(null);
		} else if (type == 2) {

			if (dto.getDay() == null || dto.getDay() < 1 || dto.getDay() > 7) {
				throw new RuntimeException("For weekly recurring, day must be between 1 and 7.");
			}

			dto.setDate(null);
			dto.setMonth(null);
		}

		else if (type == 3) {
			if (dto.getDate() == null || dto.getDate() < 1 || dto.getDate() > 28) {
				throw new RuntimeException("For monthly recurring, date must be between 1 and 28.");
			}

			dto.setDay(null);
			dto.setMonth(null);
		} else if (type == 4) {

			if (dto.getDate() == null || dto.getDate() < 1 || dto.getDate() > 28) {
				throw new RuntimeException("For yearly recurring, date must be between 1 and 28.");
			}

			if (dto.getMonth() == null || dto.getMonth() < 1 || dto.getMonth() > 12) {
				throw new RuntimeException("For yearly recurring, month must be between 1 and 12.");
			}

			dto.setDay(null);
		}
	}

	// For edit
	@Transactional(readOnly = true)
	public RecurringDTO getRecurringById(Integer recurringId, Integer userId) {

		Optional<RecurringEntity> existing = recurringRepository.findById(recurringId);

		if (existing.isEmpty()) {
			throw new RuntimeException("Recurring record not found.");
		}

		RecurringEntity recurring = existing.get();

		Optional<ClientEntity> clientOptional = clientRepository.findByClientIdAndManagerId(recurring.getClientId(),
				userId);

		if (clientOptional.isEmpty()) {
			throw new RuntimeException("You are not authorized to view this recurring record.");
		}

		return new RecurringDTO(recurring.getRecurringId(), recurring.getClientId(), recurring.getTitle(),
				recurring.getDescription(), recurring.getType(), recurring.getDate(), recurring.getDay(),
				recurring.getMonth(), recurring.getTaskCatId(), recurring.getStatus());
	}

	// For Index
//	public Page<RecurringDTO> findRecurringDetails(Pageable pageable, Short status, String title, Integer userId) {
//
//		Short statusFilter = (status != null && status == 0) ? null : status;
//		String titleFilter = (title != null && !title.trim().isEmpty()) ? title.trim() : null;
//
//		return recurringRepository.findRecurringDetails(pageable, statusFilter, userId, titleFilter);
//	}

	public Page<RecurringDTO> findRecurringDetails(Pageable pageable, Short status, String search, Integer clientId,
			Short type, Integer taskCatId, Integer userId) {

		Short statusFilter = (status != null && status == 0) ? null : status;
		String searchFilter = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
		Integer clientFilter = (clientId != null && clientId == 0) ? null : clientId;
		Short typeFilter = (type != null && type == 0) ? null : type;
		Integer taskCatFilter = (taskCatId != null && taskCatId == 0) ? null : taskCatId;

		return recurringRepository.findRecurringDetails(pageable, statusFilter, userId, searchFilter, clientFilter,
				typeFilter, taskCatFilter);
	}

	// For View
	@Transactional(readOnly = true)
	public RecurringDTO getViewRecurringById(Integer recurringId, Integer userId) {

		RecurringDTO recurring = recurringRepository.getRecurringById(recurringId);

		if (recurring == null) {
			throw new RuntimeException("Recurring record not found.");
		}

		Optional<ClientEntity> clientOptional = clientRepository.findByClientIdAndManagerId(recurring.getClientId(),
				userId);

		if (clientOptional.isEmpty()) {
			throw new RuntimeException("You are not authorized to view this recurring record.");
		}

		if (recurring != null) {
			List<TransactionEntity> history = getTransactionLogs(11, recurringId);
			recurring.setTransactionHistory(history);
		}

		return recurring;
	}

	// For Delete
	@Transactional
	public ResponseEntity<ResponseApi<String>> deleteRecurring(Integer recurringId, Integer userId,
			HttpServletRequest httpRequest) {

		int updatedRows = recurringRepository.deleteRecurring((short) 3, recurringId);

		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Recurring Deleted", 11, recurringId, -1);
			return ResponseEntity.ok(new ResponseApi<>(true, "Recurring deleted successfully", null));
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
