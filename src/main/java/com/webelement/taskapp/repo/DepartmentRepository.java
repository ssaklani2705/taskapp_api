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

import com.webelement.taskapp.dto.DepartmentDTO;
import com.webelement.taskapp.entity.DepartmentEntity;


@Repository
public interface DepartmentRepository
		extends JpaRepository<DepartmentEntity, Integer> {

	// =========================================================
	// SOFT DELETE
	// =========================================================

	@Transactional
	@Modifying
	@Query("UPDATE DepartmentEntity d "
			+ "SET d.status = :status, "
			+ "d.moddate = CURRENT_TIMESTAMP "
			+ "WHERE d.departmentId = :departmentId")
	int softDelete(
			@Param("status") Integer status,
			@Param("departmentId") Integer departmentId);

	// =========================================================
	// PAGINATION + SEARCH
	// =========================================================

	@Query("SELECT new com.webelement.taskapp.dto.DepartmentDTO("
			+ "d.departmentId, "
			+ "d.name, "
			+ "d.sequence, "
			+ "d.status, "
			+ "d.userId, "
			+ "d.regdate, "
			+ "d.moddate) "
			+ "FROM DepartmentEntity d "
			+ "WHERE d.departmentId > 0 "
			+ "AND (:statusIndex = 0 OR d.status = :statusIndex) "
			+ "AND (:search IS NULL OR :search = '' "
			+ "OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "ORDER BY d.status, d.sequence, d.name")
	Page<DepartmentDTO> findDepartmentDetails(
			Pageable pageable,
			@Param("statusIndex") int statusIndex,
			@Param("search") String search);

	// =========================================================
	// ACTIVE DEPARTMENTS
	// =========================================================

	List<DepartmentEntity> findByStatus(Integer status);

	// =========================================================
	// NAME VALIDATION
	// =========================================================

	boolean existsByNameIgnoreCase(String name);

	DepartmentEntity findByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndStatusNot(
			String name,
			Integer status);
	
	@Query(value = "SELECT * FROM t_department WHERE status = 1", nativeQuery = true)
	List<DepartmentEntity> findAllActiveDepartments();
}