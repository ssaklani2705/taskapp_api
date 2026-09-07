package com.webelement.taskapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsDTO {

    private Integer taskId;

    private String clientName;

    private LocalDateTime date;

    private String taskCategoryName;

    private String assignedToName;

    private Short priority;

    private Short status;

    private String title;
    
    private Short taskStatus;
    
    private Integer assignedTo;
    
    private Integer addedBy;
}
