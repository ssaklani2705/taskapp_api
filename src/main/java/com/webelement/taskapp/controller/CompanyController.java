package com.webelement.taskapp.controller;

import java.util.List;

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
import com.webelement.taskapp.dto.CompanyDTO;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.entity.CompanyEntity;
import com.webelement.taskapp.entity.CompanyShiftEntity;
import com.webelement.taskapp.service.CompanyService;
import com.webelement.taskapp.service.DepartmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/company")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class CompanyController {
		private final CompanyService companyService;

		@PostMapping("/add_or_update")
		public ResponseEntity<ApiResponse<CompanyDTO>> addOrUpdate(@RequestBody CompanyDTO request) {
			ApiResponse<CompanyDTO> response = companyService.addOrUpdate(request);
			return ResponseEntity.ok(response);
		}

	    @PostMapping("/delete_company")
	    public ResponseEntity<ApiResponse<CompanyDTO>> delete(
	            @RequestBody CompanyDTO request) {

	        ApiResponse<CompanyDTO> response =
	                companyService.delete(request);

	        return ResponseEntity.ok(response);
	    }

	    @GetMapping("/find_by_id/{companyId}")
	    public ResponseEntity<ApiResponse<CompanyDTO>> getById(@PathVariable Integer companyId) {
	        ApiResponse<CompanyDTO> response =companyService.getById(companyId);
	        return ResponseEntity.ok(response);
	    }

		@GetMapping("/company_list")
		public ResponseEntity<Page<CompanyDTO>> findCompanyList(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "1") int statusIndex,@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "0") Integer stateId,
				@RequestParam(defaultValue = "0") Integer companySiteOffice,
		        @RequestParam(defaultValue = "") String workingDays) {

			Page<CompanyDTO> response = companyService.findCompanyList(page, size, statusIndex, search,stateId,companySiteOffice,
		            workingDays);

			return ResponseEntity.ok(response);
		}
		
		
		@GetMapping("/active_company")
		public ResponseEntity<List<CompanyEntity>> findActiveCompany() {
			return companyService.findActiveCompany();
		}
		
		@GetMapping("/company_shift/{companyId}")
		public ResponseEntity<List<CompanyShiftEntity>> findActiveCompanyShift(@PathVariable Integer companyId) {
			return companyService.findActiveCompanyShift(companyId);
		}
}
