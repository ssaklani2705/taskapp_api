package com.webelement.taskapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ClientAssignmentCheckDTO;
import com.webelement.taskapp.dto.DepartmentFilterProjection;
import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.TaskRatingSummaryProjection;
import com.webelement.taskapp.dto.UpdateTaskStatusDTO;
import com.webelement.taskapp.entity.TaskEntity;

public interface TaskService {
	
	 long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
	 Set<String> ALLOWED_EXTENSIONS_DOC_ONLY = Set.of("pdf", "xls", "xlsx", "doc", "docx");
	 Set<String> ALLOWED_EXTENSIONS_DOC_OR_ZIP = Set.of("pdf", "xls", "xlsx", "doc", "docx", "zip");
	 DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	 short ACTIVE_STATUS = 1;
	 Random random = new Random();

	ResponseEntity<ApiResponse<?>> updateTaskAssignedUser(Integer taskId, Integer assignedTo, Integer userId,
			String remark, LocalDateTime date, LocalDateTime endDate,Integer buddyId);

	public TaskEntity updateTaskStatus(UpdateTaskStatusDTO request) throws Exception;

	public TaskEntity saveTask(Integer taskId,Integer userType, Integer clientId, LocalDateTime date, Integer taskCategoryId,
			String description, Integer assignedTo, Short priority, String title, Integer addedBy, Short status,
			MultipartFile pdfFile, MultipartFile zipFile, LocalDateTime endDate, Integer buddyId) throws Exception;

	public boolean canDisableChangeManager(TaskEntity task, Integer userId);

	public ClientAssignmentCheckDTO checkClientAssigned(Integer managerId);

	List<Map<String, Object>> getAssigneeWorkload(Integer categoryId, String startDateTime, String endDateTime, Integer excludeUserId);
//	public List<Map<String, Object>> getAssigneeWorkload(Integer categoryId, String startDateTime, String endDateTime);
	public ResponseEntity<ResponseApi<String>> deleteTask(int taskId, int createdBy);

	public Page<TaskDetailsDTO> findTaskDetails(int page, int size, int statusIndex, String search, Integer clientId,
			Integer taskCategoryId, Integer assignedTo, Integer priority, String fromDate, String toDate,
			String isAdmin, Integer userId, LinkedHashSet<Short> taskStatusIds, String loginType,
			String dashboardFilter, String isHod, boolean ignoreTaskStatusFilter,Integer taskId);
	
	public TaskEditDTO getTaskById(Integer taskId);

	 Page<TaskRatingSummaryProjection> getTaskRatingSummary(LocalDate fromDate, LocalDate toDate, String username,
	            List<Integer> departmentIds, int page, int size);
	    List<DepartmentFilterProjection> getRatingReportDepartments(String username);

	    ResponseEntity<ApiResponse<?>> updateTaskBuddy(Integer taskId, Integer buddyId, Integer userId,
	            String remark, LocalDateTime date, LocalDateTime endDate);
	
}
