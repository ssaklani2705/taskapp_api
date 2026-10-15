package com.webelement.taskapp.service;

import org.springframework.data.domain.Page;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.StateDTO;

public interface StateService {
	ApiResponse<StateDTO> addOrUpdate(StateDTO dto);

	ApiResponse<StateDTO> delete(StateDTO dto);

	ApiResponse<StateDTO> getById(Integer stateId);

	Page<StateDTO> findStateList(int page, int size, int statusIndex, String search);
}
