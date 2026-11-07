package com.webelement.taskapp.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.ReopenReportDTO;
import com.webelement.taskapp.service.ReopenService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class ReopenController {

	@Autowired
	private ReopenService reopenService;

	static final Logger logger = LoggerFactory.getLogger(ReopenService.class);

	@GetMapping("/reopen-report")
	public ResponseEntity<?> getReopenReport(@RequestParam(required = false) String search,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			@RequestParam(required = false) Integer assignedToId, @RequestParam(required = false) Integer reopenById,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Map<String, Object> response = new HashMap<>();

		try {
			Pageable pageable = PageRequest.of(page, size);

			Page<ReopenReportDTO> result = reopenService.getReopenReport(search, fromDate, toDate, assignedToId,
					reopenById, pageable);

			response.put("success", true);
			response.put("message", "Reopen report fetched successfully");
			response.put("data", result.getContent());
			response.put("currentPage", result.getNumber());
			response.put("totalItems", result.getTotalElements());
			response.put("totalPages", result.getTotalPages());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Error fetching reopen report", e);

			response.put("success", false);
			response.put("message", e.getMessage());
			response.put("data", null);

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@GetMapping("/reopen-report/assigned-users")
	public ResponseEntity<?> getAssignedToUsersInReopenReport() {
		try {
			return ResponseEntity.ok(reopenService.getAssignedToUsersInReopenReport());
		} catch (Exception e) {
			logger.error("Error fetching reopen report assigned users", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
		}
	}

	@GetMapping("/reopen-report/reopened-by-users")
	public ResponseEntity<?> getReopenedByUsersInReopenReport() {
		try {
			return ResponseEntity.ok(reopenService.getReopenedByUsersInReopenReport());
		} catch (Exception e) {
			logger.error("Error fetching reopened by users", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
		}
	}
}
