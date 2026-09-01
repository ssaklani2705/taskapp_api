package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDashboardResponse {

    private int myTasksToday;
    private int dueThisWeek;
    private int overdue;

    private TaskGroupResponse todo;
    private TaskGroupResponse inProgress;
    private TaskGroupResponse done;
}
