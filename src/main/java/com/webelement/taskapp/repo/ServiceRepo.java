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

import com.webelement.taskapp.dto.ServiceDTO;
import com.webelement.taskapp.entity.ServiceEntity;

@Repository
public interface ServiceRepo extends JpaRepository<ServiceEntity, Integer> {

    List<ServiceEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndStatusNot(String name, short status);

    @Query("SELECT new com.webelement.taskapp.dto.ServiceDTO("
            + "s.serviceId, s.name, s.status, 0) "
            + "FROM ServiceEntity s "
            + "WHERE s.serviceId > 0 "
            + "AND (:statusIndex = 0 OR s.status = :statusIndex) "
            + "AND LOWER(COALESCE(s.name, '')) "
            + "LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) "
            + "ORDER BY s.status ASC, s.registrationDate DESC, s.name ASC")
    Page<ServiceDTO> findServiceDetails(
            Pageable pageable,
            @Param("statusIndex") int statusIndex,
            @Param("search") String search);

    @Modifying
    @Transactional
    @Query("UPDATE ServiceEntity s "
            + "SET s.status = 3, s.modificationDate = CURRENT_TIMESTAMP "
            + "WHERE s.serviceId = :serviceId")
    Integer softDelete(@Param("serviceId") Integer serviceId);

    List<ServiceEntity> findByStatus(Short status);

    @Query("SELECT s.serviceId FROM ServiceEntity s "
            + "WHERE LOWER(s.name) = LOWER(:name) AND s.status = 1")
    Integer findIdByName(@Param("name") String name);
    
    List<ServiceEntity> findByStatus(Integer status);
}
