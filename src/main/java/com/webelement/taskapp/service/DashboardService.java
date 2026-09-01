package com.webelement.taskapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.TaskDashboardItem;
import com.webelement.taskapp.dto.TaskDashboardResponse;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.TaskGroupResponse;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.repo.TaskRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class DashboardService {
	
	@Autowired
	private TaskRepository taskRepository;
	
   
	 // Change these values according to your database
    private static final short TODO = 1;
    private static final short IN_PROGRESS = 2;
    private static final short DONE = 5;

    public TaskDashboardResponse getDashboard(Integer userId) {

        LocalDate today = LocalDate.now();

        // Start of current week - Monday
        LocalDate startOfWeek =
                today.with(DayOfWeek.MONDAY);

        // End of current week - Sunday
        LocalDate endOfWeek =
                today.with(DayOfWeek.SUNDAY);

        List<TaskEntity> tasks =
                taskRepository.findByAssignedTo(userId);

        // ----------------------------------------
        // My Tasks Today
        // ----------------------------------------

        List<TaskEntity> tasksToday = tasks.stream()
                .filter(task ->
                        task.getDate() != null &&
                        task.getDate().equals(today)
                )
                .collect(Collectors.toList());

        // ----------------------------------------
        // Due This Week
        // ----------------------------------------

        List<TaskEntity> dueThisWeek = tasks.stream()
                .filter(task ->
                        task.getDate() != null &&
                        !task.getDate().isBefore(startOfWeek) &&
                        !task.getDate().isAfter(endOfWeek)
                )
                .collect(Collectors.toList());

        // ----------------------------------------
        // Overdue
        // ----------------------------------------

        List<TaskEntity> overdueTasks = tasks.stream()
                .filter(task ->
                        task.getDate() != null &&
                        task.getDate().isBefore(today) &&
                        !isDone(task)
                )
                .collect(Collectors.toList());

        // ----------------------------------------
        // TODO
        // ----------------------------------------

        List<TaskEntity> todoTasks = tasks.stream()
                .filter(task ->
                        task.getTaskStatus() != null &&
                        task.getTaskStatus() == TODO
                )
                .collect(Collectors.toList());

        // ----------------------------------------
        // IN PROGRESS
        // ----------------------------------------

        List<TaskEntity> inProgressTasks = tasks.stream()
                .filter(task ->
                        task.getTaskStatus() != null &&
                        task.getTaskStatus() == IN_PROGRESS
                )
                .collect(Collectors.toList());

        // ----------------------------------------
        // DONE
        // ----------------------------------------

        List<TaskEntity> doneTasks = tasks.stream()
                .filter(task ->
                        task.getTaskStatus() != null &&
                        task.getTaskStatus() == DONE
                )
                .collect(Collectors.toList());

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


    private boolean isDone(TaskEntity task) {

        return task.getTaskStatus() != null &&
                task.getTaskStatus() == DONE;
    }


    private List<TaskDashboardItem> toDashboardItems(
            List<TaskEntity> tasks) {

        return tasks.stream()
                .map(this::toDashboardItem)
                .collect(Collectors.toList());
    }


    private TaskDashboardItem toDashboardItem(TaskEntity task) {

        return TaskDashboardItem.builder()

                .taskId(task.getTaskId())

                .clientId(task.getClientId())

                .title(task.getTitle())

                .date(task.getDate())

                .priority(getPriorityName(task.getPriority()))

                .status(getStatusName(task.getTaskStatus()))

                .progress(getProgress(task))

                .description(task.getDescription())

                .assignedTo(task.getAssignedTo())

                .addedBy(task.getAddedBy())

                .taskCategoryId(task.getTaskCategoryId())

//                .fileName1(task.getFileName1())
//
//                .fileName2(task.getFileName2())
//
//                .fileName3(task.getFileName3())
//
//                .fileName4(task.getFileName4())

                .closeRemarks(task.getCloseRemarks())

                .build();
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

	
	public List<TaskEditDTO> getTasksByStatus() {
        List<TaskEditDTO> taskList = taskRepository.findTasksByStatus();

        return taskList;
    }
    
    public int countOfActiveTask() {    
        return taskRepository.countOfActiveTask();
    }
    
    public int countOfCompletedTask() {    
        return taskRepository.countOfCompletedTask();
    }
    
    public int countOfPendingTask() {    
        return taskRepository.countOfPendingTask();
    }

}
