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

import com.webelement.taskapp.dto.TaskCategoryDTO;
import com.webelement.taskapp.entity.TaskCategoryEntity;

@Repository
public interface TaskCategoryRepository
        extends JpaRepository<TaskCategoryEntity, Integer> {
	
	@Query("SELECT tc FROM TaskCategoryEntity tc WHERE tc.status = 1 ORDER BY tc.name ASC")
	List<TaskCategoryEntity> findAllActiveTaskCategories();


    @Transactional
    @Modifying
    @Query("UPDATE TaskCategoryEntity t "
         + "SET t.status = :status, "
         + "t.moddate = CURRENT_TIMESTAMP "
         + "WHERE t.taskcategoryId = :taskcategoryId")
    int softDelete(
            @Param("status") Integer status,
            @Param("taskcategoryId") Integer taskcategoryId);


    @Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO("
         + "t.taskcategoryId, "
         + "t.departmentId, "
         + "d.name, "
         + "t.name, "
         + "t.status, "
         + "t.userId, "
         + "t.regdate, "
         + "t.moddate) "
         + "FROM TaskCategoryEntity t "
         + "LEFT JOIN DepartmentEntity d "
         + "ON d.departmentId = t.departmentId "
         + "WHERE t.taskcategoryId > 0 "
         + "AND (:statusIndex = 0 OR t.status = :statusIndex) "
         + "AND (:departmentId = 0 OR t.departmentId = :departmentId) "
         + "AND (:search IS NULL OR :search = '' "
         + "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
         + "ORDER BY t.status, t.name")
    Page<TaskCategoryDTO> findTaskCategoryDetails(
            Pageable pageable,
            @Param("statusIndex") int statusIndex,
            @Param("search") String search,
            @Param("departmentId") int departmentId);


    List<TaskCategoryEntity> findByStatus(Integer status);


    boolean existsByNameIgnoreCaseAndStatusNot(
            String name,
            Integer status);


    TaskCategoryEntity findByNameIgnoreCase(String name);
}
