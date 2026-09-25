package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskMailDTO {

	private String name;
	private String taskName;
	private String clientName;
	private String assignedBy;
	private String previousStatus;
	private String currentStatus;

	private String reopenedOn;
	private String reopendBy;
	private String submittedOn;
	private String priority;
	private String dueDate;
	private String remark;
	private String url;
	
}
