package com.webelement.taskapp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webelement.taskapp.dto.TaskNoteDTO;
import com.webelement.taskapp.entity.TaskNoteEntity;

public interface TaskNoteRepo extends JpaRepository<TaskNoteEntity, Integer>{

	 @Query("SELECT new com.webelement.taskapp.dto.TaskNoteDTO(" +
	           "n.taskNoteId, " +
	           "n.taskId, " +
	           "n.createdBy, " +
	           "u.firstName, " +
	           "n.note, " +
	           "n.registrationDate) " +
	           "FROM TaskNoteEntity n " +
	           "LEFT JOIN UserLoginEntity u ON u.userId = n.createdBy " +
	           "WHERE n.taskId = :taskId " +
	           "AND n.status = 1 " +
	           "ORDER BY n.registrationDate DESC")
	    List<TaskNoteDTO> findTaskNotes(
	            @Param("taskId") Integer taskId
	    );
	
}
