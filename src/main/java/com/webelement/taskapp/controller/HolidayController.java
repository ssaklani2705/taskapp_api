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
import com.webelement.taskapp.dto.HolidayDTO;
import com.webelement.taskapp.service.HolidayService;

@RestController
@RequestMapping("/admin/holiday")
@CrossOrigin(origins = { "http://localhost:4500", "https://app.webelement.cc", "https://13.202.30.190" })
public class HolidayController {

	@Autowired
	private HolidayService holidayService;

	@GetMapping("/getHolidayDetails")
	public Map<String, Object> findHolidayDetails(@RequestParam int page, @RequestParam int size,
			@RequestParam int statusIndex, @RequestParam(required = false) String search) {

		Page<HolidayDTO> holidayPage = holidayService.findHolidayDetails(page, size, statusIndex, search);

		Map<String, Object> response = new HashMap<>();

		response.put("data", holidayPage.getContent());

		response.put("totalElements", holidayPage.getTotalElements());

		return response;
	}

	@PostMapping("/saveHoliday")
	public ResponseEntity<ApiResponse<HolidayDTO>> saveHoliday(@RequestBody HolidayDTO dto,
			HttpServletRequest httpRequest) {

		try {

			ApiResponse<HolidayDTO> response = holidayService.addOrUpdate(dto, httpRequest);

			return ResponseEntity.ok(response);

		} catch (Exception e) {

			return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
		}
	}

	@GetMapping("/{holidayId}")
	public ApiResponse<HolidayDTO> getHolidayById(@PathVariable Integer holidayId) {

		try {

			return holidayService.getById(holidayId);

		} catch (Exception e) {

			return new ApiResponse<>(false, e.getMessage(), null);
		}
	}

	@PostMapping("/deleteHoliday")
	public ResponseEntity<ApiResponse<String>> deleteHoliday(@RequestBody HolidayDTO dto,
			HttpServletRequest httpRequest) {

		return holidayService.deleteHoliday(dto.getHolidayId(), dto.getUserId(), httpRequest);
	}

	@GetMapping("/active")
	public List<HolidayDTO> getActiveHolidays() {

		return holidayService.getActiveHolidays();
	}
}