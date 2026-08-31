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
import com.webelement.taskapp.dto.TaskCategoryDTO;
import com.webelement.taskapp.service.TaskCategoryService;

@RestController
@RequestMapping("/admin/taskcategory")
@CrossOrigin(origins = {
        "http://localhost:4500",
        "https://app.webelement.cc",
        "https://13.202.30.190"
})
public class TaskCategoryController {

    @Autowired
    private TaskCategoryService taskCategoryService;

    @GetMapping("/getTaskCategoryDetails")
    public Map<String, Object> findTaskCategoryDetails(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam int statusIndex,

            @RequestParam(required = false) String search,@RequestParam int departmentId) {
    	
        Page<TaskCategoryDTO> pageData =
                taskCategoryService.findTaskCategoryDetails(
                        page,
                        size,
                        statusIndex,
                        search,departmentId);

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

    @PostMapping("/saveTaskCategory")
    public ResponseEntity<ApiResponse<TaskCategoryDTO>> saveTaskCategory(
            @RequestBody TaskCategoryDTO dto,
            HttpServletRequest httpRequest) {

        try {

            ApiResponse<TaskCategoryDTO> response =
                    taskCategoryService.addOrUpdate(
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

    @GetMapping("/{taskcategoryId}")
    public ApiResponse<TaskCategoryDTO> getTaskCategoryById(
            @PathVariable Integer taskcategoryId) {

        try {
            return taskCategoryService.getById(taskcategoryId);

        } catch (Exception e) {
            return new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
            );
        }
    }


    @PostMapping("/deleteTaskCategory")
    public ResponseEntity<ApiResponse<String>> deleteTaskCategory(
            @RequestBody TaskCategoryDTO dto,
            HttpServletRequest httpRequest) {

        return taskCategoryService.deleteTaskCategory(
                dto.getTaskcategoryId(),
                dto.getUserId(),
                httpRequest);
    }

    @GetMapping("/active")
    public List<TaskCategoryDTO> getActiveTaskCategories() {

        return taskCategoryService
                .getActiveTaskCategories();
    }
}
