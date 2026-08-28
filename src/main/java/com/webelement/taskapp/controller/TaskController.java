package com.webelement.taskapp.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.TaskRequestDTO;
import com.webelement.taskapp.dto.UpdateTaskStatusDTO;
import com.webelement.taskapp.entity.TaskEntity;
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
	
	private final ObjectMapper objectMapper;
	
	
	@Autowired
	private ClientRepository clientRepository;
	
	@Autowired
	private TaskCategoryRepository taskCategoryRepository;
	
	@Autowired
	private UserLoginRepository userLoginRepository;
	
	@PostMapping("/deleteTask")
	public ResponseEntity<ResponseApi<String>> deleteTask(
	        @RequestParam int taskId,
	        @RequestParam int createdBy,
	        HttpServletRequest httpRequest) throws Exception {

	    return taskService.deleteTask(taskId, createdBy, httpRequest);
	}

	
	@GetMapping("/getTaskDetailsById")
	public ResponseEntity<?> getTaskDetailsById(
	        @RequestParam Integer taskId) {

	    Map<String, Object> response =
	            new HashMap<>();

	    try {

	        TaskEditDTO task =
	                taskService.getTaskById(taskId);

	        response.put(
	                "success",
	                true
	        );

	        response.put(
	                "message",
	                "Task details fetched successfully"
	        );

	        response.put(
	                "data",
	                task
	        );

	        return ResponseEntity.ok(response);

	    } catch (Exception e) {

	        e.printStackTrace();

	        response.put(
	                "success",
	                false
	        );

	        response.put(
	                "message",
	                e.getMessage()
	        );

	        response.put(
	                "data",
	                null
	        );

	        return ResponseEntity
	                .status(HttpStatus.BAD_REQUEST)
	                .body(response);
	    }
	}

	@GetMapping("/getTaskDetails")
	public Map<String, Object> findTaskDetails(
	        @RequestParam int page,
	        @RequestParam int size,@RequestParam int statusIndex ,@RequestParam String search,
	        @RequestParam(required = false, defaultValue = "0") Integer clientId,

	        @RequestParam(required = false, defaultValue = "0") Integer taskCategoryId,

	        @RequestParam(required = false, defaultValue = "0") Integer assignedTo,

	        @RequestParam(required = false, defaultValue = "0") Integer priority,
	        @RequestParam(required = false) String fromDate,
	        @RequestParam(required = false) String toDate
	        ) {

	    Page<TaskDetailsDTO> pageData =
	            taskService.findTaskDetails(page, size,statusIndex,search,
	            		 clientId,

	                     taskCategoryId,

	                     assignedTo,

	                     priority,fromDate,toDate);

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
	            userLoginRepository.findActiveUsers()
	    );


	    return response;
	}

	

    @PostMapping(
            value = "/saveTask",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> saveTask(

            @RequestParam(required = false)
            Integer taskId,

            @RequestParam
            Integer clientId,

            @RequestParam
            String date,

            @RequestParam
            Integer taskCategoryId,

            @RequestParam
            String description,

            @RequestParam
            Integer assignedTo,

            @RequestParam
            Short priority,

            @RequestParam
            String title,

            @RequestParam
            Integer addedBy,

            @RequestParam(required = false)
            Short status,

            @RequestParam(
                    required = false,
                    name = "fileName1"
            )
            MultipartFile fileName1,

            @RequestParam(
                    required = false,
                    name = "fileName2"
            )
            MultipartFile fileName2) {

        Map<String, Object> response = new HashMap<>();

        try {

            // -----------------------------------------
            // CREATE / UPDATE
            // -----------------------------------------

            boolean isUpdate = taskId != null;

            // -----------------------------------------
            // DATE
            // -----------------------------------------

            LocalDate taskDate =
                    LocalDate.parse(date);

            // -----------------------------------------
            // SAVE / UPDATE
            // -----------------------------------------

            TaskEntity savedTask =
                    taskService.saveTask(
                            taskId,
                            clientId,
                            taskDate,
                            taskCategoryId,
                            description,
                            assignedTo,
                            priority,
                            title,
                            addedBy,
                            status,
                            fileName1,
                            fileName2
                    );

            // -----------------------------------------
            // RESPONSE
            // -----------------------------------------

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    isUpdate
                            ? "Task updated successfully"
                            : "Task created successfully"
            );

            response.put(
                    "data",
                    savedTask
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    e.getMessage()
            );

            response.put(
                    "data",
                    null
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    
    @PostMapping(value = "/update_task_status",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateTaskStatus(@ModelAttribute UpdateTaskStatusDTO dto) {
        Map<String, Object> response = new HashMap<>();
        
        System.err.println("{ file 1 }"+dto.getFileName1());
        System.err.print("{file 2 }"+dto.getFileName2());
        try {
            TaskEntity updatedTask =taskService.updateTaskStatus(dto);
            response.put("success",true);
            response.put("message","Task status updated successfully");
            response.put("data",updatedTask);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            
            System.out.println("errrrrrrrrrrrrrrr "+e.getMessage());
            response.put("success",false);
            response.put( "message", e.getMessage());
            response.put("data",null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
	
}
