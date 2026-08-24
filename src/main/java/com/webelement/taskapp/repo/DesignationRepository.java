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
import com.webelement.taskapp.entity.DesignationEntity;


@Repository
public interface DesignationRepository
        extends JpaRepository<DesignationEntity, Integer> {

    @Transactional
    @Modifying
    @Query("UPDATE DesignationEntity d " +
           "SET d.status = :status, " +
           "d.moddate = CURRENT_TIMESTAMP " +
           "WHERE d.designationId = :designationId")
    int softDelete(
            @Param("status") Integer status,
            @Param("designationId") Integer designationId);

    @Query(
        "SELECT new com.webelement.taskapp.dto.DesignationDTO(" +
        "d.designationId, " +
        "d.name, " +
        "d.sequence, " +
        "d.status, " +
        "d.userId, " +
        "d.regdate, " +
        "d.moddate) " +
        "FROM DesignationEntity d " +
        "WHERE d.designationId > 0 " +
        "AND (:statusIndex = 0 OR d.status = :statusIndex) " +
        "AND (" +
        ":search IS NULL OR " +
        ":search = '' OR " +
        "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))" +
        ") " +
        "ORDER BY d.status, d.sequence, d.name"
    )
    Page<DesignationDTO> findDesignationDetails(
            Pageable pageable,
            @Param("statusIndex") int statusIndex,
            @Param("search") String search);

    List<DesignationEntity> findByStatus(Integer status);

    boolean existsByNameIgnoreCaseAndStatusNot(
            String name,
            Integer status);

    DesignationEntity findByNameIgnoreCase(String name);
}

