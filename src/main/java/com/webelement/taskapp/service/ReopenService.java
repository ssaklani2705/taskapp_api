package com.webelement.taskapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.ReopenReportDTO;
import com.webelement.taskapp.dto.ReopenUserDropdownDTO;
import com.webelement.taskapp.repo.ReopenRepository;

@Service
public class ReopenService {

	@Autowired
	private ReopenRepository reopenRepository;

	public Page<ReopenReportDTO> getReopenReport(String search, LocalDateTime fromDate, LocalDateTime toDate,
			Integer assignedToId, Integer reopenById, Pageable pageable) {

		return reopenRepository.getReopenReport(search, fromDate, toDate, assignedToId, reopenById, pageable);
	}

	public List<ReopenUserDropdownDTO> getAssignedToUsersInReopenReport() {
		return reopenRepository.findAssignedToUsersInReopenReport();
	}
	
	public List<ReopenUserDropdownDTO> getReopenedByUsersInReopenReport() {
	    return reopenRepository.findReopenedByUsersInReopenReport();
	}
}
