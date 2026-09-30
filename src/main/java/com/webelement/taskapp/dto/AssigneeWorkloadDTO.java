package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AssigneeWorkloadDTO {
	   private Integer assignedTo;
	    private Long hours;
}
