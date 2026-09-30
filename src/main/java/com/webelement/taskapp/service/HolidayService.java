package com.webelement.taskapp.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.HolidayDTO;
import com.webelement.taskapp.entity.HolidayEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.HolidayRepo;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class HolidayService {

	@Autowired
	private HolidayRepo holidayRepository;

	@Autowired
	private CommonFunction commonFunction;

	@Value("${holiday_file_path}")
	private String uploadBasePath;
	
	@Value("${app.frontend.base-url:}")
	private String frontendBaseUrl;


	public ApiResponse<HolidayDTO> addOrUpdate(HolidayDTO dto, HttpServletRequest httpRequest) {
		Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

		String name = dto.getName() == null ? "" : dto.getName().trim();

		boolean isNew = dto.getHolidayId() == null || dto.getHolidayId() == 0;

		if (isNew) {
			if (holidayRepository.existsByNameIgnoreCaseAndStatusNot(name, 3)) {
				return new ApiResponse<>(false, "Holiday name already exists", null);
			}
		} else {
			boolean duplicateName = holidayRepository.existsByNameIgnoreCaseAndHolidayIdNotAndStatusNot(name,
					dto.getHolidayId(), 3);

			if (duplicateName) {
				return new ApiResponse<>(false, "Holiday name already exists", null);
			}
		}

		if (dto.getStartDate() != null) {
			if (isNew) {
				boolean duplicateStartDate = holidayRepository.existsByStartDateAndStatusNot(dto.getStartDate(), 3);

				if (duplicateStartDate) {
					return new ApiResponse<>(false, "A holiday already exists on the selected start date", null);
				}
			} else {
				boolean duplicateStartDate = holidayRepository
						.existsByStartDateAndHolidayIdNotAndStatusNot(dto.getStartDate(), dto.getHolidayId(), 3);

				if (duplicateStartDate) {
					return new ApiResponse<>(false, "A holiday already exists on the selected start date", null);
				}
			}
		}

		if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
			return new ApiResponse<>(false, "End date cannot be before start date", null);
		}

		HolidayEntity entity;

		if (!isNew) {
			entity = holidayRepository.findById(dto.getHolidayId())
					.orElseThrow(() -> new RuntimeException("Holiday Record not found"));

			entity.setName(name);
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setUserId(dto.getUserId());

			if (dto.getStatus() != null) {
				entity.setStatus(dto.getStatus());
			}

			entity.setModdate(timestamp);
		} else {
			entity = new HolidayEntity();

			entity.setName(name);
			entity.setStartDate(dto.getStartDate());
			entity.setEndDate(dto.getEndDate());
			entity.setUserId(dto.getUserId());
			entity.setStatus(1);
			entity.setRegdate(timestamp);
		}

		HolidayEntity saved = holidayRepository.save(entity);

		HolidayDTO h = new HolidayDTO();

		h.setHolidayId(saved.getHolidayId());
		h.setName(saved.getName());
		h.setStartDate(saved.getStartDate());
		h.setEndDate(saved.getEndDate());
		h.setUserId(saved.getUserId());
		h.setStatus(saved.getStatus());
		h.setRegdate(saved.getRegdate());
		h.setModdate(saved.getModdate());

		String action = isNew ? "Holiday Added" : "Holiday Updated";

		commonFunction.createHistoryAccess(dto.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, /* module id — see note below */ 0, saved.getHolidayId(), -1);

		return new ApiResponse<>(true, "Holiday saved successfully", h);
	}

	public ApiResponse<HolidayDTO> getById(Integer id) {

		HolidayEntity entity = holidayRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Holiday Record not found"));

		HolidayDTO h = new HolidayDTO();

		h.setHolidayId(entity.getHolidayId());
		h.setName(entity.getName());
		h.setStartDate(entity.getStartDate());
		h.setEndDate(entity.getEndDate());
		h.setUserId(entity.getUserId());
		h.setStatus(entity.getStatus());
		h.setRegdate(entity.getRegdate());
		h.setModdate(entity.getModdate());

		List<TransactionEntity> history = commonFunction.getTransactionLogs(0, id);

		if (history != null && !history.isEmpty()) {
			h.setTransactionHistory(history);
		}

		return new ApiResponse<>(true, "Holiday fetched successfully", h);
	}

//	public Page<HolidayDTO> findHolidayDetails(int page, int size, int statusIndex, String search) {
//		return holidayRepository.findHolidayDetails(PageRequest.of(page, size), statusIndex, search);
//	}

	public Page<HolidayDTO> findHolidayDetails(int page, int size, int statusIndex, String search, LocalDate fromDate,
			LocalDate toDate) {

		return holidayRepository.findHolidayDetails(PageRequest.of(page, size), statusIndex, search, fromDate, toDate);
	}

	public ResponseEntity<ApiResponse<String>> deleteHoliday(Integer holidayId, Integer userId,
			HttpServletRequest httpRequest) {

		Optional<HolidayEntity> existingHoliday = holidayRepository.findById(holidayId);

		if (!existingHoliday.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse<>(false, "Holiday not found", null));
		}

		int updatedRows = holidayRepository.softDelete(3, holidayId);

		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Holiday Deleted", 0, holidayId, -1);

			return ResponseEntity.ok(new ApiResponse<>(true, "Holiday deleted successfully", null));

		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse<>(false, "Failed to delete holiday", null));
		}
	}

	public List<HolidayDTO> getActiveHolidays() {
		List<HolidayEntity> holidays = holidayRepository.findByStatus(1);

		return holidays.stream().map(h -> new HolidayDTO(h.getHolidayId(), h.getName())).collect(Collectors.toList());
	}

	public List<HolidayEntity> readHolidaysFromExcel(MultipartFile file) throws IOException {
		List<HolidayEntity> holidays = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

			Sheet sheet = workbook.getSheetAt(0);

			if (sheet == null || sheet.getLastRowNum() < 1) {
				throw new IllegalArgumentException("Excel file is empty");
			}

			validateHolidayExcelHeaders(sheet.getRow(0));

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);

				if (row == null || isEmptyHolidayRow(row)) {
					continue;
				}

				int excelRow = i + 1;

				HolidayEntity holiday = new HolidayEntity();

				holiday.setName(getCellValue(row, 1).trim());
				holiday.setStartDate(getHolidayDateCellValue(row, 2, "Start Date", excelRow));
				holiday.setEndDate(getHolidayDateCellValue(row, 3, "End Date", excelRow));
				holiday.setStatus(getHolidayStatusCellValue(row, 4, excelRow));

				holidays.add(holiday);
			}
		}

		return holidays;
	}

	@Transactional
	public ApiResponse<List<HolidayEntity>> saveHolidaysFromExcel(List<HolidayEntity> holidays, Integer userId,
			HttpServletRequest request) {

		List<HolidayEntity> savedHolidays = new ArrayList<>();
		List<Map<String, String>> failedRecords = new ArrayList<>();

		Timestamp now = new Timestamp(System.currentTimeMillis());

		Set<LocalDate> excelStartDates = new HashSet<>();

		for (int rowIndex = 0; rowIndex < holidays.size(); rowIndex++) {
			HolidayEntity excelHoliday = holidays.get(rowIndex);

			int excelRow = rowIndex + 2;

			String holidayName = excelHoliday.getName() != null ? excelHoliday.getName().trim() : "";
			String rowIdentifier = !holidayName.isEmpty() ? holidayName : "Row " + excelRow;

			List<String> reasons = new ArrayList<>();

			try {
				if (holidayName.isEmpty()) {
					reasons.add("Holiday Name is required");

				} else if (holidayName.length() > 200) {
					reasons.add("Holiday Name must not exceed 200 characters");
				} else {
					excelHoliday.setName(holidayName);
				}

				LocalDate startDate = excelHoliday.getStartDate();

				if (startDate == null) {
					reasons.add("Start Date is required");
				} else {
					if (!excelStartDates.add(startDate)) {
						reasons.add("Duplicate Start Date in uploaded file: "
								+ startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
					}
				}

				LocalDate endDate = excelHoliday.getEndDate();

				if (endDate == null) {
					reasons.add("End Date is required");
				} else if (startDate != null && endDate.isBefore(startDate)) {
					reasons.add("End Date cannot be before Start Date");
				}

				Integer status = excelHoliday.getStatus();

				if (status == null) {
					reasons.add("Status is required. " + "Allowed values: Active/Inactive or 1/2");
				} else if (status != 1 && status != 2) {
					reasons.add("Invalid Status. " + "Allowed values: Active/Inactive or 1/2");
				}

				if (!reasons.isEmpty()) {
					addFailedHolidayRecord(failedRecords, excelHoliday, String.join("; ", reasons));

					continue;
				}

				Optional<HolidayEntity> existingOptional = holidayRepository
						.findByNameIgnoreCaseAndStatusNot(holidayName, 3);

				HolidayEntity holiday;

				if (existingOptional.isPresent()) {
					holiday = existingOptional.get();

					boolean startDateUsedByAnother = holidayRepository
							.existsByStartDateAndHolidayIdNotAndStatusNot(startDate, holiday.getHolidayId(), 3);

					if (startDateUsedByAnother) {
						addFailedHolidayRecord(failedRecords, excelHoliday,
								"Start Date " + startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
										+ " already exists for another holiday");

						continue;
					}
				} else {
					boolean startDateExists = holidayRepository.existsByStartDateAndStatusNot(startDate, 3);

					if (startDateExists) {
						addFailedHolidayRecord(failedRecords, excelHoliday, "Start Date "
								+ startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " already exists");

						continue;
					}

					holiday = new HolidayEntity();

					holiday.setUserId(userId);
					holiday.setRegdate(now);
				}

				holiday.setName(holidayName);
				holiday.setStartDate(startDate);
				holiday.setEndDate(endDate);
				holiday.setStatus(status);
				holiday.setModdate(now);

				HolidayEntity saved = holidayRepository.save(holiday);

				savedHolidays.add(saved);

			} catch (Exception e) {
				String reason = e.getMessage() != null ? e.getMessage() : "Unknown error";

				addFailedHolidayRecord(failedRecords, excelHoliday, "Exception: " + reason);
			}
		}

		for (HolidayEntity saved : savedHolidays) {
			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(request),
					commonFunction.getLocalIp(), "Holiday Added/Updated from Excel", 8, saved.getHolidayId(), -1);
		}

		String message = String.format("Holidays processed. Saved/Updated: %d, Failed: %d", savedHolidays.size(),
				failedRecords.size());

		String downloadPath = null;

		if (!failedRecords.isEmpty()) {
			try {
				String fileName = "failed_holidays_" + System.currentTimeMillis() + ".xlsx";

				File dir = new File(uploadBasePath);

				if (!dir.exists()) {
					dir.mkdirs();
				}

				String fullPath = uploadBasePath + File.separator + fileName;

				exportFailedHolidayRecordsToExcel(failedRecords, fullPath);

<<<<<<< HEAD
				downloadPath = ServletUriComponentsBuilder.fromCurrentContextPath().path("/uploads/").path(fileName).toUriString();

=======
//				downloadPath = ServletUriComponentsBuilder.fromCurrentContextPath().path("/uploads/").path(fileName)
//						.toUriString();
				
				downloadPath = buildDownloadPath(fileName);   // <-- changed
				
>>>>>>> 65bdd1d9edb8813d2469b7748a782157140ac361
				message += ". Failed records exported.";
			} catch (Exception e) {
				message += ". Failed to export failed records: " + e.getMessage();
			}
		}

		System.err.println("File Path = " + uploadBasePath);
		System.err.println("downloadPath = " + downloadPath);
		return new ApiResponse<>(true, message, savedHolidays, downloadPath);
	}
	
	private String buildDownloadPath(String fileName) {

		UriComponentsBuilder builder = (frontendBaseUrl != null && !frontendBaseUrl.trim().isEmpty())
				? UriComponentsBuilder.fromHttpUrl(frontendBaseUrl.trim())   // frontend host
				: ServletUriComponentsBuilder.fromCurrentContextPath();       // fallback: API host

		return builder.path("/uploads/").path(fileName).build().encode().toUriString();
	}

	private void addFailedHolidayRecord(List<Map<String, String>> failedRecords, HolidayEntity holiday, String reason) {
		Map<String, String> map = new LinkedHashMap<>();

		map.put("Holiday Name", holiday.getName() != null ? holiday.getName() : "");
		map.put("Start Date",
				holiday.getStartDate() != null
						? holiday.getStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
						: "");
		map.put("End Date",
				holiday.getEndDate() != null ? holiday.getEndDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
						: "");
		String status = "";

		if (holiday.getStatus() != null) {
			if (holiday.getStatus() == 1) {
				status = "Active";
			} else if (holiday.getStatus() == 2) {
				status = "Inactive";
			} else {
				status = holiday.getStatus().toString();
			}
		}

		map.put("Status", status);
		map.put("Upload Status", "Unsuccessful");
		map.put("Reason", reason != null ? reason : "");

		failedRecords.add(map);
	}

	private void exportFailedHolidayRecordsToExcel(List<Map<String, String>> failedRecords, String fullPath)
			throws IOException {

		if (failedRecords == null || failedRecords.isEmpty()) {
			return;
		}

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Failed Records");

			Map<String, String> first = failedRecords.get(0);

			List<String> headers = new ArrayList<>(first.keySet());

			Row headerRow = sheet.createRow(0);

			for (int c = 0; c < headers.size(); c++) {
				Cell cell = headerRow.createCell(c);
				cell.setCellValue(headers.get(c));
			}

			for (int r = 0; r < failedRecords.size(); r++) {
				Row row = sheet.createRow(r + 1);

				Map<String, String> record = failedRecords.get(r);

				for (int c = 0; c < headers.size(); c++) {
					String key = headers.get(c);
					String value = record.getOrDefault(key, "");

					row.createCell(c).setCellValue(value);
				}
			}

			for (int c = 0; c < headers.size(); c++) {
				sheet.autoSizeColumn(c);
			}

			File outFile = new File(fullPath);

			outFile.getParentFile().mkdirs();

			try (FileOutputStream fos = new FileOutputStream(outFile)) {
				workbook.write(fos);
			}
		}
	}

	private void validateHolidayExcelHeaders(Row headerRow) {
		if (headerRow == null) {
			throw new IllegalArgumentException("Excel header row is missing");
		}

		String[] expectedHeaders = { "Sr No", "Holiday Name", "Start Date", "End Date", "Status" };

		for (int i = 0; i < expectedHeaders.length; i++) {
			String actualHeader = getCellValue(headerRow, i);

			String expectedHeader = expectedHeaders[i];

			if (!expectedHeader.equalsIgnoreCase(actualHeader != null ? actualHeader.trim() : "")) {
				throw new IllegalArgumentException("Invalid Excel header at column " + (i + 1) + ". Expected: "
						+ expectedHeader + ", Found: " + actualHeader);
			}
		}
	}

	private LocalDate getHolidayDateCellValue(Row row, int cellIndex, String fieldName, int excelRow) {

		Cell cell = row.getCell(cellIndex);

		if (cell == null || cell.getCellType() == CellType.BLANK) {
			throw new IllegalArgumentException(fieldName + " is required at Excel row " + excelRow);
		}

		if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {

			return cell.getLocalDateTimeCellValue().toLocalDate();
		}

		String value = new DataFormatter().formatCellValue(cell).trim();

		if (value.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required at Excel row " + excelRow);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

		try {
			return LocalDate.parse(value, formatter);

		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid " + fieldName + " at Excel row " + excelRow + ": " + value
					+ ". Expected format dd-MM-yyyy");
		}
	}

	private Integer getHolidayStatusCellValue(Row row, int cellIndex, int excelRow) {
		String value = getCellValue(row, cellIndex);

		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Status is required at Excel row " + excelRow);
		}

		value = value.trim().toLowerCase();

		switch (value) {

		case "1":
		case "active":
			return 1;

		case "2":
		case "inactive":
			return 2;

		default:
			throw new IllegalArgumentException("Invalid Status at Excel row " + excelRow + ": " + value
					+ ". Allowed values: Active/Inactive or 1/2");
		}
	}

	private String getCellValue(Row row, int cellIndex) {
		Cell cell = row.getCell(cellIndex);

		if (cell == null) {
			return "";
		}

		DataFormatter formatter = new DataFormatter();

		return formatter.formatCellValue(cell).trim();
	}

	private boolean isEmptyHolidayRow(Row row) {
		if (row == null) {
			return true;
		}

		for (int i = 0; i <= 4; i++) {
			Cell cell = row.getCell(i);

			if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(row, i).trim().isEmpty()) {
				return false;
			}
		}

		return true;
	}

}