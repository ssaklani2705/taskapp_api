package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskNoteRequestDTO {

    private Integer taskId;
    private Integer userId;
    private String note;
    private Boolean sendMail;
    private String isAdmin;
}
