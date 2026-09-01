package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDashboardItem {

    private Integer taskId;

    private Integer clientId;

    private String title;

    private LocalDate date;

    private String priority;

    private String status;

    private Integer progress;

    private String description;

    private Integer assignedTo;

    private Integer addedBy;

    private Integer taskCategoryId;

    private String fileName1;

    private String fileName2;

    private String fileName3;

    private String fileName4;

    private String closeRemarks;
}