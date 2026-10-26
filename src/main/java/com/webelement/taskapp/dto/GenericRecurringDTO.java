package com.webelement.taskapp.dto;

import java.time.LocalTime;
import java.util.List;

import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericRecurringDTO {

	private Integer genericRecurringId;
	private Integer forValue;
	private Integer date;
	private Short day;
	private String description;
	private Integer month;
	private Short status;
	private Integer taskCatId;
	private String taskCatName;
	private String title;
	private Short type;
	private Short priority;
	private LocalTime time;

	private List<TransactionEntity> transactionHistory;

	// For edit
	public GenericRecurringDTO(Integer genericRecurringId, Integer forValue, Integer date, Short day,
			String description, Integer month, Short status, Integer taskCatId, String title, Short type,
			Short priority, LocalTime time) {
		super();
		this.genericRecurringId = genericRecurringId;
		this.forValue = forValue;
		this.date = date;
		this.day = day;
		this.description = description;
		this.month = month;
		this.status = status;
		this.taskCatId = taskCatId;
		this.title = title;
		this.type = type;
		this.priority = priority;
		this.time = time;
	}

	public GenericRecurringDTO(Integer genericRecurringId, Integer forValue, Integer date, Short day,
			String description, Integer month, Short status, Integer taskCatId, String taskCatName, String title,
			Short type, Short priority, LocalTime time) {
		super();
		this.genericRecurringId = genericRecurringId;
		this.forValue = forValue;
		this.date = date;
		this.day = day;
		this.description = description;
		this.month = month;
		this.status = status;
		this.taskCatId = taskCatId;
		this.taskCatName = taskCatName;
		this.title = title;
		this.type = type;
		this.priority = priority;
		this.time = time;
	}

}
