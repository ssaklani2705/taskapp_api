package com.webelement.taskapp.controller;

import com.webelement.taskapp.entity.TaskEntity;

public interface TaskMailService {
	void sendTaskStatusMail(TaskEntity task, String oldStatus, String newStatus) throws Exception;
	public void sendTaskReassignMail(TaskEntity task, Integer oldAssigneeId) throws Exception;
}
