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

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.HolidayDTO;
import com.webelement.taskapp.entity.HolidayEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.HolidayRepo;

@Service
public class HolidayService {

	@Autowired
	private HolidayRepo holidayRepository;

	@Autowired
	private CommonFunction commonFunction;

	// =========================================================
	// ADD / UPDATE
	// =========================================================

	public ApiResponse<HolidayDTO> addOrUpdate(
			HolidayDTO dto,
			HttpServletRequest httpRequest) {

		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

		String name = dto.getName() == null
				? ""
				: dto.getName().trim();

		boolean isNew = dto.getHolidayId() == null
				|| dto.getHolidayId() == 0;

		/*
		 * ============================================================
		 * ADD
		 * ============================================================
		 */
		if (isNew) {

			// Check duplicate holiday name
			// Ignore deleted holidays (status = 3)
			if (holidayRepository.existsByNameIgnoreCaseAndStatusNot(
					name, 3)) {

				return new ApiResponse<>(
						false,
						"Holiday name already exists",
						null);
			}
		}

		/*
		 * ============================================================
		 * UPDATE
		 * ============================================================
		 */
		else {

			boolean duplicateName =
					holidayRepository
							.existsByNameIgnoreCaseAndHolidayIdNotAndStatusNot(
									name,
									dto.getHolidayId(),
									3);

			if (duplicateName) {

				return new ApiResponse<>(
						false,
						"Holiday name already exists",
						null);
			}
		}

		/*
		 * ============================================================
		 * DATE RANGE VALIDATION
		 * ============================================================
		 */
		if (dto.getStartDate() != null
				&& dto.getEndDate() != null
				&& dto.getEndDate().isBefore(dto.getStartDate())) {

			return new ApiResponse<>(
					false,
					"End date cannot be before start date",
					null);
		}

		/*
		 * ============================================================
		 * CREATE / UPDATE ENTITY
		 * ============================================================
		 */
		HolidayEntity entity;

		if (!isNew) {

			// UPDATE
			entity = holidayRepository.findById(dto.getHolidayId())
					.orElseThrow(() ->
							new RuntimeException("Holiday Record not found"));

			entity.setName(name);
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setUserId(dto.getUserId());

			if (dto.getStatus() != null) {
				entity.setStatus(dto.getStatus());
			}

			entity.setModdate(timestamp);

		} else {

			// ADD
			entity = new HolidayEntity();

			entity.setName(name);
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setUserId(dto.getUserId());
			entity.setStatus(1);
			entity.setRegdate(timestamp);
		}

		/*
		 * ============================================================
		 * SAVE
		 * ============================================================
		 */
		HolidayEntity saved = holidayRepository.save(entity);

		/*
		 * ============================================================
		 * CREATE RESPONSE DTO
		 * ============================================================
		 */
		HolidayDTO h = new HolidayDTO();

		h.setHolidayId(saved.getHolidayId());
		h.setName(saved.getName());
		h.setStartDate(saved.getStartDate());
		h.setEndDate(saved.getEndDate());
		h.setUserId(saved.getUserId());
		h.setStatus(saved.getStatus());
		h.setRegdate(saved.getRegdate());
		h.setModdate(saved.getModdate());

		/*
		 * ============================================================
		 * HISTORY
		 * ============================================================
		 */
		String action = isNew
				? "Holiday Added"
				: "Holiday Updated";

		commonFunction.createHistoryAccess(
				dto.getUserId(),
				commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(),
				action,
				/* module id — see note below */ 0,
				saved.getHolidayId(),
				-1);

		/*
		 * ============================================================
		 * RESPONSE
		 * ============================================================
		 */
		return new ApiResponse<>(
				true,
				"Holiday saved successfully",
				h);
	}

	// =========================================================
	// GET BY ID
	// =========================================================

	public ApiResponse<HolidayDTO> getById(Integer id) {

		HolidayEntity entity = holidayRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Holiday Record not found"));

		HolidayDTO h = new HolidayDTO();

		h.setHolidayId(entity.getHolidayId());
		h.setName(entity.getName());
		h.setStartDate(entity.getStartDate());
		h.setEndDate(entity.getEndDate());
		h.setUserId(entity.getUserId());
		h.setStatus(entity.getStatus());
		h.setRegdate(entity.getRegdate());
		h.setModdate(entity.getModdate());

		// =====================================================
		// TRANSACTION HISTORY
		// =====================================================

		List<TransactionEntity> history = commonFunction.getTransactionLogs(0, id);

		if (history != null && !history.isEmpty()) {

			h.setTransactionHistory(history);
		}

		return new ApiResponse<>(true, "Holiday fetched successfully", h);
	}

	// =========================================================
	// PAGINATION + SEARCH
	// =========================================================

	public Page<HolidayDTO> findHolidayDetails(
			int page,
			int size,
			int statusIndex,
			String search) {

		return holidayRepository
				.findHolidayDetails(
						PageRequest.of(
								page,
								size),
						statusIndex,
						search);
	}

	// =========================================================
	// DELETE
	// =========================================================

	public ResponseEntity<ApiResponse<String>> deleteHoliday(
			Integer holidayId,
			Integer userId,
			HttpServletRequest httpRequest) {

		Optional<HolidayEntity> existingHoliday =
				holidayRepository
						.findById(holidayId);

		// -----------------------------------------------------
		// NOT FOUND
		// -----------------------------------------------------

		if (!existingHoliday.isPresent()) {

			return ResponseEntity
					.status(
							HttpStatus.NOT_FOUND)
					.body(
							new ApiResponse<>(
									false,
									"Holiday not found",
									null));
		}

		// -----------------------------------------------------
		// SOFT DELETE
		// -----------------------------------------------------

		int updatedRows =
				holidayRepository
						.softDelete(
								3,
								holidayId);

		if (updatedRows > 0) {

			// -------------------------------------------------
			// HISTORY
			// -------------------------------------------------

			commonFunction.createHistoryAccess(
					userId,
					commonFunction.resolveClientIp(
							httpRequest),
					commonFunction.getLocalIp(),
					"Holiday Deleted",
					0,
					holidayId,
					-1);

			return ResponseEntity.ok(
					new ApiResponse<>(
							true,
							"Holiday deleted successfully",
							null));

		} else {

			return ResponseEntity
					.status(
							HttpStatus.INTERNAL_SERVER_ERROR)
					.body(
							new ApiResponse<>(
									false,
									"Failed to delete holiday",
									null));
		}
	}

	// =========================================================
	// ACTIVE HOLIDAYS
	// =========================================================

	public List<HolidayDTO> getActiveHolidays() {

		List<HolidayEntity> holidays =
				holidayRepository
						.findByStatus(1);

		return holidays
				.stream()
				.map(h ->
						new HolidayDTO(
								h.getHolidayId(),
								h.getName()))
				.collect(
						Collectors.toList());
	}
}