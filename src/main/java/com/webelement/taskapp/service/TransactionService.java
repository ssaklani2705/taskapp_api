package com.webelement.taskapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.ModuleDropdownProjection;
import com.webelement.taskapp.dto.TransactionActionUserProjection;
import com.webelement.taskapp.dto.TransactionHistoryProjection;
import com.webelement.taskapp.repo.ModuleRepo;
import com.webelement.taskapp.repo.TransactionRepo;

@Service
public class TransactionService {

	@Autowired
	private TransactionRepo transactionRepo;

	@Autowired
	private ModuleRepo moduleRepo;

	public Page<TransactionHistoryProjection> getTransactionHistory(String search, Integer moduleId, Integer userId,
			LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {


		Pageable pageable = PageRequest.of(page, size);


		return transactionRepo.getTransactionHistory(search, moduleId, userId, fromDate, toDate, pageable);
	}

	public List<ModuleDropdownProjection> getActiveModules() {
		return moduleRepo.findModulesByStatus(1);
	}

	public List<TransactionActionUserProjection> getTransactionActionUsers() {
		return transactionRepo.findTransactionActionUsers();
	}
}
