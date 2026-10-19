package com.webelement.taskapp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskDetailsDTO {
	private Integer taskId;
	private Integer managerId;
	private String clientName;
	private LocalDateTime date;
	private LocalDateTime dueDateTime;
	private String taskCategoryName;
	private String assignedToName;
	private Short priority;
	private Short status;
	private String title;
	private Short taskStatus;
	private Integer assignedTo;
	private Integer addedBy;
	private String assignedbyName;
	private String description;
	private Integer clientId;
	private Integer taskCategoryId;
	private Integer systemFlag;

	public TaskDetailsDTO(Integer taskId, Integer managerId, String clientName, LocalDateTime date,
			LocalDateTime dueDateTime, String taskCategoryName, String assignedToName, Short priority, Short status,
			String title, Short taskStatus, Integer assignedTo, Integer addedBy, String assignedbyName,
			String description, Integer clientId, Integer taskCategoryId, Integer systemFlag) {
		this.taskId = taskId;
		this.managerId = managerId;
		this.clientName = clientName;
		this.date = date;
		this.dueDateTime = dueDateTime;
		this.taskCategoryName = taskCategoryName;
		this.assignedToName = assignedToName != null ? assignedToName : "0";
		this.priority = priority != null ? priority : 0;
		this.status = status;
		this.title = title;
		this.taskStatus = taskStatus != null ? taskStatus : 0;
		this.assignedTo = assignedTo != null ? assignedTo : 0;
		this.addedBy = addedBy != null ? addedBy : 0;
		this.assignedbyName = assignedbyName;
		this.description = description;
		this.clientId = clientId;
		this.taskCategoryId = taskCategoryId;
		this.systemFlag = systemFlag;
	}
}
