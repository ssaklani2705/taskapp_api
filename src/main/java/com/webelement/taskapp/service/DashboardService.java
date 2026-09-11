package com.webelement.taskapp.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

	
	public List<ClientDashboardDTO> findDashboardClients(
            Integer userId,
            String isAdmin,
            String loginType) {

        List<Object[]> result = taskRepository.findDashboardClients(
                userId,
                isAdmin,
                loginType
        );

        return result.stream()
                .map(row -> new ClientDashboardDTO(
                        (Integer) row[0],
                        (String) row[1]
                ))
                .collect(Collectors.toList());
    }
	
	// Change these values according to your database

	/*
	 * ========================================================= TASK STATUS
	 * =========================================================
	 */

	private static final short TODO = 1;
	private static final short IN_PROGRESS = 2;
	private static final short DONE = 5;

	/*
	 * ========================================================= EMPLOYEE DASHBOARD
	 * =========================================================
	 */
	
	private String getAssignedUserName(TaskEntity task) {

	    if (task.getAssignedTo() == null || task.getAssignedTo() == 0) {
	        return "Unassigned";
	    }

	    if (task.getAssignedUser() == null) {
	        return "Unassigned";
	    }

	    return task.getAssignedUser().getFirstName();
	}
	
	public TaskDashboardResponse getDashboard(Integer userId,String isAdmin,Integer selectedClientId) {

	    /*
	     * =====================================================
	     * CURRENT DATE
	     * =====================================================
	     */
	    LocalDate today = LocalDate.now();

	    /*
	     * =====================================================
	     * CURRENT WEEK
	     * =====================================================
	     */
	    LocalDateTime startOfWeek =
	            today.with(DayOfWeek.MONDAY).atStartOfDay();

	    LocalDateTime endOfWeek =
	            today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

	    /*
	     * =====================================================
	     * TODAY RANGE
	     * =====================================================
	     */
	    LocalDateTime startOfToday =
	            today.atStartOfDay();

	    LocalDateTime startOfTomorrow =
	            today.plusDays(1).atStartOfDay();

	    /*
	     * =====================================================
	     * CURRENT DATE/TIME
	     * =====================================================
	     */
	    LocalDateTime now = LocalDateTime.now();

	    /*
	     * =====================================================
	     * GET EMPLOYEE TASKS
	     *
	     * Employee sees:
	     * 1. Tasks assigned to employee
	     * 2. Tasks added by employee
	     * =====================================================
	     */
	    List<TaskEntity> tasks =
	            taskRepository.findDashboardTasks(userId,isAdmin,"other",selectedClientId);

	    System.err.println("Dashboard User ID : " + userId);
	    System.err.println("Dashboard Task Count : "
	            + (tasks == null ? 0 : tasks.size()));

	    /*
	     * =====================================================
	     * NO TASKS
	     * =====================================================
	     */
	    if (tasks == null || tasks.isEmpty()) {

	        return TaskDashboardResponse.builder()
	                .myTasksToday(0)
	                .dueThisWeek(0)
	                .overdue(0)

	                .todo(
	                    TaskGroupResponse.builder()
	                        .count(0)
	                        .tasks(Collections.emptyList())
	                        .build()
	                )

	                .inProgress(
	                    TaskGroupResponse.builder()
	                        .count(0)
	                        .tasks(Collections.emptyList())
	                        .build()
	                )

	                .done(
	                    TaskGroupResponse.builder()
	                        .count(0)
	                        .tasks(Collections.emptyList())
	                        .build()
	                )

	                .build();
	    }

	    /*
	     * =====================================================
	     * MY TASKS TODAY
	     * =====================================================
	     */
	    List<TaskEntity> tasksToday = tasks.stream()
	            .filter(task -> {

	                LocalDateTime taskDate = task.getDate();

	                if (taskDate == null) {
	                    return false;
	                }

	                return !taskDate.isBefore(startOfToday)
	                        && taskDate.isBefore(startOfTomorrow);
	            })
	            .collect(Collectors.toList());

	    /*
	     * =====================================================
	     * DUE THIS WEEK
	     * =====================================================
	     */
	    List<TaskEntity> dueThisWeek = tasks.stream()
	            .filter(task -> {

	                LocalDateTime taskStartDate = task.getDate();

	                if (taskStartDate == null) {
	                    return false;
	                }

	                return task.getTaskStatus() != null
	                        && task.getTaskStatus() != 5
	                        && !taskStartDate.isBefore(startOfWeek)
	                        && !taskStartDate.isAfter(endOfWeek);
	            })
	            .collect(Collectors.toList());

	    /*
	     * =====================================================
	     * OVERDUE
	     * =====================================================
	     */
	    List<TaskEntity> overdueTasks = tasks.stream()
	            .filter(task -> {

	                LocalDateTime dueDateTime =
	                        getDueDateTime(task);

	                return dueDateTime != null
	                        && dueDateTime.isBefore(now)
	                        && !isDone(task);
	            })
	            .collect(Collectors.toList());

	    /*
	     * =====================================================
	     * TODO
	     * =====================================================
	     */
	    List<TaskEntity> todoTasks = tasks.stream()
	            .filter(task ->
	                    task.getTaskStatus() != null
	                    && task.getTaskStatus() == TODO
	            )
	            .collect(Collectors.toList());

	    /*
	     * =====================================================
	     * IN PROGRESS
	     * =====================================================
	     */
	    List<TaskEntity> inProgressTasks = tasks.stream()
	            .filter(task ->
	                    task.getTaskStatus() != null
	                    && task.getTaskStatus() == IN_PROGRESS
	            )
	            .collect(Collectors.toList());

	    /*
	     * =====================================================
	     * DONE
	     * =====================================================
	     */
	    List<TaskEntity> doneTasks = tasks.stream()
	            .filter(task ->
	                    task.getTaskStatus() != null
	                    && task.getTaskStatus() == DONE
	            )
	            .collect(Collectors.toList());

	    /*
	     * =====================================================
	     * BUILD DASHBOARD RESPONSE
	     * =====================================================
	     */
	    return TaskDashboardResponse.builder()

	            .myTasksToday(tasksToday.size())

	            .dueThisWeek(dueThisWeek.size())

	            .overdue(overdueTasks.size())

	            .todo(
	                TaskGroupResponse.builder()
	                    .count(todoTasks.size())
	                    .tasks(toDashboardItems(todoTasks))
	                    .build()
	            )

	            .inProgress(
	                TaskGroupResponse.builder()
	                    .count(inProgressTasks.size())
	                    .tasks(toDashboardItems(inProgressTasks))
	                    .build()
	            )

	            .done(
	                TaskGroupResponse.builder()
	                    .count(doneTasks.size())
	                    .tasks(toDashboardItems(doneTasks))
	                    .build()
	            )

	            .build();
	}
	

	/*
	 * ========================================================= CALCULATE DUE
	 * DATE/TIME =========================================================
	 *
	 * d_date = LocalDateTime task start date
	 *
	 * ts_duetime = number of hours from task category
	 *
	 * Example:
	 *
	 * d_date = 2026-09-01 10:30:00
	 *
	 * ts_duetime = 50
	 *
	 * Result:
	 *
	 * 2026-09-03 12:30:00
	 */
	private LocalDateTime getDueDateTime(TaskEntity task) {

		/*
		 * Validate task
		 */
		if (task == null || task.getDate() == null || task.getTaskCategoryId() == null) {

			return null;
		}

		/*
		 * ===================================================== GET DUE HOURS FROM TASK
		 * CATEGORY =====================================================
		 */
		String dueTime = taskRepository.findDueTimeByTaskCategoryId(task.getTaskCategoryId());

		/*
		 * No due time configured
		 */
		if (dueTime == null || dueTime.trim().isEmpty()) {

			return null;
		}

		try {

			/*
			 * Convert:
			 *
			 * "50"
			 *
			 * to:
			 *
			 * 50
			 */
			long dueHours = Long.parseLong(dueTime.trim());

			/*
			 * IMPORTANT:
			 *
			 * task.getDate() is already LocalDateTime.
			 *
			 * DO NOT use:
			 *
			 * task.getDate().atStartOfDay()
			 *
			 * because atStartOfDay() belongs to LocalDate.
			 *
			 * Simply add the due hours directly.
			 */
			return task.getDate().plusHours(dueHours);

		} catch (NumberFormatException e) {

			/*
			 * Invalid due time
			 */
			return null;
		}
	}

	/*
	 * ========================================================= CHECK DONE
	 * =========================================================
	 */
	private boolean isDone(TaskEntity task) {

		return task != null && task.getTaskStatus() != null && task.getTaskStatus() == DONE;
	}

	/*
	 * ========================================================= CONVERT TASK LIST
	 * =========================================================
	 */
	private List<TaskDashboardItem> toDashboardItems(List<TaskEntity> tasks) {

		return tasks.stream().map(this::toDashboardItem).collect(Collectors.toList());
	}

	/*
	 * ========================================================= CONVERT TASK ENTITY
	 * TO DASHBOARD DTO =========================================================
	 */
	private TaskDashboardItem toDashboardItem(TaskEntity task) {

		return TaskDashboardItem.builder().taskId(task.getTaskId()).clientId(task.getClientId())
				.clientName(task.getClient() != null ? task.getClient().getName() : null)
				.taskCategoryId(task.getTaskCategoryId())
				.taskCategoryName(task.getTaskCategory() != null ? task.getTaskCategory().getName() : null)
				.dueDateTime(task.getTaskCategory() != null
						? calculateDueDateTime(task.getDate(), task.getTaskCategory().getDuedatetime())
						: null)
				 .assignedUser(task.getAssignedUser() != null
                 ? task.getAssignedUser().getFirstName()
                 : null)
				 .assignedByUser(task.getAssignedByUser() != null
                 ? task.getAssignedByUser().getFirstName()
                 : null)
				.title(task.getTitle()).date(task.getDate()).priority(String.valueOf(task.getPriority()))
				.assignedTo(task.getAssignedTo()).addedBy(task.getAddedBy()).build();
				
	}

	/*
	 * ========================================================= PRIORITY
	 * =========================================================
	 */
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

	/*
	 * ========================================================= STATUS
	 * =========================================================
	 */
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

	/*
	 * ========================================================= PROGRESS
	 * =========================================================
	 */
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

	/*
	 * ========================================================= EXISTING METHODS
	 * =========================================================
	 */

	

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

        Page<TaskEditDTO> taskList = taskRepository.findTasksByStatus(pageable, clientId, userId, permission);

        return taskList;

    }
  
    //NEW
    public int countOfActiveTask() {
        return taskRepository.countOfActiveTask();
    }

    public int countOfCompletedTask() {
        return taskRepository.countOfCompletedTask();
    }

    public int countOfPendingTask() {
        return taskRepository.countOfPendingTask();
    }

    public int countOfAssignedTask() {
        return taskRepository.countOfAssignedTask();
    }

    public int countOfAssigneeClosureTask() {
        return taskRepository.countOfAssigneeClosureTask();
    }

    public int countOfReOpenTask() {
        return taskRepository.countOfReOpenTask();
    }

    public int countOfAssigneeReClosureTask() {
        return taskRepository.countOfAssigneeReClosureTask();
    }

}