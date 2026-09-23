package com.webelement.taskapp.service.impl;

import java.util.Arrays;
import java.util.HashSet;
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

		Set<Integer> userIds = new HashSet<>();

		Optional.ofNullable(task.getAssignedTo()).ifPresent(userIds::add);
		Optional.ofNullable(task.getAddedBy()).ifPresent(userIds::add);
		Optional.ofNullable(request.getUserId()).ifPresent(userIds::add);

		Map<Integer, UserLoginEntity> userMap = userLoginRepository.findAllById(userIds).stream().collect(Collectors.toMap(UserLoginEntity::getUserId, Function.identity()));

		UserLoginEntity assignedUser = userMap.get(task.getAssignedTo());
		UserLoginEntity addedByUser = userMap.get(task.getAddedBy());
		UserLoginEntity actionUser = userMap.get(request.getUserId());

		if (assignedUser == null || assignedUser.getEmail() == null || assignedUser.getEmail().trim().isEmpty()) {
			logger.warn("Assigned user email not found for task {}", task.getTaskId());
			return;
		}
		Set<String> toEmails = new LinkedHashSet<>();
		Set<String> ccEmails = new LinkedHashSet<>();
		addEmail(toEmails, assignedUser);
		boolean isAdminUser = "Y".equalsIgnoreCase(request.getIsAdmin());
		if (isAdminUser) {
			addEmail(toEmails, addedByUser);
			addEmail(ccEmails, actionUser);
		} else {
			addEmail(ccEmails, addedByUser);
		}
		ccEmails.removeAll(toEmails);

		if (toEmails.isEmpty()) {
			logger.warn("No recipients found for task {}", task.getTaskId());
			return;
		}

		String recipientName = assignedUser.getFirstName() != null ? assignedUser.getFirstName() : "";
		String actionUserName = actionUser != null && actionUser.getFirstName() != null ? actionUser.getFirstName() : "";
		String mailBody = commonFunction.getTaskNotesMailTemplate(recipientName, task.getTitle(), request.getNote(),actionUserName, "");

		String[] to = toEmails.toArray(new String[0]);
		String[] cc = ccEmails.toArray(new String[0]);

		String subject = "Task App :: Task Notes";

		SmtpEntity smtp = smtpRepo.findLatestSmtpDetails();

		Integer result = mailService.postMailAttach(to, cc, new String[0], mailBody, subject, "", "", -1, "", smtp);

		logger.info("Task Notes Mail Sent. TaskId={}, Result={}, To={}, Cc={}", task.getTaskId(), result,
				String.join(",", toEmails), String.join(",", ccEmails));

		String ip = commonFunction.resolveClientIp(httpReq);

		commonFunction.createMailLog(2, recipientName, String.join(",", toEmails), String.join(",", ccEmails), "", "",
				subject, "", ip, "", 2);
	}

	private void addEmail(Set<String> emails, UserLoginEntity user) {

		if (user == null || user.getEmail() == null) {
			return;
		}

		String email = user.getEmail().trim().toLowerCase();

		if (!email.isEmpty()) {
			emails.add(email);
		}
	}

}
