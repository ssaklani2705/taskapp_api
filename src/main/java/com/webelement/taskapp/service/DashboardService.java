package com.webelement.taskapp.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.ClientDashboardDTO;
import com.webelement.taskapp.dto.TaskDashboardItem;
import com.webelement.taskapp.dto.TaskDashboardResponse;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.TaskGroupResponse;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.repo.TaskRepository;

@Service
public class DashboardService {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	@Autowired
	private TaskRepository taskRepository;

	public List<ClientDashboardDTO> findDashboardClients(Integer userId, String isAdmin, String loginType) {

		List<Object[]> result = taskRepository.findDashboardClients(userId, isAdmin, loginType);

		return result.stream().map(row -> new ClientDashboardDTO((Integer) row[0], (String) row[1]))
				.collect(Collectors.toList());
	}

	private static final short TODO = 1;
	private static final short IN_PROGRESS = 2;
	private static final short DONE = 5;

	private String getAssignedUserName(TaskEntity task) {
		if (task.getAssignedTo() == null || task.getAssignedTo() == 0) {
			return "Unassigned";
		}

		if (task.getAssignedUser() == null) {
			return "Unassigned";
		}

		return task.getAssignedUser().getFirstName();
	}

	public TaskDashboardResponse getDashboard(Integer userId, String isAdmin, Integer selectedClientId) {
		LocalDate today = LocalDate.now();
		LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
		LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
		LocalDateTime startOfToday = today.atStartOfDay();
		LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
		LocalDateTime now = LocalDateTime.now();
		List<TaskEntity> tasks = taskRepository.findDashboardTasks(userId, isAdmin, "other", selectedClientId);
		if (tasks == null || tasks.isEmpty()) {
			return TaskDashboardResponse.builder().myTasksToday(0).dueThisWeek(0).overdue(0)
					.todo(TaskGroupResponse.builder().count(0).tasks(Collections.emptyList()).build())
					.inProgress(TaskGroupResponse.builder().count(0).tasks(Collections.emptyList()).build())
					.done(TaskGroupResponse.builder().count(0).tasks(Collections.emptyList()).build())
					.build();
		}
		
		List<TaskEntity> tasksToday = tasks.stream().filter(task -> {
			LocalDateTime taskDate = task.getDate();
			if (taskDate == null) {
				return false;
			}
			return !taskDate.isBefore(startOfToday) && taskDate.isBefore(startOfTomorrow);
		}).collect(Collectors.toList());

		List<TaskEntity> dueThisWeek = tasks.stream().filter(task -> {
			LocalDateTime taskStartDate = task.getDate();
			if (taskStartDate == null) {
				return false;
			}
			return task.getTaskStatus() != null && task.getTaskStatus() != 5 && !taskStartDate.isBefore(startOfWeek)
					&& !taskStartDate.isAfter(endOfWeek);
		}).collect(Collectors.toList());
		
		
		

		List<TaskEntity> overdueTasks = tasks.stream().filter(task -> {
			LocalDateTime dueDateTime = getDueDateTime(task);
			return dueDateTime != null && dueDateTime.isBefore(now) && !isDone(task);
		}).collect(Collectors.toList());

		List<TaskEntity> todoTasks = tasks.stream()
				.filter(task -> task.getTaskStatus() != null && task.getTaskStatus() == TODO)
				.collect(Collectors.toList());

//		List<TaskEntity> inProgressTasks = tasks.stream()
//				.filter(task -> task.getTaskStatus() != null && task.getTaskStatus() == IN_PROGRESS)
//				.collect(Collectors.toList());
		List<TaskEntity> inProgressTasks = tasks.stream()
		        .filter(task -> task.getTaskStatus() != null
		                && Arrays.asList((short) 2, (short) 3, (short) 4)
		                          .contains(task.getTaskStatus()))
		        .collect(Collectors.toList());


		List<TaskEntity> doneTasks = tasks.stream()
				.filter(task -> task.getTaskStatus() != null && task.getTaskStatus() == DONE)
				.collect(Collectors.toList());

		return TaskDashboardResponse.builder()

				.myTasksToday(tasksToday.size())

				.dueThisWeek(dueThisWeek.size())

				.overdue(overdueTasks.size())

				.todo(TaskGroupResponse.builder().count(todoTasks.size()).tasks(toDashboardItems(todoTasks)).build())

				.inProgress(TaskGroupResponse.builder().count(inProgressTasks.size())
						.tasks(toDashboardItems(inProgressTasks)).build())

				.done(TaskGroupResponse.builder().count(doneTasks.size()).tasks(toDashboardItems(doneTasks)).build())

				.build();
	}

	private LocalDateTime getDueDateTime(TaskEntity task) {

		if (task == null || task.getDate() == null || task.getTaskCategoryId() == null) {

			return null;
		}

		String dueTime = taskRepository.findDueTimeByTaskCategoryId(task.getTaskCategoryId());

		if (dueTime == null || dueTime.trim().isEmpty()) {

			return null;
		}

		try {

			long dueHours = Long.parseLong(dueTime.trim());

			return task.getDate().plusHours(dueHours);

		} catch (NumberFormatException e) {

			return null;
		}
	}

	private boolean isDone(TaskEntity task) {

		return task != null && task.getTaskStatus() != null && task.getTaskStatus() == DONE;
	}

	private List<TaskDashboardItem> toDashboardItems(List<TaskEntity> tasks) {

		return tasks.stream().map(this::toDashboardItem).collect(Collectors.toList());
	}

	private TaskDashboardItem toDashboardItem(TaskEntity task) {

		return TaskDashboardItem.builder().taskId(task.getTaskId()).clientId(task.getClientId())
				.clientName(task.getClient() != null ? task.getClient().getName() : null)
				.taskCategoryId(task.getTaskCategoryId())
				.taskCategoryName(task.getTaskCategory() != null ? task.getTaskCategory().getName() : null)
				.dueDateTime(task.getTaskCategory() != null
						? calculateDueDateTime(task.getDate(), task.getTaskCategory().getDuedatetime())
						: null)
				.assignedUser(task.getAssignedUser() != null ? task.getAssignedUser().getFirstName() : null)
				.assignedByUser(task.getAssignedByUser() != null ? task.getAssignedByUser().getFirstName() : null)
				.title(task.getTitle()).date(task.getDate()).priority(String.valueOf(task.getPriority()))
				.assignedTo(task.getAssignedTo()).addedBy(task.getAddedBy()).build();

	}

	private String getPriorityName(Short priority) {

		if (priority == null) {
			return "NORMAL";
		}

		switch (priority) {

		case 1:
			return "HIGH";

		case 2:
			return "MEDIUM";

		case 3:
			return "LOW";

		default:
			return "NORMAL";
		}
	}

	private String getStatusName(Short status) {

		if (status == null) {
			return "UNKNOWN";
		}

		switch (status) {

		case TODO:
			return "TODO";

		case IN_PROGRESS:
			return "IN_PROGRESS";

		case DONE:
			return "DONE";

		default:
			return "UNKNOWN";
		}
	}

	private Integer getProgress(TaskEntity task) {

		if (task.getTaskStatus() == null) {
			return 0;
		}

		switch (task.getTaskStatus()) {

		case TODO:
			return 0;

		case IN_PROGRESS:
			return 50;

		case DONE:
			return 100;

		default:
			return 0;
		}
	}

	private static String calculateDueDateTime(LocalDateTime date, String dueDateTimeHours) {

		if (date == null || dueDateTimeHours == null || dueDateTimeHours.isBlank()) {
			return null;
		}

		try {
			long hours = Long.parseLong(dueDateTimeHours.trim());
			return date.plusHours(hours).format(DATE_TIME_FORMATTER);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Page<TaskEditDTO> getTasksByStatus(int page, int size, Integer clientId, Integer userId, String permission) {

		Pageable pageable = PageRequest.of(page, size);

		Page<TaskEditDTO> taskList = taskRepository.findTasksByStatus(pageable, clientId, userId);

		return taskList;

	}

	// NEW
	public int countOfActiveTask(Integer clientId) {
		return taskRepository.countOfActiveTask(clientId);
	}

	public int countOfCompletedTask(Integer clientId) {
		return taskRepository.countOfCompletedTask(clientId);
	}

	public int countOfPendingTask(Integer clientId) {
		return taskRepository.countOfPendingTask(clientId);
	}

	public int countOfAssignedTask(Integer clientId) {
		return taskRepository.countOfAssignedTask(clientId);
	}

	public int countOfAssigneeClosureTask(Integer clientId) {
		return taskRepository.countOfAssigneeClosureTask(clientId);
	}

	public int countOfReOpenTask(Integer clientId) {
		return taskRepository.countOfReOpenTask(clientId);
	}

	public int countOfAssigneeReClosureTask(Integer clientId) {
		return taskRepository.countOfAssigneeReClosureTask(clientId);
	}

}