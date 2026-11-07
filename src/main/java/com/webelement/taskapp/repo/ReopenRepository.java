package com.webelement.taskapp.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
