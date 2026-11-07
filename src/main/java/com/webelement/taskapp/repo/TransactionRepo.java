package com.webelement.taskapp.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import com.webelement.taskapp.dto.TransactionActionUserProjection;
import com.webelement.taskapp.dto.TransactionHistoryProjection;
import com.webelement.taskapp.entity.TransactionEntity;
@Repository
public interface TransactionRepo extends JpaRepository<TransactionEntity, Integer> {
	
	@Query(value = "SELECT " + "tr.i_transactionid AS transactionId, " + "m.s_name AS moduleName, "
			+ "tr.i_userid AS userId, " + "u.s_firstname AS actionBy, " + "tr.ts_regdate AS updatedOn, "
			+ "tr.s_action AS action " + "FROM t_transaction tr "
			+ "LEFT JOIN t_module m ON m.i_moduleid = tr.i_moduleid "
			+ "LEFT JOIN t_userlogin u ON u.i_userid = tr.i_userid " + "WHERE (:search IS NULL OR TRIM(:search) = '' "
			+ "OR LOWER(tr.s_action) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "AND (:moduleId IS NULL OR :moduleId = 0 " + "OR tr.i_moduleid = :moduleId) "
//			+ "AND (:userId IS NULL OR :userId = 0 " + "OR tr.i_userid = :userId) "
			+ "AND (:userId IS NULL OR :userId = 0 OR (:userId = -1 AND (tr.i_userid = 0 OR tr.i_userid = -1)) OR tr.i_userid = :userId)"
			+ "AND (:fromDate IS NULL OR tr.ts_regdate >= :fromDate) "
			+ "AND (:toDate IS NULL OR tr.ts_regdate < :toDate) " + "ORDER BY tr.ts_regdate DESC", nativeQuery = true)
	Page<TransactionHistoryProjection> getTransactionHistory(@Param("search") String search,
			@Param("moduleId") Integer moduleId, @Param("userId") Integer userId,
			@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, Pageable pageable);
	
	@Query(value = "SELECT -1 AS userId, 'System' AS firstname " + "UNION ALL " + "SELECT DISTINCT "
			+ "u.i_userid AS userId, " + "u.s_firstname AS firstname " + "FROM t_transaction tr "
			+ "INNER JOIN t_userlogin u ON u.i_userid = tr.i_userid " + "WHERE u.i_status = 1 "
			+ "AND u.s_firstname IS NOT NULL " + "AND TRIM(u.s_firstname) <> '' " + "AND tr.i_userid NOT IN (0, -1) "
			+ "ORDER BY firstname ASC", nativeQuery = true)
	List<TransactionActionUserProjection> findTransactionActionUsers();
}
