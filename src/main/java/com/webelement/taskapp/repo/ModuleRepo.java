package com.webelement.taskapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webelement.taskapp.entity.ModuleEntity;

public interface ModuleRepo extends JpaRepository<ModuleEntity, Integer> {

}
