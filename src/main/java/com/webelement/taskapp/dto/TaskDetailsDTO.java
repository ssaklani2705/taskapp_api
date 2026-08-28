package com.webelement.taskapp.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsDTO {

    private Integer taskId;

    private String clientName;

    private LocalDate date;

    private String taskCategoryName;

    private String assignedToName;

    private Short priority;

    private Short status;

    private String title;
    
    private Short taskStatus;
    
    private Integer assignedTo;
    
    private Integer addedBy;
}
