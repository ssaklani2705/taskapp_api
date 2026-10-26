package com.webelement.taskapp.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ServiceDTO;
import com.webelement.taskapp.entity.ServiceEntity;

public interface ServiceService {

    ApiResponse<ServiceDTO> addOrUpdate(ServiceDTO request);

    ApiResponse<ServiceDTO> delete(ServiceDTO request);

    ApiResponse<ServiceDTO> getById(Integer serviceId);

    Page<ServiceDTO> findServiceList(
            int page, int size, int statusIndex, String search);

    List<ServiceEntity> getService();
    
    List<ServiceEntity> getActiveServices();
}