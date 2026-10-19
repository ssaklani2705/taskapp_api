package com.webelement.taskapp.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/report")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("@perm.has(13)")
    @GetMapping("/workload")
    public ApiResponse<List<Map<String, Object>>> workload(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<Integer> categoryIds) {
        return new ApiResponse<>(true, "OK", reportService.workloadPerUser(from, to, categoryIds));
    }
}
