package com.webelement.taskapp.dto;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TaskReassigneDTO {
	private Integer reassignId;
	private Integer taskId;
	private Integer previousAssigneeId;
	private Integer newAssigneeId;
	private Integer assignedById;
	private Short status;

	
}
