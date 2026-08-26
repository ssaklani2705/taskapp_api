package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.TaskCategoryRepository;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/task")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
@RequiredArgsConstructor
public class TaskController {
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private ClientRepository clientRepository;
	
	@Autowired
	private TaskCategoryRepository taskCategoryRepository;
	
	@Autowired
	private UserLoginRepository userLoginRepository;

	@GetMapping("/getTaskDetails")
	public Map<String, Object> findTaskDetails(
	        @RequestParam int page,
	        @RequestParam int size,@RequestParam int statusIndex ,@RequestParam String search,
	        @RequestParam(required = false, defaultValue = "0") Integer clientId,

	        @RequestParam(required = false, defaultValue = "0") Integer taskCategoryId,

	        @RequestParam(required = false, defaultValue = "0") Integer assignedTo,

	        @RequestParam(required = false, defaultValue = "0") Integer priority
	        ) {

	    Page<TaskDetailsDTO> pageData =
	            taskService.findTaskDetails(page, size,statusIndex,search,
	            		 clientId,

	                     taskCategoryId,

	                     assignedTo,

	                     priority);

	    Map<String, Object> response =
	            new HashMap<>();

	    response.put(
	            "data",
	            pageData.getContent());

	    response.put(
	            "totalElements",
	            pageData.getTotalElements());

	    return response;
	}
	
	@GetMapping("/getTaskFilterData")
	public Map<String, Object> getTaskFilterData() {

	    Map<String, Object> response =
	            new HashMap<>();


	    response.put(
	            "clients",
	            clientRepository.findAllActiveClients()
	    );


	    response.put(
	            "taskCategories",
	            taskCategoryRepository.findAllActiveTaskCategories()
	    );


	    response.put(
	            "assignedUsers",
	            userLoginRepository.findAllActiveUsers()
	    );


	    return response;
	}


	
}
