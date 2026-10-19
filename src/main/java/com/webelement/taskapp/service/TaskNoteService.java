package com.webelement.taskapp.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import com.webelement.taskapp.dto.TaskNoteDTO;
import com.webelement.taskapp.dto.TaskNoteRequestDTO;
import com.webelement.taskapp.entity.TaskNoteEntity;
import com.webelement.taskapp.repo.TaskNoteRepo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskNoteService {

	private final TaskNoteRepo taskNoteRepo;
	private final TaskNotesMailService taskNoteMailService;

	public TaskNoteDTO addTaskNote(TaskNoteRequestDTO request) throws Exception {
		TaskNoteEntity entity = TaskNoteEntity.builder().taskId(request.getTaskId()).createdBy(request.getUserId())
				.note(request.getNote()).registrationDate(LocalDateTime.now()).modificationDate(LocalDateTime.now())
				.status((short) 1).build();
		TaskNoteEntity saved = taskNoteRepo.save(entity);
		if (Boolean.TRUE.equals(request.getSendMail())) {
			saved.setUserId(request.getUserId());
			saved.setIsAdmin(request.getIsAdmin());
			taskNoteMailService.sendTaskNotesMail(saved);
		}
		return new TaskNoteDTO(saved.getTaskNoteId(), saved.getTaskId(), saved.getCreatedBy(), null, saved.getNote(),
				saved.getRegistrationDate());
	}

	public List<TaskNoteDTO> getTaskNotes(Integer taskId) {
		return taskNoteRepo.findTaskNotes(taskId);
	}
}
