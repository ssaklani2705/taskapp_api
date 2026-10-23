package com.webelement.taskapp.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.Exceptions.AccountExpiredException;
import com.webelement.taskapp.Exceptions.ApiException;
import com.webelement.taskapp.Exceptions.FileDownloadException;
import com.webelement.taskapp.Exceptions.InvalidCaptchaException;
import com.webelement.taskapp.Exceptions.InvalidRequestException;
import com.webelement.taskapp.Exceptions.ResourceNotFoundException;
import com.webelement.taskapp.Exceptions.TooManyAttemptsException;
import com.webelement.taskapp.Exceptions.UnauthorizedUserException;
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
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.MailLogRepo;
import com.webelement.taskapp.repo.SmtpRepo;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.AuthService;
import com.webelement.taskapp.service.CaptchaService;
import com.webelement.taskapp.service.LoginAttemptService;
import com.webelement.taskapp.service.MailService;
import com.webelement.taskapp.service.MyUserDetailsService;

import com.webelement.taskapp.service.UserAccessLogService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final CommonFunction commonFunction;
	private final UserLoginRepository userLoginRepository;
	private final SmtpRepo smtpRepo;
	private final UserAccessLogService accessLogService;
	private final MailService mailService;
	private final CaptchaService captchaService;
	private final LoginAttemptService attemptService;
	private final JwtUtil jwtUtil;
	private final CommonFunction helper;

	@Value("${file_maillog:}")
	private String file_maillog;

	@Value("${paths:}")
	private String paths;

	private HttpServletRequest httpRequest;

	@Override
	public ResponseEntity<ResponseApi<LoginResponse>> login(LoginRequest request) throws Exception {
		if (!captchaService.verifyAndConsume(request.getCaptchaId(), request.getCaptcha())) {
			throw ApiException.badRequest("Invalid captcha code.");
		}
		String loginId = request.getUsername() != null ? request.getUsername().trim().toLowerCase() : "";
		String password = request.getPassword() != null ? request.getPassword() : "";

		if (attemptService.isBlocked(loginId)) {
			throw ApiException.tooManyRequests("Too many failed attempts. Please try again in 15 minutes.");
		}

		String iplocal = commonFunction.getLocalIp();
		String logintype = request.getLogintype();
		UserInfo info = helper.getUserAccess(loginId, password, iplocal, logintype);

		if (info == null) {
			attemptService.failed(loginId);
			throw ApiException.unauthorized("Invalid email or password.");
		}

		if (info.getStatus() < 1) {
			throw ApiException
					.accountExpiredException("Your account has been expired, please contact the administrator.");
		}

		int userId = info.getUserId();
		if (userId <= 0) {
			attemptService.failed(loginId);
			throw ApiException.unauthorized("Invalid email or password.");
		}

		String permission = info.getPermission() != null ? info.getPermission() : "";
		String loginTypeNorm = "manager".equals(logintype) ? "manager" : "other";
		String token = jwtUtil.generateAccessToken(loginId, userId, loginTypeNorm);

		List<ModulePermissionDTO> modules = getModuleListByName(userId, "Y".equals(permission));
		UserAccessLogEntity accessLogEntity = accessLogService.saveLogin(userId, info.getFirstName(), httpRequest);

		List<Integer> categoryIds = new ArrayList<>();
		if (info.getTaskcategoryIds() != null && !info.getTaskcategoryIds().trim().isEmpty()) {
			categoryIds = Arrays.stream(info.getTaskcategoryIds().split(",")).map(String::trim)
					.filter(s -> !s.isEmpty()).map(Integer::parseInt).collect(Collectors.toList());
		}

		LoginResponse loginResponse = new LoginResponse(token, userId, info.getFirstName(), accessLogEntity.getLogId(),
				permission, info.getDesignationName(), modules, info.getIsHod(), info.getEmail(), categoryIds);

		attemptService.succeeded(loginId);
		return ResponseEntity.ok(new ResponseApi<>(true, "Login successful", loginResponse));

	}

	@Override
	public ResponseEntity<ResponseApi<String>> forgotpasswordMail(String emailId, Boolean isManager, String loginType)
			throws Exception {
		Optional<UserLoginEntity> userOpt;
		if (loginType.equals("manager")) {
			userOpt = userLoginRepository.findByEmailExcludeStatuses1(emailId);
		} else {
			userOpt = userLoginRepository.findByEmailExcludeStatuses(emailId);
		}
		if (!userOpt.isPresent()) {
			throw ApiException.notFound("Email not found");
		}
		UserLoginEntity user = userOpt.get();
		Integer userId = user.getUserId();
		String ip = commonFunction.resolveClientIp(httpRequest);
		int ccv = sendUserCreationEmail(user, "", userId, ip, loginType);
		if (ccv == 1) {
			return ResponseEntity.ok(new ResponseApi<>(true, "Password reset link sent successfully.", null));
		} else {
			throw ApiException.internal("Failed to send password reset link.", null);
		}

	}

	@Override
	public ResponseEntity<ResponseApi<String>> forgotpassword(String emailId, String userId, String password,
			String isManager) throws Exception {
		// Decode inputs
		String decodedEmail = commonFunction.decipher(emailId).trim();
		String decodedUserId = commonFunction.decipher(userId).trim();
		String encodedPassword = commonFunction.cipher(password);
		String isManger = null;
		if (isManager != null && !isManager.trim().isEmpty()) {
			isManger = commonFunction.decipher(isManager);
		}
		// Validate if userId is numeric before parsing
		int result = 0;
		try {
			int parsedUserId = Integer.parseInt(decodedUserId);
			result = checkURLValidity(decodedEmail, parsedUserId);
		} catch (NumberFormatException e) {
			throw ApiException.badRequest("Invalid User ID in URL");
		}
		if (result > 0) {
			Optional<Integer> findUserIdByEmailAndStatus = userLoginRepository.findUserIdByEmailAndStatus(decodedEmail,
					1);
			Integer uId = findUserIdByEmailAndStatus.get();
			if (uId > 0) {
				int count = userLoginRepository.updateForgotPassword(Integer.parseInt(decodedUserId), encodedPassword,
						null);
				if (count == 1) {
					return ResponseEntity
							.ok(new ResponseApi<>(true, "Password has been created successfully.", isManger));
				} else {
					throw ApiException.internal("Something went wrong while updating the password.", null);
				}
			} else {
				throw new ResourceNotFoundException("Invalid user details.");
			}
		} else {
			// Invalid URL → error response
			String message = "URL is no longer valid or has expired. "
					+ "Please go to the Login Page, enter your email ID, and use the Forgot Password link again.";
			throw ApiException.badRequest(message);
		}

	}

	@Override
	public List<ModulePermissionDTO> getModuleListByName(int userId, boolean isFullList) {
		List<Map<String, Object>> rows = userLoginRepository.getModuleListByName(userId);
		List<ModulePermissionDTO> list = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			ModulePermissionDTO dto = new ModulePermissionDTO(userId, helper.getInt(row.get("i_moduleid")),
					helper.getString(row.get("s_name")), helper.getInt(row.get("i_type")), helper.getString(row.get("s_add")),
					helper.getString(row.get("s_edit")), helper.getString(row.get("s_delete")), helper.getString(row.get("s_approve")),
					helper.getString(row.get("s_adminapprove")), helper.getString(row.get("s_view")),
					helper.getString(row.get("s_exportexcel")));
			if (isFullList) {
				dto.setAddPer("Y");
				dto.setEditPer("Y");
				dto.setDeletePer("Y");
				dto.setApprovePer("Y");
				dto.setAdminApprovePer("Y");
				dto.setViewPer("Y");
				dto.setExportExcel("Y");
				list.add(dto); // always include in full list
			} else if (helper.anyPermissionGranted(dto)) {
				list.add(dto);
			}
		}
		return list; // Return actual list

	}

	@Override
	public int updateForgotpasswordlink(String username, String password, String linksentdate) throws Exception {
		Timestamp linkDateTs = linksentdate != null && !linksentdate.isEmpty() ? Timestamp.valueOf(linksentdate) : null;
		return userLoginRepository.updateForgotPasswordLink(username, commonFunction.cipher(password), linkDateTs);

	}

	@Override
	public int checkURLValidity(String email, int userId) {
		return userLoginRepository.checkURLValidity(email, userId).orElse(0);

	}

	@Override
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

	public int sendUserCreationEmail(UserLoginEntity userRequest, String password, int uid, String ip,
			String isManager) throws Exception {
		String usernameN = userRequest.getEmail();
		int ccv = 0;
		if (usernameN != null && !usernameN.isEmpty()) {
			String websitePath = paths;
			String link = websitePath + "forgot-password?emailId=" + commonFunction.cipher(usernameN) + "&userId="
					+ commonFunction.cipher(Integer.toString(uid)) + "&isManagerLogin="
					+ commonFunction.cipher(isManager);
			String mailBody = commonFunction.getForgotMessageCreate(userRequest.getFirstName(), link, websitePath);
			String filePath = commonFunction.createFolder(file_maillog);
			String fname = commonFunction.writeHTMLFile(mailBody, file_maillog + "/" + filePath,
					"np-" + System.currentTimeMillis());
			String[] to = { usernameN };
			String cc[] = new String[0];
			String bcc[] = new String[0];
			String subject = "";
			subject = "Society Task App :: Forgot Password";
			SmtpEntity findLatestSmtpDetail = smtpRepo.findLatestSmtpDetails();
			ccv = mailService.postMailAttach(to, cc, bcc, mailBody, subject, "", "", -1, "", findLatestSmtpDetail);
			String iplocal = commonFunction.getLocalIp();
			if (ccv > 0) {
				@SuppressWarnings("static-access")
				String linksentdate = commonFunction.getDateAfter(commonFunction.currDate1(), 4, 24,
						"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
				updateForgotpasswordlink(userRequest.getEmail(), "", linksentdate);
				helper.createMailLog(2, userRequest.getFirstName(), usernameN, "", "", "", subject, filePath + "/" + fname, ip,
						iplocal, 1);
			} else {
				helper.createMailLog(2, userRequest.getFirstName(), usernameN, "", "", "", subject, filePath + "/" + fname, ip,
						iplocal, 2);
			}
		}
		return ccv;
	}

}
