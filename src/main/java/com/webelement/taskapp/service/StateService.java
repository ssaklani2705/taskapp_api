package com.webelement.taskapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.entity.StateEntity;
import com.webelement.taskapp.repo.StateRepository;

@Service
public class StateService {

	@Autowired
	private StateRepository stateRepository;

	public List<StateEntity> getStates() {
		return stateRepository.findByStatus((short) 1);
	}
}
