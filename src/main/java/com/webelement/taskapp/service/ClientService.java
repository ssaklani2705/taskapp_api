package com.webelement.taskapp.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

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

import com.webelement.taskapp.Exceptions.ClientValidationException;
import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.ApiResponse;
import com.webelement.taskapp.dto.ClientDTO;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.entity.StateEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.PlanRepo;
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
	private PlanRepo planRepo;

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

		if (client.getPlanId() != null && !planRepo.existsById(client.getPlanId())) {
			throw new RuntimeException("Invalid planId: " + client.getPlanId());
		}

		if (client.getOutstanding() != null && client.getOutstanding() < 0) {
			throw new ClientValidationException("Outstanding amount cannot be negative.");
		}

		if (client.getPan() != null) {
			client.setPan(client.getPan().trim());
		}

		if (client.getGstNo() != null) {
			client.setGstNo(client.getGstNo().trim());
		}

		if (client.getCode() != null) {
			client.setCode(client.getCode().trim());
		}

		if (isNew) {
			if (client.getCode() != null && clientRepository.existsByCode(client.getCode())) {
				throw new ClientValidationException("Client code already exists: " + client.getCode());
			}

			if (client.getPan() != null && !client.getPan().isEmpty()
					&& clientRepository.existsByPan(client.getPan())) {

				throw new ClientValidationException("PAN already exists: " + client.getPan());
			}

			if (client.getGstNo() != null && !client.getGstNo().isEmpty()
					&& clientRepository.existsByGstNo(client.getGstNo())) {

				throw new ClientValidationException("GST number already exists: " + client.getGstNo());
			}

			Timestamp now = new Timestamp(System.currentTimeMillis());

			client.setStatus((short) 1);

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

				throw new ClientValidationException("Client code already exists: " + client.getCode());
			}

			if (client.getPan() != null && !client.getPan().isEmpty()
					&& clientRepository.existsByPanAndClientIdNot(client.getPan(), client.getClientId())) {

				throw new ClientValidationException("PAN already exists: " + client.getPan());
			}

			if (client.getGstNo() != null && !client.getGstNo().isEmpty()
					&& clientRepository.existsByGstNoAndClientIdNot(client.getGstNo(), client.getClientId())) {

				throw new ClientValidationException("GST number already exists: " + client.getGstNo());
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
				commonFunction.getLocalIp(), action, 8, savedClient.getClientId(), -1);

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
			Short gstFlag, Short taxFlag, Integer planId, LocalDate fromDate, LocalDate toDate, String clientName,
			String clientCode, String contactName, String contactEmail, String search) {

		Short statusFilter = (status != null && status == 0) ? null : status;
		Integer managerFilter = (managerId != null && managerId == 0) ? null : managerId;
		Integer stateFilter = (stateId != null && stateId == 0) ? null : stateId;
		Integer planFilter = (planId != null && planId == 0) ? null : planId;

		return clientRepository.findClientDetails(pageable, statusFilter, managerFilter, stateFilter, planFilter,
				gstFlag, taxFlag, fromDate, toDate, clientName, clientCode, contactName, contactEmail, search);
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

			if (sheet == null || sheet.getLastRowNum() < 1) {
				throw new IllegalArgumentException("Excel file is empty");
			}

			validateClientExcelHeaders(sheet.getRow(1));

			for (int i = 2; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);

				if (row == null) {
					continue;
				}

				if (isEmptyRow(row)) {
					continue;
				}

				String firstCell = getCellValue(row, 0);

				if ("sr no".equalsIgnoreCase(firstCell.trim())) {
					continue;
				}

				int rowNum = i + 1;

				ClientEntity client = new ClientEntity();

				String managerEmail = getCellValue(row, 1);

				managerEmail = managerEmail != null ? managerEmail.trim().toLowerCase() : "";

				client.setManagerNameForExcel(managerEmail);

				if (!managerEmail.isEmpty()) {
					Optional<Integer> managerId = userLoginRepository.findIdByEmail(managerEmail);

					if (managerId.isPresent()) {
						client.setManagerId(managerId.get());
					} else {
						client.setManagerId(null);
					}
				} else {
					client.setManagerId(null);
				}

				client.setName(toCamelCase(getCellValue(row, 2)));
				client.setCode(getCellValue(row, 3));
				client.setPan(getCellValue(row, 4));
				client.setGstFlag(getFlagCellValue(row, 5));
				client.setGstNo(getCellValue(row, 6));
				client.setTaxFlag(getFlagCellValue(row, 7));
				client.setAddressLine1(getCellValue(row, 8));
				client.setAddressLine2(getCellValue(row, 9));
				client.setCity(getCellValue(row, 10));
				String rawStateName = getCellValue(row, 11);

				String stateName = rawStateName == null ? ""
						: rawStateName.replace("\u00A0", " ").replace("\n", " ").replace("\t", " ")
								.replaceAll("\\s+", " ").trim();

				client.setStateNameForExcel(stateName);

				if (!stateName.isEmpty()) {
					Optional<StateEntity> stateOptional = stateRepository.findByNameIgnoreCase(stateName);

					if (stateOptional.isPresent()) {
						client.setStateId(stateOptional.get().getStateId());
					} else {
						client.setStateId(null);
					}
				} else {
					client.setStateId(null);
				}

				client.setLocation(getCellValue(row, 12));
				client.setPincode(getCellValue(row, 13));
				client.setContactName(toCamelCase(getCellValue(row, 14)));
				client.setContactEmail(getCellValue(row, 15));
				client.setName1(getCellValue(row, 16));
				client.setEmailId1(getCellValue(row, 17));
				client.setName2(getCellValue(row, 18));
				client.setEmailId2(getCellValue(row, 19));
				client.setName3(getCellValue(row, 20));
				client.setEmailId3(getCellValue(row, 21));
				client.setEmails(getCellValue(row, 22));
				client.setStartDate(getDateCellValue(row, 23));

				String planName = getCellValue(row, 24);
				planName = planName != null ? planName.trim() : "";

				client.setPlanNameForExcel(planName);

				if (!planName.isEmpty()) {
					Integer planId = planRepo.findIdByName(planName);

					client.setPlanId(planId);
				} else {
					client.setPlanId(null);
				}

				client.setOutstanding(getDoubleCellValue(row, 25));
				client.setStatus(getStatusCellValue(row, 26));
				client.setExcelRowNumber(rowNum);

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

		Set<String> existingGsts = clientRepository.findAllGstsNormalized();
		Set<String> excelGsts = new HashSet<>();
		Set<String> excelCodes = new HashSet<>();

		Map<String, ClientEntity> existingClientsByCode = new HashMap<>();

		for (ClientEntity client : clients) {

			String code = client.getCode();

			if (code != null && !code.trim().isEmpty()) {
				String normalizedCode = code.trim().toLowerCase();

				Optional<ClientEntity> existingClient = clientRepository.findByCodeIgnoreCase(code.trim());

				if (existingClient.isPresent()) {
					existingClientsByCode.put(normalizedCode, existingClient.get());
				}
			}
		}

		for (int rowIndex = 0; rowIndex < clients.size(); rowIndex++) {

			ClientEntity client = clients.get(rowIndex);

			int excelRow = client.getExcelRowNumber() != null ? client.getExcelRowNumber() : rowIndex + 2;

			String rowIdentifier = client.getName() != null && !client.getName().trim().isEmpty()
					? client.getName().trim()
					: "(Row " + excelRow + ")";

			List<String> reasons = new ArrayList<>();

			try {
				String name = client.getName() != null ? client.getName().trim() : "";

				if (name.isEmpty()) {
					reasons.add("Client Name is required");
				} else if (name.length() < 2 || name.length() > 100) {
					reasons.add("Client Name must be between 2 and 100 characters");
				} else {
					client.setName(name);
				}

				String code = client.getCode() != null ? client.getCode().trim() : "";

				if (code.isEmpty()) {
					reasons.add("Client Code is required");
				} else {
					client.setCode(code);

					String normalizedCode = code.toLowerCase();

					if (!excelCodes.add(normalizedCode)) {
						reasons.add("Duplicate Client Code in uploaded file");
					}
				}

				String pan = client.getPan() != null ? client.getPan().trim().toUpperCase() : "";

				if (pan.isEmpty()) {
					reasons.add("PAN is required");
				} else if (!pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]$")) {
					reasons.add("Invalid PAN format");
				} else {
					client.setPan(pan);
				}

				Short gstFlag = client.getGstFlag();

				if (gstFlag == null) {
					reasons.add("GST Applicable is required");
				} else if (gstFlag != 0 && gstFlag != 1) {
					reasons.add("GST Applicable must be Yes/No or 1/0");
				}

				String gst = client.getGstNo() != null ? client.getGstNo().trim().toUpperCase() : "";

				if (gstFlag != null && gstFlag == 1) {

					if (gst.isEmpty()) {
						reasons.add("GST Number is required when GST Applicable is Yes");
					} else if (!gst.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")) {
						reasons.add("Invalid GST format");
					} else {
						client.setGstNo(gst);

						ClientEntity existingClient = existingClientsByCode.get(code.toLowerCase());

						boolean sameExistingGst = existingClient != null && existingClient.getGstNo() != null
								&& existingClient.getGstNo().trim().equalsIgnoreCase(gst);

						if (!sameExistingGst && existingGsts.contains(gst)) {
							reasons.add("GST Number already exists");
						}

						if (!excelGsts.add(gst)) {
							reasons.add("Duplicate GST Number in uploaded file");
						}
					}
				} else {
					if (!gst.isEmpty()) {
						if (!gst.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")) {
							reasons.add("Invalid GST format");
						} else {
							client.setGstNo(gst);
						}
					} else {
						client.setGstNo(null);
					}
				}

				Short taxFlag = client.getTaxFlag();

				if (taxFlag == null) {
					reasons.add("Tax Payable is required");
				} else if (taxFlag != 0 && taxFlag != 1) {
					reasons.add("Tax Payable must be Yes/No or 1/0");
				}

				if (client.getStateId() == null || client.getStateId() == 0) {
					String stateName = client.getStateNameForExcel();

					if (stateName == null || stateName.trim().isEmpty()) {
						reasons.add("State is required");
					} else {
						reasons.add("Invalid State name: " + stateName);
					}
				}

				String addressLine1 = client.getAddressLine1() != null ? client.getAddressLine1().trim() : "";

				if (addressLine1.isEmpty()) {
					reasons.add("Address Line 1 is required");
				} else {
					client.setAddressLine1(addressLine1);
				}

				String addressLine2 = client.getAddressLine2() != null ? client.getAddressLine2().trim() : "";

				client.setAddressLine2(addressLine2.isEmpty() ? null : addressLine2);

				String city = client.getCity() != null ? client.getCity().trim() : "";

				if (city.isEmpty()) {
					reasons.add("City is required");
				} else {
					client.setCity(city);
				}

				String pincode = client.getPincode() != null ? client.getPincode().trim() : "";

				if (pincode.isEmpty()) {
					reasons.add("Pincode is required");
				} else if (!pincode.matches("^[0-9]{6}$")) {
					reasons.add("Pincode must be exactly 6 digits");
				} else {
					client.setPincode(pincode);
				}

				String contactName = client.getContactName() != null ? client.getContactName().trim() : "";

				if (contactName.isEmpty()) {
					reasons.add("Contact Name is required");
				} else {
					client.setContactName(contactName);
				}

				String contactEmail = client.getContactEmail() != null ? client.getContactEmail().trim() : "";

				if (contactEmail.isEmpty()) {
					reasons.add("Contact Email is required");
				} else if (!isValidEmail(contactEmail)) {
					reasons.add("Invalid Contact Email");
				} else {
					client.setContactEmail(contactEmail);
				}

				String emailId1 = client.getEmailId1() != null ? client.getEmailId1().trim() : "";

				if (!emailId1.isEmpty() && !isValidEmail(emailId1)) {
					reasons.add("Invalid Email ID 1");
				} else {
					client.setEmailId1(emailId1.isEmpty() ? null : emailId1);
				}

				String emailId2 = client.getEmailId2() != null ? client.getEmailId2().trim() : "";

				if (!emailId2.isEmpty() && !isValidEmail(emailId2)) {
					reasons.add("Invalid Email ID 2");
				} else {
					client.setEmailId2(emailId2.isEmpty() ? null : emailId2);
				}

				String emailId3 = client.getEmailId3() != null ? client.getEmailId3().trim() : "";

				if (!emailId3.isEmpty() && !isValidEmail(emailId3)) {
					reasons.add("Invalid Email ID 3");
				} else {
					client.setEmailId3(emailId3.isEmpty() ? null : emailId3);
				}

				String emails = client.getEmails() != null ? client.getEmails().trim() : "";

				if (!emails.isEmpty() && !isValidEmailList(emails)) {
					reasons.add("Invalid Other Emails format");
				} else {
					client.setEmails(emails.isEmpty() ? null : emails);
				}

				if (client.getStartDate() == null) {
					reasons.add("Start Date is required");
				}

				String managerEmail = client.getManagerNameForExcel() != null ? client.getManagerNameForExcel().trim()
						: "";

				if (managerEmail.isEmpty()) {
					reasons.add("Society Manager Email is required");
				} else if (!isValidEmail(managerEmail)) {
					reasons.add("Invalid Society Manager Email: " + managerEmail);
				} else if (client.getManagerId() == null || client.getManagerId() == 0) {
					reasons.add("Invalid Society Manager Email: " + managerEmail);
				}

				String location = client.getLocation() != null ? client.getLocation().trim() : "";

				if (location.isEmpty()) {
					reasons.add("Location is required");
				} else {
					client.setLocation(location);
				}

				String planName = client.getPlanNameForExcel() != null ? client.getPlanNameForExcel().trim() : "";

				if (planName.isEmpty()) {
					reasons.add("Plan is required");
				} else if (client.getPlanId() == null || client.getPlanId() == 0) {
					reasons.add("Invalid Plan name: " + planName);
				}

				Short status = client.getStatus();

				if (status == null) {
					reasons.add("Status is required. Allowed values: 1=Active, 2=Inactive");
				} else if (status != 1 && status != 2) {
					reasons.add("Invalid Status. Allowed values: 1=Active, 2=Inactive");
				}

				if (!reasons.isEmpty()) {
					String reason = String.join("; ", reasons);

					skipped.add(rowIdentifier + " (" + reason + ")");

					addFailedRecord(failedRecords, client, reason);

					continue;
				}

				String normalizedCode = code.toLowerCase();

				ClientEntity existingClient = existingClientsByCode.get(normalizedCode);

				if (existingClient != null) {

					Integer clientId = existingClient.getClientId();

					existingClient.setName(client.getName());
					existingClient.setCode(client.getCode());
					existingClient.setPan(client.getPan());
					existingClient.setGstFlag(client.getGstFlag());
					existingClient.setGstNo(client.getGstNo());
					existingClient.setTaxFlag(client.getTaxFlag());
					existingClient.setAddressLine1(client.getAddressLine1());
					existingClient.setAddressLine2(client.getAddressLine2());
					existingClient.setCity(client.getCity());
					existingClient.setStateId(client.getStateId());
					existingClient.setLocation(client.getLocation());
					existingClient.setPincode(client.getPincode());
					existingClient.setContactName(client.getContactName());
					existingClient.setContactEmail(client.getContactEmail());
					existingClient.setName1(client.getName1());
					existingClient.setEmailId1(client.getEmailId1());
					existingClient.setName2(client.getName2());
					existingClient.setEmailId2(client.getEmailId2());
					existingClient.setName3(client.getName3());
					existingClient.setEmailId3(client.getEmailId3());
					existingClient.setEmails(client.getEmails());
					existingClient.setStartDate(client.getStartDate());
					existingClient.setPlanId(client.getPlanId());
					existingClient.setOutstanding(client.getOutstanding());
					existingClient.setManagerId(client.getManagerId());
					existingClient.setStatus(client.getStatus());
					existingClient.setModdate(now);
					validClients.add(existingClient);
				} else {
					client.setUserId(userId);
					client.setRegdate(now);
					client.setModdate(now);
					client.setStatus(client.getStatus());

					validClients.add(client);
				}
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
					commonFunction.getLocalIp(), "Client Added/Updated from Excel", 8, saved.getClientId(), -1);
		}

		String msg = String.format("Clients processed. Saved/Updated: %d, Failed: %d", savedClients.size(),
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
		map.put("State", client.getStateNameForExcel() != null ? client.getStateNameForExcel() : "");
		map.put("Address Line 1", client.getAddressLine1() != null ? client.getAddressLine1() : "");
		map.put("Address Line 2", client.getAddressLine2() != null ? client.getAddressLine2() : "");
		map.put("City", client.getCity() != null ? client.getCity() : "");
		map.put("Pincode", client.getPincode() != null ? client.getPincode() : "");
		map.put("Contact Name", client.getContactName() != null ? client.getContactName() : "");
		map.put("Contact Email", client.getContactEmail() != null ? client.getContactEmail() : "");
		map.put("Emails", client.getEmails() != null ? client.getEmails() : "");
		map.put("Start Date",
				client.getStartDate() != null ? client.getStartDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
						: "");
		map.put("Monthly Charge", client.getMonthlyCharge() != null ? client.getMonthlyCharge().toString() : "");
		map.put("Outstanding", client.getOutstanding() != null ? client.getOutstanding().toString() : "");
		map.put("Name 1", client.getName1() != null ? client.getName1() : "");
		map.put("Email ID 1", client.getEmailId1() != null ? client.getEmailId1() : "");
		map.put("Name 2", client.getName2() != null ? client.getName2() : "");
		map.put("Email ID 2", client.getEmailId2() != null ? client.getEmailId2() : "");
		map.put("Name 3", client.getName3() != null ? client.getName3() : "");
		map.put("Email ID 3", client.getEmailId3() != null ? client.getEmailId3() : "");
//		map.put("Society Manager", client.getManagerNameForExcel() != null ? client.getManagerNameForExcel() : "");
		map.put("Society Manager Email",
				client.getManagerNameForExcel() != null ? client.getManagerNameForExcel() : "");
		map.put("Tax Flag", client.getTaxFlag() != null ? client.getTaxFlag().toString() : "");
		map.put("Location", client.getLocation() != null ? client.getLocation() : "");
		map.put("Plan", client.getPlanNameForExcel() != null ? client.getPlanNameForExcel() : "");
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

	private boolean isValidEmail(String email) {

		if (email == null || email.trim().isEmpty()) {
			return false;
		}

		return email.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$");
	}

	private Short getFlagCellValue(Row row, int columnIndex) {

		if (row == null) {
			throw new IllegalArgumentException("Excel row is missing");
		}

		Cell cell = row.getCell(columnIndex);

		String header = "";

		Row headerRow = row.getSheet().getRow(1);

		if (headerRow != null) {
			header = getCellValue(headerRow, columnIndex);
		}

		String rawValue = getCellValue(row, columnIndex);

		if (rawValue == null) {
			rawValue = "";
		}

		rawValue = rawValue.replace("\u00A0", " ").trim();

		if (header != null && !header.trim().isEmpty() && rawValue.equalsIgnoreCase(header.trim())) {
			throw new IllegalArgumentException("Header row was processed as data at column " + (columnIndex + 1) + ": "
					+ header + ". Please check the Excel row structure.");
		}

		if (rawValue.isEmpty()) {
			return (short) 0;
		}

		String value = rawValue.toLowerCase();

		switch (value) {

		case "yes":
		case "y":
		case "true":
		case "1":
		case "1.0":
			return (short) 1;

		case "no":
		case "n":
		case "false":
		case "0":
		case "0.0":
			return (short) 0;

		default:

			throw new IllegalArgumentException("Invalid flag value at column " + (columnIndex + 1) + ": " + header
					+ ". Excel value = [" + rawValue + "]. Allowed values: Yes/No, 1/0, True/False");
		}
	}

	private Short getStatusCellValue(Row row, int columnIndex) {

		Cell cell = row.getCell(columnIndex);

		String header = getCellValue(row.getSheet().getRow(1), columnIndex);

		if (cell == null || cell.getCellType() == CellType.BLANK) {

			throw new RuntimeException("Status is required at column " + (columnIndex + 1) + ": " + header);
		}

		if (cell.getCellType() == CellType.NUMERIC) {

			double value = cell.getNumericCellValue();

			if (value == 1) {
				return 1;
			}

			if (value == 2) {
				return 2;
			}

			throw new RuntimeException("Invalid Status at column " + (columnIndex + 1) + ": " + header
					+ ". Allowed values: 1=Active, 2=Inactive");
		}

		if (cell.getCellType() == CellType.BOOLEAN) {

			throw new RuntimeException("Invalid Status at column " + (columnIndex + 1) + ": " + header
					+ ". Allowed values: Active/Inactive or 1/2");
		}

		String value = cell.getStringCellValue();

		if (value == null || value.trim().isEmpty()) {

			throw new RuntimeException("Status is required at column " + (columnIndex + 1) + ": " + header);
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
			throw new RuntimeException("Invalid Status at column " + (columnIndex + 1) + ": " + header
					+ ". Allowed values: Active/Inactive or 1/2");
		}
	}

	private boolean isValidEmailList(String emails) {

		if (emails == null || emails.trim().isEmpty()) {
			return true;
		}

		String[] emailArray = emails.split(",");

		for (String email : emailArray) {

			if (!isValidEmail(email.trim())) {
				return false;
			}
		}

		return true;
	}

	private String getCellValue(Row row, int cellIndex) {

		Cell cell = row.getCell(cellIndex);

		if (cell == null) {
			return "";
		}

		DataFormatter formatter = new DataFormatter();

		return formatter.formatCellValue(cell).trim();
	}

	private void validateClientExcelHeaders(Row headerRow) {

		if (headerRow == null) {
			throw new IllegalArgumentException("Excel header row is missing");
		}

		String[] expectedHeaders = { "Sr No", "Society Manager Email", "Client Name", "Client Code", "PAN", "GST Applicable",
				"GST Number", "Tax Payable", "Address Line 1", "Address Line 2", "City", "State", "Location", "Pincode",
				"Contact Name", "Contact Email", "Contact Person 1 Name", "Contact Person 1 Email",
				"Contact Person 2 Name", "Contact Person 2 Email", "Contact Person 3 Name", "Contact Person 3 Email",
				"Other Emails", "Start Date", "Plan", "Outstanding", "Status" };

		for (int i = 0; i < expectedHeaders.length; i++) {

			String actualHeader = getCellValue(headerRow, i);

			String expectedHeader = expectedHeaders[i];

			if (!expectedHeader.equalsIgnoreCase(actualHeader != null ? actualHeader.trim() : "")) {

				throw new IllegalArgumentException("Invalid Excel header at column " + (i + 1) + ". Expected: "
						+ expectedHeader + ", Found: " + actualHeader);
			}
		}
	}

	private boolean isEmptyRow(Row row) {

		if (row == null) {
			return true;
		}

		for (int i = 0; i <= 26; i++) {
			Cell cell = row.getCell(i);

			if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(row, i).trim().isEmpty()) {
				return false;
			}
		}
		return true;
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

	private LocalDate getDateCellValue(Row row, int cellIndex) {

		Cell cell = row.getCell(cellIndex);

		if (cell == null) {
			return null;
		}

		if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {

			Date date = cell.getDateCellValue();

			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}

		String value = getCellValue(row, cellIndex);

		if (value == null || value.trim().isEmpty()) {
			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy").withResolverStyle(ResolverStyle.STRICT);

		try {
			return LocalDate.parse(value.trim(), formatter);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid Start Date: " + value + ". Expected format dd-MM-yyyy");
		}
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

	// For Recurring
	public List<ClientDTO> getClientsForRecurring(Integer userId) {
		Short activeStatus = 1;
		List<ClientEntity> clients = clientRepository.findByManagerIdAndStatus(userId, activeStatus);

		return clients.stream().map(client -> new ClientDTO(client.getClientId(), client.getName()))
				.collect(Collectors.toList());
	}

	// Manager Change
	@Transactional
	public void changeClientManager(Integer clientId, Integer managerId,Integer userId,HttpServletRequest httpRequest) {

		if (managerId == null) {
			throw new RuntimeException("Society Manager is required");
		}

		ClientEntity client = clientRepository.findById(clientId)
				.orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));

		UserLoginEntity manager = userLoginRepository.findById(managerId)
				.orElseThrow(() -> new RuntimeException("Society Manager not found with id: " + managerId));

		if (manager.getStatus() != 1) {
			throw new RuntimeException("Selected Society manager is not active");
		}

		client.setManagerId(managerId);
		client.setModdate(new Timestamp(System.currentTimeMillis()));

		ClientEntity savedClient = clientRepository.save(client);

		commonFunction.createHistoryAccess(userId, commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), "Society Manager Changed", 8, savedClient.getClientId(), -1);
	}

	// Change Outstanding
	@Transactional
	public ApiResponse<ClientEntity> updateClientOutstanding(ClientDTO dto, Integer managerId,
			HttpServletRequest httpRequest) {

		if (dto.getClientId() == null || dto.getClientId() <= 0) {
			return new ApiResponse<>(false, "Invalid client.", null);
		}

		if (dto.getOutstanding() == null || dto.getOutstanding() < 0) {
			return new ApiResponse<>(false, "Invalid outstanding amount.", null);
		}

		Optional<ClientEntity> clientOptional = clientRepository.findByClientIdAndManagerId(dto.getClientId(),
				managerId);

		if (clientOptional.isEmpty()) {
			return new ApiResponse<>(false, "You are not authorized to update this client.", null);
		}

		ClientEntity client = clientOptional.get();

		client.setOutstanding(dto.getOutstanding());
		client.setModdate(Timestamp.from(Instant.now()));

		ClientEntity saved = clientRepository.save(client);

		commonFunction.createHistoryAccess(saved.getUserId(), commonFunction.resolveClientIp(httpRequest),
				commonFunction.getLocalIp(), "Outstanding Updated", 8, saved.getClientId(), -1);

		return new ApiResponse<>(true, "Outstanding updated successfully", saved);
	}
}
