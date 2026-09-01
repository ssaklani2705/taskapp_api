package com.webelement.taskapp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskGroupResponse {

    private int count;
    private List<TaskDashboardItem> tasks;
}
