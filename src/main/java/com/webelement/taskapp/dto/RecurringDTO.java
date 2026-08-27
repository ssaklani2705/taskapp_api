package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringDTO {

	private Integer recurringId;
	private Integer clientId;
	private String clientName;
	private String title;
	private String description;
	private Short type;
	private Integer date;
	private Short day;
	private Integer month;
	private Integer taskCatId;
	private String taskCatName;
	private Short status;

	public RecurringDTO(Integer recurringId, Integer clientId, String title, String description, Short type,
			Integer date, Short day, Integer month, Integer taskCatId, Short status) {
		super();
		this.recurringId = recurringId;
		this.clientId = clientId;
		this.title = title;
		this.description = description;
		this.type = type;
		this.date = date;
		this.day = day;
		this.month = month;
		this.taskCatId = taskCatId;
		this.status = status;
	}

}
