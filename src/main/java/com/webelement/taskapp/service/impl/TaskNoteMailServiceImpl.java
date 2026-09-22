package com.webelement.taskapp.service.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.entity.SmtpEntity;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.entity.TaskNoteEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.SmtpRepo;
import com.webelement.taskapp.repo.TaskRepository;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.MailService;
import com.webelement.taskapp.service.TaskNotesMailService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskNoteMailServiceImpl implements TaskNotesMailService {
	private final UserLoginRepository userLoginRepository;
	private final TaskRepository taskRepo;
	private final MailService mailService;
	private final CommonFunction commonFunction;
	private final SmtpRepo smtpRepo;
	static final Logger logger = LoggerFactory.getLogger(TaskNoteMailServiceImpl.class);
	
	private final HttpServletRequest httpReq;
	@Override
	public void sendTaskNotesMail(TaskNoteEntity request) throws Exception {
		TaskEntity task = taskRepo.findById(request.getTaskId())
				.orElseThrow(() -> new RuntimeException("Task not found"));

		List<Integer> userIds = Arrays.asList(task.getAssignedTo(), task.getAddedBy(), request.getUserId());

		Map<Integer, UserLoginEntity> userMap = userLoginRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(UserLoginEntity::getUserId, Function.identity()));
		UserLoginEntity assignedUser = userMap.get(task.getAssignedTo());
		UserLoginEntity addedByUser = userMap.get(task.getAddedBy());
		UserLoginEntity actionUser = userMap.get(request.getUserId());
		if (assignedUser == null || assignedUser.getEmail() == null || assignedUser.getEmail().trim().isEmpty()) {
			return;
		}
		String mailBody = commonFunction.getTaskNotesMailTemplate(assignedUser.getFirstName(), task.getTitle(),request.getNote(), actionUser != null ? actionUser.getFirstName() : "", "");
		Set<String> toEmails = new LinkedHashSet<>();
		Set<String> ccEmails = new LinkedHashSet<>();
		// Always send to assigned user
		toEmails.add(assignedUser.getEmail());
		boolean isAdminUser = actionUser != null && "Y".equalsIgnoreCase(request.getIsAdmin());
		if (isAdminUser) {
			// Send to Assigned User + Added By User
			if (addedByUser != null && addedByUser.getEmail() != null && !addedByUser.getEmail().trim().isEmpty()) {

				toEmails.add(addedByUser.getEmail());
			}
			// Admin in CC
			if (actionUser.getEmail() != null && !actionUser.getEmail().trim().isEmpty()) {
				ccEmails.add(actionUser.getEmail());
			}
		} else {
			// Normal User
			if (addedByUser != null && addedByUser.getEmail() != null && !addedByUser.getEmail().trim().isEmpty()) {
				ccEmails.add(addedByUser.getEmail());
			}
		}
		String[] to = toEmails.toArray(new String[0]);
		String[] cc = ccEmails.toArray(new String[0]);
		String[] bcc = new String[0];
		String subject = "Task App :: Task Notes";
		SmtpEntity smtp = smtpRepo.findLatestSmtpDetails();
		Integer result = mailService.postMailAttach(to, cc, bcc, mailBody, subject, "", "", -1, "", smtp);

		logger.info("Mail service response = {}", result);
		

		String ip = commonFunction.resolveClientIp(httpReq);
		
		commonFunction.createMailLog(2, addedByUser.getFirstName(), addedByUser.getEmail(), "", "", "", subject, "", ip,"", 2);
	}

}
