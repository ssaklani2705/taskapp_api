package com.webelement.taskapp.repo;

import java.awt.print.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.dto.TaskDetailsDTO;
import com.webelement.taskapp.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer>{

	@Query("SELECT new com.webelement.taskapp.dto.TaskDetailsDTO(t.taskId, c.name, t.date, tc.name, u.firstName, t.priority, t.status, t.title) FROM TaskEntity t LEFT JOIN ClientEntity c ON c.clientId = t.clientId LEFT JOIN TaskCategoryEntity tc ON tc.taskcategoryId = t.taskCategoryId LEFT JOIN UserLoginEntity u ON u.userId = t.assignedTo WHERE t.taskId > 0 AND (:statusIndex = 0 OR t.status = :statusIndex) AND (:search IS NULL OR :search = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(tc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))) AND (:clientId = 0 OR t.clientId = :clientId) AND (:taskCategoryId = 0 OR t.taskCategoryId = :taskCategoryId) AND (:assignedTo = 0 OR t.assignedTo = :assignedTo) AND (:priority = 0OR t.priority = :priority) ORDER BY t.date DESC, t.taskId DESC") Page<TaskDetailsDTO> findTaskDetails(PageRequest pageable, @Param("statusIndex") int statusIndex, @Param("search") String search,
			  @Param("clientId")
	Integer clientId,

    @Param("taskCategoryId")
	Integer taskCategoryId,

    @Param("assignedTo")
	Integer assignedTo,

    @Param("priority")
    Integer priority);
	
	
}
