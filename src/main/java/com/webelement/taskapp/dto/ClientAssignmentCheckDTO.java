package com.webelement.taskapp.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAssignmentCheckDTO {

    private boolean assigned;
    private String message;
}
