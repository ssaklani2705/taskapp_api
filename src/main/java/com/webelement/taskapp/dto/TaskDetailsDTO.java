package com.webelement.taskapp.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskDetailsDTO {
	 private static final DateTimeFormatter DATE_TIME_FORMATTER =
	            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	    private Integer taskId;
	    private String clientName;
	    private LocalDateTime date;
	    private String dueDateTime;
	    private String taskCategoryName;
	    private String assignedToName;
	    private Short priority;
	    private Short status;
	    private String title;
	    private Short taskStatus;
	    private Integer assignedTo;
	    private Integer addedBy;

		public TaskDetailsDTO(int taskId, String clientName, LocalDateTime date, String dueDateTimeHours, // e.g. "50"
																											// -> hours
																											// to add on
																											// top of
																											// `date`
				String taskCategoryName, String assignedToName, short priority, short status, String title,
				short taskStatus, int assignedTo, int addedBy) {

			this.taskId = taskId;
			this.clientName = clientName;
			this.date = date;

			if (date != null && dueDateTimeHours != null && !dueDateTimeHours.isBlank()) {
				try {
					long hours = Long.parseLong(dueDateTimeHours.trim());
					this.dueDateTime = date.plusHours(hours).format(DATE_TIME_FORMATTER);
				} catch (NumberFormatException e) {
					this.dueDateTime = null; // or log a warning
				}
			} else {
				this.dueDateTime = null;
			}

			this.taskCategoryName = taskCategoryName;
			this.assignedToName = assignedToName;
			this.priority = priority;
			this.status = status;
			this.title = title;
			this.taskStatus = taskStatus;
			this.assignedTo = assignedTo;
			this.addedBy = addedBy;
		}
}