package com.webelement.taskapp.service;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.webelement.taskapp.dto.UpdateTaskStatusDTO;
import com.webelement.taskapp.entity.TaskEntity;
 
public interface TaskService {
	 TaskEntity updateTaskStatus(UpdateTaskStatusDTO request) throws Exception;
		public TaskEntity saveTask(Integer taskId, Integer clientId, LocalDate date, Integer taskCategoryId,
				String description, Integer assignedTo, Short priority, String title, Integer addedBy, Short status,
				MultipartFile pdfFile, MultipartFile zipFile) throws Exception;
}
