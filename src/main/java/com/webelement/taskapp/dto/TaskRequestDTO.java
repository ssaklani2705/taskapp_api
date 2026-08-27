package com.webelement.taskapp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TaskRequestDTO {

    private Integer taskId;

    private String title;

    private Integer clientId;

    private LocalDate date;

    private Integer taskCategoryId;

    private String description;

    private Integer assignedTo;

    private Short priority;

    /*
     * Only used during UPDATE.
     *
     * CREATE:
     * status should be ignored/defaulted to 1.
     */
    private Short status;

    private String closeRemarks;

    private Integer taskGroupId;

    private Integer addedBy;
}
