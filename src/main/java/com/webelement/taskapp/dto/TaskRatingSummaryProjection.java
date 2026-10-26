package com.webelement.taskapp.dto;

public interface TaskRatingSummaryProjection {
    String getName();
    Long getTotalTasks();
    Long getRatedTasks();
    Double getAvgRating();
}