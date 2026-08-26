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

import com.webelement.taskapp.dto.DesignationDTO;
import com.webelement.taskapp.dto.StateDTO;
import com.webelement.taskapp.entity.StateEntity;
@Repository
public interface StateRepo extends JpaRepository<StateEntity, Integer>{

	StateEntity save(StateDTO state);
	
	
    boolean existsByNameIgnoreCaseAndStatusNot(
            String name,
            Short status);

    List<StateEntity> findByNameIgnoreCase(
            String name);
    
    boolean existsByNameIgnoreCaseAndStateIdNotAndStatusNot(
            String name,
            Integer stateId,
            Short status);
    
    @Query(
    	    "SELECT new com.webelement.taskapp.dto.StateDTO(" +
    	    "s.stateId, " +
    	    "s.name, " +
    	    "s.code, " +
    	    "s.status, " +
    	    "s.userId) " +
    	    "FROM StateEntity s " +
    	    "WHERE s.stateId > 0 " +
    	    "AND (:statusIndex = 0 OR s.status = :statusIndex) " +
    	    "AND LOWER(COALESCE(s.name, '')) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) " +
    	    "ORDER BY s.status, s.name"
    	)
    	Page<StateDTO> findStateDetails(
    	        Pageable pageable,
    	        @Param("statusIndex") int statusIndex,
    	        @Param("search") String search);
    
    
    @Modifying
    @Transactional
    @Query("UPDATE StateEntity s SET s.status = 3, s.modificationDate = CURRENT_TIMESTAMP WHERE s.stateId = :stateId")
    Integer softDelete(@Param("stateId") Integer stateId);
    
    List<StateEntity> findByStatus(Short status);


	boolean existsByCodeIgnoreCaseAndStatusNot(String code, short s);


	List<StateEntity> findByCodeIgnoreCase(String code);


}
