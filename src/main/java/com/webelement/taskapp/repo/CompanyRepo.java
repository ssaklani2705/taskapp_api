package com.webelement.taskapp.repo;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.webelement.taskapp.dto.PlanDTO;
import com.webelement.taskapp.entity.CompanyEntity;
import com.webelement.taskapp.entity.PlanEntity;
import com.webelement.taskapp.dto.CompanyDTO;

public interface CompanyRepo extends JpaRepository<CompanyEntity, Integer> {
	
	@EntityGraph(attributePaths = "state")
	@Query(value = "SELECT c FROM CompanyEntity c " +
			"WHERE c.companyId > 0 " +
			"AND (:statusIndex = 0 OR c.status = :statusIndex) " +
			 "AND (:stateId = 0 OR c.stateId = :stateId) " +
			"AND (" +
			"    LOWER(COALESCE(c.companyName, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
			"    OR LOWER(COALESCE(c.city, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
			"    OR LOWER(COALESCE(c.location, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
			"    OR LOWER(COALESCE(c.pan, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
			"    OR LOWER(COALESCE(c.gstNo, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
			"    OR LOWER(COALESCE(c.stateId, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
			") " +
			"ORDER BY c.status ASC, c.registrationDate DESC, c.companyName ASC")
	Page<CompanyEntity> findCompanyDetails(
			Pageable pageable,
			@Param("statusIndex") int statusIndex,
			@Param("search") String search,@Param("stateId") Integer stateId);

	boolean existsByCompanyNameIgnoreCaseAndStatusNot(String name, short s);

	List<CompanyEntity> findByCompanyNameIgnoreCase(String name);
	
	@Query(value = "SELECT COUNT(*) FROM tbl_client WHERE i_companyid = :companyId AND i_status <> 3", nativeQuery = true)
	Integer existsByCompanyId(@Param("companyId") Integer companyId);
	
	@Query("SELECT c FROM CompanyEntity c WHERE c.status= 1 ORDER BY c.companyName")
	List<CompanyEntity> activeCompany(); 

	@Modifying
	@Transactional
	@Query("UPDATE CompanyEntity c SET c.status = 3, c.modificationDate = CURRENT_TIMESTAMP WHERE c.companyId = :companyId")
	int softDelete(@Param("companyId") Integer companyId);
	
	
	@EntityGraph(attributePaths = "state")
	List<CompanyEntity> findByStatus(Short status);
	
	@Override
	@EntityGraph(attributePaths = "state")
	Optional<CompanyEntity> findById(Integer id);
}
