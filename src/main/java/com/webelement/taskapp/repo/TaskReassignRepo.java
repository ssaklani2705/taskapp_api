package com.webelement.taskapp.repo;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.DepartmentFilterProjection;
import com.webelement.taskapp.dto.OldUserTaskCountDto;
import com.webelement.taskapp.entity.ReassignEntity;

@Repository
public interface TaskReassignRepo extends JpaRepository<ReassignEntity, Integer> {
	
	@EntityGraph(attributePaths = { "taskEntity", "taskEntity.taskCategory", "taskEntity.client", "currentUser", "newUser","assignedBy" })
	@Query("SELECT tr FROM ReassignEntity tr " +
	       "WHERE tr.reassignId > 0 " +
	       "AND (:fromDate IS NULL OR tr.registrationDate >= :fromDate) " +
	       "AND (:toDate IS NULL OR tr.registrationDate < :toDate) " +
	       "AND (:taskCategoryId = 0 OR tr.taskEntity.taskCategoryId = :taskCategoryId) " +
	       "AND (:clientId = 0 OR tr.taskEntity.clientId = :clientId) " +
	       "AND (:userId = 0 OR tr.currentUserId = :userId OR tr.newUserId = :userId) " +
	       "AND (:departmentFilter = 0 OR tr.taskEntity.taskCategory.departmentId IN :departmentIds) " +
	       "AND (LOWER(COALESCE(tr.taskEntity.title, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))) " +
	       "ORDER BY tr.registrationDate DESC")
	Page<ReassignEntity> getTaskReassignReport(
	        Pageable pageable,
	        @Param("fromDate") LocalDateTime fromDate,
	        @Param("toDate") LocalDateTime toDate,
	        @Param("taskCategoryId") int taskCategoryId,
	        @Param("clientId") int clientId,
	        @Param("userId") int userId,
	        @Param("search") String search,
	        @Param("departmentFilter") int departmentFilter,
	        @Param("departmentIds") List<Integer> departmentIds);

	
	@Query(value = "SELECT " + "u.s_firstname AS userName, " + "COUNT(tr.i_taskid) AS taskCount "
            + "FROM t_userlogin u " + "LEFT JOIN t_reassign tr ON " + "tr.i_currentuserid = u.i_userid "
            + "AND tr.i_reassignid > 0 " + "AND (:fromDate IS NULL OR tr.ts_regdate >= :fromDate) "
            + "AND (:toDate IS NULL OR tr.ts_regdate < :toDate) " + "WHERE u.i_status = 1 "
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
    Page<OldUserTaskCountDto> getOldUserTaskCount(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate, @Param("search") String search,
            @Param("departmentIds") String departmentIds, Pageable pageable);




@Query(value = "SELECT d.i_departmentid AS departmentId, d.s_name AS name FROM t_department d "
            + "WHERE d.i_status = 1 ORDER BY d.s_name ASC", nativeQuery = true)
    List<DepartmentFilterProjection> findReassignSummaryReportDepartments();
	
	
}
