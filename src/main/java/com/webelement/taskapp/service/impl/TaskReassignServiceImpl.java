package com.webelement.taskapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.CompanyDTO;
import com.webelement.taskapp.dto.DepartmentFilterProjection;
import com.webelement.taskapp.dto.OldUserTaskCountDto;
import com.webelement.taskapp.dto.TaskReassignReportDto;
import com.webelement.taskapp.entity.ReassignEntity;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.repo.TaskReassignRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskReassignServiceImpl implements TaskReassignService  {

	private final TaskReassignRepo reassignRepo;
	
	@Override
    public Page<OldUserTaskCountDto> getOldUserTaskCount(int page, int size, LocalDateTime fromDate,
            LocalDateTime toDate, String search, String departmentIds) {


        LocalDateTime toExclusive = (toDate == null) ? null : toDate.toLocalDate().plusDays(1).atStartOfDay();


        String userName = (search == null || search.trim().isEmpty()) ? null : search.trim();


        String departments = (departmentIds == null || departmentIds.trim().isEmpty()) ? null : departmentIds.trim();


        return reassignRepo.getOldUserTaskCount(fromDate, toExclusive, userName, departments,
                PageRequest.of(page, size));
    }




    @Override
    public List<DepartmentFilterProjection> getReassignSummaryReportDepartments() {
        return reassignRepo.findReassignSummaryReportDepartments();
    }
	
	
	 private TaskReassignReportDto toDto(ReassignEntity tr) {
	        TaskEntity task = tr.getTaskEntity();
	        return new TaskReassignReportDto(
	        		tr.getRegistrationDate(),
	                task.getTitle(),
	                task.getTaskCategory() != null ? task.getTaskCategory().getName() : null,
	                task.getClient() != null ? task.getClient().getName() : null,
	                tr.getCurrentUser() != null ? tr.getCurrentUser().getFirstName() : null,
	                tr.getNewUser() != null ? tr.getNewUser().getFirstName() : null,tr.getAssignedBy() != null ? tr.getAssignedBy().getFirstName() : null);
	    }

	 @Override
	 public Page<TaskReassignReportDto> getTaskReassignReport(int page, int size, LocalDateTime fromDate,
	         LocalDateTime toDate, int taskCategoryId, int clientId, int userId, String search,
	         List<Integer> deptIds) {

	     LocalDateTime toExclusive = (toDate == null) ? null : toDate.toLocalDate().plusDays(1).atStartOfDay();

	     int departmentFilter = (deptIds == null || deptIds.isEmpty()) ? 0 : 1;
	     List<Integer> ids = departmentFilter == 0 ? List.of(-1) : deptIds;

	     return reassignRepo.getTaskReassignReport(PageRequest.of(page, size), fromDate, toExclusive,
	             taskCategoryId, clientId, userId, search, departmentFilter, ids).map(this::toDto);
	 }

}
