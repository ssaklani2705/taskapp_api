package com.webelement.taskapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ServiceDTO;
import com.webelement.taskapp.entity.ServiceEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.ServiceRepo;
import com.webelement.taskapp.service.ServiceService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepo serviceRepo;
    private final CommonFunction commonFunction;
    private final HttpServletRequest httpRequest;

    @Override
    public ApiResponse<ServiceDTO> addOrUpdate(ServiceDTO request) {

        LocalDateTime now = LocalDateTime.now();
        String name = request.getName() != null
                ? request.getName().trim() : "";

        if (name.isEmpty()) {
            return new ApiResponse<>(false, "Service name is required", null);
        }

        boolean isNew = request.getServiceId() == null
                || request.getServiceId() == 0;

        if (isNew) {
            if (serviceRepo.existsByNameIgnoreCaseAndStatusNot(
                    name, (short) 3)) {
                return new ApiResponse<>(
                        false, "A service with this name already exists", null);
            }
        } else {
            List<ServiceEntity> duplicates =
                    serviceRepo.findByNameIgnoreCase(name);

            boolean exists = duplicates.stream().anyMatch(
                    duplicate ->
                            !duplicate.getServiceId().equals(
                                    request.getServiceId())
                            && duplicate.getStatus() != null
                            && duplicate.getStatus() != 3);

            if (exists) {
                return new ApiResponse<>(
                        false, "A service with this name already exists", null);
            }
        }

        ServiceEntity entity;

        if (!isNew) {
            Optional<ServiceEntity> existing =
                    serviceRepo.findById(request.getServiceId());

            if (!existing.isPresent()) {
                return new ApiResponse<>(
                        false, "Service record not found", null);
            }

            entity = existing.get();
            entity.setName(name);
            entity.setStatus(request.getStatus());
            entity.setModificationDate(now);

        } else {
            entity = new ServiceEntity();
            entity.setName(name);
            entity.setStatus(
                    request.getStatus() != null
                            ? request.getStatus() : (short) 1);
            entity.setRegistrationDate(now);
        }

        ServiceEntity saved = serviceRepo.save(entity);

        String action = isNew ? "Service Added" : "Service Updated";

        commonFunction.createHistoryAccess(
                request.getUserId(),
                commonFunction.resolveClientIp(httpRequest),
                commonFunction.getLocalIp(),
                action,
                14,
                saved.getServiceId(),
                -1);

        return new ApiResponse<>(
                true, "Service saved successfully", null);
    }

    @Override
    public ApiResponse<ServiceDTO> delete(ServiceDTO request) {

        Integer serviceId = request.getServiceId();

        Optional<ServiceEntity> existing =
                serviceRepo.findById(serviceId);

        if (!existing.isPresent()) {
            return new ApiResponse<>(
                    false, "Service not found", null);
        }

        int updatedRows = serviceRepo.softDelete(serviceId);

        if (updatedRows > 0) {
            commonFunction.createHistoryAccess(
                    request.getUserId(),
                    commonFunction.resolveClientIp(httpRequest),
                    commonFunction.getLocalIp(),
                    "Service Deleted",
                    14,
                    serviceId,
                    -1);

            return new ApiResponse<>(
                    true, "Service deleted successfully", null);
        }

        return new ApiResponse<>(
                false, "Service delete failed", null);
    }

    @Override
    public ApiResponse<ServiceDTO> getById(Integer serviceId) {

        ServiceEntity entity =
                serviceRepo.findById(serviceId).orElse(null);

        if (entity == null) {
            return new ApiResponse<>(
                    false, "Service record not found", null);
        }

        ServiceDTO dto = ServiceDTO.builder()
                .serviceId(entity.getServiceId())
                .name(entity.getName())
                .status(entity.getStatus())
                .build();

        List<TransactionEntity> history =
                commonFunction.getTransactionLogs(14, serviceId);

        if (history != null && !history.isEmpty()) {
            dto.setTransactionHistory(history);
        }

        return new ApiResponse<>(
                true, "Service fetched successfully", dto);
    }

    @Override
    public Page<ServiceDTO> findServiceList(
            int page, int size, int statusIndex, String search) {

        return serviceRepo.findServiceDetails(
                PageRequest.of(page, size),
                statusIndex,
                search);
    }

    @Override
    public List<ServiceEntity> getService() {
        return serviceRepo.findByStatus((short) 1);
    }
}