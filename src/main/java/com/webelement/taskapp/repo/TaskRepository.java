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

	@Query("SELECT DISTINCT c.clientId, c.name " + "FROM ClientEntity c " + "WHERE c.status = 1 " + "AND EXISTS ("
			+ "    SELECT 1 " + "    FROM TaskEntity t " + "    WHERE t.clientId = c.clientId "
			+ "    AND t.status = 1 " + "    AND (" + "        :isAdmin = 'Y' " + "        OR " + "        ("
			+ "            :loginType <> 'manager' " + "            AND (" + "                t.assignedTo = :userId "
			+ "                OR t.addedBy = :userId " + "                OR ("
			+ "                    :isAdmin <> 'Y' " + "                    AND EXISTS ("
			+ "                        SELECT 1 " + "                        FROM TaskCategoryEntity tc2 "
			+ "                        WHERE tc2.taskcategoryId = t.taskCategoryId "
			+ "                        AND tc2.departmentId IN ("
			+ "                            SELECT ul.departmentId "
			+ "                            FROM UserLoginEntity ul "
			+ "                            WHERE ul.userId = :userId" + "                        )"
			+ "                    )" + "                )" + "            )" + "        )" + "    )" + ")")
	List<Object[]> findDashboardClients(@Param("userId") Integer userId, @Param("isAdmin") String isAdmin,
			@Param("loginType") String loginType);

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
			+ "tc.duedatetime, " + "tc.name, " + "COALESCE(u.firstName, '0'), " + "t.priority, " + "t.status, "
			+ "t.title, " + "t.taskStatus, " + "t.assignedTo, " + "t.addedBy" + ") "

			+ "FROM TaskEntity t "

			+ "LEFT JOIN ClientEntity c " + "ON c.clientId = t.clientId "

			+ "LEFT JOIN TaskCategoryEntity tc " + "ON tc.taskcategoryId = t.taskCategoryId "

			+ "LEFT JOIN UserLoginEntity u " + "ON u.userId = t.assignedTo "

			+ "WHERE t.taskId > 0 "

			+ "AND (:statusIndex = 0 OR t.status = :statusIndex) "

			+ "AND (" + "    :search IS NULL " + "    OR :search = '' "
			+ "    OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "    OR LOWER(tc.name) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))" + ") "

			+ "AND (:clientId = 0 OR t.clientId = :clientId) "

			+ "AND (:taskCategoryId = 0 OR t.taskCategoryId = :taskCategoryId) "

			+ "AND (:assignedTo = -1 OR t.assignedTo = :assignedTo) "

			+ "AND (:priority = 0 OR t.priority = :priority) "

			+ "AND (:taskStatusId = 0 OR t.taskStatus = :taskStatusId) "

			+ "AND (:fromDate IS NULL OR :fromDate = '' OR t.date >= :fromDate) "

			+ "AND (:toDate IS NULL OR :toDate = '' OR t.date <= :toDate) "

			+ "AND ("

			// MANAGER LOGIN
			+ "    (" + "        :loginType = 'manager' " + "        AND (" + "            t.assignedTo = :userId "
			+ "            OR t.addedBy = :userId " + "            OR c.managerId = :userId" + "        )" + "    ) "

			+ "    OR "

			// NON-MANAGER LOGIN
			+ "    (" + "        :loginType <> 'manager' " + "        AND (" + "            :isAdmin = 'Y' "
			+ "            OR t.assignedTo = :userId " + "            OR t.addedBy = :userId " + "            OR ("
			+ "                :isAdmin <> 'Y' " + "                AND EXISTS (" + "                    SELECT 1 "
			+ "                    FROM TaskCategoryEntity tc2 "
			+ "                    WHERE tc2.taskcategoryId = t.taskCategoryId "
			+ "                    AND tc2.departmentId IN (" + "                        SELECT ul.departmentId "
			+ "                        FROM UserLoginEntity ul " + "                        WHERE ul.userId = :userId"
			+ "                    )" + "                )" + "            )" + "        )" + "    )"

			+ ") "

			+ "ORDER BY t.status ASC, c.name ASC")
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

	@Query("SELECT new com.webelement.taskapp.dto.TaskEditDTO("
			+ "t.taskId, t.assignedTo, COALESCE(NULLIF(u.firstName,''),'-') ," + "COALESCE(NULLIF(t.title,''),'-'), "
			+ "t.taskStatus, t.addedBy, COALESCE(NULLIF(a.firstName,''),'-'), t.date as startDate ,tc.duedatetime, t.priority,COALESCE(NULLIF(c.name, ''), '-')) "
			+ "FROM TaskEntity t LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo "
			+ "LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId "
			+ "LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
			+ "LEFT JOIN UserLoginEntity a ON a.userId = t.addedBy " + "WHERE t.status = 1"
			+ "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND (" + ":permission = 'Y' "
			+ "OR t.assignedTo = :userId " + "OR t.addedBy = :userId " + "OR c.managerId = :userId" + ") "
			+ "ORDER BY t.status ASC")
	Page<TaskEditDTO> findTasksByStatus(Pageable pageable, @Param("clientId") Integer clientId,
			@Param("userId") Integer userId, @Param("permission") String permission);

	List<TaskEntity> findByAssignedTo(Integer assignedTo);

	@Query("SELECT tc.duedatetime FROM TaskCategoryEntity tc WHERE tc.taskcategoryId = :taskCategoryId")
	String findDueTimeByTaskCategoryId(@Param("taskCategoryId") Integer taskCategoryId);

	@EntityGraph(attributePaths = { "client", "taskCategory", "assignedUser" })
	@Query("SELECT t " + "FROM TaskEntity t "
			+ "WHERE t.status = 1 AND (:selectedClientId = 0 OR t.clientId = :selectedClientId) " + "AND ("

			// ADMIN LOGIN
			+ "    :isAdmin = 'Y' "

			+ "    OR "

			// NON-MANAGER LOGIN
			+ "    (" + "        :loginType <> 'manager' " + "        AND " + "("
			+ "            t.assignedTo = :userId " + "            OR t.addedBy = :userId " + "            OR ("
			+ "                :isAdmin <> 'Y' " + "                AND EXISTS (" + "                    SELECT 1 "
			+ "                    FROM TaskCategoryEntity tc2 "
			+ "                    WHERE tc2.taskcategoryId = t.taskCategoryId "
			+ "                    AND tc2.departmentId IN (" + "                        SELECT ul.departmentId "
			+ "                        FROM UserLoginEntity ul " + "                        WHERE ul.userId = :userId"
			+ "                    )" + "                )" + "            )" + "        )" + "    )" + ")")
	List<TaskEntity> findDashboardTasks(@Param("userId") Integer userId, @Param("isAdmin") String isAdmin,
			@Param("loginType") String loginType, @Param("selectedClientId") Integer selectedClientId);

	// NEW
	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.status = 1")
	int countOfActiveTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 5 AND t.status = 1")
	int countOfCompletedTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus IN (1, 2, 3, 4) AND t.status = 1")
	int countOfPendingTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 1 AND t.status = 1")
	int countOfAssignedTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 2 AND t.status = 1")
	int countOfAssigneeClosureTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 3 AND t.status = 1")
	int countOfReOpenTask();

	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 4 AND t.status = 1")
	int countOfAssigneeReClosureTask();

}
