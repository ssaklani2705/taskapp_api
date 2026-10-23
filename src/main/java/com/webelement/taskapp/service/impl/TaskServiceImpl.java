package com.webelement.taskapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.webelement.taskapp.Exceptions.DuplicateTaskTitleException;
import com.webelement.taskapp.Exceptions.FileValidationException;
import com.webelement.taskapp.Exceptions.InvalidRequestException;
import com.webelement.taskapp.Exceptions.WorkloadFetchException;
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.controller.TaskMailService;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ClientAssignmentCheckDTO;
import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.UpdateTaskStatusDTO;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.HolidayRepo;
import com.webelement.taskapp.repo.TaskRepository;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.TaskService;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

	static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
	private final TaskRepository taskRepository;
	private final CommonFunction commonFunction;
	private final HttpServletRequest httpRequest;
	private final TaskMailService taskMailService;
	private final ClientRepository clientRepository;
	private final HolidayRepo holidayRepository;
	private final UserLoginRepository userLoginRepository;
	
	
	@Value("${task.upload-dir}")
	private String uploadDir;


	public ClientAssignmentCheckDTO checkClientAssigned(Integer managerId) {
		boolean hasActiveClient = clientRepository.existsByManagerIdAndStatus(managerId, ACTIVE_STATUS);
		if (!hasActiveClient) {
			return new ClientAssignmentCheckDTO(false,
					"No client is currently assigned. Please assign a client to proceed.");
		}
		return new ClientAssignmentCheckDTO(true, "");
	}

	@Override
	public ResponseEntity<ApiResponse<?>> updateTaskAssignedUser(Integer taskId, Integer assignedTo, Integer userId,
			String remark, LocalDateTime date, LocalDateTime endDate) {
		Optional<TaskEntity> optionalTask = taskRepository.findById(taskId);
		if (optionalTask.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Task not found", null));
		}
		if (date == null) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Start Date & Time is required", null));
		}
		if (endDate == null) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(false, "End Date & Time is required", null));
		}
		if (endDate.isBefore(date)) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(false, "End Date & Time cannot be before Start Date & Time", null));
		}
		TaskEntity task = optionalTask.get();
		Integer oldAssigneeId = task.getAssignedTo();
		task.setAssignedTo(assignedTo);
		if (task.getAddedBy() == null || task.getAddedBy() == 0) {
			task.setAddedBy(userId);
		}
		task.setTaskStatus((short) 1);
		task.setDate(date);
		task.setEndDate(endDate);
		task.setModificationDate(LocalDateTime.now());
		task.setCloseRemarks(remark);
		TaskEntity savedTask = taskRepository.save(task);
		String assignedUserName = "Unknown";
		if (savedTask.getAssignedTo() != null) {
			assignedUserName = userLoginRepository.findById(savedTask.getAssignedTo())
					.map(UserLoginEntity::getFirstName).orElse("Unknown");
		}
		String action = "Task reassigned to " + assignedUserName
				+ (remark != null && !remark.trim().isEmpty() ? ". Remarks: " + remark.trim() : "");
		commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, 10, savedTask.getTaskId(), -1);
		try {
			taskMailService.sendTaskReassignMail(savedTask, oldAssigneeId);
		} catch (Exception e) {
			logger.debug("{} ERROR FOUND ", e.getMessage());
			e.printStackTrace();
		}
		return ResponseEntity.ok(new ApiResponse<>(true, "User assigned successfully", null));
	}
	
	@Override
	public Page<TaskDetailsDTO> findTaskDetails(int page, int size, int statusIndex, String search, Integer clientId,
			Integer taskCategoryId, Integer assignedTo, Integer priority, String fromDate, String toDate,
			String isAdmin, Integer userId, LinkedHashSet<Short> taskStatusIds, String loginType,
			String dashboardFilter, String isHod, boolean ignoreTaskStatusFilter) {
		LinkedHashSet<Integer> statusIdsParam = taskStatusIds.stream().map(Short::intValue)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		LocalDate today = LocalDate.now();
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime startOfToday = today.atStartOfDay();
		LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
		LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
		LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
		return taskRepository.findTaskDetails(PageRequest.of(page, size), statusIndex, search, clientId, taskCategoryId,
				assignedTo, priority, fromDate, toDate, isAdmin, userId, statusIdsParam, loginType, dashboardFilter,
				startOfToday, startOfTomorrow, startOfWeek, endOfWeek, currentTime, isHod);

	}

	@Transactional
	@Override
	public TaskEntity saveTask(Integer taskId,Integer userType, Integer clientId, LocalDateTime date, Integer taskCategoryId,
			String description, Integer assignedTo, Short priority, String title, Integer addedBy, Short status,
			MultipartFile pdfFile, MultipartFile zipFile, LocalDateTime endDate) throws Exception {
		boolean isUpdate = taskId != null;

		if (title != null && clientId != null) {
			String trimmedTitle = title.trim();
			boolean isDuplicate = isUpdate
					? taskRepository.existsByTitleIgnoreCaseAndClientIdAndTaskIdNot(trimmedTitle, clientId, taskId)
					: taskRepository.existsByTitleIgnoreCaseAndClientId(trimmedTitle, clientId);
			if (isDuplicate) {
				throw new DuplicateTaskTitleException(
						"A task with the title '" + trimmedTitle + "' already exists for this client");
			}
		}

		TaskEntity task;
		if (isUpdate) {
			task = taskRepository.findById(taskId)
					.orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
		} else {
			task = new TaskEntity();
			task.setRegistrationDate(LocalDateTime.now());
			task.setStatus((short) 1);
			task.setAddedBy(addedBy);
			task.setTaskStatus((short) 1);
		}
		task.setTitle(title);
		task.setClientId(clientId);
		task.setDate(date);
		task.setTaskCategoryId(taskCategoryId);
		task.setDescription(description);
		task.setAssignedTo(assignedTo);
		task.setPriority(priority);
		task.setEndDate(endDate);
		task.setUserType(userType);
		if (isUpdate) {
			if (status != null) {
				task.setStatus(status);
			}
			task.setModificationDate(LocalDateTime.now());
		}

		if (pdfFile != null && !pdfFile.isEmpty()) {
			commonFunction.validateFile(pdfFile, ALLOWED_EXTENSIONS_DOC_ONLY);
			String fileName = commonFunction.saveFile(pdfFile, getExtension(pdfFile));
			task.setFileName1(fileName);
		}
		if (zipFile != null && !zipFile.isEmpty()) {
			commonFunction.validateFile(zipFile, ALLOWED_EXTENSIONS_DOC_OR_ZIP);
			String fileName = commonFunction.saveFile(zipFile, getExtension(zipFile));
			task.setFileName2(fileName);
		}
		TaskEntity savedTask = taskRepository.save(task);
		String action;
		if (isUpdate) {
			action = "Task Updated";
		} else {
			String assignedUserName = "Unknown";
			if (savedTask.getAssignedTo() != null) {
				assignedUserName = userLoginRepository.findById(savedTask.getAssignedTo())
						.map(UserLoginEntity::getFirstName).orElse("Unknown");
			}
			action = "Task Added and Assigned to " + assignedUserName;
		}
		commonFunction.createHistoryAccess(addedBy, commonFunction.resolveClientIp(httpRequest),
		commonFunction.getLocalIp(), action, 10, savedTask.getTaskId(), -1);

		if (!isUpdate) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						taskMailService.sendTaskAssignedMail(savedTask);
					} catch (Exception e) {
						logger.error("Task assigned mail failed for task {}", savedTask.getTaskId(), e);
					}
				}
			});
		}
		return savedTask;
	}

	

	public TaskEditDTO getTaskById(Integer taskId) {
		List<TransactionEntity> history = commonFunction.getTransactionLogs(10, taskId);
		TaskEditDTO taskDto = taskRepository.findTaskById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
		taskDto.setTransactionHistory(history);
		return taskDto;
	}

	public ResponseEntity<ResponseApi<String>> deleteTask(int taskId, int createdBy) {
		Optional<TaskEntity> existingTask = taskRepository.findById(taskId);
		if (!existingTask.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseApi<>(false, "Task not found", null));
		}
		int updatedRows = taskRepository.deleteTask((short) 3, taskId);
		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(createdBy, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Task Deleted", 1, taskId, -1);
			return ResponseEntity.ok(new ResponseApi<>(true, "Task deleted successfully", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseApi<>(false, "Failed to delete task", null));
		}
	}

	

	@Transactional
	@Override
	public TaskEntity updateTaskStatus(UpdateTaskStatusDTO dto) throws Exception {
		TaskEntity task = taskRepository.findByIdForUpdate(dto.getTaskId())
				.orElseThrow(() -> new RuntimeException("Task not found"));
		Short currentStatus = task.getTaskStatus();
		Short nextStatus;
		boolean isReopen = false;
		if (currentStatus == 2 || currentStatus == 4) {
			Short selectedStatus = Short.valueOf(dto.getSelectedTaskStatusId());
			if (selectedStatus == 3) {
				isReopen = true;
				Integer reopenCount = task.getReopenCount() == null ? 1 : task.getReopenCount();
				if (reopenCount >= 3) {
					throw new RuntimeException("Task can only be reopened 3 times.");
				}
				task.setReopenCount(reopenCount + 1);
			}
			nextStatus = TaskConstants.REOPEN_FLOW.get(selectedStatus);
			if (nextStatus == null) {
				throw new RuntimeException("Please select a valid task status.");
			}
		} else {
			nextStatus = TaskConstants.STATUS_FLOW.get(currentStatus);
			if (nextStatus == null) {
				if (currentStatus == 5) {
					throw new RuntimeException("This task is already closed and cannot be updated.");
				}
				throw new RuntimeException("Invalid task status: " + currentStatus);
			}
		}
		String oldStatus = TaskConstants.STATUS_LABELS.getOrDefault(currentStatus, "Unknown");
		String newStatus = TaskConstants.STATUS_LABELS.getOrDefault(nextStatus, "Unknown");
		String statusPrefix = newStatus.trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "").toUpperCase();
		String actionMessage = buildActionMessage(dto, currentStatus, nextStatus, newStatus, isReopen);
		task.setTaskStatus(nextStatus);
		task.setCloseRemarks(dto.getDescription());
		task.setModificationDate(LocalDateTime.now());
		if (dto.getFileName3() != null && !dto.getFileName3().isEmpty()) {
			commonFunction.validateFile(dto.getFileName3(), ALLOWED_EXTENSIONS_DOC_ONLY);
			String fileName = commonFunction.saveFileNew(dto.getFileName3(), getExtension(dto.getFileName3()), statusPrefix);
			task.setFileName3(fileName);
		}
		if (dto.getFileName4() != null && !dto.getFileName4().isEmpty()) {
			commonFunction.validateFile(dto.getFileName4(), ALLOWED_EXTENSIONS_DOC_OR_ZIP);
			String fileName = commonFunction.saveFileNew(dto.getFileName4(), getExtension(dto.getFileName4()), statusPrefix);
			task.setFileName4(fileName);
		}
		TaskEntity savedTask = taskRepository.save(task);
		commonFunction.createHistoryAccess(dto.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), actionMessage, 10, savedTask.getTaskId(), -1);
		logger.debug("Sending mail over here {}", savedTask.toString());
		taskMailService.sendTaskStatusMail(savedTask, oldStatus, newStatus);
		return savedTask;
	}

	private String getExtension(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			return "";
		}
		return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
	}

	private String buildActionMessage(UpdateTaskStatusDTO dto, Short currentStatus, Short nextStatus, String newStatus,
			boolean isReopen) {
		Integer userId = dto.getUserId();
		boolean isSystemActor = (userId == null || userId == -1);
		if (isSystemActor) {
			return "Task added by the system.";
		}
		String actorName = resolveUserName(userId);
		if (isReopen) {
			return "Task reopened by " + actorName + ".";
		}
		// Closed scenario
		if (nextStatus == 5) {
			return "Task closed by " + actorName + ".";
		}
		// Added and assigned scenario
		if ("Assigned".equalsIgnoreCase(newStatus)) {
			return "Task added and assigned to " + actorName + ".";
		}

		// Fallback: generic status change — now includes actor name
		String oldStatus = TaskConstants.STATUS_LABELS.getOrDefault(currentStatus, "Unknown");
		return "Task status updated from " + oldStatus + " to " + newStatus + " by " + actorName + ".";
	}

	private String resolveUserName(Integer userId) {
		if (userId == null) {
			return "Unknown";
		}
		return userLoginRepository.findById(userId).map(UserLoginEntity::getFirstName).orElse("Unknown");
	}

	@Override
	public boolean canDisableChangeManager(TaskEntity task, Integer userId) {
		// Status 5 => closed, locked for everyone, no exceptions
		if (task.getTaskStatus() == 5) {
			return true;
		}

		boolean selfAssigned = task.getAddedBy() != null && task.getAddedBy().equals(task.getAssignedTo());

		if (selfAssigned) {
			// Any other status => self-assigned user can always change manager
			return false;
		}

		boolean addedByMatch = task.getAddedBy() != null && task.getAddedBy().equals(userId)
				&& (task.getTaskStatus() == 1 || task.getTaskStatus() == 3);

		boolean assignedToMatch = task.getAssignedTo() != null && task.getAssignedTo().equals(userId)
				&& (task.getTaskStatus() == 2 || task.getTaskStatus() == 4);

		return addedByMatch || assignedToMatch;
	}

	@Override
	public List<Map<String, Object>> getAssigneeWorkload(Integer categoryId, String startDateTime, String endDateTime) {
		if (categoryId == null || categoryId <= 0) {
			throw new InvalidRequestException("Invalid category id");
		}
		LocalDateTime start = parseStartDateTime(startDateTime);
		LocalDateTime end = parseStartDateTime(endDateTime);
		String startDate = start.toLocalDate().toString();
		String endDate = end.toLocalDate().toString();
		
		int dayOfWeek = toWeeklyOffDay(start.toLocalDate());
		Integer pickedUserId = null;
		Double pickedHours = null;
		long holidayCount = holidayRepository.countHolidayOverlap(start.toLocalDate(), end.toLocalDate());
		if (holidayCount > 0) {
			throw new InvalidRequestException("Selected date range falls on a holiday.");
		}
		try {
			List<Object[]> rows = taskRepository.findWorkloadByCategory(categoryId, dayOfWeek, start, startDate);

			if (rows != null && !rows.isEmpty()) {
				double min = Double.MAX_VALUE;
				List<Integer> candidates = new ArrayList<Integer>();
				for (Object[] row : rows) {
					int userId = ((Number) row[0]).intValue();
					double hours = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
					if (hours < min) {
						min = hours;
						candidates.clear();
						candidates.add(userId);
					} else if (hours == min) {
						candidates.add(userId);
					}
				}
				pickedUserId = candidates.get(random.nextInt(candidates.size()));
				pickedHours = min;
			} else {
				pickedUserId = taskRepository.findRandomEligibleUser(categoryId, dayOfWeek).orElse(null);
			}
		} catch (Exception e) {
			throw new WorkloadFetchException("Unable to fetch assignee workload", e);
		}

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (pickedUserId == null || pickedUserId == 0) {
			return result;
		}
		Map<String, Object> item = new LinkedHashMap<String, Object>();
		item.put("assignedTo", pickedUserId);
		item.put("hours", pickedHours);
		result.add(item);
		return result;
	}

	private LocalDateTime parseStartDateTime(String value) {
		if (value == null || value.trim().isEmpty()) {
			return LocalDateTime.now();
		}
		
		String v = value.trim();
		
		try {
			return v.length() == 10 ? LocalDate.parse(v).atStartOfDay() : LocalDateTime.parse(v, DATE_TIME_FMT);
		} catch (DateTimeParseException e) {
			throw new InvalidRequestException("Invalid start date. Use yyyy-MM-dd HH:mm:ss");
		}
	}

	private int toWeeklyOffDay(LocalDate date) {
		return (date.getDayOfWeek().getValue() % 7) + 1;
	}

	
}
