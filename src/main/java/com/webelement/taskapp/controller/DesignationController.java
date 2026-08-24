package com.webelement.taskapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.DesignationDTO;
import com.webelement.taskapp.service.DesignationService;

@RestController
@RequestMapping("/admin/designation")
@CrossOrigin(origins = {
        "http://localhost:4500",
        "https://app.webelement.cc",
        "https://13.202.30.190"
})
public class DesignationController {

    @Autowired
    private DesignationService desigmationService;

    // ----------------------------------------------------
    // PAGINATION + SEARCH
    // ----------------------------------------------------

    @GetMapping("/getDesignationDetails")
    public Map<String, Object> findDesigmationDetails(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam int statusIndex,
            @RequestParam(required = false) String search) {

        Page<DesignationDTO> pageData =
                desigmationService.findDesigmationDetails(
                        page,
                        size,
                        statusIndex,
                        search);

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "data",
                pageData.getContent());

        response.put(
                "totalElements",
                pageData.getTotalElements());

        return response;
    }

    // ----------------------------------------------------
    // ADD / UPDATE
    // ----------------------------------------------------

    @PostMapping("/saveDesignation")
    public ResponseEntity<ApiResponse<DesignationDTO>> saveDesigmation(
            @RequestBody DesignationDTO dto,
            HttpServletRequest httpRequest) {

        try {

            ApiResponse<DesignationDTO> response =
                    desigmationService.addOrUpdate(
                            dto,
                            httpRequest);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null));
        }
    }

    // ----------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------

    @GetMapping("/{designationId}")
    public ApiResponse<DesignationDTO> getDesigmationById(
            @PathVariable Integer designationId) {
    	System.out.println("designationId --------------->: "+designationId);
        try {

            return desigmationService.getById(
            		designationId);

        } catch (Exception e) {

            return new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null);
        }
    }

    // ----------------------------------------------------
    // DELETE
    // ----------------------------------------------------

    @PostMapping("/deleteDesignation")
    public ResponseEntity<ApiResponse<String>> deleteDesigmation(
            @RequestBody DesignationDTO dto,
            HttpServletRequest httpRequest) {

        return desigmationService.deleteDesigmation(
                dto.getDesigmationId(),
                dto.getUserId(),
                httpRequest);
    }

    // ----------------------------------------------------
    // ACTIVE
    // ----------------------------------------------------

    @GetMapping("/active")
    public List<DesignationDTO> getActiveDesigmations() {

        return desigmationService
                .getActiveDesigmations();
    }
}

