package com.webelement.taskapp.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.entity.StateEntity;

@Repository
public interface StateRepository extends JpaRepository<StateEntity, Integer> {

	Optional<StateEntity> findByNameIgnoreCase(String name);
	
	List<StateEntity> findByStatus(Short status);
}
