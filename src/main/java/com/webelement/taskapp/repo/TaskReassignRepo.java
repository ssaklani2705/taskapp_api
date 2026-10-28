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

import com.webelement.taskapp.dto.PlanDTO;
import com.webelement.taskapp.entity.ReassignEntity;
import com.webelement.taskapp.entity.TaskCategoryEntity;
import com.webelement.taskapp.dto.TaskReassigneDTO;

@Repository
public interface TaskReassignRepo extends JpaRepository<ReassignEntity, Integer> {
	@EntityGraph(attributePaths = { "taskEntity", "taskEntity.taskCategory", "taskEntity.client", "currentUser", "newUser" })
	@Query("SELECT tr FROM ReassignEntity tr " +
	       "WHERE tr.reassignId > 0 " +
	       "AND (:fromDate IS NULL OR tr.registrationDate >= :fromDate) " +
	       "AND (:toDate IS NULL OR tr.registrationDate < :toDate) " +
	       "AND (:taskCategoryId = 0 OR tr.taskEntity.taskCategoryId = :taskCategoryId) " +
	       "AND (:clientId = 0 OR tr.taskEntity.clientId = :clientId) " +
	       "AND (:userId = 0 OR tr.currentUserId = :userId OR tr.newUserId = :userId) " +
	       "AND (:departmentFilter = 0 OR tr.taskEntity.taskCategory.departmentId IN :departmentIds) " +
	       "AND (LOWER(COALESCE(tr.taskEntity.title, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))) " +
	       "ORDER BY tr.reassignId DESC")
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
	
	
}
