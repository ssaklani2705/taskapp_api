package com.webelement.taskapp.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.CompanyDTO;


public interface CompanyService {
	public ApiResponse<CompanyDTO> addOrUpdate(CompanyDTO request);

	public ApiResponse<CompanyDTO> delete(CompanyDTO request);

	public ApiResponse<CompanyDTO> getById(Integer companyId);

	public Page<CompanyDTO> findCompanyList(int page, int size, int statusIndex, String search,Integer stateId);
}
