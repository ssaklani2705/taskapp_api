package com.webelement.taskapp.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.controller.TaskMailService;
import com.webelement.taskapp.entity.SmtpEntity;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.PlanRepo;
import com.webelement.taskapp.repo.SmtpRepo;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.MailService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskMailServiceImpl implements TaskMailService {
	
	private final UserLoginRepository userLoginRepository;
	private final MailService mailService;
	private final CommonFunction commonFunction;
	private final SmtpRepo smtpRepo;
	static final Logger logger = LoggerFactory.getLogger(TaskMailServiceImpl.class);
	
	@Override
	public void sendTaskStatusMail(TaskEntity task, String oldStatus, String newStatus) throws Exception {
		List<Integer> userIds = Arrays.asList(task.getAssignedTo(), task.getAddedBy());

		Map<Integer, UserLoginEntity> userMap = userLoginRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(UserLoginEntity::getUserId, Function.identity()));

		UserLoginEntity assignedUser = userMap.get(task.getAssignedTo());
		UserLoginEntity addedByUser = userMap.get(task.getAddedBy());
		if (assignedUser == null || assignedUser.getEmail() == null || assignedUser.getEmail().trim().isEmpty()) {
			return;
		}
		String mailBody = commonFunction.getTaskStatusMailTemplate(assignedUser.getFirstName(), task.getTitle(),
				oldStatus, newStatus, "");
		String[] to = { assignedUser.getEmail() };
		String[] cc = { addedByUser.getEmail() };
		String[] bcc = new String[0];
		String subject = "Task App :: Task Status Updated";
		SmtpEntity smtp = smtpRepo.findLatestSmtpDetails();
		Integer result = mailService.postMailAttach(to, cc, bcc, mailBody, subject, "", "", -1, "", smtp);
		logger.info("Mail service response = {}", result);
	}
}
