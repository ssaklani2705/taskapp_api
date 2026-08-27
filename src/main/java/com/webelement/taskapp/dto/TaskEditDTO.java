package com.webelement.taskapp.dto;

import java.time.LocalDate;
import java.util.List;

import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEditDTO {

	private Integer taskId;
	private Integer addedBy;
	private Integer assignedTo;
	private Integer clientId;
	private String closeRemarks;
	private LocalDate date;
	private String description;
	private String fileName1;
	private String fileName2;
	private String fileName3;
	private String fileName4;
	private Short priority;
	private Short status;
	private Integer taskCategoryId;
	private String title;
	private List<TransactionEntity> transactionHistory;
	public TaskEditDTO(Integer taskId, Integer addedBy, Integer assignedTo, Integer clientId, String closeRemarks,
			LocalDate date, String description, String fileName1, String fileName2, String fileName3, String fileName4,
			Short priority, Short status, Integer taskCategoryId, String title) {
		super();
		this.taskId = taskId;
		this.addedBy = addedBy;
		this.assignedTo = assignedTo;
		this.clientId = clientId;
		this.closeRemarks = closeRemarks;
		this.date = date;
		this.description = description;
		this.fileName1 = fileName1;
		this.fileName2 = fileName2;
		this.fileName3 = fileName3;
		this.fileName4 = fileName4;
		this.priority = priority;
		this.status = status;
		this.taskCategoryId = taskCategoryId;
		this.title = title;
	}
}