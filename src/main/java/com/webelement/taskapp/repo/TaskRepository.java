package com.webelement.taskapp.repo;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {
	Optional<TaskEntity> findByTaskId(Integer taskId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskEditDTO(" + "t.taskId, " + "t.addedBy, " + "t.assignedTo, "
			+ "t.clientId, " + "t.closeRemarks, " + "t.date, " + "t.description, " + "t.fileName1, " + "t.fileName2, "
			+ "t.fileName3, " + "t.fileName4, " + "t.priority, " + "t.status, " + "t.taskCategoryId, " + "t.title, "
			+ "c.name, " + "tc.name, " + "u.firstName, " + // assignedTo user
			"a.firstName, t.taskStatus) " + // addedBy user
			"FROM TaskEntity t " + "LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
			+ "LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId "
			+ "LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo "
			+ "LEFT JOIN UserLoginEntity a ON a.userId = t.addedBy " + "WHERE t.taskId = :taskId")
	Optional<TaskEditDTO> findTaskById(@Param("taskId") Integer taskId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskDetailsDTO(" + "t.taskId, " + "c.name, " + "t.date, "
			+ "tc.duedatetime, " + // <--
			// replace
			// CASE
			// block
			"tc.name, " + "u.firstName, " + "t.priority, " + "t.status, " + "t.title, " + "t.taskStatus, "
			+ "t.assignedTo, " + "t.addedBy" + ") " + "FROM TaskEntity t "
			+ "LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
			+ "LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId "
			+ "LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo " + "WHERE t.taskId > 0")
	Page<TaskDetailsDTO> findTaskDetails(PageRequest pageable, @Param("statusIndex") int statusIndex,
			@Param("search") String search, @Param("clientId") Integer clientId,
			@Param("taskCategoryId") Integer taskCategoryId, @Param("assignedTo") Integer assignedTo,
			@Param("priority") Integer priority, @Param("fromDate") String fromDate, @Param("toDate") String toDate,
			@Param("isAdmin") String isAdmin, @Param("userId") Integer userId,
			@Param("taskStatusId") Integer taskStatusId, @Param("loginType") String loginType);

	@Modifying
	@Transactional
	@Query("UPDATE TaskEntity t SET t.status = :status WHERE t.taskId = :taskId")
	int deleteTask(@Param("status") Short status, @Param("taskId") int taskId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskEditDTO(" + "t.taskId, t.assignedTo, u.firstName ," + "t.title, "
			+ "t.taskStatus,t.addedBy) " + "FROM TaskEntity t LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo "
			+ "WHERE t.status = 1")
	Page<TaskEditDTO> findTasksByStatus(Pageable pageable);

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.status = 1")
	int countOfActiveTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 5")
	int countOfCompletedTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 1")
	int countOfPendingTask();

	List<TaskEntity> findByAssignedTo(Integer assignedTo);

	@Query("SELECT tc.duedatetime FROM TaskCategoryEntity tc WHERE tc.taskcategoryId = :taskCategoryId")
	String findDueTimeByTaskCategoryId(@Param("taskCategoryId") Integer taskCategoryId);

	@EntityGraph(attributePaths = { "client", "taskCategory", "assignedUser" })
	@Query("SELECT t " + "FROM TaskEntity t " + "WHERE t.status = 1 "
			+ "AND (t.assignedTo = :userId OR t.addedBy = :userId)")
	List<TaskEntity> findDashboardTasks(@Param("userId") Integer userId);

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 1 AND t.status = 1")
	int countOfAssignedTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 2 AND t.status = 1")
	int countOfAssigneeClosureTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 3 AND t.status = 1")
	int countOfReOpenTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 4 AND t.status = 1")
	int countOfAssigneeReClosureTask();

}
