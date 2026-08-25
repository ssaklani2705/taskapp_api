package com.webelement.taskapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webelement.taskapp.entity.StateEntity;

public interface StateRepo extends JpaRepository<StateEntity, Integer>{

}
