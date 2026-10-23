package com.webelement.taskapp.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.entity.ReassignEntity;

@Repository
public interface TaskReassignRepo extends JpaRepository<ReassignEntity, Integer>{}
