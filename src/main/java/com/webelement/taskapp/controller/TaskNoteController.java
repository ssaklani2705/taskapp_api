package com.webelement.taskapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.webelement.taskapp.dto.TaskNoteDTO;
import com.webelement.taskapp.dto.TaskNoteRequestDTO;
import com.webelement.taskapp.service.TaskNoteService;

import lombok.RequiredArgsConstructor;


@RestController

@RequestMapping("/admin/taskNote")
@CrossOrigin(origins =  "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class TaskNoteController {

	private final SimpMessagingTemplate messagingTemplate;
    private final TaskNoteService taskNoteService;

	// =====================================================
	// ADD NOTE
	// =====================================================
	@PostMapping("/add")
	public ResponseEntity<TaskNoteDTO> addTaskNote(@RequestBody TaskNoteRequestDTO request) throws Exception {
		TaskNoteDTO response = taskNoteService.addTaskNote(request);

		messagingTemplate.convertAndSend("/topic/task-notes/" + request.getTaskId(), response);
		return ResponseEntity.ok(response);
	}
    // =====================================================
    // GET NOTES BY TASK ID
    // =====================================================
    @GetMapping("/getByTaskId/{taskId}")
    public ResponseEntity<List<TaskNoteDTO>> getTaskNotes(@PathVariable Integer taskId) {
        List<TaskNoteDTO> response =taskNoteService.getTaskNotes(taskId);
        return ResponseEntity.ok(response);
    }
}
