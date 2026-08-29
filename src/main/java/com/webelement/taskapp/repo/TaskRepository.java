package com.webelement.taskapp.repo;

import java.awt.print.Pageable;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.dto.TaskEditDTO;
import com.webelement.taskapp.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer>{
	
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

    @Query("SELECT new com.webelement.taskapp.dto.TaskDetailsDTO(t.taskId, c.name, t.date, tc.name, u.firstName, t.priority, t.status, t.title,t.taskStatus,t.assignedTo,t.addedBy) " +
    	       "FROM TaskEntity t " +
    	       "LEFT JOIN ClientEntity c ON c.clientId = t.clientId " +
    	       "LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId " +
    	       "LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo " +
    	       "WHERE t.taskId > 0 " +
    	       "AND (:statusIndex = 0 OR t.status = :statusIndex) " +
    	       "AND (:search IS NULL OR :search = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(tc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
    	       "AND (:clientId = 0 OR t.clientId = :clientId) " +
    	       "AND (:taskCategoryId = 0 OR t.taskCategoryId = :taskCategoryId) " +
    	       "AND (:assignedTo = 0 OR t.assignedTo = :assignedTo) " +
    	       "AND (:priority = 0 OR t.priority = :priority) " +
    	       "AND (:taskStatusId = 0 OR t.taskStatus = :taskStatusId) " +
    	       "AND (:fromDate IS NULL OR :fromDate = '' OR t.date >= :fromDate) " +
    	       "AND (:toDate IS NULL OR :toDate = '' OR t.date <= :toDate) " +
    	       
			//ADMIN / USER ACCESS
			"AND (" +
			    ":isAdmin = 'Y' " +
			    "OR (" +
			        "t.assignedTo = :userId " +
			        "OR t.addedBy = :userId" +
			    ")" +
			") " +


    	       "ORDER BY t.status ASC,  c.name ASC")
    	Page<TaskDetailsDTO> findTaskDetails(
    	        PageRequest pageable,
    	        @Param("statusIndex") int statusIndex,
    	        @Param("search") String search,
    	        @Param("clientId") Integer clientId,
    	        @Param("taskCategoryId") Integer taskCategoryId,
    	        @Param("assignedTo") Integer assignedTo,
    	        @Param("priority") Integer priority,
    	        @Param("fromDate") String fromDate,
    	        @Param("toDate") String toDate,
    	        @Param("isAdmin")
    	        String isAdmin,
    	        @Param("userId")
    	        Integer userId,
    	        @Param("taskStatusId")
    	        Integer taskStatusId
    	);
    
@Modifying
@Transactional
@Query("UPDATE TaskEntity t SET t.status = :status WHERE t.taskId = :taskId")
int deleteTask(@Param("status") Short status,
               @Param("taskId") int taskId);









	
	
}
