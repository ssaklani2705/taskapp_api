package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import com.webelement.taskapp.dto.TaskCategoryDTO;
import com.webelement.taskapp.entity.TaskCategoryEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.DepartmentRepository;
import com.webelement.taskapp.repo.TaskCategoryRepository;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class TaskCategoryService {

	@Autowired
	private TaskCategoryRepository taskCategoryRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private CommonFunction commonFunction;
	
	@Autowired
	private UserLoginRepository userLoginRepository;

	public ApiResponse<TaskCategoryDTO> addOrUpdate(TaskCategoryDTO dto, HttpServletRequest httpRequest) {

		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

		String name = dto.getName() == null ? "" : dto.getName().trim();

		if (name.isEmpty()) {
			return new ApiResponse<>(false, "Task Category name is required", null);
		}

		// ADD
		if (dto.getTaskcategoryId() == null || dto.getTaskcategoryId() == 0) {

			if (taskCategoryRepository.existsByNameIgnoreCaseAndStatusNot(name, 3)) {

				return new ApiResponse<>(false, "Task Category name already exists", null);
			}
		}

		// UPDATE
		else {

			TaskCategoryEntity existing = taskCategoryRepository.findByNameIgnoreCase(name);

			if (existing != null && !existing.getTaskcategoryId().equals(dto.getTaskcategoryId())
					&& existing.getStatus() != 3) {

				return new ApiResponse<>(false, "Task Category name already exists", null);
			}
		}

		TaskCategoryEntity entity;

		// UPDATE
		if (dto.getTaskcategoryId() != null && dto.getTaskcategoryId() > 0) {

			entity = taskCategoryRepository.findById(dto.getTaskcategoryId())
					.orElseThrow(() -> new RuntimeException("Task Category Record not found"));

			entity.setDepartmentId(dto.getDepartmentId());
			entity.setName(name);
			entity.setUserId(dto.getUserId());

			entity.setDuedatetime(dto.getDueDateTime());

			if (dto.getStatus() != null) {
				entity.setStatus(dto.getStatus());
			}

			entity.setModdate(timestamp);
		}

		// ADD
		else {

			entity = new TaskCategoryEntity();

			entity.setDepartmentId(dto.getDepartmentId());
			entity.setName(name);
			entity.setUserId(dto.getUserId());
			entity.setStatus(1);
			entity.setRegdate(timestamp);
			entity.setDuedatetime(dto.getDueDateTime());
		}

		TaskCategoryEntity saved = taskCategoryRepository.save(entity);

		TaskCategoryDTO responseDto = new TaskCategoryDTO();

		responseDto.setTaskcategoryId(saved.getTaskcategoryId());

		responseDto.setDepartmentId(saved.getDepartmentId());

		responseDto.setName(saved.getName());

		responseDto.setStatus(saved.getStatus());

		responseDto.setUserId(saved.getUserId());

		responseDto.setRegdate(saved.getRegdate());

		responseDto.setModdate(saved.getModdate());

		boolean isNew = dto.getTaskcategoryId() == null || dto.getTaskcategoryId() == 0;

		String action = isNew ? "Task Category Added" : "Task Category Updated";

		commonFunction.createHistoryAccess(dto.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, 5, saved.getTaskcategoryId(), -1);

		return new ApiResponse<>(true, "Task Category saved successfully", responseDto);
	}

	public ApiResponse<TaskCategoryDTO> getById(Integer taskcategoryId) {

		TaskCategoryEntity entity = taskCategoryRepository.findById(taskcategoryId)
				.orElseThrow(() -> new RuntimeException("Task Category Record not found"));

		TaskCategoryDTO dto = new TaskCategoryDTO();

		dto.setTaskcategoryId(entity.getTaskcategoryId());
		dto.setDepartmentId(entity.getDepartmentId());
		dto.setName(entity.getName());
		dto.setStatus(entity.getStatus());
		dto.setUserId(entity.getUserId());
		dto.setRegdate(entity.getRegdate());
		dto.setModdate(entity.getModdate());
		dto.setDueDateTime(entity.getDuedatetime());
		/*
		 * Get Department Name
		 */
		if (entity.getDepartmentId() != null && entity.getDepartmentId() > 0) {

			departmentRepository.findById(entity.getDepartmentId()).ifPresent(department -> {

				dto.setDepartmentName(department.getName());

			});
		}

		/*
		 * Transaction History
		 */
		List<TransactionEntity> history = commonFunction.getTransactionLogs(5, taskcategoryId);

		if (history != null && !history.isEmpty()) {
			dto.setTransactionHistory(history);
		}

		return new ApiResponse<>(true, "Task Category fetched successfully", dto);
	}

	public Page<TaskCategoryDTO> findTaskCategoryDetails(int page, int size, int statusIndex, String search,
			int departmentId) {

		return taskCategoryRepository.findTaskCategoryDetails(PageRequest.of(page, size), statusIndex, search,
				departmentId);
	}

	public ResponseEntity<ApiResponse<String>> deleteTaskCategory(Integer taskcategoryId, Integer userId,
			HttpServletRequest httpRequest) {

		Integer taskCategoryExist = taskCategoryRepository.existsByTaskCategoryId(taskcategoryId);
		if (taskCategoryExist != null && taskCategoryExist > 0) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(false,
					"Task Category is assigned to one or more task and cannot be deleted", null));
		}
		
		Integer userUsageCount = userLoginRepository.countActiveUsersUsingTaskCategory(taskcategoryId);
		if (userUsageCount != null && userUsageCount > 0) {
		    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(false,
		            "Task Category is assigned to one or more active users and cannot be deleted", null));
		}

		Optional<TaskCategoryEntity> existing = taskCategoryRepository.findById(taskcategoryId);

		if (!existing.isPresent()) {

			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse<>(false, "Task Category not found", null));
		}

		int updatedRows = taskCategoryRepository.softDelete(3, taskcategoryId);

		if (updatedRows > 0) {

			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Task Category Deleted", 5, taskcategoryId, -1);

			return ResponseEntity.ok(new ApiResponse<>(true, "Task Category deleted successfully", null));
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse<>(false, "Failed to delete Task Category", null));
	}

	public List<TaskCategoryDTO> getActiveTaskCategories() {

		List<TaskCategoryEntity> list = taskCategoryRepository.findByStatus(1);

		return list.stream().map(t -> new TaskCategoryDTO(t.getTaskcategoryId(), t.getName()))
				.collect(Collectors.toList());
	}
	
//	public List<TaskCategoryDTO> getActiveTaskCategoriesForRecurring(Integer userId,String isAdmin,String loginType) {
//		List<TaskCategoryEntity> list = taskCategoryRepository.findByStatus(1);
//		return list.stream().map(t -> new TaskCategoryDTO(t.getTaskcategoryId(), t.getName()))
//		.collect(Collectors.toList());
//	}
	
	public List<TaskCategoryDTO> getActiveTaskCategoriesForRecurring(
	        Integer userId,
	        String isAdmin,
	        String loginType) {

	    // Get all active categories
	    List<TaskCategoryEntity> list =
	            taskCategoryRepository.findByStatus(1);

	    /*
	     * ADMIN
	     * -----
	     * Admin can see all active task categories.
	     */
	    if ("Y".equalsIgnoreCase(isAdmin)) {

	        return list.stream()
	                .map(t -> new TaskCategoryDTO(
	                        t.getTaskcategoryId(),
	                        t.getName()))
	                .collect(Collectors.toList());
	    }

	    /*
	     * MANAGER / EMPLOYEE
	     * ------------------
	     * Get category IDs assigned to this user
	     * from t_userlogin.s_taskcategoryIds
	     */
	    String taskcategoryIds =
	            userLoginRepository.findTaskcategoryIdsByUserId(userId);

	    if (taskcategoryIds == null || taskcategoryIds.trim().isEmpty()) {
	        return Collections.emptyList();
	    }

	    /*
	     * Convert:
	     *
	     * "1,3,5,8"
	     *
	     * into:
	     *
	     * Set<Integer> = [1,3,5,8]
	     */
	    Set<Integer> allowedCategoryIds = Arrays.stream(
	                    taskcategoryIds.split(","))
	            .map(String::trim)
	            .filter(value -> !value.isEmpty())
	            .map(value -> {
	                try {
	                    return Integer.valueOf(value);
	                } catch (NumberFormatException e) {
	                    return null;
	                }
	            })
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    /*
	     * Return only active categories assigned
	     * to this manager/employee.
	     */
	    return list.stream()
	            .filter(t -> allowedCategoryIds.contains(
	                    t.getTaskcategoryId()))
	            .map(t -> new TaskCategoryDTO(
	                    t.getTaskcategoryId(),
	                    t.getName()))
	            .collect(Collectors.toList());
	}

	public List<TaskCategoryDTO> getCategoriesByDepartmentId(Integer departmentId) {
		return taskCategoryRepository.findCategoriesByDepartmentId(departmentId);
	}
}
