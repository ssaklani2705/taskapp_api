package com.webelement.taskapp.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.ReopenAssignedUserCountProjection;
import com.webelement.taskapp.dto.ReopenReportDTO;
import com.webelement.taskapp.dto.ReopenUserDropdownDTO;
import com.webelement.taskapp.entity.ReopenEntity;

@Repository
public interface ReopenRepository extends JpaRepository<ReopenEntity, Integer> {

	@Query(value = "SELECT new com.webelement.taskapp.dto.ReopenReportDTO( " + "r.taskId, " + "t.title, " + "t.date, "
			+ "t.endDate, " + "r.assignedToId, " + "assignedUser.firstName, " + "r.reopenById, "
			+ "reopenByUser.firstName, " + "r.reopenDate, " + "r.description " + ") " + "FROM ReopenEntity r "
			+ "LEFT JOIN r.task t " + "LEFT JOIN r.assignedToUser assignedUser "
			+ "LEFT JOIN r.reopenByUser reopenByUser " + "WHERE " + "(:search IS NULL OR TRIM(:search) = '' "
			+ "OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) "
//			+ "OR LOWER(assignedUser.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
//			+ "OR LOWER(reopenByUser.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "AND (:fromDate IS NULL OR r.reopenDate >= :fromDate) "
			+ "AND (:toDate IS NULL OR r.reopenDate < :toDate) "
			+ "AND (:assignedToId IS NULL OR r.assignedToId = :assignedToId) "
			+ "AND (:reopenById IS NULL OR r.reopenById = :reopenById) "
			+ "ORDER BY r.reopenDate DESC", countQuery = "SELECT COUNT(r) " + "FROM ReopenEntity r "
					+ "LEFT JOIN r.task t " + "LEFT JOIN r.assignedToUser assignedUser "
					+ "LEFT JOIN r.reopenByUser reopenByUser " + "WHERE " + "(:search IS NULL OR TRIM(:search) = '' "
					+ "OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) "
					+ "OR LOWER(assignedUser.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
					+ "OR LOWER(reopenByUser.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
					+ "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))) "
					+ "AND (:fromDate IS NULL OR r.reopenDate >= :fromDate) "
					+ "AND (:toDate IS NULL OR r.reopenDate < :toDate) "
					+ "AND (:assignedToId IS NULL OR r.assignedToId = :assignedToId) "
					+ "AND (:reopenById IS NULL OR r.reopenById = :reopenById)")
	Page<ReopenReportDTO> getReopenReport(@Param("search") String search, @Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate, @Param("assignedToId") Integer assignedToId,
			@Param("reopenById") Integer reopenById, Pageable pageable);

	@Query("SELECT DISTINCT new com.webelement.taskapp.dto.ReopenUserDropdownDTO(" + "r.assignedToId, u.firstName) "
			+ "FROM ReopenEntity r " + "INNER JOIN r.assignedToUser u " + "WHERE r.assignedToId IS NOT NULL "
			+ "ORDER BY u.firstName ASC")
	List<ReopenUserDropdownDTO> findAssignedToUsersInReopenReport();

	@Query("SELECT DISTINCT new com.webelement.taskapp.dto.ReopenUserDropdownDTO(" + "r.reopenById, u.firstName) "
			+ "FROM ReopenEntity r " + "INNER JOIN r.reopenByUser u " + "WHERE r.reopenById IS NOT NULL "
			+ "ORDER BY u.firstName ASC")
	List<ReopenUserDropdownDTO> findReopenedByUsersInReopenReport();
	
	@Query(value = "SELECT " + "u.i_userid AS assignedToId, " + "u.s_firstname AS username, "
			+ "COUNT(r.i_reopenid) AS taskCount " + "FROM t_userlogin u "
			+ "LEFT JOIN t_reopen r ON r.i_assigntoid = u.i_userid " + "AND r.i_reopenid > 0 "
			+ "AND (:fromDate IS NULL OR r.d_reopendate >= :fromDate) "
			+ "AND (:toDate IS NULL OR r.d_reopendate < :toDate) " + "WHERE u.i_status = 1 "
			+ "AND (:search IS NULL OR TRIM(:search) = '' "
			+ "OR LOWER(u.s_firstname) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "AND (:departmentIds IS NULL OR TRIM(:departmentIds) = '' "
			+ "OR u.s_departmentids REGEXP CONCAT('(^|,)(', " + "REPLACE(:departmentIds, ',', '|'), ')(,|$)')) "
			+ "GROUP BY u.i_userid, u.s_firstname " + "ORDER BY u.s_firstname ASC", countQuery = "SELECT COUNT(*) "
					+ "FROM t_userlogin u " + "WHERE u.i_status = 1 " + "AND (:search IS NULL OR TRIM(:search) = '' "
					+ "OR LOWER(u.s_firstname) LIKE LOWER(CONCAT('%', :search, '%'))) "
					+ "AND (:departmentIds IS NULL OR TRIM(:departmentIds) = '' "
					+ "OR u.s_departmentids REGEXP CONCAT('(^|,)(', "
					+ "REPLACE(:departmentIds, ',', '|'), ')(,|$)'))", nativeQuery = true)
	Page<ReopenAssignedUserCountProjection> getReopenCountByAssignedUser(@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate, @Param("search") String search,
			@Param("departmentIds") String departmentIds, Pageable pageable);
}
