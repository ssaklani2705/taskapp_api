package com.webelement.taskapp.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.dto.TaskDashboardItem;
import com.webelement.taskapp.dto.TaskDashboardResponse;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.dto.TaskGroupResponse;
import com.webelement.taskapp.entity.TaskEntity;
import com.webelement.taskapp.repo.TaskRepository;

@Service
public class DashboardService {

	@Autowired
	private TaskRepository taskRepository;
	
   
	 // Change these values according to your database


    /*
     * Task Status
     */

    private static final short TODO = 1;
    private static final short IN_PROGRESS = 2;
    private static final short DONE = 5;


    /*
     * =========================================================
     * EMPLOYEE DASHBOARD
     * =========================================================
     */
    public TaskDashboardResponse getDashboard(Integer userId) {

        LocalDate today = LocalDate.now();

        /*
         * Current week:
         *
         * Monday -> Sunday
         */
        LocalDate startOfWeek =
                today.with(DayOfWeek.MONDAY);

        LocalDate endOfWeek =
                today.with(DayOfWeek.SUNDAY);


        /*
         * =====================================================
         * GET EMPLOYEE TASKS
         * =====================================================
         *
         * Employee should see:
         *
         * 1. Tasks assigned to employee
         * OR
         * 2. Tasks added by employee
         */
//        List<TaskEntity> tasks =
//                taskRepository.findDashboardTasks(userId);
        
        List<TaskEntity> tasks =
                taskRepository.findByAssignedTo(userId);


        /*
         * =====================================================
         * MY TASKS TODAY
         * =====================================================
         *
         * Based ONLY on task start date (d_date).
         *
         * Example:
         *
         * d_date = 2026-09-04
         *
         * Then it is "My tasks today".
         */
        List<TaskEntity> tasksToday =
                tasks.stream()
                        .filter(task ->
                                task.getDate() != null
                                        && task.getDate().equals(today)
                        )
                        .collect(Collectors.toList());


        /*
         * =====================================================
         * DUE THIS WEEK
         * =====================================================
         *
         * IMPORTANT:
         *
         * Due This Week is based on TASK START DATE.
         *
         * d_date = 2026-09-01
         *
         * If 2026-09-01 is inside the current week,
         * it is counted in Due This Week.
         *
         * We DO NOT use ts_duetime here.
         *
         * This allows the same task to be:
         *
         * Due This Week = 1
         * AND
         * Overdue = 1
         */
        List<TaskEntity> dueThisWeek =
                tasks.stream()
                        .filter(task -> {

                            LocalDate taskStartDate =
                                    task.getDate();

                            if (taskStartDate == null) {
                                return false;
                            }

                            return !taskStartDate
                                    .isBefore(startOfWeek)
                                    && !taskStartDate
                                    .isAfter(endOfWeek);
                        })
                        .collect(Collectors.toList());


        /*
         * =====================================================
         * OVERDUE
         * =====================================================
         *
         * Overdue is calculated using:
         *
         * Task Start Date + Due Hours
         *
         * Example:
         *
         * d_date     = 2026-09-01
         * ts_duetime = 50
         *
         * Start:
         * 2026-09-01 00:00
         *
         * + 50 hours
         *
         * Due:
         * 2026-09-03 02:00
         *
         * If current date/time is after that,
         * task is overdue.
         *
         * DONE tasks are NOT overdue.
         */
        LocalDateTime now =
                LocalDateTime.now();

        List<TaskEntity> overdueTasks =
                tasks.stream()
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
        List<TaskEntity> todoTasks =
                tasks.stream()
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
        List<TaskEntity> inProgressTasks =
                tasks.stream()
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
        List<TaskEntity> doneTasks =
                tasks.stream()
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

                /*
                 * My Tasks Today
                 */
                .myTasksToday(
                        tasksToday.size()
                )

                /*
                 * Due This Week
                 */
                .dueThisWeek(
                        dueThisWeek.size()
                )

                /*
                 * Overdue
                 */
                .overdue(
                        overdueTasks.size()
                )

                /*
                 * TODO
                 */
                .todo(
                        TaskGroupResponse.builder()
                                .count(todoTasks.size())
                                .tasks(
                                        toDashboardItems(todoTasks)
                                )
                                .build()
                )

                /*
                 * IN PROGRESS
                 */
                .inProgress(
                        TaskGroupResponse.builder()
                                .count(inProgressTasks.size())
                                .tasks(
                                        toDashboardItems(
                                                inProgressTasks
                                        )
                                )
                                .build()
                )

                /*
                 * DONE
                 */
                .done(
                        TaskGroupResponse.builder()
                                .count(doneTasks.size())
                                .tasks(
                                        toDashboardItems(
                                                doneTasks
                                        )
                                )
                                .build()
                )

                .build();
    }


    /*
     * =========================================================
     * CALCULATE DUE DATE/TIME
     * =========================================================
     *
     * d_date       = task start date
     * ts_duetime   = number of hours from task category
     *
     * Example:
     *
     * d_date = 2026-09-01
     * ts_duetime = 50
     *
     * Result:
     *
     * 2026-09-03 02:00
     */
    private LocalDateTime getDueDateTime(
            TaskEntity task) {

        if (task == null ||
                task.getDate() == null) {

            return null;
        }


        /*
         * Get ts_duetime from t_taskcategory
         */
        String dueTime =
                taskRepository.findDueTimeByTaskCategoryId(
                        task.getTaskCategoryId()
                );


        if (dueTime == null ||
                dueTime.trim().isEmpty()) {

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
            long dueHours =
                    Long.parseLong(
                            dueTime.trim()
                    );


            /*
             * Convert LocalDate to LocalDateTime
             *
             * 2026-09-01
             *
             * becomes:
             *
             * 2026-09-01 00:00
             */
            LocalDateTime startDateTime =
                    task.getDate().atStartOfDay();


            /*
             * Add due hours
             */
            return startDateTime.plusHours(
                    dueHours
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }


    /*
     * =========================================================
     * CHECK DONE
     * =========================================================
     */
    private boolean isDone(TaskEntity task) {

        return task.getTaskStatus() != null
                && task.getTaskStatus() == DONE;
    }


    /*
     * =========================================================
     * CONVERT TASK LIST
     * =========================================================
     */
    private List<TaskDashboardItem> toDashboardItems(
            List<TaskEntity> tasks) {

        return tasks.stream()
                .map(this::toDashboardItem)
                .collect(Collectors.toList());
    }


    /*
     * =========================================================
     * CONVERT TASK ENTITY TO DASHBOARD DTO
     * =========================================================
     */
    private TaskDashboardItem toDashboardItem(
            TaskEntity task) {

        return TaskDashboardItem.builder()

                .taskId(
                        task.getTaskId()
                )

                .clientId(
                        task.getClientId()
                )

                .title(
                        task.getTitle()
                )

                .date(
                        task.getDate()
                )

                .priority(
                        getPriorityName(
                                task.getPriority()
                        )
                )

                .status(
                        getStatusName(
                                task.getTaskStatus()
                        )
                )

                .progress(
                        getProgress(task)
                )

                .description(
                        task.getDescription()
                )

                .assignedTo(
                        task.getAssignedTo()
                )

                .addedBy(
                        task.getAddedBy()
                )

                .taskCategoryId(
                        task.getTaskCategoryId()
                )

                .closeRemarks(
                        task.getCloseRemarks()
                )

                .build();
    }


    /*
     * =========================================================
     * PRIORITY
     * =========================================================
     */
    private String getPriorityName(
            Short priority) {

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
     * =========================================================
     * STATUS
     * =========================================================
     */
    private String getStatusName(
            Short status) {

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
     * =========================================================
     * PROGRESS
     * =========================================================
     */
    private Integer getProgress(
            TaskEntity task) {

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
     * =========================================================
     * EXISTING METHODS
     * =========================================================
     */

    public List<TaskEditDTO> getTasksByStatus() {

        return taskRepository.findTasksByStatus();
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