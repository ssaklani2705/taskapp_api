package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ClientDTO;
import com.webelement.taskapp.dto.RecurringDTO;
import com.webelement.taskapp.entity.RecurringEntity;
import com.webelement.taskapp.service.ClientService;
import com.webelement.taskapp.service.RecurringService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
public class RecurringController {

	@Autowired
	private RecurringService recurringService;

	@Autowired
	private ClientService clientService;

	@GetMapping("/getRecurringClients")
	public ResponseEntity<?> getRecurringClients(@RequestParam("userId") Integer userId) {
		try {
			System.out.println("getRecurringClients userId = " + userId);
			List<ClientDTO> clients = clientService.getClientsForRecurring(userId);
			System.out.println("clients found = " + clients.size());

			return ResponseEntity.ok(clients);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	// For AddOrUpdate
	@PostMapping("/addOrUpdateRecurring")
	public ResponseEntity<ApiResponse<RecurringEntity>> addOrUpdateRecurring(@RequestBody RecurringDTO dto,
			@RequestParam("userId") Integer userId, HttpServletRequest httpRequest) {

		ApiResponse<RecurringEntity> response = recurringService.addOrUpdateRecurring(dto, userId, httpRequest);

		return ResponseEntity.ok(response);

	}

	// For edit
	@GetMapping("/getRecurringById/{recurringId}")
	public ResponseEntity<ApiResponse<RecurringDTO>> getRecurringById(@PathVariable Integer recurringId,
			@RequestParam("userId") Integer userId) {

		RecurringDTO recurring = recurringService.getRecurringById(recurringId, userId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Recurring details fetched successfully", recurring));
	}

	private static final Map<String, String> RECURRING_SORT_MAP = Map.of("title", "r.title", "clientName", "c.name",
			"description", "r.description", "type", "r.type", "taskCategory", "tc.name", "status", "r.status",
			"regDate", "r.regDate");

	// For index
//	@GetMapping("/getRecurringDetails")
//	public Map<String, Object> getRecurringDetails(@RequestParam(defaultValue = "0") int page,
//			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "0") Short status,
//			@RequestParam(required = false) String title, @RequestParam(defaultValue = "title") String sortColumn,
//			@RequestParam(defaultValue = "asc") String sortDirection, @RequestParam("userId") Integer userId) {
//
//		String sortBy = RECURRING_SORT_MAP.getOrDefault(sortColumn, "r.title");
//
//		Sort.Direction direction = sortDirection.trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC
//				: Sort.Direction.ASC;
//
//		sortBy = sortBy.replace("r.", "").replace("c.", "").replace("tc.", "");
//
//		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
//
//		Page<RecurringDTO> recurringPage = recurringService.findRecurringDetails(pageable, status, title, userId);
//
//		Map<String, Object> response = new HashMap<>();
//
//		response.put("data", recurringPage.getContent());
//		response.put("totalElements", recurringPage.getTotalElements());
//		response.put("totalPages", recurringPage.getTotalPages());
//		response.put("currentPage", recurringPage.getNumber());
//		response.put("pageSize", recurringPage.getSize());
//
//		return response;
//	}

	@GetMapping("/getRecurringDetails")
	public Map<String, Object> getRecurringDetails(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "0") Short status,
			@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") Integer clientId,
			@RequestParam(defaultValue = "0") Short type, @RequestParam(defaultValue = "0") Integer taskCatId,
			@RequestParam(defaultValue = "title") String sortColumn,
			@RequestParam(defaultValue = "asc") String sortDirection, @RequestParam Integer userId) {

		String sortBy = RECURRING_SORT_MAP.getOrDefault(sortColumn, "r.title");

		Sort.Direction direction = sortDirection.trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC
				: Sort.Direction.ASC;

		sortBy = sortBy.replace("r.", "").replace("c.", "").replace("tc.", "");

		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

		Page<RecurringDTO> recurringPage = recurringService.findRecurringDetails(pageable, status, search, clientId,
				type, taskCatId, userId);

		Map<String, Object> response = new HashMap<>();

		response.put("data", recurringPage.getContent());
		response.put("totalElements", recurringPage.getTotalElements());
		response.put("totalPages", recurringPage.getTotalPages());
		response.put("currentPage", recurringPage.getNumber());
		response.put("pageSize", recurringPage.getSize());

		return response;
	}

	// For View
	@GetMapping("/recurringDetails/{recurringId}")
	public ResponseEntity<?> getRecurringDetailsById(@PathVariable Integer recurringId,
			@RequestParam("userId") Integer userId) {

		RecurringDTO recurring = recurringService.getViewRecurringById(recurringId, userId);

		return ResponseEntity.ok(new ApiResponse<>(true, "Recurring details fetched successfully", recurring));
	}

	// For Delete
	@PostMapping("/deleteRecurring")
	public ResponseEntity<ResponseApi<String>> deletdeleteRecurringeClient(@RequestParam int recurringId,
			@RequestParam int userId, HttpServletRequest httpRequest) throws Exception {
		return recurringService.deleteRecurring(recurringId, userId, httpRequest);
	}

}
