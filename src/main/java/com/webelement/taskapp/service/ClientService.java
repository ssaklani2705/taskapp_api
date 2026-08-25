package com.webelement.taskapp.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ClientDTO;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.entity.StateEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.StateRepository;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class ClientService {

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private UserLoginRepository userLoginRepository;

	@Autowired
	private CommonFunction commonFunction;

	@Value("${client_file_path}")
	private String uploadBasePath;

	public ApiResponse<ClientEntity> addOrUpdateClient(ClientEntity client, HttpServletRequest httpRequest) {

		boolean isNew = (client.getClientId() == null || client.getClientId() == 0);

		if (client.getStateId() != null && !stateRepository.existsById(client.getStateId())) {

			throw new RuntimeException("Invalid stateId: " + client.getStateId());
		}

		if (client.getManagerId() != null && !userLoginRepository.existsById(client.getManagerId())) {

			throw new RuntimeException("Invalid managerId: " + client.getManagerId());
		}

		if (isNew) {
			if (client.getCode() != null && clientRepository.existsByCode(client.getCode())) {

				throw new RuntimeException("Client code already exists: " + client.getCode());
			}

			Timestamp now = new Timestamp(System.currentTimeMillis());

			if (client.getGstFlag() == null) {
				client.setGstFlag((short) 0);
			}

			if (client.getTaxFlag() == null) {
				client.setTaxFlag((short) 0);
			}

			client.setRegdate(now);
			client.setModdate(now);
		} else {

			ClientEntity existingClient = clientRepository.findById(client.getClientId())
					.orElseThrow(() -> new RuntimeException("Client not found with id: " + client.getClientId()));

			if (client.getCode() != null
					&& clientRepository.existsByCodeAndClientIdNot(client.getCode(), client.getClientId())) {

				throw new RuntimeException("Client code already exists: " + client.getCode());
			}

			client.setRegdate(existingClient.getRegdate());

			client.setModdate(new Timestamp(System.currentTimeMillis()));

			if (client.getGstFlag() == null) {
				client.setGstFlag(existingClient.getGstFlag());
			}

			if (client.getTaxFlag() == null) {
				client.setTaxFlag(existingClient.getTaxFlag());
			}
		}

		ClientEntity savedClient = clientRepository.save(client);

		String action = isNew ? "Client Added" : "Client Updated";

		commonFunction.createHistoryAccess(savedClient.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), action, 8, // moduleType = Client
				savedClient.getClientId(), -1);

		return new ApiResponse<>(true, isNew ? "Client added successfully" : "Client updated successfully",
				savedClient);
	}

	public ClientDTO getClientDetailsById(int clientId) {

		ClientDTO client = clientRepository.getClientById(clientId);

		if (client != null) {
			List<TransactionEntity> history = getTransactionLogs(8, clientId);
			client.setTransactionHistory(history);
		}

		return client;
	}

	public Page<ClientDTO> findClientDetails(Pageable pageable, Short status, Integer managerId, Integer stateId,
			String clientName, String clientCode, String contactName, String contactEmail, String search) {

		Short statusFilter = (status != null && status == 0) ? null : status;

		Integer managerFilter = (managerId != null && managerId == 0) ? null : managerId;

		Integer stateFilter = (stateId != null && stateId == 0) ? null : stateId;

		return clientRepository.findClientDetails(pageable, statusFilter, managerFilter, stateFilter, clientName,
				clientCode, contactName, contactEmail, search);
	}

	@Transactional
	public ResponseEntity<ResponseApi<String>> deleteClient(Integer clientId, Integer userId,
			HttpServletRequest httpRequest) {

		int updatedRows = clientRepository.deleteClient((short) 3, clientId);

		if (updatedRows > 0) {
			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
					commonFunction.getLocalIp(), "Client Deleted", 8, clientId, -1);
			return ResponseEntity.ok(new ResponseApi<>(true, "Client deleted successfully", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseApi<>(false, "Failed to delete Client", null));
		}
	}

	public List<ClientEntity> readClientsFromExcel(MultipartFile file) throws IOException {

		List<ClientEntity> clients = new ArrayList<>();

		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

			Sheet sheet = workbook.getSheetAt(0);

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);

				if (row == null) {
					continue;
				}

				int rowNum = i + 1;

				ClientEntity client = new ClientEntity();

				client.setName(toCamelCase(getCellValue(row, 0)));
				client.setCode(getCellValue(row, 1));
				client.setPan(getCellValue(row, 2));
				client.setGstFlag(getShortCellValue(row, 3));
				client.setGstNo(getCellValue(row, 4));
				String rawStateName = getCellValue(row, 5);

				String stateName = rawStateName == null ? null
						: rawStateName.replace("\u00A0", "").replace("\n", "").replace("\t", "").replaceAll("\\s+", " ")
								.trim();

				if (stateName == null || stateName.isEmpty()) {
					client.setStateId(null);
				} else {
					StateEntity state = stateRepository.findByNameIgnoreCase(stateName)
							.orElseThrow(() -> new IllegalArgumentException(
									"Invalid State name at Excel row " + rowNum + ": [" + stateName + "]"));

					client.setStateId(state.getStateId());
				}

				client.setAddressLine1(getCellValue(row, 6));
				client.setAddressLine2(getCellValue(row, 7));
				client.setCity(getCellValue(row, 8));
				client.setPincode(getCellValue(row, 9));
				client.setContactName(toCamelCase(getCellValue(row, 10)));
				client.setContactEmail(getCellValue(row, 11));
				client.setEmails(getCellValue(row, 12));
				client.setStartDate(getDateCellValue(row, 13));
				client.setMonthlyCharge(getDoubleCellValue(row, 14));
				client.setOutstanding(getDoubleCellValue(row, 15));
				client.setName1(getCellValue(row, 16));
				client.setEmailId1(getCellValue(row, 17));
				client.setName2(getCellValue(row, 18));
				client.setEmailId2(getCellValue(row, 19));
				client.setName3(getCellValue(row, 20));
				client.setEmailId3(getCellValue(row, 21));
				String managerName = getCellValue(row, 22);

				if (managerName != null && !managerName.trim().isEmpty()) {

					Integer managerId = userLoginRepository.findIdByName(managerName.trim());

					if (managerId != null) {
						client.setManagerId(managerId);
					} else {
						throw new IllegalArgumentException(
								"Invalid Manager name at Excel row " + rowNum + ": [" + managerName + "]");
					}
				}

				client.setTaxFlag(getShortCellValue(row, 23));
				client.setLocation(getCellValue(row, 24));

				clients.add(client);
			}
		}

		return clients;
	}

	@Transactional
	public ApiResponse<List<ClientEntity>> saveClientsFromExcel(List<ClientEntity> clients, Integer userId,
			HttpServletRequest request) {

		List<ClientEntity> validClients = new ArrayList<>();

		List<ClientEntity> savedClients = new ArrayList<>();

		List<String> skipped = Collections.synchronizedList(new ArrayList<>());

		List<Map<String, String>> failedRecords = Collections.synchronizedList(new ArrayList<>());

		Timestamp now = new Timestamp(System.currentTimeMillis());

		List<String> allNames = clients.stream().map(ClientEntity::getName).filter(Objects::nonNull).map(String::trim)
				.filter(s -> !s.isEmpty()).collect(Collectors.toList());

		List<String> allCodes = clients.stream().map(ClientEntity::getCode).filter(Objects::nonNull).map(String::trim)
				.filter(s -> !s.isEmpty()).collect(Collectors.toList());

		List<String> allGsts = clients.stream().map(ClientEntity::getGstNo).filter(Objects::nonNull).map(String::trim)
				.filter(s -> !s.isEmpty()).collect(Collectors.toList());

		Set<String> existingNames = new HashSet<>(clientRepository.findExistingNames(allNames));

		Set<String> existingCodes = new HashSet<>(clientRepository.findExistingCodes(allCodes));

		Set<String> existingGsts = clientRepository.findAllGstsNormalized();

		Set<String> excelNames = new HashSet<>();

		Set<String> excelCodes = new HashSet<>();

		Set<String> excelGsts = new HashSet<>();

		for (int rowIndex = 0; rowIndex < clients.size(); rowIndex++) {

			ClientEntity client = clients.get(rowIndex);

			String rowIdentifier = client.getName() != null ? client.getName() : "(Row " + (rowIndex + 2) + ")";

			try {
				String name = client.getName() != null ? client.getName().trim() : "";

				if (name.isEmpty()) {

					skipped.add(rowIdentifier + " (Client Name Required)");
					addFailedRecord(failedRecords, client, "Client Name is required");

					continue;
				}

				if (name.length() < 2 || name.length() > 100) {

					skipped.add(rowIdentifier + " (Invalid Client Name)");
					addFailedRecord(failedRecords, client, "Client Name must be between 2 to 100 characters");

					continue;
				}

				client.setName(name);

				String code = client.getCode() != null ? client.getCode().trim() : "";

				if (!code.isEmpty()) {

					if (existingCodes.contains(code.toLowerCase())) {

						skipped.add(rowIdentifier + " (Duplicate Client Code)");
						addFailedRecord(failedRecords, client, "Client Code already exists");

						continue;
					}

					if (!excelCodes.add(code.toLowerCase())) {

						skipped.add(rowIdentifier + " (Duplicate Client Code in File)");
						addFailedRecord(failedRecords, client, "Duplicate Client Code in uploaded file");

						continue;
					}

					client.setCode(code);
				}

				if (existingNames.contains(name.toLowerCase())) {

					skipped.add(rowIdentifier + " (Duplicate Client Name)");
					addFailedRecord(failedRecords, client, "Client with this name already exists");

					continue;
				}

				if (!excelNames.add(name.toLowerCase())) {

					skipped.add(rowIdentifier + " (Duplicate Client Name in File)");
					addFailedRecord(failedRecords, client, "Duplicate Client Name in uploaded file");

					continue;
				}

				String gst = client.getGstNo() != null ? client.getGstNo().trim().toUpperCase() : "";

				if (!gst.isEmpty()) {

					if (!gst.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) {

						skipped.add(rowIdentifier + " (Invalid GST)");
						addFailedRecord(failedRecords, client, "Invalid GST format");

						continue;
					}

					if (existingGsts.contains(gst)) {

						skipped.add(rowIdentifier + " (Duplicate GST in DB)");
						addFailedRecord(failedRecords, client, "GST number already exists");

						continue;
					}

					if (!excelGsts.add(gst)) {

						skipped.add(rowIdentifier + " (Duplicate GST in File)");
						addFailedRecord(failedRecords, client, "Duplicate GST in uploaded file");

						continue;
					}

					client.setGstNo(gst);
				}

				String pan = client.getPan() != null ? client.getPan().trim().toUpperCase() : "";

				if (!pan.isEmpty() && !pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) {

					skipped.add(rowIdentifier + " (Invalid PAN)");
					addFailedRecord(failedRecords, client, "Invalid PAN format");

					continue;
				}

				client.setPan(pan.isEmpty() ? null : pan);

				String contactEmail = client.getContactEmail() != null ? client.getContactEmail().trim() : "";

				if (!contactEmail.isEmpty() && !contactEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

					skipped.add(rowIdentifier + " (Invalid Contact Email)");
					addFailedRecord(failedRecords, client, "Invalid Contact Email");

					continue;
				}

				client.setContactEmail(contactEmail.isEmpty() ? null : contactEmail);

				if (client.getStateId() == null || client.getStateId() == 0) {

					skipped.add(rowIdentifier + " (State Required)");
					addFailedRecord(failedRecords, client, "State is required");

					continue;
				}

				String pincode = client.getPincode() != null ? client.getPincode().trim() : "";

				if (!pincode.isEmpty() && !pincode.matches("^[0-9]{6}$")) {

					skipped.add(rowIdentifier + " (Invalid Pincode)");
					addFailedRecord(failedRecords, client, "Pincode must be 6 digits");

					continue;
				}

				client.setPincode(pincode.isEmpty() ? null : pincode);

				if (client.getGstFlag() == null) {
					client.setGstFlag((short) 0);
				}

				if (client.getTaxFlag() == null) {
					client.setTaxFlag((short) 0);
				}

				client.setStatus((short) 1);
				client.setUserId(userId);
				client.setRegdate(now);
				client.setModdate(now);

				validClients.add(client);

			} catch (Exception e) {

				String reason = e.getMessage() != null ? e.getMessage() : "Unknown error";

				skipped.add(rowIdentifier + " (Error: " + reason + ")");
				addFailedRecord(failedRecords, client, "Exception: " + reason);
			}
		}

		if (!validClients.isEmpty()) {

			savedClients = clientRepository.saveAll(validClients);
		}

		for (ClientEntity saved : savedClients) {

			commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(request),
					commonFunction.getLocalIp(), "Client Added from Excel", 8, saved.getClientId(), -1);
		}

		String msg = String.format("Clients processed. Saved: %d, Failed: %d", savedClients.size(),
				failedRecords.size());

		String downloadPath = null;

		if (!failedRecords.isEmpty()) {

			try {

				String fileName = "failed_clients_" + System.currentTimeMillis() + ".xlsx";

				File dir = new File(uploadBasePath);

				if (!dir.exists()) {
					dir.mkdirs();
				}

				String fullPath = uploadBasePath + File.separator + fileName;

				exportFailedRecordsToExcel(failedRecords, fullPath);

				downloadPath = ServletUriComponentsBuilder.fromCurrentContextPath().path("/uploads/").path(fileName)
						.toUriString();

				msg += ". Failed records exported.";

			} catch (Exception e) {

				msg += ". Failed to export: " + e.getMessage();
			}
		}

		return new ApiResponse<>(true, msg, savedClients, downloadPath);
	}

	private void addFailedRecord(List<Map<String, String>> failedRecords, ClientEntity client, String reason) {

		Map<String, String> map = new LinkedHashMap<>();

		map.put("Client Name", client.getName() != null ? client.getName() : "");
		map.put("Client Code", client.getCode() != null ? client.getCode() : "");
		map.put("PAN", client.getPan() != null ? client.getPan() : "");
		map.put("GST Flag", client.getGstFlag() != null ? client.getGstFlag().toString() : "");
		map.put("GST No", client.getGstNo() != null ? client.getGstNo() : "");
		map.put("State ID", client.getStateId() != null ? client.getStateId().toString() : "");
		map.put("Address Line 1", client.getAddressLine1() != null ? client.getAddressLine1() : "");
		map.put("Address Line 2", client.getAddressLine2() != null ? client.getAddressLine2() : "");
		map.put("City", client.getCity() != null ? client.getCity() : "");
		map.put("Pincode", client.getPincode() != null ? client.getPincode() : "");
		map.put("Contact Name", client.getContactName() != null ? client.getContactName() : "");
		map.put("Contact Email", client.getContactEmail() != null ? client.getContactEmail() : "");
		map.put("Emails", client.getEmails() != null ? client.getEmails() : "");
		map.put("Start Date",
				client.getStartDate() != null ? new SimpleDateFormat("dd-MM-yyyy").format(client.getStartDate()) : "");
		map.put("Monthly Charge", client.getMonthlyCharge() != null ? client.getMonthlyCharge().toString() : "");
		map.put("Outstanding", client.getOutstanding() != null ? client.getOutstanding().toString() : "");
		map.put("Name 1", client.getName1() != null ? client.getName1() : "");
		map.put("Email ID 1", client.getEmailId1() != null ? client.getEmailId1() : "");
		map.put("Name 2", client.getName2() != null ? client.getName2() : "");
		map.put("Email ID 2", client.getEmailId2() != null ? client.getEmailId2() : "");
		map.put("Name 3", client.getName3() != null ? client.getName3() : "");
		map.put("Email ID 3", client.getEmailId3() != null ? client.getEmailId3() : "");
		map.put("Manager ID", client.getManagerId() != null ? client.getManagerId().toString() : "");
		map.put("Tax Flag", client.getTaxFlag() != null ? client.getTaxFlag().toString() : "");
		map.put("Location", client.getLocation() != null ? client.getLocation() : "");
		map.put("Status", "Unsuccessful");
		map.put("Reason", reason);

		failedRecords.add(map);
	}

	private void exportFailedRecordsToExcel(List<Map<String, String>> failedRecords, String fullPath)
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

	private String getCellValue(Row row, int cellIndex) {

		Cell cell = row.getCell(cellIndex);

		if (cell == null) {
			return "";
		}

		DataFormatter formatter = new DataFormatter();

		return formatter.formatCellValue(cell).trim();
	}

	private Short getShortCellValue(Row row, int cellIndex) {

		String value = getCellValue(row, cellIndex);

		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		try {
			return Short.valueOf(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid numeric value at column " + (cellIndex + 1) + ": " + value);
		}
	}

	private Double getDoubleCellValue(Row row, int cellIndex) {

		String value = getCellValue(row, cellIndex);

		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		try {
			return Double.valueOf(value.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid amount at column " + (cellIndex + 1) + ": " + value);
		}
	}

	private Date getDateCellValue(Row row, int cellIndex) {

		Cell cell = row.getCell(cellIndex);

		if (cell == null) {
			return null;
		}

		if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {

			return cell.getDateCellValue();
		}

		String value = getCellValue(row, cellIndex);

		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		List<String> patterns = Arrays.asList("dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd");

		for (String pattern : patterns) {

			try {

				SimpleDateFormat sdf = new SimpleDateFormat(pattern);

				sdf.setLenient(false);

				return sdf.parse(value.trim());

			} catch (ParseException ignored) {
			}
		}

		throw new IllegalArgumentException("Invalid date format: " + value + ". Expected dd-MM-yyyy");
	}

	private String toCamelCase(String input) {
		if (input == null || input.trim().isEmpty()) {
			return input;
		}
		return Arrays.stream(input.trim().toLowerCase().split("\\s+"))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
				.collect(Collectors.joining(" "));
	}

	public List<TransactionEntity> getTransactionLogs(int moduleId, Integer recordId) {
		List<Object[]> results = userLoginRepository.getTransactionLogs(moduleId, recordId);
		return results.stream().map(obj -> {
			TransactionEntity dto = new TransactionEntity();
			dto.setEntryDate((String) obj[0]);
			dto.setName((String) obj[1]);
			dto.setAction((String) obj[2]);
			dto.setUserId(obj[3] != null ? ((Number) obj[3]).intValue() : null);
			dto.setFlag((String) obj[4]);
			return dto;
		}).collect(Collectors.toList());
	}
}
