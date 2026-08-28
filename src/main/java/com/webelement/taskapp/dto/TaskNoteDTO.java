package com.webelement.taskapp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskNoteDTO {

    private Integer taskNoteId;

    private Integer taskId;

    private Integer createdBy;

    private String creatorName;

    private String note;

    private LocalDateTime timestamp;
}
