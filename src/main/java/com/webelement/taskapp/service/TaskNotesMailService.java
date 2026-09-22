package com.webelement.taskapp.service;

import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.entity.TaskNoteEntity;

public interface TaskNotesMailService {
	 void sendTaskNotesMail(TaskNoteEntity request) throws Exception;
}
