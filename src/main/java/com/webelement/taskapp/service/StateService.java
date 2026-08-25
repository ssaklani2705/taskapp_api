package com.webelement.taskapp.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.DesignationDTO;
import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.entity.StateEntity;

public interface StateService {
	ApiResponse<StateDTO> addOrUpdate(StateDTO dto);
	
	ApiResponse<StateDTO> delete(StateDTO dto);

	ApiResponse<StateDTO> getById(Integer stateId);

	Page<StateDTO> findStateList(int page, int size, int statusIndex, String search);
	
	
	
}
