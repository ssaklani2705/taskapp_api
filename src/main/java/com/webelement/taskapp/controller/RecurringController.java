package com.webelement.taskapp.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.common.ResponseApi;
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
	public ResponseEntity<?> getRecurringClients(@RequestAttribute("userId") Integer userId) {
		try {
			List<ClientDTO> clients = clientService.getClientsForRecurring(userId);

			return ResponseEntity.ok(clients);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	// For AddOrUpdate
	@PostMapping("/addOrUpdateRecurring")
	public ResponseEntity<?> addOrUpdateRecurring(@RequestBody RecurringDTO dto,
			@RequestAttribute("userId") Integer userId) {
		try {
			RecurringEntity recurring = recurringService.addOrUpdateRecurring(dto, userId);

			return ResponseEntity.ok(recurring);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
		}
	}

	// For edit
	@GetMapping("/getRecurringById/{recurringId}")
	public ResponseEntity<?> getRecurringById(@PathVariable Integer recurringId,
			@RequestAttribute("userId") Integer userId) {

		try {
			RecurringDTO recurring = recurringService.getRecurringById(recurringId, userId);

			return ResponseEntity.ok(recurring);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
		}
	}

	// For index
	@GetMapping("/getRecurringDetails")
	public ResponseEntity<?> getRecurringDetails(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) Short status,
			@RequestParam(required = false) String title, @RequestAttribute("userId") Integer userId) {
		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recurringId"));
			Page<RecurringDTO> recurring = recurringService.findRecurringDetails(pageable, status, null, title, userId);
			return ResponseEntity.ok(recurring);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	// For Delete
	@PostMapping("/deleteRecurring")
	public ResponseEntity<ResponseApi<String>> deletdeleteRecurringeClient(@RequestParam int recurringId,
			@RequestParam int userId, HttpServletRequest httpRequest) throws Exception {
		return recurringService.deleteRecurring(recurringId, userId, httpRequest);
	}

}
