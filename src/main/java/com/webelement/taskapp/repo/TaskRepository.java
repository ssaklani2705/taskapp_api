package com.webelement.taskapp.repo;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.dto.AssigneeWorkloadDTO;
import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {

//	@Query("SELECT DISTINCT c.clientId, c.name " + "FROM ClientEntity c " + "WHERE c.status = 1 " + "AND EXISTS ("
//			+ "    SELECT 1 " + "    FROM TaskEntity t " + "    WHERE t.clientId = c.clientId "
//			+ "    AND t.status = 1 " + "    AND (" + "        :isAdmin = 'Y' " + "        OR " + "        ("
//			+ "            :loginType <> 'manager' " + "            AND (" + "                t.assignedTo = :userId "
//			+ "                OR t.addedBy = :userId " + "                OR ("
//			+ "                    :isAdmin <> 'Y' " + "                    AND EXISTS ("
//			+ "                        SELECT 1 " + "                        FROM TaskCategoryEntity tc2 "
//			+ "                        WHERE tc2.taskcategoryId = t.taskCategoryId "
//			+ "                        AND tc2.departmentId IN ("
//			+ "                            SELECT ul.departmentId "
//			+ "                            FROM UserLoginEntity ul "
//			+ "                            WHERE ul.userId = :userId" + "                        )"
//			+ "                    )" + "                )" + "            )" + "        )" + "    )" + ")")
//	List<Object[]> findDashboardClients(@Param("userId") Integer userId, @Param("isAdmin") String isAdmin,
//			@Param("loginType") String loginType);

	@Query("SELECT DISTINCT c.clientId, c.name " + "FROM ClientEntity c " + "WHERE c.status = 1 " + "AND EXISTS ("
			+ "    SELECT 1 " + "    FROM TaskEntity t " + "    WHERE t.clientId = c.clientId "
			+ "    AND t.status = 1 " + "    AND (" + "        :isAdmin = 'Y' " + "        OR " + "        ("
			+ "            :loginType <> 'manager' " + "            AND (" + "                t.assignedTo = :userId "
			+ "                OR t.addedBy = :userId " + "                OR ("
			+ "                    :isAdmin <> 'Y' " + "                    AND EXISTS ("
			+ "                        SELECT 1 " + "                        FROM TaskCategoryEntity tc2 "
			+ "                        WHERE tc2.taskcategoryId = t.taskCategoryId "
			// ===== CHANGED: was "AND tc2.departmentId IN (SELECT ul.departmentId ...)"
			// =====
			+ "                        AND EXISTS (" + "                            SELECT 1 "
			+ "                            FROM UserLoginEntity ul "
			+ "                            WHERE ul.userId = :userId "
			+ "                            AND CONCAT(',', ul.departmentIdsCsv, ',') "
			+ "                                LIKE CONCAT('%,', tc2.departmentId, ',%') " + "                        )"
			// ===== END CHANGE =====
			+ "                    )" + "                )" + "            )" + "        )" + "    )" + ")")
	List<Object[]> findDashboardClients(@Param("userId") Integer userId, @Param("isAdmin") String isAdmin,
			@Param("loginType") String loginType);

	Optional<TaskEntity> findByTaskId(Integer taskId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskEditDTO(" + "t.taskId, " + "t.addedBy, " + "t.assignedTo, "
			+ "t.clientId, " + "t.closeRemarks, " + "t.date, " + "t.description, " + "t.fileName1, " + "t.fileName2, "
			+ "t.fileName3, " + "t.fileName4, " + "t.priority, " + "t.status, " + "t.taskCategoryId, " + "t.title, "
			+ "c.name, " + "tc.name, " + "u.firstName, " // assignedTo user
			+ "a.firstName, " // addedBy user
			+ "t.taskStatus," + "t.endDate " + ") " + "FROM TaskEntity t " + "LEFT JOIN ClientEntity c "
			+ "ON c.clientId = t.clientId " + "LEFT JOIN TaskCategoryEntity tc "
			+ "ON tc.taskcategoryId = t.taskCategoryId " + "LEFT JOIN UserLoginEntity u "
			+ "ON u.userId = t.assignedTo " + "LEFT JOIN UserLoginEntity a " + "ON a.userId = t.addedBy "
			+ "WHERE t.taskId = :taskId")
	Optional<TaskEditDTO> findTaskById(@Param("taskId") Integer taskId);

//	@Query("SELECT new com.webelement.taskapp.dto.TaskDetailsDTO("
//	        + "t.taskId, c.managerId, "
//	        + "c.name, "
//	        + "t.date, "
//	        + "tc.duedatetime, "
//	        + "tc.name, "
//	        + "COALESCE(u.firstName, '0'), "
//	        + "COALESCE(t.priority, 0), "
//	        + "t.status, "
//	        + "t.title, "
//	        + "COALESCE(t.taskStatus, 0), "
//	        + "COALESCE(t.assignedTo, 0), "
//	        + "COALESCE(t.addedBy, 0), "
//	        + "u1.firstName, "
//	        + "t.description, "
//	        + "t.clientId, "
//	        + "t.taskCategoryId"
//	        + ") "
//	        + "FROM TaskEntity t "
//	        + "LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
//	        + "LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId "
//	        + "LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo "
//	        + "LEFT JOIN UserLoginEntity u1 ON u1.userId = t.addedBy "
//	        + "WHERE t.taskId > 0 "
//	        + "AND (:statusIndex = 0 OR t.status = :statusIndex) "
//	        + "AND ("
//	        + "     :search IS NULL "
//	        + "     OR :search = '' "
//	        + "     OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) "
//	        + "     OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) "
//	        + "     OR LOWER(tc.name) LIKE LOWER(CONCAT('%', :search, '%')) "
//	        + "     OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))"
//	        + ") "
//	        + "AND (:clientId = 0 OR t.clientId = :clientId) "
//	        + "AND (:taskCategoryId = 0 OR t.taskCategoryId = :taskCategoryId) "
////	        + "AND (:assignedTo = -1 OR t.assignedTo = :assignedTo) "
//			+ "AND (:assignedTo = -1 "
//			+ "     OR (:assignedTo = 0 AND (t.assignedTo = 0 OR t.assignedTo IS NULL)) "
//			+ "     OR t.assignedTo = :assignedTo) "			
//	        + "AND (:priority = 0 OR t.priority = :priority) "
//	        + "AND ( "
//	        + "     0 IN :taskStatusIds "
//	        + "     OR t.taskStatus IN :taskStatusIds "
//	        + "     OR (-1 IN :taskStatusIds AND (t.assignedTo IS NULL OR t.assignedTo = 0)) "
//	        + ") "
//			+ "AND (:fromDate IS NULL OR :fromDate = '' OR FUNCTION('DATE', t.date) >= FUNCTION('DATE', CAST(:fromDate AS date))) "
//			+ "AND (:toDate IS NULL OR :toDate = '' OR FUNCTION('DATE', t.date) <= FUNCTION('DATE', CAST(:toDate AS date))) "
//	        // Dashboard Filter
//	        + "AND ( "
//	        + "     :dashboardFilter IS NULL "
//	        + "     OR :dashboardFilter = '' "
//	        + "     OR ( "
//	        + "          :dashboardFilter = 'today' "
//	        + "          AND t.date >= :startOfToday "
//	        + "          AND t.date < :startOfTomorrow "
//	        + "        ) "
//
//	        + "     OR ( "
//	        + "          :dashboardFilter = 'week' "
//	        + "          AND (t.taskStatus IS NULL OR t.taskStatus <> 5) "
//	        + "          AND t.date >= :startOfWeek "
//	        + "          AND t.date <= :endOfWeek "
//	        + "        ) "
//	        + "     OR ( "
//	        + "          :dashboardFilter = 'overdue' "
//	        + "          AND (t.taskStatus IS NULL OR t.taskStatus <> 5) "
//	        + "			AND tc.duedatetime IS NOT NULL "
//	        + "        AND FUNCTION('TIMESTAMPADD', HOUR, "
//	        		+ "           CAST(tc.duedatetime AS integer), t.date) < :currentTime "
//	        + "        ) "
//	        + ") "
//	        + "AND ( "
//	        + "     ( "
//	        + "          :loginType = 'manager' "
//	        + "          AND ( "
//	        + "               t.assignedTo = :userId "
//	        + "               OR t.addedBy = :userId "
//	        + "               OR c.managerId = :userId "
//	        + "          ) "
//	        + "     ) "
//	        + "     OR "
//	        + "     ( "
//	        + "          :loginType <> 'manager' "
//	        + "          AND ( "
//	        + "               :isAdmin = 'Y' "
//	        + "               OR t.assignedTo = :userId "
//	        + "               OR t.addedBy = :userId "
//	        + "               OR ( "
//	        + "                    :isAdmin <> 'Y' "
//	        + "                    AND (t.assignedTo = 0 OR t.assignedTo IS NULL) "
//	        + "                    AND EXISTS ( "
//	        + "                         SELECT 1 "
//	        + "                         FROM UserLoginEntity ul "
//	        + "                         WHERE ul.userId = :userId "
//	        + "                         AND CONCAT(',', ul.taskcategoryIds, ',') "
//	        + "                             LIKE CONCAT('%,', t.taskCategoryId, ',%') "
//	        + "                    ) "
//	        + "               ) "
//	        + "          ) "
//	        + "     ) "
//	        + ") "
//	        + "ORDER BY t.status ASC, t.date DESC, t.title ASC")
//	Page<TaskDetailsDTO> findTaskDetails(PageRequest pageable, @Param("statusIndex") int statusIndex,
//			@Param("search") String search, @Param("clientId") Integer clientId,
//			@Param("taskCategoryId") Integer taskCategoryId, @Param("assignedTo") Integer assignedTo,
//			@Param("priority") Integer priority, @Param("fromDate") String fromDate, @Param("toDate") String toDate,
//			@Param("isAdmin") String isAdmin, @Param("userId") Integer userId,
//			@Param("taskStatusIds") Set<Integer> taskStatusIds, @Param("loginType") String loginType,
//			@Param("dashboardFilter") String dashboardFilter,@Param("startOfToday") LocalDateTime startOfToday,
//			@Param("startOfTomorrow") LocalDateTime startOfTomorrow,
//			@Param("startOfWeek") LocalDateTime startOfWeek,
//			@Param("endOfWeek") LocalDateTime endOfWeek,  @Param("currentTime") LocalDateTime currentTime,String isHod);

	@Query("SELECT new com.webelement.taskapp.dto.TaskDetailsDTO(" + "t.taskId, c.managerId, " + "c.name, " + "t.date, "
			+ "t.endDate, " + "tc.name, " + "COALESCE(u.firstName, '0'), " + "COALESCE(t.priority, 0), " + "t.status, "
			+ "t.title, " + "COALESCE(t.taskStatus, 0), " + "COALESCE(t.assignedTo, 0), " + "COALESCE(t.addedBy, 0), "
			+ "u1.firstName, " + "t.description, " + "t.clientId, " + "t.taskCategoryId" + ") " + "FROM TaskEntity t "
			+ "LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
			+ "LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId "
			+ "LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo "
			+ "LEFT JOIN UserLoginEntity u1 ON u1.userId = t.addedBy " + "WHERE t.taskId > 0 "
			+ "AND (:statusIndex = 0 OR t.status = :statusIndex) " + "AND (" + "     :search IS NULL "
			+ "     OR :search = '' " + "     OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "     OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "     OR LOWER(tc.name) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "     OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))" + ") "
			+ "AND (:clientId = 0 OR t.clientId = :clientId) "
			+ "AND (:taskCategoryId = 0 OR t.taskCategoryId = :taskCategoryId) " + "AND (:assignedTo = -1 "
			+ "     OR (:assignedTo = 0 AND (t.assignedTo = 0 OR t.assignedTo IS NULL)) "
			+ "     OR t.assignedTo = :assignedTo) " + "AND (:priority = 0 OR t.priority = :priority) " + "AND ( "
			+ "     0 IN :taskStatusIds " + "     OR t.taskStatus IN :taskStatusIds "
			+ "     OR (-1 IN :taskStatusIds AND (t.assignedTo IS NULL OR t.assignedTo = 0)) " + ") "
			+ "AND (:fromDate IS NULL OR :fromDate = '' OR FUNCTION('DATE', t.date) >= FUNCTION('DATE', CAST(:fromDate AS date))) "
			+ "AND (:toDate IS NULL OR :toDate = '' OR FUNCTION('DATE', t.date) <= FUNCTION('DATE', CAST(:toDate AS date))) "

			// ================= Dashboard Filter =================
			+ "AND ( " + "     :dashboardFilter IS NULL " + "     OR :dashboardFilter = '' " + "     OR ( "
			+ "          :dashboardFilter = 'today' " + "          AND t.date >= :startOfToday "
			+ "          AND t.date < :startOfTomorrow " + "        ) " + "     OR ( "
			+ "          :dashboardFilter = 'week' " + "          AND (t.taskStatus IS NULL OR t.taskStatus <> 5) "
			+ "          AND t.date >= :startOfWeek " + "          AND t.date <= :endOfWeek " + "        ) "
			+ "     OR ( " + "          :dashboardFilter = 'overdue' "
			+ "          AND (t.taskStatus IS NULL OR t.taskStatus <> 5) " + "          AND tc.duedatetime IS NOT NULL "
			+ "          AND FUNCTION('TIMESTAMPADD', HOUR, "
			+ "               CAST(tc.duedatetime AS integer), t.date) < :currentTime " + "        ) " + ") "

			// ================= Access Control =================
			+ "AND ( "

			// ---- HOD: all tasks of own task categories ----
			+ "     ( " + "          :isHod = 'Y' " + "          AND EXISTS ( " + "               SELECT 1 "
			+ "               FROM UserLoginEntity h " + "               WHERE h.userId = :userId "
			+ "               AND CONCAT(',', REPLACE(h.taskcategoryIds, ' ', ''), ',') "
			+ "                   LIKE CONCAT('%,', t.taskCategoryId, ',%') " + "          ) " + "     ) "

			// ---- MANAGER ----
			+ "     OR ( " + "          :loginType = 'manager' " + "          AND ( "
			+ "               t.assignedTo = :userId " + "               OR t.addedBy = :userId "
			+ "               OR c.managerId = :userId " + "          ) " + "     ) "

			// ---- ADMIN / OTHERS ----
			+ "     OR ( " + "          :loginType <> 'manager' " + "          AND ( "
			+ "               :isAdmin = 'Y' " + "               OR t.assignedTo = :userId "
			+ "               OR t.addedBy = :userId " + "               OR ( " + "                    :isAdmin <> 'Y' "
			+ "                    AND (t.assignedTo = 0 OR t.assignedTo IS NULL) "
			+ "                    AND EXISTS ( " + "                         SELECT 1 "
			+ "                         FROM UserLoginEntity ul "
			+ "                         WHERE ul.userId = :userId "
			+ "                         AND CONCAT(',', ul.taskcategoryIds, ',') "
			+ "                             LIKE CONCAT('%,', t.taskCategoryId, ',%') " + "                    ) "
			+ "               ) " + "          ) " + "     ) " + ") "
			+ "ORDER BY t.status ASC, t.date DESC, t.title ASC")
	Page<TaskDetailsDTO> findTaskDetails(PageRequest pageable, @Param("statusIndex") int statusIndex,
			@Param("search") String search, @Param("clientId") Integer clientId,
			@Param("taskCategoryId") Integer taskCategoryId, @Param("assignedTo") Integer assignedTo,
			@Param("priority") Integer priority, @Param("fromDate") String fromDate, @Param("toDate") String toDate,
			@Param("isAdmin") String isAdmin, @Param("userId") Integer userId,
			@Param("taskStatusIds") Set<Integer> taskStatusIds, @Param("loginType") String loginType,
			@Param("dashboardFilter") String dashboardFilter, @Param("startOfToday") LocalDateTime startOfToday,
			@Param("startOfTomorrow") LocalDateTime startOfTomorrow, @Param("startOfWeek") LocalDateTime startOfWeek,
			@Param("endOfWeek") LocalDateTime endOfWeek, @Param("currentTime") LocalDateTime currentTime,
			@Param("isHod") String isHod);

	@Modifying
	@Transactional
	@Query("UPDATE TaskEntity t SET t.status = :status WHERE t.taskId = :taskId")
	int deleteTask(@Param("status") Short status, @Param("taskId") int taskId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskEditDTO(" + "t.taskId, " + "t.assignedTo, "
			+ "COALESCE(NULLIF(u.firstName,''),'-'), " + "COALESCE(NULLIF(t.title,''),'-'), " + "t.taskStatus, "
			+ "t.addedBy, " + "COALESCE(NULLIF(a.firstName,''),'-'), " + "t.date, " + "t.endDate, " + "t.priority, "
			+ "COALESCE(NULLIF(c.name, ''), '-')" + ") " + "FROM TaskEntity t "
			+ "LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo "
			+ "LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId "
			+ "LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
			+ "LEFT JOIN UserLoginEntity a ON a.userId = t.addedBy " + "WHERE t.status = 1 "
			+ "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND (" + "    t.assignedTo = :userId "
			+ "    OR t.addedBy = :userId " + "    OR c.managerId = :userId " + "    OR c.userId = :userId" + ") "

//	        + "AND ("
//	        + "    :permission = 'Y' "
//	        + "    OR t.assignedTo = :userId "
//	        + "    OR t.addedBy = :userId "
//	        + "    OR c.managerId = :userId "
//	        + "    OR c.userId = :userId"
//	        + ") "
			+ "ORDER BY t.status ASC")
	Page<TaskEditDTO> findTasksByStatus(Pageable pageable, @Param("clientId") Integer clientId,
			@Param("userId") Integer userId);

//	@Param("permission") String permission

	List<TaskEntity> findByAssignedTo(Integer assignedTo);

	@Query("SELECT tc.duedatetime FROM TaskCategoryEntity tc WHERE tc.taskcategoryId = :taskCategoryId")
	String findDueTimeByTaskCategoryId(@Param("taskCategoryId") Integer taskCategoryId);

//	@EntityGraph(attributePaths = { "client", "taskCategory", "assignedUser", "assignedByUser" })
//	@Query("SELECT t " + "FROM TaskEntity t "
//			+ "WHERE t.status = 1 AND (:selectedClientId = 0 OR t.clientId = :selectedClientId) " + "AND ("
//
//			// ADMIN LOGIN
//			+ "    :isAdmin = 'Y' "
//
//			+ "    OR "
//
//			// NON-MANAGER LOGIN
//			+ "    (" + "        :loginType <> 'manager' " + "        AND " + "("
//			+ "            t.assignedTo = :userId " + "            OR t.addedBy = :userId " + "            OR ("
//			+ "                :isAdmin <> 'Y' AND (t.assignedTo = 0 OR t.assignedTo IS NULL)  " + "                AND EXISTS (" + "                    SELECT 1 "
//			+ "                    FROM TaskCategoryEntity tc2 "
//			+ "                    WHERE tc2.taskcategoryId = t.taskCategoryId "
//			+ "                    AND tc2.departmentId IN (" + "                        SELECT ul.departmentId "
//			+ "                        FROM UserLoginEntity ul " + "                        WHERE ul.userId = :userId"
//			+ "                    )" + "                )" + "            )" + "        )" + "    )" + ")")
//	List<TaskEntity> findDashboardTasks(@Param("userId") Integer userId, @Param("isAdmin") String isAdmin,
//			@Param("loginType") String loginType, @Param("selectedClientId") Integer selectedClientId);

//	@EntityGraph(attributePaths = { "client", "taskCategory", "assignedUser", "assignedByUser" })
//	@Query("SELECT t "
//	        + "FROM TaskEntity t "
//	        + "WHERE t.status = 1 AND (:selectedClientId = 0 OR t.clientId = :selectedClientId) "
//	        + "AND ("
//
//	        // ADMIN LOGIN
//	        + "    :isAdmin = 'Y' "
//
//	        + "    OR "
//
//	        // NON-MANAGER LOGIN
//	        + "    ("
//	        + "        :loginType <> 'manager' "
//	        + "        AND ("
//	        + "            t.assignedTo = :userId "
//	        + "            OR t.addedBy = :userId "
//	        + "            OR ("
//	        + "                :isAdmin <> 'Y' AND (t.assignedTo = 0 OR t.assignedTo IS NULL) "
//	        + "                AND EXISTS ("
//	        + "                    SELECT 1 "
//	        + "                    FROM TaskCategoryEntity tc2 "
//	        + "                    WHERE tc2.taskcategoryId = t.taskCategoryId "
//	        // ===== CHANGED: was "AND tc2.departmentId IN (SELECT ul.departmentId ...)" =====
//	        + "                    AND EXISTS ("
//	        + "                        SELECT 1 "
//	        + "                        FROM UserLoginEntity ul "
//	        + "                        WHERE ul.userId = :userId "
//	        + "                        AND CONCAT(',', ul.departmentIdsCsv, ',') "
//	        + "                            LIKE CONCAT('%,', tc2.departmentId, ',%') "
//	        + "                    )"
//	        // ===== END CHANGE =====
//	        + "                )"
//	        + "            )"
//	        + "        )"
//	        + "    )"
//	        + ")")
//	List<TaskEntity> findDashboardTasks(@Param("userId") Integer userId, @Param("isAdmin") String isAdmin,
//	        @Param("loginType") String loginType, @Param("selectedClientId") Integer selectedClientId,String isHod);

	@EntityGraph(attributePaths = { "client", "taskCategory", "assignedUser", "assignedByUser" })
	@Query("SELECT t " + "FROM TaskEntity t "
			+ "WHERE t.status = 1 AND (:selectedClientId = 0 OR t.clientId = :selectedClientId) " + "AND ("

			// ADMIN LOGIN
			+ "    :isAdmin = 'Y' "

			+ "    OR "

			// HOD: all tasks of own task categories
			+ "    (" + "        :isHod = 'Y' " + "        AND EXISTS (" + "            SELECT 1 "
			+ "            FROM UserLoginEntity h " + "            WHERE h.userId = :userId "
			+ "            AND CONCAT(',', REPLACE(h.taskcategoryIds, ' ', ''), ',') "
			+ "                LIKE CONCAT('%,', t.taskCategoryId, ',%') " + "        )" + "    )"

			+ "    OR "

			// NON-MANAGER LOGIN
			+ "    (" + "        :loginType <> 'manager' " + "        AND (" + "            t.assignedTo = :userId "
			+ "            OR t.addedBy = :userId " + "            OR ("
			+ "                :isAdmin <> 'Y' AND (t.assignedTo = 0 OR t.assignedTo IS NULL) "
			+ "                AND EXISTS (" + "                    SELECT 1 "
			+ "                    FROM TaskCategoryEntity tc2 "
			+ "                    WHERE tc2.taskcategoryId = t.taskCategoryId " + "                    AND EXISTS ("
			+ "                        SELECT 1 " + "                        FROM UserLoginEntity ul "
			+ "                        WHERE ul.userId = :userId "
			+ "                        AND CONCAT(',', ul.departmentIdsCsv, ',') "
			+ "                            LIKE CONCAT('%,', tc2.departmentId, ',%') " + "                    )"
			+ "                )" + "            )" + "        )" + "    )" + ")")
	List<TaskEntity> findDashboardTasks(@Param("userId") Integer userId, @Param("isAdmin") String isAdmin,
			@Param("loginType") String loginType, @Param("selectedClientId") Integer selectedClientId,
			@Param("isHod") String isHod);

	// NEW
	@Query("SELECT COUNT(t) " + "FROM TaskEntity t " + "JOIN t.client c " + "WHERE t.status = 1 "
			+ "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND (" + "    t.assignedTo = :userId "
			+ "    OR t.addedBy = :userId " + "    OR c.managerId = :userId " + "    OR c.userId = :userId" + ")")
	int countOfActiveTask(@Param("clientId") Integer clientId, @Param("userId") Integer userId);

	@Query("SELECT COUNT(t) " + "FROM TaskEntity t " + "JOIN t.client c " + "WHERE t.taskStatus = 5 "
			+ "AND t.status = 1 " + "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND ("
			+ "    t.assignedTo = :userId " + "    OR t.addedBy = :userId " + "    OR c.managerId = :userId "
			+ "    OR c.userId = :userId" + ")")
	int countOfCompletedTask(@Param("clientId") Integer clientId, @Param("userId") Integer userId);

	@Query("SELECT COUNT(t) " + "FROM TaskEntity t " + "JOIN t.client c " + "WHERE t.taskStatus IN (1, 2, 3, 4) "
			+ "AND t.status = 1 " + "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND ("
			+ "    t.assignedTo = :userId " + "    OR t.addedBy = :userId " + "    OR c.managerId = :userId "
			+ "    OR c.userId = :userId" + ")")
	int countOfPendingTask(@Param("clientId") Integer clientId, @Param("userId") Integer userId);

	@Query("SELECT COUNT(t) " + "FROM TaskEntity t " + "JOIN t.client c " + "WHERE t.taskStatus = 1 "
			+ "AND t.status = 1 " + "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND ("
			+ "    t.assignedTo = :userId " + "    OR t.addedBy = :userId " + "    OR c.managerId = :userId "
			+ "    OR c.userId = :userId" + ")")
	int countOfAssignedTask(@Param("clientId") Integer clientId, @Param("userId") Integer userId);

	@Query("SELECT COUNT(t) " + "FROM TaskEntity t " + "JOIN t.client c " + "WHERE t.taskStatus = 2 "
			+ "AND t.status = 1 " + "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND ("
			+ "    t.assignedTo = :userId " + "    OR t.addedBy = :userId " + "    OR c.managerId = :userId "
			+ "    OR c.userId = :userId" + ")")
	int countOfAssigneeClosureTask(@Param("clientId") Integer clientId, @Param("userId") Integer userId);

	@Query("SELECT COUNT(t) " + "FROM TaskEntity t " + "JOIN t.client c " + "WHERE t.taskStatus = 3 "
			+ "AND t.status = 1 " + "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND ("
			+ "    t.assignedTo = :userId " + "    OR t.addedBy = :userId " + "    OR c.managerId = :userId "
			+ "    OR c.userId = :userId" + ")")
	int countOfReOpenTask(@Param("clientId") Integer clientId, @Param("userId") Integer userId);

	@Query("SELECT COUNT(t) " + "FROM TaskEntity t " + "JOIN t.client c " + "WHERE t.taskStatus = 4 "
			+ "AND t.status = 1 " + "AND (:clientId = 0 OR t.clientId = :clientId) " + "AND ("
			+ "    t.assignedTo = :userId " + "    OR t.addedBy = :userId " + "    OR c.managerId = :userId "
			+ "    OR c.userId = :userId" + ")")
	int countOfAssigneeReClosureTask(@Param("clientId") Integer clientId, @Param("userId") Integer userId);

//	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.status = 1 AND (:clientId = 0 OR t.clientId = :clientId)")
//	int countOfActiveTask(@Param("clientId") Integer clientId,@Param("userId") Integer userId);
//
//	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 5 AND t.status = 1 AND (:clientId = 0 OR t.clientId = :clientId)")
//	int countOfCompletedTask(@Param("clientId") Integer clientId,@Param("userId") Integer userId);
//
//	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus IN (1, 2, 3, 4) AND t.status = 1 AND (:clientId = 0 OR t.clientId = :clientId)")
//	int countOfPendingTask(@Param("clientId") Integer clientId,@Param("userId") Integer userId);
//
//	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 1 AND t.status = 1 AND (:clientId = 0 OR t.clientId = :clientId)")
//	int countOfAssignedTask(@Param("clientId") Integer clientId,@Param("userId") Integer userId);
//
//	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 2 AND t.status = 1 AND (:clientId = 0 OR t.clientId = :clientId)")
//	int countOfAssigneeClosureTask(@Param("clientId") Integer clientId,@Param("userId") Integer userId);
//
//	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 3 AND t.status = 1 AND (:clientId = 0 OR t.clientId = :clientId)")
//	int countOfReOpenTask(@Param("clientId") Integer clientId,@Param("userId") Integer userId);
//
//	@Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.taskStatus = 4 AND t.status = 1 AND (:clientId = 0 OR t.clientId = :clientId)")
//	int countOfAssigneeReClosureTask(@Param("clientId") Integer clientId,@Param("userId") Integer userId);

	// For CREATE: any task with same title for this client
	boolean existsByTitleIgnoreCaseAndClientId(String title, Integer clientId);

	// For UPDATE: same title for this client, but a different taskId
	boolean existsByTitleIgnoreCaseAndClientIdAndTaskIdNot(String title, Integer clientId, Integer taskId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM TaskEntity t WHERE t.taskId = :taskId")
	Optional<TaskEntity> findByIdForUpdate(@Param("taskId") Integer taskId);

//	@Query(value = "SELECT t.i_assignedto AS assignedTo, "
//			+ "COALESCE(SUM(TIMESTAMPDIFF(HOUR, t.d_date, t.d_enddate)), 0) AS hours " + "FROM t_task t "
//			+ "WHERE t.i_assignedto IN ( " + "    SELECT u.i_userid " + "    FROM t_userlogin u "
//			+ "    WHERE FIND_IN_SET(:categoryId, u.i_taskcategoryid) " + "      AND u.i_status = 1 "
//			+ "      AND u.i_weekly_off != 3 " + ") " + "AND ((NOW() >= t.d_date AND NOW() <= t.d_enddate) "
//			+ "     OR DATE(t.d_date) = CURRENT_DATE()) " + "AND t.i_taskcategoryid = :categoryId "
//			+ "AND t.i_taskstatus IN (1,3) " + "GROUP BY t.i_assignedto " + "ORDER BY hours ", nativeQuery = true)
//	List<AssigneeWorkloadDTO> findAssigneeWorkload(@Param("categoryId") Integer categoryId);
	
	
	@Query(value = "SELECT i_assignedto, SUM(TIMESTAMPDIFF(HOUR, d_date, d_enddate)) AS hours FROM t_task "
            + "WHERE i_assignedto IN (SELECT i_userid FROM t_userlogin "
            + "     WHERE FIND_IN_SET(:taskCatId, s_taskcategoryIds) AND i_status = 1 "
            + "     AND (i_weekly_off IS NULL OR i_weekly_off != :dayOfWeek)) "
            + "AND ((:startDateTime >= d_date AND :startDateTime <= d_enddate) "
            + "     OR DATE_FORMAT(d_date, '%Y-%m-%d') = :startDate) "
//            + "AND i_assignedto IN (SELECT i_userid FROM t_attendance "
//            + "     WHERE DATE_FORMAT(ts_intime, '%Y-%m-%d') = :startDate) "
            + "AND i_taskcategoryid = :taskCatId AND i_taskstatus IN (1,3) "
            + "GROUP BY i_assignedto ORDER BY hours", nativeQuery = true)
    List<Object[]> findWorkloadByCategory(@Param("taskCatId") int taskCatId,
                                          @Param("dayOfWeek") int dayOfWeek,
                                          @Param("startDateTime") LocalDateTime startDateTime,
                                          @Param("startDate") String startDate);
    
    // Fallback: random eligible user
    @Query(value = "SELECT i_userid FROM t_userlogin WHERE i_status = 1 "
            + "AND FIND_IN_SET(:taskCatId, s_taskcategoryIds) "
            + "AND (i_weekly_off IS NULL OR i_weekly_off != :dayOfWeek) "
            + "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Integer> findRandomEligibleUser(@Param("taskCatId") int taskCatId,
                                             @Param("dayOfWeek") int dayOfWeek);
	
	
	
	
}