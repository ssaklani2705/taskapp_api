package com.webelement.taskapp.service;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.dto.UserAccessLogDTO;
import com.webelement.taskapp.entity.UserAccessLogEntity;
import com.webelement.taskapp.repo.UserAccessLogRepo;
;

@Service
public class UserAccessLogService {

	@Autowired
	private UserAccessLogRepo accessLogRepo;

	@Autowired
	private CommonFunction commonFunction;

	public UserAccessLogEntity saveLogin(Integer userId, String username, HttpServletRequest request) {
		UserAccessLogEntity log = new UserAccessLogEntity();
		log.setUserId(userId);
		log.setIpAddress(commonFunction.resolveClientIp(request));
		log.setLoginTime(LocalDateTime.now());
		log.setStatus(0); // active session
		log.setIbankUserId(-1);
		log.setUserName(username);

		return accessLogRepo.save(log);
	}

	public void updateLogout(int sessionId) {
		Optional<UserAccessLogEntity> optionalLog = accessLogRepo.findByLogId(sessionId);

		if (optionalLog.isPresent()) {
			UserAccessLogEntity log = optionalLog.get();
			log.setLogoutTime(LocalDateTime.now());
			log.setStatus(1); // mark session as logged out
			accessLogRepo.save(log);
		}
	}

	public Page<UserAccessLogDTO> getUserAccessDetails(int page, int size, String search) {
		Pageable pageable = PageRequest.of(page, size);
		return accessLogRepo.findUserAccessDetails(pageable, search);
	}

}
