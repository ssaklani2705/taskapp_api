package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskNoteRequestDTO {

    private Integer taskId;
    private Integer userId;
    private String note;
}
