package com.webelement.taskapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.UpdateTaskStatusDTO;
import com.webelement.taskapp.entity.TaskEntity;

public interface TaskService {
	
	ResponseEntity<ApiResponse<?>> updateTaskAssignedUser(
	        Integer taskId,
	        Integer assignedTo,Integer userId);

	TaskEntity updateTaskStatus(UpdateTaskStatusDTO request) throws Exception;

	public TaskEntity saveTask(Integer taskId, Integer clientId, LocalDateTime date, Integer taskCategoryId,
			String description, Integer assignedTo, Short priority, String title, Integer addedBy, Short status,
			MultipartFile pdfFile, MultipartFile zipFile) throws Exception;


			public boolean canDisableChangeManager(TaskEntity task, Integer userId);

}
