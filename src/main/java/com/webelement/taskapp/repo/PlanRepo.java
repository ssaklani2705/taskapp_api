package com.webelement.taskapp.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.entity.PermissionEntity;
import com.webelement.taskapp.entity.PlanEntity;

import com.webelement.taskapp.dto.PlanDTO;

@Repository
public interface PlanRepo  extends JpaRepository<PlanEntity, Integer>{

	List<PlanEntity> findByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndStatusNot(String name, short s);
	
	@Query(
		    "SELECT new com.webelement.taskapp.dto.PlanDTO(" +
		    "p.planId, " +
		    "p.name, " +
		    "p.rate, " +
		    "p.status) " +
		    "FROM PlanEntity p " +
		    "WHERE p.planId > 0 " +
		    "AND (:statusIndex = 0 OR p.status = :statusIndex) " +
		    "AND LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
		    "ORDER BY p.status, p.name"
		)
		Page<PlanDTO> findPlanDetails(
		        Pageable pageable,
		        @Param("statusIndex") int statusIndex,
		        @Param("search") String search);
	
	
	@Modifying
	@Transactional
	@Query("UPDATE PlanEntity s SET s.status = 3, s.modificationDate = CURRENT_TIMESTAMP WHERE s.planId = :planId")
	Integer softDelete(@Param("planId") Integer planId);

}
