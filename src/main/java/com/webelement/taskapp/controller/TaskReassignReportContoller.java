package com.webelement.taskapp.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.webelement.taskapp.dto.TaskReassignReportDto;
import com.webelement.taskapp.service.TaskNoteService;
import com.webelement.taskapp.service.impl.TaskReassignService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/task_reassign")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class TaskReassignReportContoller {
	
	private final TaskReassignService taskReassignService;
	
	@GetMapping("/report")
	public ResponseEntity<Page<TaskReassignReportDto>> getTaskReassignReport(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String fromDate,
			@RequestParam(required = false) String toDate, @RequestParam(defaultValue = "0") int taskCategoryId,
			@RequestParam(defaultValue = "0") int clientId, @RequestParam(defaultValue = "0") int userId,
			@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "") String departmentIds) {

		LocalDateTime from = parseDate(fromDate);
		LocalDateTime to = parseDate(toDate);
		 List<Integer> deptIds = Arrays.stream(departmentIds.split(","))
		            .map(String::trim)
		            .filter(s -> !s.isEmpty())
		            .map(Integer::valueOf)
		            .collect(Collectors.toList());

		return ResponseEntity.ok(taskReassignService.getTaskReassignReport(page, size, from, to, taskCategoryId,
				clientId, userId, search,deptIds));
	}

	private LocalDateTime parseDate(String date) {
	    if (date == null || date.isBlank()) {
	        return null;                       
	    }
	    try {
	        return LocalDate.parse(date.trim()).atStartOfDay();   // expects yyyy-MM-dd
	    } catch (DateTimeParseException e) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
	                "Invalid date '" + date + "'. Use format yyyy-MM-dd");
	    }
	}
}
