package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ServiceDTO;
import com.webelement.taskapp.entity.ServiceEntity;
import com.webelement.taskapp.service.ServiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/service")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping("/add_or_Update")
    public ResponseEntity<ApiResponse<ServiceDTO>> addOrUpdate(
            @RequestBody ServiceDTO dto) {

        ApiResponse<ServiceDTO> response =
                serviceService.addOrUpdate(dto);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> findServiceList(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam int statusIndex,
            @RequestParam(required = false) String search) {

        Page<ServiceDTO> pageData =
                serviceService.findServiceList(
                        page, size, statusIndex, search);

        Map<String, Object> data = new HashMap<>();
        data.put("data", pageData.getContent());
        data.put("totalElements", pageData.getTotalElements());

        return new ApiResponse<>(
                true, "Service list fetched successfully", data);
    }

    @GetMapping("/get_by_Id/{serviceId}")
    public ApiResponse<ServiceDTO> getById(
            @PathVariable Integer serviceId) {

        return serviceService.getById(serviceId);
    }

    @PostMapping("/delete")
    public ApiResponse<ServiceDTO> delete(
            @RequestBody ServiceDTO dto) {

        return serviceService.delete(dto);
    }

    @GetMapping("/getService")
    public ApiResponse<List<ServiceEntity>> getService() {

        List<ServiceEntity> services = serviceService.getService();

        return new ApiResponse<>(
                true, "Service fetched successfully", services);
    }
}