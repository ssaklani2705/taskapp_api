package com.webelement.taskapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webelement.taskapp.dto.TaskNoteDTO;
import com.webelement.taskapp.dto.TaskNoteRequestDTO;
import com.webelement.taskapp.service.TaskNoteService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/admin/taskNote")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
@RequiredArgsConstructor
public class TaskNoteController {

    @Autowired
    private TaskNoteService taskNoteService;


    // =====================================================
    // ADD NOTE
    // =====================================================

    @PostMapping("/add")
    public ResponseEntity<TaskNoteDTO> addTaskNote(
            @RequestBody TaskNoteRequestDTO request) {

        TaskNoteDTO response =
                taskNoteService.addTaskNote(request);

        return ResponseEntity.ok(response);
    }


    // =====================================================
    // GET NOTES BY TASK ID
    // =====================================================

    @GetMapping("/getByTaskId/{taskId}")
    public ResponseEntity<List<TaskNoteDTO>> getTaskNotes(
            @PathVariable Integer taskId) {

        List<TaskNoteDTO> response =
                taskNoteService.getTaskNotes(taskId);

        return ResponseEntity.ok(response);
    }
}
