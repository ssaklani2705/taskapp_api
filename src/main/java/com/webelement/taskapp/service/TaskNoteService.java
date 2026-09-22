package com.webelement.taskapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.controller.TaskMailService;
import com.webelement.taskapp.dto.TaskNoteDTO;
import com.webelement.taskapp.dto.TaskNoteRequestDTO;
import com.webelement.taskapp.entity.TaskNoteEntity;
import com.webelement.taskapp.repo.SmtpRepo;
import com.webelement.taskapp.repo.TaskNoteRepo;
import com.webelement.taskapp.repo.UserLoginRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskNoteService {
    private final TaskNoteRepo taskNoteRepo;
    private final TaskNotesMailService taskNoteMailService;

	// =====================================================
	// ADD NOTE
	// =====================================================
	public TaskNoteDTO addTaskNote(TaskNoteRequestDTO request) throws Exception {

		System.err.println(" Requested data " + request.toString());
		TaskNoteEntity entity = TaskNoteEntity.builder()
				.taskId(request.getTaskId())
				.createdBy(request.getUserId())
				.note(request.getNote()).registrationDate(LocalDateTime.now()).modificationDate(LocalDateTime.now()).status((short) 1).build();
		
		TaskNoteEntity saved = taskNoteRepo.save(entity);
		
		if (Boolean.TRUE.equals(request.getSendMail())) {
			saved.setUserId(request.getUserId());
			saved.setIsAdmin(request.getIsAdmin());
			taskNoteMailService.sendTaskNotesMail(saved);
		}
		return new TaskNoteDTO(saved.getTaskNoteId(), saved.getTaskId(), saved.getCreatedBy(), null, saved.getNote(),saved.getRegistrationDate());
	}


    // =====================================================
    // GET PREVIOUS NOTES
    // =====================================================

   


	public List<TaskNoteDTO> getTaskNotes(Integer taskId) {

    	 return taskNoteRepo.findTaskNotes(taskId);
//        List<TaskNoteEntity> notes =
//                taskNoteRepo.findByTaskId(taskId);
//
//        return notes.stream()
//                .map(note -> new TaskNoteDTO(
//                        note.getTaskNoteId(),
//                        note.getTaskId(),
//                        note.getCreatedBy(),
//                        null,
//                        note.getNote(),
//                        note.getRegistrationDate()
//                ))
//                .collect(Collectors.toList());
    }
}
