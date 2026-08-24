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
import com.webelement.taskapp.dto.DepartmentDTO;
import com.webelement.taskapp.entity.DepartmentEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.DepartmentRepository;


@Service
public class DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private CommonFunction commonFunction;

	// =========================================================
	// ADD / UPDATE
	// =========================================================

	public ApiResponse<DepartmentDTO> addOrUpdate(
			DepartmentDTO dto,
			HttpServletRequest httpRequest) {

		Timestamp timestamp =
				Timestamp.valueOf(
						LocalDateTime.now());

		String name =
				dto.getName() == null
						? ""
						: dto.getName().trim();

		// -----------------------------------------------------
		// NAME VALIDATION
		// -----------------------------------------------------

		// ADD
		if (dto.getDepartmentId() == null
				|| dto.getDepartmentId() == 0) {

			if (departmentRepository
					.existsByNameIgnoreCaseAndStatusNot(
							name,
							3)) {

				return new ApiResponse<>(
						false,
						"Department name already exists",
						null);
			}
		}

		// UPDATE
		else {

			DepartmentEntity existing =
					departmentRepository
							.findByNameIgnoreCase(
									name);

			if (existing != null
					&& !existing
							.getDepartmentId()
							.equals(
									dto.getDepartmentId())
					&& existing.getStatus() != 3) {

				return new ApiResponse<>(
						false,
						"Department name already exists",
						null);
			}
		}

		// =====================================================
		// ENTITY
		// =====================================================

		DepartmentEntity entity;

		// -----------------------------------------------------
		// UPDATE
		// -----------------------------------------------------

		if (dto.getDepartmentId() != null
				&& dto.getDepartmentId() != 0) {

			entity =
					departmentRepository
							.findById(
									dto.getDepartmentId())
							.orElseThrow(
									() -> new RuntimeException(
											"Department Record not found"));

			entity.setName(name);

			entity.setSequence(
					dto.getSequence());

			entity.setUserId(
					dto.getUserId());

			if (dto.getStatus() != null) {

				entity.setStatus(
						dto.getStatus());
			}

			entity.setModdate(
					timestamp);
		}

		// -----------------------------------------------------
		// ADD
		// -----------------------------------------------------

		else {

			entity =
					new DepartmentEntity();

			entity.setName(name);

			entity.setSequence(
					dto.getSequence());

			entity.setUserId(
					dto.getUserId());

			entity.setStatus(1);

			entity.setRegdate(
					timestamp);
		}

		// =====================================================
		// SAVE
		// =====================================================

		DepartmentEntity saved =
				departmentRepository.save(
						entity);

		// =====================================================
		// DTO RESPONSE
		// =====================================================

		DepartmentDTO d =
				new DepartmentDTO();

		d.setDepartmentId(
				saved.getDepartmentId());

		d.setName(
				saved.getName());

		d.setSequence(
				saved.getSequence());

		d.setUserId(
				saved.getUserId());

		d.setStatus(
				saved.getStatus());

		d.setRegdate(
				saved.getRegdate());

		d.setModdate(
				saved.getModdate());

		// =====================================================
		// HISTORY
		// =====================================================

		boolean isNew =
				dto.getDepartmentId() == null
				|| dto.getDepartmentId() == 0;

		String action =
				isNew
						? "Department Added"
						: "Department Updated";

		/*
		 * IMPORTANT:
		 * Change 3 below if your Department module
		 * has a different module ID.
		 */
		commonFunction.createHistoryAccess(
				dto.getUserId(),
				commonFunction.resolveClientIp(
						httpRequest),
				commonFunction.getLocalIp(),
				action,
				3,
				saved.getDepartmentId(),
				-1);

		return new ApiResponse<>(
				true,
				"Department saved successfully",
				d);
	}

	// =========================================================
	// GET BY ID
	// =========================================================

	public ApiResponse<DepartmentDTO> getById(
			Integer id) {

		DepartmentEntity entity =
				departmentRepository
						.findById(id)
						.orElseThrow(
								() -> new RuntimeException(
										"Department Record not found"));

		DepartmentDTO d =
				new DepartmentDTO();

		d.setDepartmentId(
				entity.getDepartmentId());

		d.setName(
				entity.getName());

		d.setSequence(
				entity.getSequence());

		d.setUserId(
				entity.getUserId());

		d.setStatus(
				entity.getStatus());

		d.setRegdate(
				entity.getRegdate());

		d.setModdate(
				entity.getModdate());

		// =====================================================
		// TRANSACTION HISTORY
		// =====================================================

		List<TransactionEntity> history =
				commonFunction.getTransactionLogs(
						3,
						id);

		if (history != null
				&& !history.isEmpty()) {

			d.setTransactionHistory(
					history);
		}

		return new ApiResponse<>(
				true,
				"Department fetched successfully",
				d);
	}

	// =========================================================
	// PAGINATION + SEARCH
	// =========================================================

	public Page<DepartmentDTO> findDepartmentDetails(
			int page,
			int size,
			int statusIndex,
			String search) {

		return departmentRepository
				.findDepartmentDetails(
						PageRequest.of(
								page,
								size),
						statusIndex,
						search);
	}

	// =========================================================
	// DELETE
	// =========================================================

	public ResponseEntity<ApiResponse<String>> deleteDepartment(
			Integer departmentId,
			Integer userId,
			HttpServletRequest httpRequest) {

		Optional<DepartmentEntity> existingDepartment =
				departmentRepository
						.findById(departmentId);

		// -----------------------------------------------------
		// NOT FOUND
		// -----------------------------------------------------

		if (!existingDepartment.isPresent()) {

			return ResponseEntity
					.status(
							HttpStatus.NOT_FOUND)
					.body(
							new ApiResponse<>(
									false,
									"Department not found",
									null));
		}

		// -----------------------------------------------------
		// SOFT DELETE
		// -----------------------------------------------------

		int updatedRows =
				departmentRepository
						.softDelete(
								3,
								departmentId);

		if (updatedRows > 0) {

			// -------------------------------------------------
			// HISTORY
			// -------------------------------------------------

			commonFunction.createHistoryAccess(
					userId,
					commonFunction.resolveClientIp(
							httpRequest),
					commonFunction.getLocalIp(),
					"Department Deleted",
					3,
					departmentId,
					-1);

			return ResponseEntity.ok(
					new ApiResponse<>(
							true,
							"Department deleted successfully",
							null));

		} else {

			return ResponseEntity
					.status(
							HttpStatus.INTERNAL_SERVER_ERROR)
					.body(
							new ApiResponse<>(
									false,
									"Failed to delete department",
									null));
		}
	}

	// =========================================================
	// ACTIVE DEPARTMENTS
	// =========================================================

	public List<DepartmentDTO> getActiveDepartments() {

		List<DepartmentEntity> departments =
				departmentRepository
						.findByStatus(1);

		return departments
				.stream()
				.map(d ->
						new DepartmentDTO(
								d.getDepartmentId(),
								d.getName()))
				.collect(
						Collectors.toList());
	}
}
