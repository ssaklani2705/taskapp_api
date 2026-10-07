package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.ClientAssignmentCheckDTO;
import com.webelement.taskapp.dto.ClientDashboardDTO;
import com.webelement.taskapp.dto.TaskDashboardResponse;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.DashboardService;
import com.webelement.taskapp.service.TaskService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/dashboard")
@CrossOrigin(origins =  "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class DashboardController {
	private final DashboardService dashboardService;
	private final UserLoginRepository userLoginRepository;
	private final ClientRepository clientRepository;
	private final TaskService taskService;
	
	
	@GetMapping("/dashboard")
	public ResponseEntity<TaskDashboardResponse> getDashboard(@RequestParam(defaultValue = "0") Integer userId,
			@RequestParam(required = false) String isAdmin, @RequestParam(defaultValue = "0") Integer selectedClientId,
			@RequestParam(required = false) String isHod) {
		TaskDashboardResponse response = dashboardService.getDashboard(userId, isAdmin, selectedClientId, isHod);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/getTasksByStatus")
	public ResponseEntity<Map<String, Object>> getTasksByStatus(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false, defaultValue = "0") Integer clientId,
			@RequestParam("userId") Integer userId) {
		Map<String, Object> map = new HashMap<>();
		size = Math.min(size, 20);
		String permission = userLoginRepository.findById(userId).map(UserLoginEntity::getPermission).orElse("N");
		Page<TaskEditDTO> taskList = dashboardService.getTasksByStatus(page, size, clientId, userId, permission);
		Double totalOutstanding = dashboardService.getTotalOutstanding(clientId, userId);
		map.put("taskList", taskList.getContent());
		map.put("totalTasks", taskList.getTotalElements());
		map.put("totalOutstanding", totalOutstanding);
		return ResponseEntity.ok(map);
	}

	@GetMapping("/countOfActiveTask")
	public ResponseEntity<Map<String, Object>> countOfActiveTask(@RequestParam Integer clientId,
			@RequestParam("userId") Integer userId) {

		Map<String, Object> map = new HashMap<>();
		int count = dashboardService.countOfActiveTask(clientId, userId);
		map.put("count", count);
		return ResponseEntity.ok(map);
	}

	@GetMapping("/countOfCompletedTask")
	public ResponseEntity<Map<String, Object>> countOfCompletedTask(@RequestParam Integer clientId,
			@RequestParam("userId") Integer userId) {

		Map<String, Object> map = new HashMap<>();

		int count = dashboardService.countOfCompletedTask(clientId, userId);

		map.put("count", count);

		return ResponseEntity.ok(map);
	}

	@GetMapping("/countOfPendingTask")
	public ResponseEntity<Map<String, Object>> countOfPendingTask(@RequestParam Integer clientId,
			@RequestParam("userId") Integer userId) {

		Map<String, Object> map = new HashMap<>();

		int count = dashboardService.countOfPendingTask(clientId, userId);

		map.put("count", count);

		return ResponseEntity.ok(map);
	}

	@GetMapping("/countOfAssignedTask")
	public ResponseEntity<Map<String, Object>> countOfAssignedTask(@RequestParam Integer clientId,
			@RequestParam("userId") Integer userId) {

		Map<String, Object> map = new HashMap<>();

		int count = dashboardService.countOfAssignedTask(clientId, userId);

		map.put("count", count);

		return ResponseEntity.ok(map);
	}

	@GetMapping("/countOfAssigneeClosureTask")
	public ResponseEntity<Map<String, Object>> countOfAssigneeClosureTask(@RequestParam Integer clientId,
			@RequestParam("userId") Integer userId) {

		Map<String, Object> map = new HashMap<>();

		int count = dashboardService.countOfAssigneeClosureTask(clientId, userId);

		map.put("count", count);

		return ResponseEntity.ok(map);
	}

	@GetMapping("/countOfReOpenTask")
	public ResponseEntity<Map<String, Object>> countOfReOpenTask(@RequestParam Integer clientId,
			@RequestParam("userId") Integer userId) {

		Map<String, Object> map = new HashMap<>();

		int count = dashboardService.countOfReOpenTask(clientId, userId);

		map.put("count", count);

		return ResponseEntity.ok(map);
	}

	@GetMapping("/countOfAssigneeReClosureTask")
	public ResponseEntity<Map<String, Object>> countOfAssigneeReClosureTask(@RequestParam Integer clientId,
			@RequestParam("userId") Integer userId) {

		Map<String, Object> map = new HashMap<>();

		int count = dashboardService.countOfAssigneeReClosureTask(clientId, userId);

        return ResponseEntity.ok(map);
    }
    
    @GetMapping("/countOfUnAssigneeTask")
    public ResponseEntity<Map<String, Object>> countOfUnAssigneeTask(@RequestParam Integer clientId,
            @RequestParam("userId") Integer userId) {
        Map<String, Object> map = new HashMap<>();
        int count = dashboardService.countOfUnAssigneeTask(clientId, userId);
        map.put("count", count);
        return ResponseEntity.ok(map);
    }
    

	@GetMapping("/getTaskClient")
	public Map<String, Object> getTaskClient(@RequestParam("userId") Integer userId) {

		Map<String, Object> response = new HashMap<>();

		String permission = userLoginRepository.findById(userId).map(UserLoginEntity::getPermission).orElse("N");

		List<ClientEntity> clients;

		if ("Y".equalsIgnoreCase(permission)) {
			// Admin -> all active societies
			clients = clientRepository.findAllActiveClients();
		} else {
			// Society Manager -> only societies managed by logged-in user
			clients = clientRepository.findAllActiveClientsByManagerId(userId);
		}

		response.put("clients", clients);

		return response;
	}
	
	
	@GetMapping("/client/check-assignment/{managerId}")
	public ClientAssignmentCheckDTO checkClientAssignment(@PathVariable Integer managerId) {

		return taskService.checkClientAssigned(managerId);
	}

	@GetMapping("/dashboard-clients")
	public ResponseEntity<List<ClientDashboardDTO>> getDashboardClients(@RequestParam Integer userId,
			@RequestParam String isAdmin, @RequestParam String loginType) {

		List<ClientDashboardDTO> clients = dashboardService.findDashboardClients(userId, isAdmin, loginType);

		return ResponseEntity.ok(clients);
	}

}
