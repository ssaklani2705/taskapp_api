package com.webelement.taskapp.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.webelement.taskapp.dto.CompanyDTO;
import com.webelement.taskapp.dto.TaskReassignReportDto;
import com.webelement.taskapp.dto.TaskReassigneDTO;

public interface TaskReassignService {
	 Page<TaskReassignReportDto> getTaskReassignReport(int page, int size,
	            LocalDateTime fromDate, LocalDateTime toDate,
	            int taskCategoryId, int clientId, int userId, String search);
}
