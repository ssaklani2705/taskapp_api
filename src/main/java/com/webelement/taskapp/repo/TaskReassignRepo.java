package com.webelement.taskapp.repo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.PlanDTO;
import com.webelement.taskapp.entity.ReassignEntity;
import com.webelement.taskapp.dto.TaskReassigneDTO;

@Repository
public interface TaskReassignRepo extends JpaRepository<ReassignEntity, Integer>{
	
	
}
