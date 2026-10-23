package com.webelement.taskapp.service;

import java.util.List;

import org.springframework.data.domain.Page;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.entity.StateEntity;

public interface StateService {
	public ApiResponse<StateDTO> addOrUpdate(StateDTO dto);

	public ApiResponse<StateDTO> delete(StateDTO dto);

	public ApiResponse<StateDTO> getById(Integer stateId);

	public Page<StateDTO> findStateList(int page, int size, int statusIndex, String search);
	public List<StateEntity> getStates();
}
