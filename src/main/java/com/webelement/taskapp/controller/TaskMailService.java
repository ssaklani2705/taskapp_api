package com.webelement.taskapp.controller;

import com.webelement.taskapp.entity.TaskEntity;

public interface TaskMailService {
	   void sendTaskStatusMail(TaskEntity task,
               String oldStatus,
               String newStatus) throws Exception;
}
