package com.webelement.taskapp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data

@NoArgsConstructor

public class TaskReassignReportDto {
	private LocalDateTime taskDate;
	private String taskName;
	private String taskCategory;
	private String clientName;
	private String oldUser;
	private String newUser;
	private String assignedBy;

	public TaskReassignReportDto(LocalDateTime taskDate, String taskName, String taskCategory, String clientName,
			String oldUser, String newUser, String assignedBy) {
		super();
		this.taskDate = taskDate;
		this.taskName = taskName;
		this.taskCategory = taskCategory;
		this.clientName = clientName;
		this.oldUser = oldUser;
		this.newUser = newUser;
		this.assignedBy = assignedBy;
	}
	
	
}
