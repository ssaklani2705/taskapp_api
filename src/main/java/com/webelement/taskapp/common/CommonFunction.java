package com.webelement.taskapp.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.webelement.taskapp.Exceptions.FileValidationException;
import com.webelement.taskapp.dto.ModulePermissionDTO;
import com.webelement.taskapp.dto.TaskMailDTO;
import com.webelement.taskapp.dto.UserInfo;
import com.webelement.taskapp.entity.MailLogEntity;
import com.webelement.taskapp.entity.TransactionEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.MailLogRepo;
import com.webelement.taskapp.repo.TransactionRepo;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.PasswordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommonFunction {

	static final Logger logger = LoggerFactory.getLogger(CommonFunction.class);
	long MAX_FILE_SIZE = 10L * 1024 * 1024;
	private final MailLogRepo logRepo;
	private final ResourceLoader resourceLoader;
	private final TransactionRepo transactionRepo;
	private final UserLoginRepository userLoginRepository;
	private final PasswordService passwordService;

	@Value("${ipflag:}")
	private String ipFlag;

	@Value("${algorithm:}")
	private String ALGORITHM;

	@Value("${secret_key:}")
	private String SECRET_KEY;

	@Value("${pdffilepath}")
	private String templatePath;

	@Value("${task.upload-dir}")
	private String uploadDir;

	public List<TransactionEntity> getTransactionLogs(int moduleId, Integer recordId) {
		List<Object[]> results = userLoginRepository.getTransactionLogs(moduleId, recordId);
		System.out.println("results : " + results);
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

	@Async
	public void createHistoryAccess(int userId, String ipAddrStr, String localip, String desc, int moduleId,
			int recordid, int bankUserId) {
		TransactionEntity entity = new TransactionEntity();
		entity.setModuleId(moduleId);
		entity.setRecordId(recordid);
		entity.setUserId(userId);
		entity.setIpAddress(ipAddrStr);
		entity.setLocalIp(localip);
		entity.setAction(desc);
		entity.setRegDate(new Timestamp(System.currentTimeMillis())); // current time
		entity.setBankUserId(bankUserId);
		transactionRepo.save(entity);
	}

	public String getLocalIp() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			return localHost.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
			return "UNKNOWN";
		}
	}

	public String decipher(String data) throws Exception {
		if (SECRET_KEY == null || SECRET_KEY.length() != 8) {
			throw new Exception("Invalid key length - 8 bytes key needed!");
		}
		SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(toByte(data)));
	}

	// Converts hex string to byte array
	private static byte[] toByte(String hexString) {
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

	public String cipher(String data) throws Exception {

		if (SECRET_KEY == null || SECRET_KEY.length() != 8) {
			throw new IllegalArgumentException("Invalid key length - 8 bytes key needed!");
		}
		SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedBytes = cipher.doFinal(data.getBytes());
		return toHex(encryptedBytes);
	}

	// Converts byte array to hex string
	private static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	public String resolveClientIp(HttpServletRequest request) {
		String ipAddrStr = "";
		String iplocalserver = ipFlag;

		try {
			if ("localIp".equalsIgnoreCase(iplocalserver)) {
				// Get local server IP address
				InetAddress addr = InetAddress.getLocalHost();
				ipAddrStr = addr.getHostAddress();
			} else {
				// Try to get real client IP from headers (in case of proxy or load balancer)
				ipAddrStr = request.getHeader("X-FORWARDED-FOR");

				// Fallback to remote address
				if (ipAddrStr == null || ipAddrStr.isEmpty()) {
					ipAddrStr = request.getRemoteAddr();
				}
			}
		} catch (Exception e) {
			ipAddrStr = "UNKNOWN";
		}
		return ipAddrStr;
	}

	public String getForgotMessageCreate(String name, String link, String url) {
		try {
			// Load the HTML template from resources/templates/
			Resource resource = resourceLoader.getResource("classpath:templates/newuser_admin.html");
			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
			content = content.replace("__NAME__", name);
			content = content.replace("__LINK__", link);
			content = content.replace("__URL__", url); // Optional
			return content;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public String createFolder(String path) {
		String foldername = "";
		try {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
			int month = (cal.get(Calendar.MONTH) + 1);
			int year = cal.get(Calendar.YEAR);
			SimpleDateFormat sdf1 = new SimpleDateFormat("M");
			SimpleDateFormat sdf2 = new SimpleDateFormat("MMM");
			String monthName = sdf2.format(sdf1.parse(month + ""));

			foldername = (monthName + "-" + year).toLowerCase();
			File dir = new File(path + foldername + "/");
//	            if (!dir.exists()) {
//	                dir.mkdir();
//	            }
			if (!dir.exists()) {
				dir.mkdirs(); // This is safer
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return foldername;
	}

	public String writeHTMLFile(String content, String filePath, String fileName) {
		try {
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			fileName = fileName + ".html";
			File file = new File(dir, fileName);

			try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
				writer.write(content);
			}
			return fileName;
		} catch (Exception e) {
			e.printStackTrace(); // You could use a logger instead
			return null;
		}
	}

	public String currDate1() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
		java.util.Date dt = cal.getTime();
		return sdf.format(dt);
	}

	public static String getDateAfter(String date, int type, int no, String sformatv, String endformatv) {
		String dt = "";
		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sformat = new SimpleDateFormat(sformatv);
			SimpleDateFormat endformat = new SimpleDateFormat(endformatv);
			long l1 = sformat.parse(date).getTime();
			cal.setTimeInMillis(l1);
			switch (type) {
			case 1:
				cal.add(Calendar.DATE, no);
				break;
			case 2:
				cal.add(Calendar.MONTH, no);
				break;
			case 3:
				cal.add(Calendar.YEAR, no);
				break;
			case 4:
				cal.add(Calendar.HOUR, no);
				break;
			default:
				break;
			}
			dt = endformat.format(cal.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dt;
	}

	public void createMailLog(int type, String name, String to, String cc, String bcc, String from, String subject,
			String filename, String ip, String iplocal, int status) {
		MailLogEntity log = new MailLogEntity();
		log.setType(type);
		log.setName(name);
		log.setTo(to);
		log.setCc(cc);
		log.setBcc(bcc);
		log.setFrom(from);
		log.setSubject(subject);
		log.setStatus(status);
		log.setFilename(filename);
		log.setRegDate(LocalDateTime.now());
		log.setModDate(LocalDateTime.now());
		log.setIpAddress(ip);
		log.setLocalIp(iplocal);
		logRepo.save(log);
	}

	public String getTaskAssignedMailTemplate(String assigneeName, String taskTitle, String assignedBy,
			String clientName, String priority, String startDate, String dueDate, String description) {

		try {

			Resource resource = resourceLoader.getResource("classpath:templates/task_assigned_mail.html");

			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

			content = content.replace("__ASSIGNEE_NAME__", assigneeName != null ? assigneeName : "");
			content = content.replace("__TASK_TITLE__", taskTitle != null ? taskTitle : "");
			content = content.replace("__ASSIGNED_BY__", assignedBy != null ? assignedBy : "");
			content = content.replace("__CLIENT_NAME__", clientName != null ? clientName : "");
			content = content.replace("__PRIORITY__", priority != null ? priority : "");
			content = content.replace("__START_DATE__", startDate != null ? startDate : "");
			content = content.replace("__DUE_DATE__", dueDate != null ? dueDate : "");
			content = content.replace("__DESCRIPTION__", description != null ? description : "");

			return content;

		} catch (IOException e) {
			logger.error("Error loading task assigned email template", e);
			return "";
		}
	}

	public String getTaskReAssignedMailTemplate(String assigneeName, String taskTitle, String assignedBy,
			String clientName, String priority, String startDate, String dueDate, String description) {

		try {

			Resource resource = resourceLoader.getResource("classpath:templates/task_reassigned_mail.html");

			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

			content = content.replace("__ASSIGNEE_NAME__", assigneeName != null ? assigneeName : "");
			content = content.replace("__TASK_TITLE__", taskTitle != null ? taskTitle : "");
			content = content.replace("__ASSIGNED_BY__", assignedBy != null ? assignedBy : "");
			content = content.replace("__CLIENT_NAME__", clientName != null ? clientName : "");
			content = content.replace("__PRIORITY__", priority != null ? priority : "");
			content = content.replace("__START_DATE__", startDate != null ? startDate : "");
			content = content.replace("__DUE_DATE__", dueDate != null ? dueDate : "");
			content = content.replace("__DESCRIPTION__", description != null ? description : "");

			return content;

		} catch (IOException e) {
			logger.error("Error loading task assigned email template", e);
			return "";
		}
	}

	public String getTaskNotesMailTemplate(String name, String taskTitle, String note, String createdBy,
			String websitePath) {

		try {

			Resource resource = resourceLoader.getResource("classpath:templates/task_notes_mail.html");

			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

			content = content.replace("__NAME__", name != null ? name : "");
			content = content.replace("__TASK_TITLE__", taskTitle != null ? taskTitle : "");
			content = content.replace("__CREATED_BY__", createdBy != null ? createdBy : "");
			content = content.replace("__NOTE__", note != null ? note : "");

			return content;

		} catch (IOException e) {
			logger.error("Error loading task notes email template", e);
			return "";
		}
	}

	public String getTaskStatusMailTemplate(TaskMailDTO param) {

		try {

			Resource resource = resourceLoader.getResource("classpath:templates/task_status_mail.html");

			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

			content = content.replace("__NAME__", param.getName() != null ? param.getName() : "");

			content = content.replace("__TASK_TITLE__", param.getTaskName() != null ? param.getTaskName() : "");

			content = content.replace("__URL__", param.getUrl() != null ? param.getUrl() : "");

			content = content.replace("__STATUS_ROWS__", buildStatusRows(param));
			return content;
		} catch (IOException e) {
			logger.error("Error loading task status email template", e);
			return "";
		}
	}

	private String buildStatusRows(TaskMailDTO param) {

		Map<String, String> rows = new LinkedHashMap<>();

		String currentStatus = nvl(param.getCurrentStatus());

		rows.put("Client Name", param.getClientName());

		if (!"Re-Open".equalsIgnoreCase(currentStatus)) {
			rows.put("Assigned By", param.getAssignedBy());
		}

		rows.put("Previous Status", param.getPreviousStatus());
		rows.put("Current Status", currentStatus);

		switch (currentStatus.toLowerCase()) {

		case "re-open":

			rows.put("Reopened On", param.getReopenedOn());
			rows.put("Reopened By", param.getReopendBy());
			break;

		case "assignee closure":

			rows.put("Closed/Submitted On", param.getSubmittedOn());
			rows.put("Closed By", param.getClosedBy());
			break;

		case "assignee re-closure":

			rows.put("Re-Closed/Submitted On", param.getSubmittedOn());
			rows.put("Re-Closed By", param.getReClosedBy());
			break;

		case "assignor closure":

			rows.put("Submitted On", param.getSubmittedOn());
			break;

		default:

			return "<tr><td><b>Task Name</b></td><td>: " + nvl(param.getTaskName()) + "</td></tr>"

					+ "<tr><td><b>Previous Status</b></td><td>: " + nvl(param.getPreviousStatus()) + "</td></tr>"

					+ "<tr><td><b>Current Status</b></td><td>: " + currentStatus + "</td></tr>";
		}

		rows.put("Priority", param.getPriority());
		rows.put("Due Date", formatDate(param.getDueDate()));
		rows.put("Description", param.getRemark());

		StringBuilder statusRows = new StringBuilder();

		for (Map.Entry<String, String> row : rows.entrySet()) {

			statusRows.append("<tr>").append("<td><b>").append(row.getKey()).append("</b></td>").append("<td>: ")
					.append(nvl(row.getValue())).append("</td>").append("</tr>");
		}

		return statusRows.toString();
	}

	private String nvl(String value) {
		return value == null ? "" : value;
	}

	public String formatDate(LocalDateTime dateTime) {
		if (dateTime == null) {
			return "";
		}
		return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
	}

	public String getPriorityName(Short priority) {

		if (priority == null) {
			return "";
		}

		switch (priority) {
		case 1:
			return "High";

		case 2:
			return "Medium";

		case 3:
			return "Low";

		default:
			return "";
		}
	}

	public void addEmail(Set<String> emails, UserLoginEntity user) {

		if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {

			emails.add(user.getEmail().trim());
		}
	}

	public String getFirstName(UserLoginEntity user) {
		return user != null ? user.getFirstName() : "";
	}

	public String formatNow() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
	}

	public boolean anyPermissionGranted(ModulePermissionDTO dto) {
		return "Y".equalsIgnoreCase(dto.getViewPer()) || "Y".equalsIgnoreCase(dto.getAddPer())
				|| "Y".equalsIgnoreCase(dto.getEditPer()) || "Y".equalsIgnoreCase(dto.getDeletePer())
				|| "Y".equalsIgnoreCase(dto.getApprovePer()) || "Y".equalsIgnoreCase(dto.getAdminApprovePer())
				|| "Y".equalsIgnoreCase(dto.getExportExcel());
	}

	public String getString(Object obj) {
		return obj != null ? obj.toString() : "";
	}

	public Integer getInt(Object obj) {
		return obj instanceof Number ? ((Number) obj).intValue() : 0;
	}

	public UserInfo getUserAccess(String username, String password, String ip, String logintype) {
		List<Map<String, Object>> results = userLoginRepository.findActiveLoginByEmail(username, logintype);
		if (results.isEmpty()) {
			return null;
		}
		Map<String, Object> row = results.get(0);
		String stored = (String) row.get("storedPassword");
		if (!passwordService.matches(password, stored)) {
			return null;
		}

		Integer userId = ((Number) row.get("i_userid")).intValue();
		if (!passwordService.isHashed(stored)) { // upgrade on first good login
			userLoginRepository.updatePasswordHash(userId, passwordService.hash(password));
		}

		UserInfo info = new UserInfo(userId, (String) row.get("s_firstname"), (String) row.get("s_email"),
				(String) row.get("s_mobileno"), ((Number) row.get("d_expirydate")).intValue(),
				(String) row.get("s_permission"), (String) row.get("designationName"), (String) row.get("isHod"));
		info.setTaskcategoryIds((String) row.get("taskcategoryIds"));
		return info;
	}

	public String escapeApostrophes(String input) {
		if (input == null) {
			return "";
		}
		return input.replace("'", "''");
	}

	public void validateFile(MultipartFile file, Set<String> allowedExtensions) throws IOException {
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new FileValidationException("File size must not exceed 10 MB");
		}
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new FileValidationException("Invalid file name");
		}
		String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
		if (!allowedExtensions.contains(extension)) {
			throw new FileValidationException(
					"Invalid file type. Allowed types: " + String.join(", ", allowedExtensions));
		}
		byte[] header = new byte[8];
		int bytesRead;
		InputStream is = file.getInputStream();
		try {
			bytesRead = is.read(header);
		} finally {
			is.close();
		}
		if (bytesRead < 4) {
			throw new FileValidationException("Corrupted or empty file");
		}
		if ("pdf".equals(extension)) {
			String magic = new String(header, 0, 4);
			if (!magic.startsWith("%PDF")) {
				throw new FileValidationException("Corrupted PDF file");
			}
		} else if ("zip".equals(extension) || "docx".equals(extension) || "xlsx".equals(extension)) {
			if (header[0] != 'P' || header[1] != 'K') {
				throw new FileValidationException("Corrupted " + extension.toUpperCase() + " file");
			}
		} else if ("doc".equals(extension)) {
			boolean isOle2 = (header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF && (header[2] & 0xFF) == 0x11
					&& (header[3] & 0xFF) == 0xE0;
			String headerText = new String(header).toLowerCase();
			boolean isHtmlDoc = headerText.contains("<html") || headerText.contains("<head");
			boolean isRtfDoc = headerText.startsWith("{\\rtf");
			if (!isOle2 && !isHtmlDoc && !isRtfDoc) {
				throw new FileValidationException("Corrupted DOC file");
			}
		} else if ("xls".equals(extension)) {
			boolean isOle2 = (header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF && (header[2] & 0xFF) == 0x11
					&& (header[3] & 0xFF) == 0xE0;
			String headerText = new String(header).toLowerCase();
			boolean isHtmlExcel = headerText.contains("<html") || headerText.contains("<!doctyp")
					|| headerText.contains("<head");
			if (!isOle2 && !isHtmlExcel) {
				throw new FileValidationException("Corrupted XLS file");
			}
		} else {
			throw new FileValidationException("Unsupported file type: " + extension);
		}
	}

	public String saveFileNew(MultipartFile file, String type, String statusPrefix) throws IOException {
		Path directory = Paths.get(uploadDir, type);
		Files.createDirectories(directory);
		String originalName = file.getOriginalFilename();
		String extension = "";
		String fileNameWithoutExtension = "file";
		if (originalName != null && !originalName.trim().isEmpty()) {
			int index = originalName.lastIndexOf(".");
			if (index >= 0) {
				extension = originalName.substring(index);
				fileNameWithoutExtension = originalName.substring(0, index);
			} else {
				fileNameWithoutExtension = originalName;
			}
		}
		// Clean original file name
		fileNameWithoutExtension = fileNameWithoutExtension.replaceAll("[^a-zA-Z0-9_-]", "_");
		String newFileName = statusPrefix + "_" + System.currentTimeMillis() + extension;
		Path target = directory.resolve(newFileName);
		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		return newFileName;
	}

	public String saveFile(MultipartFile file, String type) throws IOException {
		Path directory = Paths.get(uploadDir, type);
		Files.createDirectories(directory);
		String originalName = file.getOriginalFilename();
		String extension = "";
		if (originalName != null) {
			int index = originalName.lastIndexOf(".");
			if (index >= 0) {
				extension = originalName.substring(index);
			}
		}
		String newFileName = UUID.randomUUID().toString() + extension;
		Path target = directory.resolve(newFileName);
		Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		return newFileName;
	}

}
