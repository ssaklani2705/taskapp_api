package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

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
import com.webelement.taskapp.dto.RecurringDTO;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.entity.RecurringEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.RecurringRepository;

@Service
public class RecurringService {

	@Autowired
	private RecurringRepository recurringRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private CommonFunction commonFunction;

	// For AddOrUpdate
	@Transactional
	public RecurringEntity addOrUpdateRecurring(RecurringDTO dto, Integer userId) {
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

		if (dto.getRecurringId() == null || dto.getRecurringId() == 0) {
			recurring = new RecurringEntity();

			recurring.setRegDate(Timestamp.from(Instant.now()));
			recurring.setStatus((short) 1);
		} else {

			Optional<RecurringEntity> existing = recurringRepository.findById(dto.getRecurringId());

			if (existing.isEmpty()) {
				throw new RuntimeException("Recurring record not found.");
			}

			recurring = existing.get();

			Optional<ClientEntity> existingClient = clientRepository
					.findByClientIdAndManagerIdAndStatus(recurring.getClientId(), userId, (short) 1);

			if (existingClient.isEmpty()) {
				throw new RuntimeException("You are not authorized to update this recurring record.");
			}

			recurring.setModDate(Timestamp.from(Instant.now()));
		}

		recurring.setClientId(dto.getClientId());
		recurring.setTitle(dto.getTitle());
		recurring.setDescription(dto.getDescription());
		recurring.setType(dto.getType());
		recurring.setDate(dto.getDate());
		recurring.setDay(dto.getDay());
		recurring.setMonth(dto.getMonth());
		recurring.setTaskCatId(dto.getTaskCatId());

		if (dto.getRecurringId() != null && dto.getRecurringId() != 0 && dto.getStatus() != null) {
			recurring.setStatus(dto.getStatus());
		}

		return recurringRepository.save(recurring);
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
	public Page<RecurringDTO> findRecurringDetails(Pageable pageable, Short status, Integer managerId, String title,
			Integer userId) {

		Integer managerFilter = userId;

		Short statusFilter = (status != null && status == 0) ? null : status;

		String titleFilter = (title != null && !title.trim().isEmpty()) ? title.trim() : null;

		return recurringRepository.findRecurringDetails(pageable, statusFilter, managerFilter, titleFilter);
	}

	// For Delete
	@Transactional
	public ResponseEntity<ResponseApi<String>> deleteRecurring(Integer recurringId, Integer userId,
			HttpServletRequest httpRequest) {

		int updatedRows = recurringRepository.deleteRecurring((short) 3, recurringId);

		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Recurring Deleted", 8, recurringId, -1);
			return ResponseEntity.ok(new ResponseApi<>(true, "Recurring deleted successfully", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseApi<>(false, "Failed to delete Recurring", null));
		}
	}
}
