package com.webelement.taskapp.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.TaskCategoryDTO;
import com.webelement.taskapp.entity.TaskCategoryEntity;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategoryEntity, Integer> {

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO("
			+ "tc.taskcategoryId, tc.departmentId, tc.name, d.name, tc.duedatetime) " + "FROM TaskCategoryEntity tc "
			+ "LEFT JOIN DepartmentEntity d ON d.departmentId = tc.departmentId " + "WHERE " + " EXISTS ( "
			+ "     SELECT 1 FROM TaskEntity t " + "     LEFT JOIN ClientEntity c ON c.clientId = t.clientId "
			+ "     WHERE t.taskCategoryId = tc.taskcategoryId " + "     AND ( "
			// ---- HOD: tasks of own task categories ----
			+ "          ( " + "               :isHod = 'Y' " + "               AND EXISTS ( "
			+ "                    SELECT 1 FROM UserLoginEntity h " + "                    WHERE h.userId = :userId "
			+ "                    AND CONCAT(',', REPLACE(h.taskcategoryIds, ' ', ''), ',') "
			+ "                        LIKE CONCAT('%,', t.taskCategoryId, ',%') " + "               ) "
			+ "          ) "
			// ---- MANAGER ----
			+ "          OR ( " + "               :loginType = 'manager' " + "               AND ( "
			+ "                    t.assignedTo = :userId " + "                    OR t.addedBy = :userId "
			+ "                    OR c.managerId = :userId " + "               ) " + "          ) "
			// ---- ADMIN / OTHERS ----
			+ "          OR ( " + "               :loginType <> 'manager' " + "               AND ( "
			+ "                    :isAdmin = 'Y' " + "                    OR t.assignedTo = :userId "
			+ "                    OR t.addedBy = :userId " + "                    OR ( "
			+ "                         :isAdmin <> 'Y' "
			+ "                         AND (t.assignedTo = 0 OR t.assignedTo IS NULL) "
			+ "                         AND EXISTS ( "
			+ "                              SELECT 1 FROM UserLoginEntity ul "
			+ "                              WHERE ul.userId = :userId "
			+ "                              AND CONCAT(',', ul.taskcategoryIds, ',') "
			+ "                                  LIKE CONCAT('%,', t.taskCategoryId, ',%') "
			+ "                         ) " + "                    ) " + "               ) " + "          ) "
			+ "     ) " + ") " + "ORDER BY d.name ASC, tc.name ASC")
	List<TaskCategoryDTO> findAllActiveTaskCategoriesForIndex(@Param("userId") Integer userId,
			@Param("loginType") String loginType, @Param("isAdmin") String isAdmin, @Param("isHod") String isHod);

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO("
			+ "tc.taskcategoryId, tc.departmentId, tc.name, d.name, tc.duedatetime) " + "FROM TaskCategoryEntity tc "
			+ "LEFT JOIN DepartmentEntity d ON d.departmentId = tc.departmentId " + "WHERE tc.status = 1 "
			+ "ORDER BY d.name ASC, tc.name ASC")
	List<TaskCategoryDTO> findAllActiveTaskCategories();

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO("
			+ "tc.taskcategoryId, tc.departmentId, tc.name, d.name, tc.duedatetime) " + "FROM TaskCategoryEntity tc "
			+ "LEFT JOIN DepartmentEntity d ON d.departmentId = tc.departmentId " + "WHERE tc.status = 1 "
			+ "AND (:excludeDeptFlag = false OR tc.departmentId IS NULL OR tc.departmentId NOT IN (:departmentIds)) "
			+ "AND (:filterServiceFlag = false OR tc.serviceId IN (:serviceIds)) " + "ORDER BY d.name ASC, tc.name ASC")
	List<TaskCategoryDTO> findActiveTaskCategoriesFiltered(@Param("excludeDeptFlag") boolean excludeDeptFlag,
			@Param("departmentIds") List<Integer> departmentIds, @Param("filterServiceFlag") boolean filterServiceFlag,
			@Param("serviceIds") List<Integer> serviceIds);

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO("
			+ "tc.taskcategoryId, tc.departmentId, tc.name, d.name, tc.duedatetime) " + "FROM TaskCategoryEntity tc "
			+ "LEFT JOIN DepartmentEntity d ON d.departmentId = tc.departmentId "
			+ "WHERE tc.status = 1 AND (tc.departmentId IS NULL OR tc.departmentId NOT IN (:departmentIds)) "
			+ "ORDER BY d.name ASC, tc.name ASC")
	List<TaskCategoryDTO> findAllActiveTaskCategoriesExcludingDepartments(
			@Param("departmentIds") List<Integer> departmentIds);

	@Query("SELECT tc FROM TaskCategoryEntity tc " + "WHERE tc.status = 1 " + "AND EXISTS ("
			+ "    SELECT 1 FROM UserLoginEntity ul " + "    WHERE ul.userId = ("
			+ "        SELECT c.managerId FROM ClientEntity c WHERE c.clientId = :clientId" + "    ) "
			+ "    AND CONCAT(',', ul.departmentIdsCsv, ',') " + "        LIKE CONCAT('%,', tc.departmentId, ',%')"
			+ ") " + "ORDER BY tc.name ASC")
	List<TaskCategoryEntity> findAllActiveTaskCategoriesByclientId(@Param("clientId") int clientId);

	@Transactional
	@Modifying
	@Query("UPDATE TaskCategoryEntity t " + "SET t.status = :status, " + "t.moddate = CURRENT_TIMESTAMP "
			+ "WHERE t.taskcategoryId = :taskcategoryId")
	int softDelete(@Param("status") Integer status, @Param("taskcategoryId") Integer taskcategoryId);

//	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO(" + "t.taskcategoryId, " + "t.departmentId, "
//			+ "d.name, " + "t.name, " + "t.status, " + "t.userId, " + "t.regdate, " + "t.moddate) "
//			+ "FROM TaskCategoryEntity t " + "LEFT JOIN DepartmentEntity d " + "ON d.departmentId = t.departmentId "
//			+ "WHERE t.taskcategoryId > 0 "
//			+ "AND ( :statusIndex IS NULL OR :statusIndex = 0 OR t.status = :statusIndex) "
//			+ "AND (:departmentId = 0 OR t.departmentId = :departmentId) " + "AND (:search IS NULL OR :search = '' "
//			+ "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))) " + "ORDER BY t.status, t.regdate DESC,t.name")
//	Page<TaskCategoryDTO> findTaskCategoryDetails(Pageable pageable, @Param("statusIndex") int statusIndex,
//			@Param("search") String search, @Param("departmentId") int departmentId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO(" + "t.taskcategoryId, " + "t.departmentId, "
			+ "d.name, " + "t.name, " + "t.status, " + "t.userId, " + "t.regdate, " + "t.moddate, " + "t.serviceId) "
			+ "FROM TaskCategoryEntity t " + "LEFT JOIN DepartmentEntity d ON d.departmentId = t.departmentId "
			+ "WHERE t.taskcategoryId > 0 "
			+ "AND (:statusIndex IS NULL OR :statusIndex = 0 OR t.status = :statusIndex) "
			+ "AND (:departmentId = 0 OR t.departmentId = :departmentId) " + "AND (:search IS NULL OR :search = '' "
			+ "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))) " + "ORDER BY t.status, t.regdate DESC, t.name")
	Page<TaskCategoryDTO> findTaskCategoryDetails(Pageable pageable, @Param("statusIndex") int statusIndex,
			@Param("search") String search, @Param("departmentId") int departmentId);

	@EntityGraph(attributePaths = { "department" })
	@Query("SELECT tc FROM TaskCategoryEntity tc " + "WHERE tc.status = :status "
			+ "AND LOWER(TRIM(tc.name)) <> 'admin task'")
	List<TaskCategoryEntity> findByStatus(@Param("status") Integer status);

	@EntityGraph(attributePaths = { "department" })
	@Query("SELECT tc FROM TaskCategoryEntity tc " + "WHERE tc.status = 1 "
			+ "AND LOWER(TRIM(tc.name)) <> 'admin task' " + "AND EXISTS (" + "   SELECT t.taskId FROM TaskEntity t "
			+ "   WHERE t.taskCategoryId = tc.taskcategoryId " + "   AND EXISTS ("
			+ "       SELECT r.reassignId FROM ReassignEntity r " + "       WHERE r.taskId = t.taskId" + "   )" + ")")
	List<TaskCategoryEntity> findByTaskCategory();

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO("
			+ "tc.taskcategoryId, tc.name, tc.departmentId, d.name, tc.duedatetime) " + "FROM TaskCategoryEntity tc "
			+ "LEFT JOIN DepartmentEntity d ON d.departmentId = tc.departmentId " + "WHERE tc.status = :status "
			+ "AND LOWER(TRIM(tc.name)) <> 'admin task'")
	List<TaskCategoryDTO> findByStatusWithDepartment(@Param("status") Integer status);

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO("
			+ "tc.taskcategoryId, tc.name, tc.departmentId, d.name, tc.duedatetime) " + "FROM TaskCategoryEntity tc "
			+ "LEFT JOIN DepartmentEntity d ON d.departmentId = tc.departmentId " + "WHERE tc.status = :status "
			+ "AND EXISTS ( " + "     SELECT 1 FROM RecurringEntity r WHERE r.taskCatId = tc.taskcategoryId " + ") "
			+ "ORDER BY tc.name ASC")
	List<TaskCategoryDTO> findByStatusForIndex(@Param("status") Integer status);

	boolean existsByNameIgnoreCaseAndStatusNot(String name, Integer status);

	TaskCategoryEntity findByNameIgnoreCase(String name);

	@Query(value = "SELECT EXISTS (SELECT 1 FROM t_task WHERE i_taskcategoryid = :taskCategoryId AND i_status <> 3)", nativeQuery = true)
	Integer existsByTaskCategoryId(@Param("taskCategoryId") Integer taskCategoryId);

	@Query("SELECT new com.webelement.taskapp.dto.TaskCategoryDTO(" + "t.taskcategoryId, t.name) "
			+ "FROM TaskCategoryEntity t "
			+ "WHERE (:departmentId = 0 OR :departmentId IS NULL OR t.departmentId = :departmentId) "
			+ "AND t.status = 1 ORDER BY t.name ASC")
	List<TaskCategoryDTO> findCategoriesByDepartmentId(@Param("departmentId") Integer departmentId);

	@Query("SELECT DISTINCT new com.webelement.taskapp.dto.TaskCategoryDTO(" + "t.taskcategoryId, t.name) "
			+ "FROM TaskCategoryEntity t " + "WHERE t.departmentId IN (:departmentIds) AND t.status = 1 "
			+ "ORDER BY t.name ASC")
	List<TaskCategoryDTO> findCategoriesByDepartmentIds(@Param("departmentIds") List<Integer> departmentIds);

	@Query("SELECT tc.name " + "FROM TaskCategoryEntity tc " + "WHERE tc.taskcategoryId IN :categoryIds "
			+ "AND tc.status = 1 " + "ORDER BY tc.name")
	List<String> findNamesByIds(@Param("categoryIds") List<Integer> categoryIds);

	@Query("SELECT t.duedatetime FROM TaskCategoryEntity t " + "WHERE t.taskcategoryId = :categoryId")
	String findDueTimeByCategoryId(@Param("categoryId") Integer categoryId);
}