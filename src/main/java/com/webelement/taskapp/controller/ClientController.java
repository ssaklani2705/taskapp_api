package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ClientDTO;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.service.ClientService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
public class ClientController {

	@Autowired
	private ClientService clientService;

	@Autowired
	private ClientRepository clientRepository;

	// Add or Update
	@PostMapping("/addOrUpdateClient")
	public ResponseEntity<ApiResponse<ClientEntity>> addOrUpdateClient(@RequestBody ClientEntity client,
			HttpServletRequest httpRequest) {

		ApiResponse<ClientEntity> response = clientService.addOrUpdateClient(client, httpRequest);

		return ResponseEntity.ok(response);
	}

	// For View
	@GetMapping("/clientDetails/{clientId}")
	public ResponseEntity<?> getClientDetailsById(@PathVariable int clientId) {
		ClientDTO client = clientService.getClientDetailsById(clientId);
		if (client != null) {
			return ResponseEntity.ok(client);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// For Edit
	@GetMapping("getClient/{clientId}")
	public ApiResponse<ClientEntity> getCClientById(@PathVariable Integer clientId) {
		Optional<ClientEntity> company = clientRepository.findById(clientId);
		if (company.isPresent()) {
			return new ApiResponse<>(true, "Client fetched successfully", company.get());
		} else {
			return new ApiResponse<>(false, "Client not found", null);
		}
	}

	private static final Map<String, String> CLIENT_SORT_MAP = Map.of("name", "c.name", "code", "c.code", "status",
			"c.status", "managerName", "u.firstName", "stateName", "s.name", "contactName", "c.contactName",
			"contactEmail", "c.contactEmail", "city", "c.city", "regdate", "c.regdate");

	// For Index
	@GetMapping("/getClientDetails")
	public Map<String, Object> findClientDetails(@RequestParam int page, @RequestParam int size,
			@RequestParam(defaultValue = "0") Short status, @RequestParam(defaultValue = "0") Integer managerId,
			@RequestParam(defaultValue = "0") Integer stateId, @RequestParam(required = false) String clientName,
			@RequestParam(required = false) String clientCode, @RequestParam(required = false) String contactName,
			@RequestParam(required = false) String contactEmail, @RequestParam(required = false) String search,
			@RequestParam(defaultValue = "name") String sortColumn,
			@RequestParam(defaultValue = "asc") String sortDirection) {

		String sortBy = CLIENT_SORT_MAP.getOrDefault(sortColumn, "c.name");

		Sort.Direction direction = sortDirection.trim().equalsIgnoreCase("desc") ? Sort.Direction.DESC
				: Sort.Direction.ASC;

		sortBy = sortBy.replace("c.", "").replace("u.", "").replace("s.", "");

		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

		Page<ClientDTO> clientPage = clientService.findClientDetails(pageable, status, managerId, stateId, clientName,
				clientCode, contactName, contactEmail, search);

		Map<String, Object> response = new HashMap<>();

		response.put("data", clientPage.getContent());
		response.put("totalElements", clientPage.getTotalElements());
		response.put("totalPages", clientPage.getTotalPages());
		response.put("currentPage", clientPage.getNumber());
		response.put("pageSize", clientPage.getSize());

		return response;
	}

	// Delete client
	@PostMapping("/deleteClient")
	public ResponseEntity<ResponseApi<String>> deleteClient(@RequestParam int clientId, @RequestParam int userId,
			HttpServletRequest httpRequest) throws Exception {
		return clientService.deleteClient(clientId, userId, httpRequest);
	}

	// Upload excel
	@PostMapping("/uploadClientsExcel")
	public ResponseEntity<ApiResponse<List<ClientEntity>>> uploadClientsExcel(@RequestParam("file") MultipartFile file,
			@RequestParam("userId") Integer userId, HttpServletRequest request) {

		try {

			if (file == null || file.isEmpty()) {
				return ResponseEntity.ok(new ApiResponse<>(false, "Please select an Excel file", null));
			}

			String fileName = file.getOriginalFilename();

			if (fileName == null
					|| !(fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {

				return ResponseEntity.ok(new ApiResponse<>(false, "Only Excel files (.xlsx, .xls) are allowed", null));
			}

			List<ClientEntity> clients = clientService.readClientsFromExcel(file);

			ApiResponse<List<ClientEntity>> response = clientService.saveClientsFromExcel(clients, userId, request);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.ok(new ApiResponse<>(false,
					e.getMessage() != null ? e.getMessage() : "Failed to upload Excel file", null));
		}
	}

	// Change manager
	@PutMapping("/client/{clientId}/manager")
	public ResponseEntity<ApiResponse<String>> changeClientManager(@PathVariable Integer clientId,
			@RequestBody Map<String, Integer> request, HttpServletRequest httpRequest) {

		Integer managerId = request.get("managerId");

		clientService.changeClientManager(clientId, managerId, httpRequest);

		return ResponseEntity.ok(new ApiResponse<>(true, "Manager changed successfully", null));
	}
}
