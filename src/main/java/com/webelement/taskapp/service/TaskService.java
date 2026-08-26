package com.webelement.taskapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.repo.TaskRepository;

@Service
public class TaskService {
	
	@Autowired
	private TaskRepository taskRepository;
	
	public Page<TaskDetailsDTO> findTaskDetails(
	        int page,
	        int size,
	        int statusIndex,
	        String search,
	        Integer clientId,

	        Integer taskCategoryId,

	        Integer assignedTo,

	        Integer priority) {

	    return taskRepository.findTaskDetails(
	            PageRequest.of(page, size),
	            statusIndex,
	            search,

	            clientId,

	            taskCategoryId,

	            assignedTo,

	            priority
	            
	            
	    );
	}

}
