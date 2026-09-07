package com.webelement.taskapp.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /*
     * =========================================================
     * TASK STATUS
     * =========================================================
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

        /*
         * Current date
         */
        LocalDate today = LocalDate.now();


        /*
         * =====================================================
         * CURRENT WEEK
         * =====================================================
         *
         * Monday 00:00:00
         *       ->
         * Sunday 23:59:59.999999999
         *
         * IMPORTANT:
         *
         * task.getDate() is LocalDateTime,
         * therefore startOfWeek and endOfWeek
         * must also be LocalDateTime.
         */
        LocalDateTime startOfWeek =
                today.with(DayOfWeek.MONDAY)
                     .atStartOfDay();

        LocalDateTime endOfWeek =
                today.with(DayOfWeek.SUNDAY)
                     .atTime(LocalTime.MAX);


        /*
         * =====================================================
         * TODAY RANGE
         * =====================================================
         *
         * Today:
         *
         * 2026-09-07 00:00:00
         *
         * until
         *
         * 2026-09-08 00:00:00
         */
        LocalDateTime startOfToday =
                today.atStartOfDay();

        LocalDateTime startOfTomorrow =
                today.plusDays(1)
                     .atStartOfDay();


        /*
         * =====================================================
         * CURRENT DATE/TIME
         * =====================================================
         */
        LocalDateTime now =
                LocalDateTime.now();


        /*
         * =====================================================
         * GET EMPLOYEE TASKS
         * =====================================================
         *
         * Employee sees:
         *
         * 1. Tasks assigned to employee
         * OR
         * 2. Tasks added by employee
         */
        List<TaskEntity> tasks =
                taskRepository.findDashboardTasks(userId);


        /*
         * =====================================================
         * MY TASKS TODAY
         * =====================================================
         *
         * Since d_date is LocalDateTime,
         * compare the complete datetime range.
         *
         * Example:
         *
         * d_date = 2026-09-07 15:30:00
         *
         * This will be counted as today's task.
         */
        List<TaskEntity> tasksToday =
                tasks.stream()
                        .filter(task -> {

                            LocalDateTime taskDate =
                                    task.getDate();

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
         *
         * Based on task start date (d_date).
         *
         * Monday 00:00
         * ->
         * Sunday 23:59:59.999999999
         */
        List<TaskEntity> dueThisWeek =
                tasks.stream()
                        .filter(task -> {

                            LocalDateTime taskStartDate =
                                    task.getDate();

                            if (taskStartDate == null) {
                                return false;
                            }

                            return !taskStartDate.isBefore(startOfWeek)
                                    && !taskStartDate.isAfter(endOfWeek);
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
         * d_date     = 2026-09-01 10:30:00
         * ts_duetime = 50
         *
         * Due:
         *
         * 2026-09-03 12:30:00
         *
         * If current datetime is after the due datetime,
         * task is overdue.
         *
         * DONE tasks are NOT overdue.
         */
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
         * TODO TASKS
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
         * IN PROGRESS TASKS
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
         * DONE TASKS
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
     * d_date       = LocalDateTime task start date
     *
     * ts_duetime   = number of hours from task category
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
    private LocalDateTime getDueDateTime(
            TaskEntity task) {

        /*
         * Validate task
         */
        if (task == null
                || task.getDate() == null
                || task.getTaskCategoryId() == null) {

            return null;
        }


        /*
         * =====================================================
         * GET DUE HOURS FROM TASK CATEGORY
         * =====================================================
         */
        String dueTime =
                taskRepository.findDueTimeByTaskCategoryId(
                        task.getTaskCategoryId()
                );


        /*
         * No due time configured
         */
        if (dueTime == null
                || dueTime.trim().isEmpty()) {

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
            return task.getDate()
                    .plusHours(dueHours);

        } catch (NumberFormatException e) {

            /*
             * Invalid due time
             */
            return null;
        }
    }


    /*
     * =========================================================
     * CHECK DONE
     * =========================================================
     */
    private boolean isDone(TaskEntity task) {

        return task != null
                && task.getTaskStatus() != null
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