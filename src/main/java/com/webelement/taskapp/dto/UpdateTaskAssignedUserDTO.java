package com.webelement.taskapp.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskAssignedUserDTO {
	private Integer taskId;
	private Integer assignedTo;
	private Integer userId;
	private String remarks;
   
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime date;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endDate;
}