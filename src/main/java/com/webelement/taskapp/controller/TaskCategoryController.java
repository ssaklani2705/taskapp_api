package com.webelement.taskapp.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.TaskCategoryDTO;
import com.webelement.taskapp.service.TaskCategoryService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/taskcategory")
@CrossOrigin(origins =  "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class TaskCategoryController {

	private final TaskCategoryService taskCategoryService;

	@GetMapping("/getTaskCategoryDetails")
	public Map<String, Object> findTaskCategoryDetails(@RequestParam int page, @RequestParam int size,
			@RequestParam int statusIndex,

			@RequestParam(required = false) String search, @RequestParam int departmentId) {

		Page<TaskCategoryDTO> pageData = taskCategoryService.findTaskCategoryDetails(page, size, statusIndex, search,
				departmentId);

		Map<String, Object> response = new HashMap<>();

		response.put("data", pageData.getContent());

		response.put("totalElements", pageData.getTotalElements());

		return response;
	}
	
	@GetMapping("task/category/employee-available")
    public boolean isEmployeeAvailableForTaskCategoryForTask(@RequestParam("taskCategoryId") Integer taskCategoryId) {
        return taskCategoryService.isEmployeeAvailableForTaskCategoryForTask(taskCategoryId);
    }

	@PostMapping("/saveTaskCategory")
	public ResponseEntity<ApiResponse<TaskCategoryDTO>> saveTaskCategory(@RequestBody TaskCategoryDTO dto,
			HttpServletRequest httpRequest) {

		try {

			ApiResponse<TaskCategoryDTO> response = taskCategoryService.addOrUpdate(dto, httpRequest);

			return ResponseEntity.ok(response);

		} catch (Exception e) {

			return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
		}
	}

	@GetMapping("/{taskcategoryId}")
	public ApiResponse<TaskCategoryDTO> getTaskCategoryById(@PathVariable Integer taskcategoryId) {

		try {
			return taskCategoryService.getById(taskcategoryId);

		} catch (Exception e) {
			return new ApiResponse<>(false, e.getMessage(), null);
		}
	}

	@PostMapping("/deleteTaskCategory")
	public ResponseEntity<ApiResponse<String>> deleteTaskCategory(@RequestBody TaskCategoryDTO dto,
			HttpServletRequest httpRequest) {

		return taskCategoryService.deleteTaskCategory(dto.getTaskcategoryId(), dto.getUserId(), httpRequest);
	}

	@GetMapping("/active")
	public List<TaskCategoryDTO> getActiveTaskCategories() {

		return taskCategoryService.getActiveTaskCategories();
	}

	@GetMapping("recurring/active")
	public List<TaskCategoryDTO> getActiveTaskCategoriesForRecurring(@RequestParam("isAdmin") String isAdmin,
			@RequestParam("loginType") String loginType, @RequestParam("userId") Integer userId,
			@RequestParam("isHod") String isHod) {

		return taskCategoryService.getActiveTaskCategoriesForRecurring(userId, isAdmin, loginType, isHod);
	}

	@GetMapping("recurring/category/employee-available")
	public boolean isEmployeeAvailableForTaskCategory(@RequestParam("taskCategoryId") Integer taskCategoryId) {
		return taskCategoryService.isEmployeeAvailableForTaskCategory(taskCategoryId);
	}

	@GetMapping("recurringForindex/active")
	public List<TaskCategoryDTO> getActiveTaskCategoriesForRecurringForIndex(@RequestParam("isAdmin") String isAdmin,
			@RequestParam("loginType") String loginType, @RequestParam("userId") Integer userId) {

		return taskCategoryService.getActiveTaskCategoriesForRecurringForIndex(userId, isAdmin, loginType);
	}

	@GetMapping("/department/{departmentId}")
	public List<TaskCategoryDTO> getCategoriesByDepartmentId(@PathVariable Integer departmentId) {

		return taskCategoryService.getCategoriesByDepartmentId(departmentId);
	}

	@GetMapping("/departments")
	public List<TaskCategoryDTO> getCategoriesByDepartmentIds(
			@RequestParam("departmentIds") List<Integer> departmentIds) {

		return taskCategoryService.getCategoriesByDepartmentIds(departmentIds);
	}
}
