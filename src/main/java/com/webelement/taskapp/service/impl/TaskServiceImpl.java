package com.webelement.taskapp.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.controller.TaskMailService;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.UpdateTaskStatusDTO;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.StateRepo;
import com.webelement.taskapp.repo.TaskRepository;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.TaskService;

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
	
	@Autowired
	private UserLoginRepository userLoginRepository; // adjust to your actual repo name


	@Value("${task.upload-dir}")
	private String uploadDir;

	@Override
	public ResponseEntity<ApiResponse<?>> updateTaskAssignedUser(Integer taskId, Integer assignedTo, Integer userId,String remark) {
		Optional<TaskEntity> optionalTask = taskRepository.findById(taskId);
		
		if (optionalTask.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Task not found", null));
		}
		TaskEntity task = optionalTask.get();
		Integer oldAssigneeId = task.getAssignedTo();
		String actorName = resolveUserName(userId);
		// Update only assigned user
		task.setAssignedTo(assignedTo);
//		task.setAddedBy(userId);
		// Only set addedBy if it isn't already populated
	    if (task.getAddedBy() == null || task.getAddedBy() == 0) {
	        task.setAddedBy(userId);
	    }
		task.setTaskStatus((short) 1);
		task.setDate(LocalDateTime.now());
		task.setModificationDate(LocalDateTime.now());
		TaskEntity savedTask = taskRepository.save(task);
		String assignedUserName = "Unknown";
		if (savedTask.getAssignedTo() != null) {
			assignedUserName = userLoginRepository.findById(savedTask.getAssignedTo()).map(UserLoginEntity::getFirstName).orElse("Unknown");
		}
		String action = "Task reassigned to " + assignedUserName + (remark != null && !remark.trim().isEmpty() ? ". Remarks: " + remark.trim() : "");
		commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),commonFunction.getLocalIp(), action, 10, savedTask.getTaskId(), -1);
		try {
			taskMailService.sendTaskReassignMail(savedTask, oldAssigneeId);
		} catch (Exception e) {
			logger.debug("{} ERROR FOUND " ,e.getMessage());
			e.printStackTrace();
		}
		return ResponseEntity.ok(new ApiResponse<>(true, "User assigned successfully", null));
	}

	public Page<TaskDetailsDTO> findTaskDetails(int page, int size, int statusIndex, String search, Integer clientId,
			Integer taskCategoryId, Integer assignedTo, Integer priority, String fromDate, String toDate,
			String isAdmin, Integer userId, LinkedHashSet<Short> taskStatusIds, String loginType,
			String dashboardFilter) {
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
				startOfToday, startOfTomorrow, startOfWeek, endOfWeek, currentTime);

	}

	@Transactional
	@Override
	public TaskEntity saveTask(Integer taskId, Integer clientId, LocalDateTime date, Integer taskCategoryId,
			String description, Integer assignedTo, Short priority, String title, Integer addedBy, Short status,
			MultipartFile pdfFile, MultipartFile zipFile) throws Exception {
		boolean isUpdate = taskId != null;
		if (title != null) {
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
			// ID will be generated by database if AUTO_INCREMENT
			task.setRegistrationDate(LocalDateTime.now());
			// Default status for CREATE
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
		if (isUpdate) {
			if (status != null) {
				task.setStatus(status);
			}
			task.setModificationDate(LocalDateTime.now());
		}
		if (pdfFile != null && !pdfFile.isEmpty()) {
			validatePdf(pdfFile);
			String fileName = saveFile(pdfFile, "pdf");
			task.setFileName1(fileName);
		}
		if (zipFile != null && !zipFile.isEmpty()) {
			validateZip(zipFile);
			String fileName = saveFile(zipFile, "zip");
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
		commonFunction.createHistoryAccess(addedBy, commonFunction.resolveClientIp(httpRequest),commonFunction.getLocalIp(), action, 10, savedTask.getTaskId(), -1);
		if(!isUpdate) {
		try {
			taskMailService.sendTaskAssignedMail(savedTask);
		} catch (Exception e) {
			logger.debug("{} ERROR FOUND " ,e.getMessage());
			e.printStackTrace();
		}
		}
		return savedTask;
	}

	private void validatePdf(MultipartFile file) throws IOException {
		String contentType = file.getContentType();

		if (!"application/pdf".equalsIgnoreCase(contentType)) {
			throw new FileValidationException("Invalid PDF file");
		}

		byte[] header = new byte[4];
		file.getInputStream().read(header);

		String magic = new String(header);
		if (!magic.startsWith("%PDF")) {
			throw new FileValidationException("Corrupted PDF file");
		}
	}

	private void validateZip(MultipartFile file) throws IOException {

		byte[] header = new byte[4];
		file.getInputStream().read(header);

		if (header[0] != 'P' || header[1] != 'K') {
			throw new FileValidationException("Invalid ZIP file");
		}
	}

	// =========================================================
	// SAVE FILE
	// =========================================================

	private String saveFileNew(MultipartFile file, String type, String statusPrefix) throws IOException {

		Path directory = Paths.get(uploadDir, type);

		Files.createDirectories(directory);

		String originalName = file.getOriginalFilename();

		String extension = "";
		String fileNameWithoutExtension = "file";

		if (originalName != null && !originalName.trim().isEmpty()) {

			int index = originalName.lastIndexOf(".");

			if (index >= 0) {
				extension = originalName.substring(index);
				fileNameWithoutExtension = originalName.substring(0, index);
			} else {
				fileNameWithoutExtension = originalName;
			}
		}

		// Clean original file name
		fileNameWithoutExtension = fileNameWithoutExtension.replaceAll("[^a-zA-Z0-9_-]", "_");

		String newFileName = statusPrefix + "_" + System.currentTimeMillis() + "_" + fileNameWithoutExtension
				+ extension;

		Path target = directory.resolve(newFileName);

		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

		return newFileName;
	}

	private String saveFile(MultipartFile file, String type) throws IOException {

		Path directory = Paths.get(uploadDir, type);

		Files.createDirectories(directory);

		String originalName = file.getOriginalFilename();

		String extension = "";

		if (originalName != null) {

			int index = originalName.lastIndexOf(".");

			if (index >= 0) {
				extension = originalName.substring(index);
			}
		}

		String newFileName = UUID.randomUUID().toString() + extension;

		Path target = directory.resolve(newFileName);

		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

		return newFileName;
	}

	public TaskEditDTO getTaskById(Integer taskId) {
		List<TransactionEntity> history = commonFunction.getTransactionLogs(10, taskId);
		TaskEditDTO taskDto = taskRepository.findTaskById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
		taskDto.setTransactionHistory(history);
		return taskDto;
	}

	public ResponseEntity<ResponseApi<String>> deleteTask(int taskId, int createdBy, HttpServletRequest httpRequest) {

		Optional<TaskEntity> existingTask = taskRepository.findById(taskId);

		if (!existingTask.isPresent()) {

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseApi<>(false, "Task not found", null));
		}
		// 3 = Deleted
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

	    // Convert status name into filename format
	    String statusPrefix = newStatus.trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "").toUpperCase();

	    // Build the action message per scenario
	    String actionMessage = buildActionMessage(dto, currentStatus, nextStatus, newStatus, isReopen);

	    task.setTaskStatus(nextStatus);
	    task.setCloseRemarks(dto.getDescription());
	    task.setModificationDate(LocalDateTime.now());

	    // PDF Upload
	    if (dto.getFileName3() != null && !dto.getFileName3().isEmpty()) {
	        validatePdf(dto.getFileName3());
	        String pdfFileName = saveFileNew(dto.getFileName3(), "pdf", statusPrefix);
	        task.setFileName3(pdfFileName);
	    }
	    // ZIP Upload
	    if (dto.getFileName4() != null && !dto.getFileName4().isEmpty()) {
	        validateZip(dto.getFileName4());
	        String zipFileName = saveFileNew(dto.getFileName4(), "zip", statusPrefix);
	        task.setFileName4(zipFileName);
	    }

	    TaskEntity savedTask = taskRepository.save(task);
	    commonFunction.createHistoryAccess(dto.getUserId(), commonFunction.resolveClientIp(httpRequest),
	            commonFunction.getLocalIp(), actionMessage, 10, savedTask.getTaskId(), -1);
	    logger.debug("Sending mail over here {}", savedTask.toString());
	    taskMailService.sendTaskStatusMail(savedTask, oldStatus, newStatus);
	    return savedTask;
	}

	/**
	 * Builds a human-readable action message depending on what actually happened
	 * to the task (added+assigned, added by system, closed, reopened, or a
	 * generic status change).
	 */
	private String buildActionMessage(UpdateTaskStatusDTO dto, Short currentStatus, Short nextStatus,
	        String newStatus, boolean isReopen) {
	    Integer userId = dto.getUserId();
	    boolean isSystemActor = (userId == null || userId == -1); // adjust condition to your system-user marker
	    // Task added by the system (no human actor)
	    if (isSystemActor) {
	        return "Task added by the system.";
	    }
	    String actorName = resolveUserName(userId);
	    // Reopen scenario
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

	/**
	 * Fetches the display name of the user from t_userlogin.
	 */
	private String resolveUserName(Integer userId) {
	    if (userId == null) {
	        return "Unknown";
	    }
	    return userLoginRepository.findById(userId)
	            .map(UserLoginEntity::getFirstName)
	            .orElse("Unknown");
	}
	
	


	@Override
	public boolean canDisableChangeManager(TaskEntity task, Integer userId) {
		boolean isManager = task.getManagerId() != null && task.getManagerId().equals(userId);

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
}
