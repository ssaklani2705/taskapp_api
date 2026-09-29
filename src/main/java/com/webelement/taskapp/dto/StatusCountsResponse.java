package com.webelement.taskapp.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusCountsResponse {
    private int unassigned;
    private int assigned;
    private int assigneeClosure;
    private int reOpen;
    private int assigneeReClosure;
    private int assignorClosure;
}
