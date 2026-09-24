package com.webelement.taskapp.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webelement.taskapp.common.CommonFunction;
import com.webelement.taskapp.controller.TaskMailService;
import com.webelement.taskapp.entity.ClientEntity;
import com.webelement.taskapp.entity.SmtpEntity;
import com.webelement.taskapp.entity.TaskCategoryEntity;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.entity.UserLoginEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.PlanRepo;
import com.webelement.taskapp.repo.SmtpRepo;
import com.webelement.taskapp.repo.UserLoginRepository;
import com.webelement.taskapp.service.MailService;
import com.webelement.taskapp.repo.TaskCategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskMailServiceImpl implements TaskMailService {

	private final TaskCategoryRepository  TaskCategoryRepository;
	private final UserLoginRepository userLoginRepository;
	private final ClientRepository clientRepository;
	private final MailService mailService;
	private final CommonFunction commonFunction;
	private final SmtpRepo smtpRepo;
	static final Logger logger = LoggerFactory.getLogger(TaskMailServiceImpl.class);
	private final HttpServletRequest request;
	
	@Value("${file_maillog:}")
	private  String file_maillog;
	
	

	@Override
	public void sendTaskStatusMail(TaskEntity task, String oldStatus, String newStatus) throws Exception {
		String filePath = commonFunction.createFolder(file_maillog);
		ClientEntity client = clientRepository.findById(task.getClientId()).orElse(null);
		Set<Integer> userIds = new HashSet<>();

		Optional.ofNullable(task.getAssignedTo()).ifPresent(userIds::add);
		Optional.ofNullable(task.getAddedBy()).ifPresent(userIds::add);

		if (client != null) {
			Optional.ofNullable(client.getManagerId()).ifPresent(userIds::add);
		}
		Map<Integer, UserLoginEntity> userMap = userLoginRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(UserLoginEntity::getUserId, Function.identity()));
		UserLoginEntity assignedUser = userMap.get(task.getAssignedTo());
		UserLoginEntity addedByUser = userMap.get(task.getAddedBy());
		UserLoginEntity societyManager = client != null ? userMap.get(client.getManagerId()) : null;
		Set<String> toSet = new LinkedHashSet<>();
		Set<String> ccSet = new LinkedHashSet<>();
		
		
//		boolean isClose = "Assignor Closure".equalsIgnoreCase(newStatus);
		boolean isClose = "Assignor Closure".equalsIgnoreCase(newStatus) || "Re-Open".equalsIgnoreCase(newStatus);
        
		logger.info("{} check the status ",isClose);
		String recipientName = "";
		if (isClose) {
			
			addEmail(toSet, assignedUser);
			addEmail(ccSet, addedByUser);
			addEmail(ccSet, societyManager);
			recipientName = assignedUser.getFirstName();
		}else {
			addEmail(toSet, addedByUser);
			addEmail(ccSet, assignedUser);
			addEmail(ccSet, societyManager);
			recipientName = addedByUser.getFirstName();
		}
//		if (isClose) {
//			addEmail(toSet, addedByUser);
//			addEmail(ccSet, assignedUser);
//			addEmail(ccSet, societyManager);
//		} else {
//			addEmail(toSet, assignedUser);
//			addEmail(ccSet, addedByUser);
//			addEmail(ccSet, societyManager);
//		}
		ccSet.removeAll(toSet);
		if (toSet.isEmpty()) {
			logger.warn("No recipient found for task {}", task.getTaskId());
			return;
		}
//		String recipientName = assignedUser != null ? assignedUser.getFirstName() : "";
		String mailBody = commonFunction.getTaskStatusMailTemplate(recipientName, task.getTitle(), oldStatus, newStatus,"");
		String[] to = toSet.toArray(new String[0]);
		String[] cc = ccSet.toArray(new String[0]);
		SmtpEntity smtp = smtpRepo.findLatestSmtpDetails();
		Integer result = mailService.postMailAttach(to, cc, new String[0], mailBody, "Task App :: Task Status Updated",
				"", "", -1, "", smtp);
		logger.info("Mail service response = {}", result);
		String ip = commonFunction.resolveClientIp(request);
		String fname = commonFunction.writeHTMLFile(mailBody, file_maillog + "/" + filePath,"np-" + System.currentTimeMillis());
		commonFunction.createMailLog(1, recipientName, String.join(",", toSet), String.join(",", ccSet), "", "","Task App :: Task Status Updated", filePath + "/" + fname, ip, commonFunction.getLocalIp(), 1);
	}
	
	@Override
	public void sendTaskReassignMail(TaskEntity task, Integer oldAssigneeId) throws Exception {
		String filePath = commonFunction.createFolder(file_maillog);
		ClientEntity client = clientRepository.findById(task.getClientId()).orElse(null);
		Set<Integer> userIds = new HashSet<>();
		Optional.ofNullable(task.getAssignedTo()).ifPresent(userIds::add);
		Optional.ofNullable(task.getAddedBy()).ifPresent(userIds::add);
		Optional.ofNullable(oldAssigneeId).ifPresent(userIds::add);
		if (client != null) {
			Optional.ofNullable(client.getManagerId()).ifPresent(userIds::add);
		}
		Map<Integer, UserLoginEntity> userMap = userLoginRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(UserLoginEntity::getUserId, Function.identity()));
		UserLoginEntity newAssignee = userMap.get(task.getAssignedTo());
		UserLoginEntity assignor = userMap.get(task.getAddedBy());
		UserLoginEntity oldAssignee = userMap.get(oldAssigneeId);
		UserLoginEntity societyManager = client != null ? userMap.get(client.getManagerId()) : null;
		Set<String> toSet = new LinkedHashSet<>();
		Set<String> ccSet = new LinkedHashSet<>();
		addEmail(toSet, newAssignee);
		addEmail(ccSet, assignor);
		addEmail(ccSet, societyManager);
		addEmail(ccSet, oldAssignee);
		ccSet.removeAll(toSet);
		if (toSet.isEmpty()) {
			logger.warn("No recipient found for task {}", task.getTaskId());
			return;
		}
		String recipientName = newAssignee != null ? newAssignee.getFirstName() : "";
		String startDate = "";

		if (task.getDate() != null) {
			startDate = task.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
		}
		String mailBody = commonFunction.getTaskReAssignedMailTemplate(recipientName, task.getTitle(),
				assignor != null ? assignor.getFirstName() : "", client != null ? client.getName() : "",
				getPriorityName(task.getPriority()), startDate, calculateDueDate(task), task.getDescription());
		String[] to = toSet.toArray(new String[0]);
		String[] cc = ccSet.toArray(new String[0]);
		SmtpEntity smtp = smtpRepo.findLatestSmtpDetails();
		Integer result = mailService.postMailAttach(to, cc, new String[0], mailBody, "Task App :: Task Reassigned", "",
				"", -1, "", smtp);
		logger.info("Reassign mail response = {}", result);
		String ip = commonFunction.resolveClientIp(request);
		String fname = commonFunction.writeHTMLFile(mailBody, file_maillog + "/" + filePath,
				"np-" + System.currentTimeMillis());
		commonFunction.createMailLog(1, recipientName, String.join(",", toSet), String.join(",", ccSet), "", "",
				"Task App :: Task Reassigned", filePath + "/" + fname, ip, commonFunction.getLocalIp(), 1);
	}
	
	@Override
	public void sendTaskAssignedMail(TaskEntity task) throws Exception {
		String filePath = commonFunction.createFolder(file_maillog);

		ClientEntity client = clientRepository.findById(task.getClientId()).orElse(null);
		Set<Integer> userIds = new HashSet<>();
		Optional.ofNullable(task.getAssignedTo()).ifPresent(userIds::add);
		Optional.ofNullable(task.getAddedBy()).ifPresent(userIds::add);
		if (client != null) {
			Optional.ofNullable(client.getManagerId()).ifPresent(userIds::add);
		}
		Map<Integer, UserLoginEntity> userMap = userLoginRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(UserLoginEntity::getUserId, Function.identity()));
		UserLoginEntity assignedUser = userMap.get(task.getAssignedTo());
		UserLoginEntity addedByUser = userMap.get(task.getAddedBy());
		UserLoginEntity societyManager = client != null ? userMap.get(client.getManagerId()) : null;
		if (assignedUser == null || assignedUser.getEmail() == null || assignedUser.getEmail().trim().isEmpty()) {
			logger.warn("Assigned user email not found for task {}", task.getTaskId());
			return;
		}
		Set<String> toSet = new LinkedHashSet<>();
		Set<String> ccSet = new LinkedHashSet<>();
		addEmail(toSet, assignedUser);
		addEmail(ccSet, addedByUser);
		addEmail(ccSet, societyManager);
		ccSet.removeAll(toSet);
		String recipientName = assignedUser.getFirstName();
		String startDate = "";

		if (task.getDate() != null) {
			startDate = task.getDate()
		            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));
		}
		String mailBody = commonFunction.getTaskAssignedMailTemplate(recipientName, task.getTitle(),
				addedByUser != null ? addedByUser.getFirstName() : "", client != null ? client.getName() : "", getPriorityName(task.getPriority()),startDate ,calculateDueDate(task) ,
				task.getDescription());	
		String[] to = toSet.toArray(new String[0]);
		String[] cc = ccSet.toArray(new String[0]);
		String subject = "Task App :: New Task Assigned";
		SmtpEntity smtp = smtpRepo.findLatestSmtpDetails();
		Integer result = mailService.postMailAttach(to, cc, new String[0], mailBody, subject, "", "", -1, "", smtp);
		logger.info("Task assignment mail response = {}", result);
		String ip = commonFunction.resolveClientIp(request);
		String fname = commonFunction.writeHTMLFile(mailBody, file_maillog + "/" + filePath,"np-" + System.currentTimeMillis());
		commonFunction.createMailLog(1, recipientName, String.join(",", toSet), String.join(",", ccSet), "", "",subject, filePath + "/" + fname, ip, commonFunction.getLocalIp(), 1);
	}

	private void addEmail(Set<String> emails, UserLoginEntity user) {

		if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {

			emails.add(user.getEmail().trim());
		}
	}
	
	private String getPriorityName(Short priority) {

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
	
	
	private String calculateDueDate(TaskEntity task) {

		if (task == null || task.getTaskCategoryId() == null || task.getDate() == null) {
			return "";
		}

		TaskCategoryEntity taskCategory = TaskCategoryRepository.findById(task.getTaskCategoryId()).orElse(null);

		if (taskCategory == null || taskCategory.getDuedatetime() == null || taskCategory.getDuedatetime().isBlank()) {
			return "";
		}

		try {

			long hours = Long.parseLong(taskCategory.getDuedatetime().trim());

			return task.getDate().plusHours(hours).format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm"));

		} catch (Exception e) {

			logger.error("Error calculating due date for task {}", task.getTaskId(), e);

			return "";
		}
	}
}
