package com.webelement.taskapp.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskAssignedUserDTO {

    private Integer taskId;

    private Integer assignedTo;
    
    private Integer userId;
    
    private String remarks;
}