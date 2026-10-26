package com.webelement.taskapp.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.GenericRecurringDTO;
import com.webelement.taskapp.entity.GenericRecurringEntity;

@Repository
public interface GenericRecurringRepository extends JpaRepository<GenericRecurringEntity, Integer> {

	Optional<GenericRecurringEntity> findById(Integer recurringId);

	@Query("SELECT new com.webelement.taskapp.dto.GenericRecurringDTO(" + "r.genericRecurringId, " + "r.forValue, "
			+ "r.date, " + "r.day, " + "r.description, " + "r.month, " + "r.status, " + "r.taskCatId, " + "tc.name, "
			+ "r.title, " + "r.type, " + "r.priority, " + "r.time) " + "FROM GenericRecurringEntity r "
			+ "LEFT JOIN TaskCategoryEntity tc " + "ON r.taskCatId = tc.taskcategoryId "
			+ "WHERE r.genericRecurringId > 0 " + "AND (:status IS NULL OR r.status = :status) "
			+ "AND (:priority IS NULL OR :priority = 0 OR r.priority = :priority) " + "AND (" + ":isAdmin = 'Y' "
			+ "OR (:loginType = 'manager' AND :isAdmin <> 'Y') " + "OR (" + ":loginType <> 'manager' "
			+ "AND FUNCTION('FIND_IN_SET', " + "CAST(r.taskCatId AS string), "
			+ "(SELECT u.taskcategoryIds FROM UserLoginEntity u " + "WHERE u.userId = :userId)" + ") > 0" + ")" + ") "
			+ "AND (:type IS NULL OR r.type = :type) " + "AND (:taskCatId IS NULL OR r.taskCatId = :taskCatId) "
			+ "AND (:forValue IS NULL OR r.forValue = :forValue)" + "AND (" + ":search IS NULL " + "OR :search = '' "
			+ "OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))" + ") "
			+ "ORDER BY r.status ASC, r.regDate DESC")
	Page<GenericRecurringDTO> findGenericRecurringDetails(Pageable pageable, @Param("status") Short status,
			@Param("search") String search, @Param("type") Short type, @Param("taskCatId") Integer taskCatId,
			@Param("forValue") Integer forValue, @Param("isAdmin") String isAdmin, @Param("loginType") String loginType,
			@Param("userId") Integer userId, @Param("priority") Integer priority);

	@Modifying
	@Query("UPDATE GenericRecurringEntity u " + "SET u.status = :status "
			+ "WHERE u.genericRecurringId = :genericRecurringId")
	int deleteGenericRecurring(@Param("status") Short status, @Param("genericRecurringId") Integer genericRecurringId);

	@Query("SELECT new com.webelement.taskapp.dto.GenericRecurringDTO(" + "r.genericRecurringId, " + "r.forValue, "
			+ "r.date, " + "r.day, " + "r.description, " + "r.month, " + "r.status, " + "r.taskCatId, " + "tc.name, "
			+ "r.title, " + "r.type, " + "r.priority, " + "r.time) " + "FROM GenericRecurringEntity r "
			+ "LEFT JOIN TaskCategoryEntity tc " + "ON r.taskCatId = tc.taskcategoryId "
			+ "WHERE r.genericRecurringId = :genericRecurringId")
	GenericRecurringDTO getGenericRecurringById(@Param("genericRecurringId") Integer genericRecurringId);


	boolean existsByTitleIgnoreCaseAndStatusInAndGenericRecurringIdNot(String title, List<Short> statuses,
            Integer genericRecurringId);
    boolean existsByTitleIgnoreCaseAndStatusIn(String title, List<Short> statuses);
	
}
