package com.webelement.taskapp.service.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${file_maillog:}")
	private String file_maillog;

	public void sendTaskNotesMail(TaskNoteEntity request) throws Exception {
		String filePath = commonFunction.createFolder(file_maillog);
		TaskEntity task = taskRepo.findById(request.getTaskId())
				.orElseThrow(() -> new RuntimeException("Task not found"));
		Set<Integer> userIds = new HashSet<>();
		Optional.ofNullable(task.getAssignedTo()).ifPresent(userIds::add);
		Optional.ofNullable(task.getAddedBy()).ifPresent(userIds::add);
		Optional.ofNullable(task.getBuddyId()).ifPresent(userIds::add);      // ADD
		Optional.ofNullable(request.getUserId()).ifPresent(userIds::add);
		Map<Integer, UserLoginEntity> userMap = userLoginRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(UserLoginEntity::getUserId, Function.identity()));
		UserLoginEntity assignedUser = userMap.get(task.getAssignedTo());
		UserLoginEntity addedByUser = userMap.get(task.getAddedBy());
		UserLoginEntity buddyUser = task.getBuddyId() != null ? userMap.get(task.getBuddyId()) : null;   // ADD
		UserLoginEntity actionUser = userMap.get(request.getUserId());
		Set<String> toEmails = new LinkedHashSet<>();
		Set<String> ccEmails = new LinkedHashSet<>();
		boolean isAdminUser = "Y".equalsIgnoreCase(request.getIsAdmin());
		boolean noteByAssignee = Objects.equals(request.getUserId(), task.getAssignedTo());
		UserLoginEntity recipientUser;
		if (isAdminUser) {
			commonFunction.addEmail(toEmails, assignedUser);
			commonFunction.addEmail(toEmails, addedByUser);
			commonFunction.addEmail(ccEmails, actionUser);
			recipientUser = assignedUser;
		} else if (noteByAssignee) {
			commonFunction.addEmail(toEmails, addedByUser);
			commonFunction.addEmail(ccEmails, assignedUser);
			recipientUser = addedByUser;
		} else {
			commonFunction.addEmail(toEmails, assignedUser);
			commonFunction.addEmail(ccEmails, addedByUser);
			recipientUser = assignedUser;
		}
		
		if (buddyUser != null) {
		    commonFunction.addEmail(ccEmails, buddyUser);
		}
		ccEmails.removeAll(toEmails);
		if (toEmails.isEmpty()) {
			logger.warn("No recipients found for task {}", task.getTaskId());
			return;
		}
		String recipientName = commonFunction.getFirstName(recipientUser);
		String actionUserName = commonFunction.getFirstName(actionUser);
		String mailBody = commonFunction.getTaskNotesMailTemplate(recipientName, task.getTitle(), request.getNote(),
				actionUserName, "");
		String[] to = toEmails.toArray(new String[0]);
		String[] cc = ccEmails.toArray(new String[0]);
//		String subject = "Task App :: Task Notes";
		String subject = "Task App :: Task Notes";
		String emailSubject = "Task Id: " + request.getTaskId() + " - " + subject;
		SmtpEntity smtp = smtpRepo.findLatestSmtpDetails();
		Integer result = mailService.postMailAttach(to, cc, new String[0], mailBody, subject, "", "", -1, "", smtp);
//		logger.info("Task Notes Mail Sent. TaskId={}, Result={}, To={}, Cc={}", task.getTaskId(), result,
//				String.join(",", toEmails), String.join(",", ccEmails));
		String ip = commonFunction.resolveClientIp(httpReq);
		String iplocal = commonFunction.getLocalIp();
		String fname = commonFunction.writeHTMLFile(mailBody, file_maillog + "/" + filePath,
				"np-" + System.currentTimeMillis());
		commonFunction.createMailLog(1, recipientName, String.join(",", toEmails), String.join(",", ccEmails),
				String.join(",", toEmails), "", subject, filePath + "/" + fname, ip, iplocal, 1);
	}

}
