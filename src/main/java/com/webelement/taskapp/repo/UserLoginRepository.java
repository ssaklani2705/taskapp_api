package com.webelement.taskapp.repo;


import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.UserActiveDTO;
import com.webelement.taskapp.dto.UserInfo;
import com.webelement.taskapp.entity.UserLoginEntity;


@Repository
public interface UserLoginRepository extends JpaRepository<UserLoginEntity, Integer> {
	
//	@Query("SELECT u FROM UserLoginEntity u WHERE u.status = 1 ORDER BY u.firstName ASC")
//	List<UserLoginEntity> findAllActiveUsers();


	boolean existsByDepartmentId(Integer departmentId);
	
	boolean existsByDesignationId(Integer designationId);
	
//	@Query(value = "SELECT i_userid, s_firstname, s_email, s_mobileno, CURRENT_DATE() <= d_expirydate as d_expirydate,s_permission FROM t_userlogin WHERE i_status =1 AND s_email =:email AND s_password =:password ", nativeQuery = true)
//	List<Map<String, Object>> findActiveLogin(@Param("email") String email, @Param("password") String password);

	@Query(value = "SELECT i_userid, s_firstname, s_email, s_mobileno, CURRENT_DATE() <= d_expirydate as d_expirydate,s_permission FROM t_userlogin WHERE i_status =1 AND s_email =:email AND s_password =:password AND ( (:logintype = 'manager' AND i_departmentid = 1) OR (:logintype <> 'manager' AND i_departmentid <> 1) ) ", nativeQuery = true)
	List<Map<String, Object>> findActiveLogin(@Param("email") String email, @Param("password") String password, @Param("logintype") String logintype);
	
	@Query(value = "SELECT s_email, s_password FROM t_userlogin WHERE i_status = 1 AND s_email = :email", nativeQuery = true)
	List<Map<String, Object>> findLoginByEmail(@Param("email") String email);

	@Query("SELECT u FROM UserLoginEntity u WHERE u.userId = :userId")
	UserLoginEntity getUserById(@Param("userId") int userId);

	@Query("SELECT new com.webelement.taskapp.dto.UserInfo(" +
		       "u.userId, " +
		       "u.firstName, " +
		       "u.email, " +
		       "u.mobileNo, " +
		       "u.status, " +
		       "u.permission, " +
		       "d.name,de.name) " +
		       "FROM UserLoginEntity u " +
		       "LEFT JOIN DepartmentEntity d ON d.departmentId = u.departmentId " +
		       "LEFT JOIN DesignationEntity de ON de.designationId = u.designationId " +
		       "WHERE u.userId > 0  " +
//		       AND d.departmentId != 1
		       "AND (:statusIndex = 0 OR u.status = :statusIndex) " +
		       "AND (:departmentId = 0 OR u.departmentId = :departmentId) " +
		       "AND (:designationId = 0 OR u.designationId = :designationId) " +
		       "AND (:search IS NULL OR :search = '' " +
		       "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		       "OR LOWER(u.mobileNo) LIKE LOWER(CONCAT('%', :search, '%')) " +
		       "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
		       "OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
		       "ORDER BY u.status, u.firstName")
		Page<UserInfo> findBasicUserInfo(Pageable pageable,@Param("statusIndex") int statusIndex,@Param("search") String search,
				int departmentId,int designationId);

	@Query("SELECT u.userId FROM UserLoginEntity u WHERE u.email = :email")
	Optional<Integer> findUserIdByEmail(@Param("email") String email); // fetch only userId

	@Query("SELECT u FROM UserLoginEntity u WHERE u.email = :email AND u.status NOT IN (2, 3)")
	Optional<UserLoginEntity> findByEmailExcludeStatuses(@Param("email") String email);

	@Query(value = "SELECT DATE_FORMAT(t.ts_regdate, '%d-%m-%Y %h:%i %p') AS entryDate, REPLACE(CONCAT_WS(' ', u.s_firstname), '  ', ' ') AS name, t.s_action AS action, t.i_userid AS userId, t.s_flag AS flag FROM t_transaction t LEFT JOIN t_userlogin u ON u.i_userid = t.i_userid WHERE t.i_moduleid = :moduleId AND t.s_action NOT LIKE '%Log%' AND (:recordId IS NULL OR t.i_recordid = :recordId)  order by t.ts_regdate desc", nativeQuery = true)
	List<Object[]> getTransactionLogs(@Param("moduleId") int moduleId, @Param("recordId") Integer recordId);

	@Transactional
	@Modifying
	@Query("UPDATE UserLoginEntity u " + "SET u.password = :password, " + "    u.sentLinkDate = :linksentdate, "
			+ "    u.modDate = CURRENT_TIMESTAMP " + "WHERE u.email = :username")
	int updateForgotPasswordLink(@Param("username") String username, @Param("password") String password,@Param("linksentdate") Timestamp linksentdate);

	@Query("SELECT u.userId FROM UserLoginEntity u " + "WHERE u.email = :email " + "AND u.userId = :userId "
			+ "AND u.status = 1 " + "AND u.sentLinkDate > CURRENT_TIMESTAMP")
	Optional<Integer> checkURLValidity(@Param("email") String email, @Param("userId") Integer userId);

	@Query("SELECT u.userId FROM UserLoginEntity u WHERE u.email = :email AND u.status = :status")
	Optional<Integer> findUserIdByEmailAndStatus(@Param("email") String email, @Param("status") int status);

	@Transactional
	@Modifying
	@Query("UPDATE UserLoginEntity u " + "SET u.password = :password, " + "    u.sentLinkDate = :linksentdate, "
			+ "    u.modDate = CURRENT_TIMESTAMP " + "WHERE u.userId = :userId")
	int updateForgotPassword(@Param("userId") Integer userId, @Param("password") String password,
			@Param("linksentdate") Timestamp linksentdate);

	@Query(value = "SELECT M.i_moduleid, M.s_name, M.i_type, P1.s_add, P1.s_edit, P1.s_delete, P1.s_approve, P1.s_adminapprove, P1.s_view,P1.s_exportexcel  FROM t_module AS M \r\n"
			+ "LEFT JOIN (SELECT P.i_moduleid, P.s_view, P.s_add, P.s_edit, P.s_delete, P.s_approve, P.s_adminapprove, P.i_userid,P.s_exportexcel FROM t_permisson AS P \r\n"
			+ "WHERE i_userid = :userid ) AS P1 ON (M.i_moduleid = P1.i_moduleid) WHERE M.i_status = 1 ORDER BY M.i_type, M.i_sequence, M.s_name ", nativeQuery = true)
	List<Map<String, Object>> getModuleListByName(@Param("userid") int userid);

	@Query(value = "SELECT P1.i_userid AS userId, M.i_moduleid AS moduleId, M.s_name AS name, M.i_type AS type, P1.s_add AS addPer, P1.s_edit AS editPer, P1.s_delete AS deletePer, P1.s_approve AS approvePer, P1.s_adminapprove AS adminApprovePer, P1.s_view AS viewPer,P1.s_exportexcel AS exportExcel FROM t_module M LEFT JOIN (SELECT P.i_moduleid, P.i_userid, P.s_view, P.s_add, P.s_edit, P.s_delete, P.s_approve, P.s_adminapprove,P.s_exportexcel FROM t_permisson P WHERE P.i_userid = :userId) P1 ON M.i_moduleid = P1.i_moduleid WHERE M.i_status = 1 and  M.i_moduleid!=-1 ORDER BY M.i_type, M.i_sequence, M.s_name", nativeQuery = true)
	List<Object[]> getModulePermissions(@Param("userId") int userId);

	@Query(value = "SELECT  COUNT(*) as count FROM t_userlogin WHERE s_email = :email AND i_status IN (1,2) AND  (:userId = 0 OR i_userid != :userId)", nativeQuery = true)
	int checkDuplicacy(@Param("userId") int userId, @Param("email") String email);

	@Transactional
	@Modifying
	@Query("UPDATE UserLoginEntity u " + "SET u.status = :status " + "WHERE u.userId = :userId")
	int deleteUser(@Param("status") int status, @Param("userId") int userId);

	// Fetch only active users (status = 1)
	@Query("SELECT new com.webelement.taskapp.dto.UserActiveDTO(u.userId, u.firstName) "
			+ "FROM UserLoginEntity u WHERE u.status = 1 order by u.firstName asc")
	List<UserActiveDTO> findActiveUsers();
	
	@Query("SELECT new com.webelement.taskapp.dto.UserActiveDTO(u.userId, u.firstName) "
			+ "FROM UserLoginEntity u WHERE u.status = 1 and u.departmentId = 1 order by u.firstName asc")
	List<UserActiveDTO> findActiveManager();

//	Optional<UserLoginEntity> findByEmail(String email);
	Optional<UserLoginEntity> findByEmailAndStatus(String email,int status);

	@Query("SELECT u.userId FROM UserLoginEntity u WHERE LOWER(u.firstName) = LOWER(:name)")
	Integer findIdByName(@Param("name") String name);

}
