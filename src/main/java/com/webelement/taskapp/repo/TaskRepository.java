package com.webelement.taskapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webelement.taskapp.entity.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Integer>{

}
