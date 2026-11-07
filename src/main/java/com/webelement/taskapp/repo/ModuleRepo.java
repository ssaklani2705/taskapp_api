package com.webelement.taskapp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.ModuleDropdownProjection;
import com.webelement.taskapp.entity.ModuleEntity;

@Repository
public interface ModuleRepo extends JpaRepository<ModuleEntity, Integer> {
	@Query("SELECT " + "m.moduleId AS moduleId, " + "m.name AS name " + "FROM ModuleEntity m "
			+ "WHERE m.status = :status " + "AND LOWER(m.name) NOT LIKE '%report%' "
			+ "AND LOWER(m.name) NOT LIKE '%dashboard%' " + "ORDER BY m.sequence ASC, m.name ASC")
	List<ModuleDropdownProjection> findModulesByStatus(@Param("status") Integer status);
}
