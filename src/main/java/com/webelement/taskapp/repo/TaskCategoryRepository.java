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
public interface TaskCategoryRepository extends JpaRepository<TaskCategoryEntity, Integer> {
	
	@Query("SELECT tc FROM TaskCategoryEntity tc "
	        + "WHERE tc.status = 1 "
	        + "AND EXISTS ( "
	        + "     SELECT 1 FROM TaskEntity t "
	        + "     LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
	        + "     WHERE t.taskCategoryId = tc.taskcategoryId "
	        + "     AND ( "
	        + "          ( "
	        + "               :loginType = 'manager' "
	        + "               AND ( "
	        + "                    t.assignedTo = :userId "
	        + "                    OR t.addedBy = :userId "
	        + "                    OR c.managerId = :userId "
	        + "               ) "
	        + "          ) "
	        + "          OR "
	        + "          ( "
	        + "               :loginType <> 'manager' "
	        + "               AND ( "
	        + "                    :isAdmin = 'Y' "
	        + "                    OR t.assignedTo = :userId "
	        + "                    OR t.addedBy = :userId "
	        + "                    OR ( "
	        + "                         :isAdmin <> 'Y' "
	        + "                         AND (t.assignedTo = 0 OR t.assignedTo IS NULL) "
	        + "                         AND EXISTS ( "
	        + "                              SELECT 1 FROM UserLoginEntity ul "
	        + "                              WHERE ul.userId = :userId "
	        + "                              AND CONCAT(',', ul.taskcategoryIds, ',') "
	        + "                                  LIKE CONCAT('%,', t.taskCategoryId, ',%') "
	        + "                         ) "
	        + "                    ) "
	        + "               ) "
	        + "          ) "
	        + "     ) "
	        + ") "
	        + "ORDER BY tc.name ASC")
	List<TaskCategoryEntity> findAllActiveTaskCategoriesForIndex(
	        @Param("userId") Integer userId,
	        @Param("loginType") String loginType,
	        @Param("isAdmin") String isAdmin);
	
	@Query("SELECT tc FROM TaskCategoryEntity tc WHERE tc.status = 1 ORDER BY tc.name ASC")
	List<TaskCategoryEntity> findAllActiveTaskCategories();
	
	@Query("SELECT tc FROM TaskCategoryEntity tc " +
		       "WHERE tc.status = 1 " +
		       "AND tc.departmentId = (" +
		       "    SELECT ul.departmentId FROM UserLoginEntity ul " +
		       "    WHERE ul.userId = (" +
		       "        SELECT c.managerId FROM ClientEntity c WHERE c.clientId = :clientId" +
		       "    )" +
		       ") " +
		       "ORDER BY tc.name ASC")
		List<TaskCategoryEntity> findAllActiveTaskCategoriesByclientId(@Param("clientId") int clientId);

	@Transactional
	@Modifying
	@Query("UPDATE TaskCategoryEntity t " + "SET t.status = :status, " + "t.moddate = CURRENT_TIMESTAMP "
			+ "WHERE t.taskcategoryId = :taskcategoryId")
	int softDelete(@Param("status") Integer status, @Param("taskcategoryId") Integer taskcategoryId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO(" + "t.taskcategoryId, " + "t.departmentId, "
			+ "d.name, " + "t.name, " + "t.status, " + "t.userId, " + "t.regdate, " + "t.moddate) "
			+ "FROM TaskCategoryEntity t " + "LEFT JOIN DepartmentEntity d " + "ON d.departmentId = t.departmentId "
			+ "WHERE t.taskcategoryId > 0 " + "AND ( :statusIndex IS NULL OR :statusIndex = 0 OR t.status = :statusIndex) "
			+ "AND (:departmentId = 0 OR t.departmentId = :departmentId) " + "AND (:search IS NULL OR :search = '' "
			+ "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))) " + "ORDER BY t.status, t.regdate DESC,t.name")
	Page<TaskCategoryDTO> findTaskCategoryDetails(Pageable pageable, @Param("statusIndex") int statusIndex,
			@Param("search") String search, @Param("departmentId") int departmentId);

	List<TaskCategoryEntity> findByStatus(Integer status);
	
	@Query("SELECT tc FROM TaskCategoryEntity tc "
	        + "WHERE tc.status = :status "
	        + "AND EXISTS ( "
	        + "     SELECT 1 FROM RecurringEntity r WHERE r.taskCatId = tc.taskcategoryId "
	        + ") "
	        + "ORDER BY tc.name ASC")
	List<TaskCategoryEntity> findByStatusForIndex(@Param("status") Integer status);

	boolean existsByNameIgnoreCaseAndStatusNot(String name, Integer status);

	TaskCategoryEntity findByNameIgnoreCase(String name);

	@Query(value = "SELECT EXISTS (SELECT 1 FROM t_task WHERE i_taskcategoryid = :taskCategoryId AND i_status <> 3)", nativeQuery = true)
	Integer existsByTaskCategoryId(@Param("taskCategoryId") Integer taskCategoryId);
	
//	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO(" +
//	           "t.taskcategoryId, t.name) " +
//	           "FROM TaskCategoryEntity t " +
//	           "WHERE t.departmentId = :departmentId AND status =1 order by t.name ASC ")
	
	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO(" +
	           "t.taskcategoryId, t.name) " +
	           "FROM TaskCategoryEntity t " +
	           "WHERE (:departmentId = 0 OR :departmentId IS NULL OR t.departmentId = :departmentId) AND status =1 order by t.name ASC ")
	    List<TaskCategoryDTO> findCategoriesByDepartmentId(
	            @Param("departmentId") Integer departmentId);
	
	
	@Query("SELECT tc.name " +
		       "FROM TaskCategoryEntity tc " +
		       "WHERE tc.taskcategoryId IN :categoryIds " +
		       "AND tc.status = 1 " +
		       "ORDER BY tc.name")
		List<String> findNamesByIds(
		        @Param("categoryIds") List<Integer> categoryIds);
}
