package com.webelement.taskapp.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.CompanyDTO;
import com.webelement.taskapp.dto.TaskReassignReportDto;
import com.webelement.taskapp.entity.ReassignEntity;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.repo.TaskReassignRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskReassignServiceImpl implements TaskReassignService  {

	private final TaskReassignRepo reassignRepo;
	
	
	 private TaskReassignReportDto toDto(ReassignEntity tr) {
	        TaskEntity task = tr.getTaskEntity();
	        return new TaskReassignReportDto(
	        		tr.getModificationDate(),
	                task.getTitle(),
	                task.getTaskCategory() != null ? task.getTaskCategory().getName() : null,
	                task.getClient() != null ? task.getClient().getName() : null,
	                tr.getCurrentUser() != null ? tr.getCurrentUser().getFirstName() : null,
	                tr.getNewUser() != null ? tr.getNewUser().getFirstName() : null);
	    }

		@Override
		public Page<TaskReassignReportDto> getTaskReassignReport(int page, int size, LocalDateTime fromDate,
				LocalDateTime toDate, int taskCategoryId, int clientId, int userId, String search) {
			LocalDateTime toExclusive = (toDate == null) ? null : toDate.toLocalDate().plusDays(1).atStartOfDay();

			return reassignRepo.getTaskReassignReport(PageRequest.of(page, size), fromDate, toExclusive, taskCategoryId,
					clientId, userId, search).map(this::toDto);
		}

}
