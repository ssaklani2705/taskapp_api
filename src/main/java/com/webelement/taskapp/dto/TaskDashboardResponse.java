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

	  private DashboardMetricDTO myTasksToday;
	    private DashboardMetricDTO dueThisWeek;
	    private DashboardMetricDTO overdue;

    private TaskGroupResponse todo;
    private TaskGroupResponse inProgress;
    private TaskGroupResponse done;
    
    private StatusCountsResponse statusCounts;
}
