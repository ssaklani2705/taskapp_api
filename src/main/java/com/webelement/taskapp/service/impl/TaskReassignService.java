package com.webelement.taskapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.webelement.taskapp.dto.CompanyDTO;
import com.webelement.taskapp.dto.TaskReassignReportDto;

public interface TaskReassignService {
	 Page<TaskReassignReportDto> getTaskReassignReport(int page, int size,
	            LocalDateTime fromDate, LocalDateTime toDate,
	            int taskCategoryId, int clientId, int userId, String search, List<Integer> deptIds);
}
