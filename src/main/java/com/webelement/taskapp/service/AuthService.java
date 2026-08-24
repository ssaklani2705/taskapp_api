package com.webelement.taskapp.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.common.ResponseApi;
import com.webelement.taskapp.dto.LoginRequest;
import com.webelement.taskapp.dto.LoginResponse;
import com.webelement.taskapp.dto.ModulePermissionDTO;
import com.webelement.taskapp.dto.UserInfo;
import com.webelement.taskapp.entity.MailLogEntity;
import com.webelement.taskapp.entity.SmtpEntity;
import com.webelement.taskapp.entity.UserAccessLogEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.jwttoken.JwtUtil;
import com.webelement.taskapp.repo.MailLogRepo;
import com.webelement.taskapp.repo.SmtpRepo;
import com.webelement.taskapp.repo.UserLoginRepository;

@Service
public class AuthService {
	
	@Autowired
	private CommonFunction commonFunction;
	@Autowired
	UserLoginRepository userLoginRepository;
	
	@Autowired
	private MailLogRepo logRepo;
	
	@Autowired
	private SmtpRepo smtpRepo;
	
	@Autowired
	private UserAccessLogService accessLogService;
	
	@Autowired
	private MyUserDetailsService detailsService;

	@Autowired
	private JwtUtil jwtUtil;
	
	@Value("${file_maillog:}")
	private String file_maillog;
	@Value("${paths:}")
	private String paths;
	
	@Autowired
	private MailService mailService;


	public ResponseEntity<ResponseApi<LoginResponse>> login(LoginRequest request, HttpSession session,
			HttpServletRequest httpRequest) throws Exception {
		String userCaptcha = request.getCaptcha();
		String serverCaptcha = request.getCaptchaAns();
		if (userCaptcha == null || serverCaptcha == null || !userCaptcha.equalsIgnoreCase(serverCaptcha)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseApi<>(false, "Invalid captcha code.", null));
		}

		String loginId = request.getUsername() != null ? request.getUsername().trim().toLowerCase() : "";
		String password = request.getPassword() != null ? request.getPassword() : "";
		String iplocal = commonFunction.getLocalIp();

		UserInfo info = getUserAccess(loginId, password, iplocal);

		String permission = "N";
		if (info != null) {

			if (info.getStatus() < 1) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseApi<>(false,
						"Your account has been expired, please contact the administrator.", null));

			} else {

				int userId = 0;
				userId = info.getUserId();
				permission = info.getPermission() != null ? info.getPermission() : "";

				if (userId > 0) {

					detailsService.setUser(request);
					String token = jwtUtil.generateAccessToken(loginId);
					boolean b = false;
					if (permission.equals("Y")) {

						b = true;
					}
					List<ModulePermissionDTO> modules = getModuleListByName(userId, b);
					UserAccessLogEntity accessLogEntity = accessLogService.saveLogin(userId, info.getFirstName(),
							httpRequest);
					LoginResponse loginResponse = new LoginResponse(token, userId, info.getFirstName(),
							accessLogEntity.getLogId(), permission, modules);
					session.removeAttribute("captcha");
					return ResponseEntity.ok(new ResponseApi<>(true, "Login successful", loginResponse));
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(new ResponseApi<>(false, "Invalid login credentials.", null));
				}

			}

		} else {
			int ccc = getLoginIdMatch(loginId, password);

			String message;
			switch (ccc) {
			case 1:
				message = "Email ID and Password both are incorrect. Please enter correct Email ID and Password.";
				break;
			case 2:
				message = "Email ID is incorrect. Please enter correct Email ID.";
				break;
			case 3:
				message = "Your Password is incorrect. Please enter correct Password.";
				break;
			case 4:
				message = "Your Email ID is not registered. Please contact to dupak Team.";
				break;
			default:
				message = "Invalid username or password.";
			}

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseApi<>(false, message, null));

		}

	}
	
	public int getLoginIdMatch(String userName, String password) throws Exception {
		int cc = 0;
		List<Map<String, Object>> result = userLoginRepository.findLoginByEmail(userName);

		if (result.isEmpty()) {
			cc = 4;
		} else {
			for (Map<String, Object> row : result) {
				String e = row.get("s_email") != null ? row.get("s_email").toString() : "";
				String p = row.get("s_password") != null ? row.get("s_password").toString() : "";

				if (!p.isEmpty()) {
					p = commonFunction.decipher(p); // You must implement this method
				}

				if (!e.equals(userName) && !p.equals(password)) {
					cc = 1;
				} else if (!e.equals(userName)) {
					cc = 2;
				} else if (!p.equals(password)) {
					cc = 3;
				}
			}
		}

		return cc;
	}
	
	
	private UserInfo getUserAccess(String username, String password, String ip) {
		String newPassword = "";
		try {
			newPassword = commonFunction.cipher(escapeApostrophes(password));

		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Map<String, Object>> results = userLoginRepository.findActiveLogin(username, newPassword);

		UserInfo info = null;
		if (!results.isEmpty()) {
			Map<String, Object> row = results.get(0);

			Integer userId = ((Number) row.get("i_userid")).intValue();
			String firstName = (String) row.get("s_firstname");
			String email = (String) row.get("s_email");
			String mobileNo = (String) row.get("s_mobileno");
			Integer i_status = ((Number) row.get("d_expirydate")).intValue();
			String s_permission = (String) row.get("s_permission");

			info = new UserInfo(userId, firstName, email, mobileNo, i_status, s_permission);
		}
		return info;
	}

	

	public String escapeApostrophes(String input) {
		if (input == null) {
			return "";
		}
		return input.replace("'", "''");
	}
	
	public List<ModulePermissionDTO> getModuleListByName(int userId, boolean isFullList) {
		List<Map<String, Object>> rows = userLoginRepository.getModuleListByName(userId);
		List<ModulePermissionDTO> list = new ArrayList<>();

		for (Map<String, Object> row : rows) {
			ModulePermissionDTO dto = new ModulePermissionDTO(userId, getInt(row.get("i_moduleid")),
					getString(row.get("s_name")), getInt(row.get("i_type")), getString(row.get("s_add")),
					getString(row.get("s_edit")), getString(row.get("s_delete")), getString(row.get("s_approve")),
					getString(row.get("s_adminapprove")), getString(row.get("s_view")), getString(row.get("s_exportexcel")));
			if (isFullList) {
				dto.setAddPer("Y");
				dto.setEditPer("Y");
				dto.setDeletePer("Y");
				dto.setApprovePer("Y");
				dto.setAdminApprovePer("Y");
				dto.setViewPer("Y");
				dto.setExportExcel("Y");

				list.add(dto); // always include in full list
			} else if (anyPermissionGranted(dto)) {
				list.add(dto);
			}

		}

		return list; // Return actual list
	}
	
	private boolean anyPermissionGranted(ModulePermissionDTO dto) {
		return "Y".equalsIgnoreCase(dto.getViewPer()) || "Y".equalsIgnoreCase(dto.getAddPer())
				|| "Y".equalsIgnoreCase(dto.getEditPer()) || "Y".equalsIgnoreCase(dto.getDeletePer())
				|| "Y".equalsIgnoreCase(dto.getApprovePer()) || "Y".equalsIgnoreCase(dto.getAdminApprovePer()) || "Y".equalsIgnoreCase(dto.getExportExcel());
	}

	private String getString(Object obj) {
		return obj != null ? obj.toString() : "";
	}

	private Integer getInt(Object obj) {
		return obj instanceof Number ? ((Number) obj).intValue() : 0;
	}
	
	
	public ResponseEntity<ResponseApi<String>> forgotpasswordMail(String emailId, HttpServletRequest httpRequest)
			throws Exception {

		Optional<UserLoginEntity> userOpt = userLoginRepository.findByEmailExcludeStatuses(emailId);

		if (!userOpt.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseApi<>(false, "Email not found", null));
		}

		UserLoginEntity user = userOpt.get();
		Integer userId = user.getUserId();

		String ip = commonFunction.resolveClientIp(httpRequest);

		int ccv = sendUserCreationEmail(user, "", userId, ip);

		if (ccv == 1) {
			return ResponseEntity.ok(new ResponseApi<>(true, "Password reset link sent successfully.", null));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseApi<>(false, "Failed to send password reset link.", null));
		}
	}
	
	private int sendUserCreationEmail(UserLoginEntity userRequest, String password, int uid, String ip)
			throws Exception {
		String usernameN = userRequest.getEmail();
		int ccv = 0;
		if (usernameN != null && !usernameN.isEmpty()) {
			String websitePath = paths;
			String link = websitePath + "forgot-password?emailId=" + commonFunction.cipher(usernameN) + "&userId="
					+ commonFunction.cipher(Integer.toString(uid));
			String mailBody = commonFunction.getForgotMessageCreate(userRequest.getFirstName(), link, websitePath);
			String filePath = commonFunction.createFolder(file_maillog);
			String fname = commonFunction.writeHTMLFile(mailBody, file_maillog + "/" + filePath,
					"np-" + System.currentTimeMillis());
			String[] to = { usernameN };
			String cc[] = new String[0];
			String bcc[] = new String[0];
			String subject = "";
			subject = "Task App :: Forgot Password";
			SmtpEntity findLatestSmtpDetail = smtpRepo.findLatestSmtpDetails();

			ccv = mailService.postMailAttach(to, cc, bcc, mailBody, subject, "", "", -1, "", findLatestSmtpDetail);
			String iplocal = commonFunction.getLocalIp();
			if (ccv > 0) {
				@SuppressWarnings("static-access")
				String linksentdate = commonFunction.getDateAfter(commonFunction.currDate1(), 4, 24,
						"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
				updateForgotpasswordlink(userRequest.getEmail(), "", linksentdate);
				createMailLog(2, userRequest.getFirstName(), usernameN, "", "", "", subject, filePath + "/" + fname, ip,
						iplocal, 1);

			} else {
				createMailLog(2, userRequest.getFirstName(), usernameN, "", "", "", subject, filePath + "/" + fname, ip,
						iplocal, 2);
			}
		}
		return ccv;
	}
	
	private void createMailLog(int type, String name, String to, String cc, String bcc, String from, String subject,
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
	
	
	@Transactional
	public int updateForgotpasswordlink(String username, String password, String linksentdate) throws Exception {
		Timestamp linkDateTs = linksentdate != null && !linksentdate.isEmpty() ? Timestamp.valueOf(linksentdate) : null;

		return userLoginRepository.updateForgotPasswordLink(username, commonFunction.cipher(password), linkDateTs);
	}
	
	public ResponseEntity<ResponseApi<String>> forgotpassword(String emailId, String userId, String password)
			throws Exception {
		// Decode inputs
		String decodedEmail = commonFunction.decipher(emailId).trim();
		String decodedUserId = commonFunction.decipher(userId).trim();
		String encodedPassword = commonFunction.cipher(password);

		// Validate if userId is numeric before parsing
		int result = 0;
		try {
			int parsedUserId = Integer.parseInt(decodedUserId);
			result = checkURLValidity(decodedEmail, parsedUserId);
		} catch (NumberFormatException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseApi<>(false, "Invalid User ID in URL", null));
		}

		if (result > 0) {

			Optional<Integer> findUserIdByEmailAndStatus = userLoginRepository.findUserIdByEmailAndStatus(decodedEmail,
					1);
			Integer uId = findUserIdByEmailAndStatus.get();
			if (uId > 0) {
				int count = userLoginRepository.updateForgotPassword(Integer.parseInt(decodedUserId), encodedPassword,
						null);

				if (count == 1) {
					return ResponseEntity.ok(new ResponseApi<>(true, "Password has been created successfully.", null));
				} else {
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body(new ResponseApi<>(false, "Something went wrong while updating the password.", null));
				}
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseApi<>(false, "Invalid user details.", null));
			}

		} else {
			// Invalid URL → error response
			String message = "URL is no longer valid or has expired. "
					+ "Please go to the Login Page, enter your email ID, and use the Forgot Password link again.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseApi<>(false, message, null));
		}
	}
	
	public int checkURLValidity(String email, int userId) {
		// Assuming repository returns Optional<Integer>
		return userLoginRepository.checkURLValidity(email, userId).orElse(0);
	}

}
