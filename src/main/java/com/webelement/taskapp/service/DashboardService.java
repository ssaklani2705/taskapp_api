package com.webelement.taskapp.service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.ClientDashboardDTO;
import com.webelement.taskapp.dto.DashboardMetricDTO;
import com.webelement.taskapp.dto.StatusCountsResponse;
import com.webelement.taskapp.dto.TaskDashboardItem;
import com.webelement.taskapp.dto.TaskDashboardResponse;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.TaskGroupResponse;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.repo.ClientRepository;
import com.webelement.taskapp.repo.TaskRepository;

@Service
public class DashboardService {
	
	private static final short TODO = 1;
	private static final short IN_PROGRESS = 2;
	private static final short DONE = 5;
	
	
	private static final short ST_UNASSIGNED         = 0;  // also null
	private static final short ST_ASSIGNED           = 1;  // TODO: confirm
	private static final short ST_ASSIGNEE_CLOSURE   = 2;  // TODO: confirm
	private static final short ST_REOPEN             = 3;  // TODO: confirm
	private static final short ST_ASSIGNEE_RECLOSURE = 4;  // TODO: confirm
	private static final short ST_ASSIGNOR_CLOSURE   = 5;  // TODO: confirm
	
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private ClientRepository clientRepository;

	public List<ClientDashboardDTO> findDashboardClients(Integer userId, String isAdmin, String loginType) {

		List<Object[]> result = taskRepository.findDashboardClients(userId, isAdmin, loginType);

		return result.stream().map(row -> new ClientDashboardDTO((Integer) row[0], (String) row[1]))
				.collect(Collectors.toList());
	}



	private String getAssignedUserName(TaskEntity task) {
		if (task.getAssignedTo() == null || task.getAssignedTo() == 0) {
			return "Unassigned";
		}

		if (task.getAssignedUser() == null) {
			return "Unassigned";
		}

		return task.getAssignedUser().getFirstName();
	}

	public TaskDashboardResponse getDashboard(Integer userId, String isAdmin, Integer selectedClientId, String isHod) {

		LocalDate today = LocalDate.now();

		LocalDateTime startOfWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
		LocalDateTime endOfWeek = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

		LocalDateTime startOfToday = today.atStartOfDay();
		LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

		LocalDateTime now = LocalDateTime.now();

		List<TaskEntity> tasks = taskRepository.findDashboardTasks(userId, isAdmin, "other", selectedClientId, isHod);

		if (tasks == null || tasks.isEmpty()) {

			return TaskDashboardResponse.builder()

					.myTasksToday(DashboardMetricDTO.builder().count(0).taskStatusIds(Collections.emptyList()).build())

					.dueThisWeek(DashboardMetricDTO.builder().count(0).taskStatusIds(Collections.emptyList()).build())

					.overdue(DashboardMetricDTO.builder().count(0).taskStatusIds(Collections.emptyList()).build())

					.todo(TaskGroupResponse.builder().count(0).tasks(Collections.emptyList()).build())

					.inProgress(TaskGroupResponse.builder().count(0).tasks(Collections.emptyList()).build())

					.done(TaskGroupResponse.builder().count(0).tasks(Collections.emptyList()).build())

					.statusCounts(new StatusCountsResponse()).build();
		}
		
		List<TaskEntity> tasksToday = tasks.stream().filter(task -> {
			LocalDateTime taskDate = task.getDate();

			return taskDate != null && !taskDate.isBefore(startOfToday) && taskDate.isBefore(startOfTomorrow);
		}).collect(Collectors.toList());

		List<TaskEntity> dueThisWeek = tasks.stream().filter(task -> {

			LocalDateTime taskStartDate = task.getDate();

			if (taskStartDate == null) {
				return false;
			}

			boolean notClosed = task.getTaskStatus() == null || task.getTaskStatus() != 5;

			return notClosed && !taskStartDate.isBefore(startOfWeek) && !taskStartDate.isAfter(endOfWeek);

		}).collect(Collectors.toList());

		List<TaskEntity> overdueTasks = tasks.stream().filter(task -> {

			LocalDateTime dueDateTime = getDueDateTime(task);

			return dueDateTime != null && dueDateTime.isBefore(now) && !isDone(task);

		}).collect(Collectors.toList());

		List<TaskEntity> todoTasks = tasks.stream()
				.filter(task -> task.getTaskStatus() == null || task.getTaskStatus() == TODO)
				.collect(Collectors.toList());

		List<TaskEntity> inProgressTasks = tasks.stream()
				.filter(task -> task.getTaskStatus() != null
						&& Arrays.asList((short) 2, (short) 3, (short) 4).contains(task.getTaskStatus()))
				.collect(Collectors.toList());

		List<TaskEntity> doneTasks = tasks.stream()
				.filter(task -> task.getTaskStatus() != null && task.getTaskStatus() == DONE)
				.collect(Collectors.toList());

		DashboardMetricDTO myTasksTodayMetric = DashboardMetricDTO.builder().count(tasksToday.size())
				.taskStatusIds(getStatusIds(tasksToday))
				.build();

		DashboardMetricDTO dueThisWeekMetric = DashboardMetricDTO.builder().count(dueThisWeek.size())
				.taskStatusIds(getStatusIds(dueThisWeek))
				.build();

		DashboardMetricDTO overdueMetric = DashboardMetricDTO.builder().count(overdueTasks.size())
				.taskStatusIds(getStatusIds(overdueTasks))
				.build();

		return TaskDashboardResponse.builder()

				.myTasksToday(myTasksTodayMetric)

				.dueThisWeek(dueThisWeekMetric)

				.overdue(overdueMetric)

				.todo(TaskGroupResponse.builder().count(todoTasks.size()).tasks(toDashboardItems(todoTasks)).build())

				.inProgress(TaskGroupResponse.builder().count(inProgressTasks.size())
						.tasks(toDashboardItems(inProgressTasks)).build())

				.done(TaskGroupResponse.builder().count(doneTasks.size()).tasks(toDashboardItems(doneTasks)).build())

				.statusCounts(buildStatusCounts(tasks))

				.build();
	}
	
	private StatusCountsResponse buildStatusCounts(List<TaskEntity> tasks) {
	    return StatusCountsResponse.builder()
	            .unassigned((int) countUnassigned(tasks))
	            .assigned((int) countByStatus(tasks, ST_ASSIGNED))
	            .assigneeClosure((int) countByStatus(tasks, ST_ASSIGNEE_CLOSURE))
	            .reOpen((int) countByStatus(tasks, ST_REOPEN))
	            .assigneeReClosure((int) countByStatus(tasks, ST_ASSIGNEE_RECLOSURE))
	            .assignorClosure((int) countByStatus(tasks, ST_ASSIGNOR_CLOSURE))
	            .build();
	}
	
	private long countByStatus(List<TaskEntity> tasks, short status) {
	    return tasks.stream()
	            .filter(t -> t.getTaskStatus() != null && t.getTaskStatus() == status)
	            .count();
	}

	private long countUnassigned(List<TaskEntity> tasks) {
	    return tasks.stream()
	            .filter(t -> t.getTaskStatus() == null || t.getTaskStatus() == ST_UNASSIGNED)
	            .count();
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
                .dueDateTime(task.getEndDate())
                .assignedUser(task.getAssignedUser() != null ? task.getAssignedUser().getFirstName() : null)
                .assignedByUser(task.getAssignedByUser() != null ? task.getAssignedByUser().getFirstName() : null)
                .title(task.getTitle()).taskStatus(task.getTaskStatus()).date(task.getDate()).priority(String.valueOf(task.getPriority()))
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
	
	public Double getTotalOutstanding(Integer clientId, Integer userId) {
        return clientRepository.getTotalOutstanding(clientId, userId);
    }

	// NEW
	public int countOfActiveTask(Integer clientId,Integer userId) {
		return taskRepository.countOfActiveTask(clientId,userId);
	}

	public int countOfCompletedTask(Integer clientId,Integer userId) {
		return taskRepository.countOfCompletedTask(clientId,userId);
	}

	public int countOfPendingTask(Integer clientId,Integer userId) {
		return taskRepository.countOfPendingTask(clientId,userId);
	}

	public int countOfAssignedTask(Integer clientId,Integer userId) {
		return taskRepository.countOfAssignedTask(clientId,userId);
	}

	public int countOfAssigneeClosureTask(Integer clientId,Integer userId) {
		return taskRepository.countOfAssigneeClosureTask(clientId,userId);
	}

	public int countOfReOpenTask(Integer clientId,Integer userId) {
		return taskRepository.countOfReOpenTask(clientId,userId);
	}

	public int countOfAssigneeReClosureTask(Integer clientId,Integer userId) {
		return taskRepository.countOfAssigneeReClosureTask(clientId,userId);
	}
	
	public int countOfUnAssigneeTask(Integer clientId, Integer userId) {
        return taskRepository.countOfUnassignedTask(clientId, userId);
    }

	private List<Short> getStatusIds(List<TaskEntity> tasks) {
	    return tasks.stream()
	            .map(task -> task.getTaskStatus() == null ? (short) -1 : task.getTaskStatus())
	            .distinct()
	            .sorted()
	            .collect(Collectors.toList());
	}
}