package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.TaskDashboardResponse;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.service.DashboardService;
import com.webelement.taskapp.service.TaskService;

@RestController
@RequestMapping("/admin/dashboard")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
public class DashboardController {
	
	@Autowired
	private DashboardService taskService;
	
//	@Autowired
//	private TaskService taskService;
	
	// For Employee dashboard
	 @GetMapping("/dashboard")
	    public ResponseEntity<TaskDashboardResponse> getDashboard(
	            @RequestParam(defaultValue = "0") Integer userId) {

	        TaskDashboardResponse response =
	        		taskService.getDashboard(userId);

	        return ResponseEntity.ok(response);
	    }
	
	// For manager dashboard
//    @GetMapping("/getTasksByStatus")
//    public ResponseEntity<Map<String, Object>> getTasksByStatus() {
//
//        Map<String, Object> map = new HashMap<>();
//
//        List<TaskEditDTO> taskList = taskService.getTasksByStatus();
//
//        map.put("taskList", taskList);
//
//        return ResponseEntity.ok(map);
//    }


    //NEW

@GetMapping("/getTasksByStatus")
    public ResponseEntity<Map<String, Object>> getTasksByStatus(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> map = new HashMap<>();
        size = Math.min(size, 20);

        Page<TaskEditDTO> taskList = taskService.getTasksByStatus(page, size);

        map.put("taskList", taskList.getContent());
        map.put("totalTasks", taskList.getTotalElements());

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfActiveTask")
    public ResponseEntity<Map<String, Object>> countOfActiveTask() {

        Map<String, Object> map = new HashMap<>();

        int count = taskService.countOfActiveTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfCompletedTask")
    public ResponseEntity<Map<String, Object>> countOfCompletedTask() {

        Map<String, Object> map = new HashMap<>();

        int count = taskService.countOfCompletedTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfPendingTask")
    public ResponseEntity<Map<String, Object>> countOfPendingTask() {

        Map<String, Object> map = new HashMap<>();

        int count = taskService.countOfPendingTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfAssignedTask")
    public ResponseEntity<Map<String, Object>> countOfAssignedTask() {

        Map<String, Object> map = new HashMap<>();

        int count = taskService.countOfAssignedTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfAssigneeClosureTask")
    public ResponseEntity<Map<String, Object>> countOfAssigneeClosureTask() {

        Map<String, Object> map = new HashMap<>();

        int count = taskService.countOfAssigneeClosureTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfReOpenTask")
    public ResponseEntity<Map<String, Object>> countOfReOpenTask() {

        Map<String, Object> map = new HashMap<>();

        int count = taskService.countOfReOpenTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfAssigneeReClosureTask")
    public ResponseEntity<Map<String, Object>> countOfAssigneeReClosureTask() {

        Map<String, Object> map = new HashMap<>();

        int count = taskService.countOfAssigneeReClosureTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }
}
