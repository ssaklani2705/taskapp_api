package com.webelement.taskapp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReopenReportDTO {

	private Integer taskId;
	private String title;
	private LocalDateTime taskStartDate;
	private LocalDateTime taskEndDate;

	private Integer assignedToId;
	private String assignedToName;

	private Integer reopenById;
	private String reopenByName;

	private LocalDateTime reopenDate;
	private String description;
}
