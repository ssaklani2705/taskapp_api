package com.webelement.taskapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.TaskNoteDTO;
import com.webelement.taskapp.dto.TaskNoteRequestDTO;
import com.webelement.taskapp.service.TaskNoteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/taskNote")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class TaskNoteController {
	private final TaskNoteService taskNoteService;

	@PostMapping("/add")
	public ResponseEntity<TaskNoteDTO> addTaskNote(@RequestBody TaskNoteRequestDTO request) throws Exception {
		TaskNoteDTO response = taskNoteService.addTaskNote(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/getByTaskId/{taskId}")
	public ResponseEntity<List<TaskNoteDTO>> getTaskNotes(@PathVariable Integer taskId) {
		List<TaskNoteDTO> response = taskNoteService.getTaskNotes(taskId);
		return ResponseEntity.ok(response);
	}
}
