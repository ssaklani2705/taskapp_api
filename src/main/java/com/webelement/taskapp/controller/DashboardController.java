package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
	private DashboardService dashboardService;
	
	// For Employee dashboard
	 @GetMapping("/dashboard")
	    public ResponseEntity<TaskDashboardResponse> getDashboard(
	            @RequestParam Integer userId) {

	        TaskDashboardResponse response =
	        		dashboardService.getDashboard(userId);

	        return ResponseEntity.ok(response);
	    }
	
	// For manager dashboard
    @GetMapping("/getTasksByStatus")
    public ResponseEntity<Map<String, Object>> getTasksByStatus() {

        Map<String, Object> map = new HashMap<>();

        List<TaskEditDTO> taskList = dashboardService.getTasksByStatus();

        map.put("taskList", taskList);

        return ResponseEntity.ok(map);
    }

    @GetMapping("/countOfActiveTask")
    public ResponseEntity<Map<String, Object>> countOfActiveTask() {

        Map<String, Object> map = new HashMap<>();

        int count = dashboardService.countOfActiveTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }
    
    @GetMapping("/countOfCompletedTask")
    public ResponseEntity<Map<String, Object>> countOfCompletedTask() {

        Map<String, Object> map = new HashMap<>();

        int count = dashboardService.countOfCompletedTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }
    
    @GetMapping("/countOfPendingTask")
    public ResponseEntity<Map<String, Object>> countOfPendingTask() {

        Map<String, Object> map = new HashMap<>();

        int count = dashboardService.countOfPendingTask();

        map.put("count", count);

        return ResponseEntity.ok(map);
    }

}
