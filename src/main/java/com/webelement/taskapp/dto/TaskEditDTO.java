package com.webelement.taskapp.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEditDTO {

    private Integer taskId;

    private Integer addedBy;

    private Integer assignedTo;

    private Integer clientId;

    private String closeRemarks;

    private LocalDate date;

    private String description;

    private String fileName1;

    private String fileName2;

    private String fileName3;

    private String fileName4;

    private Short priority;

    private Short status;

    private Integer taskCategoryId;

    private String title;
}