package com.webelement.taskapp.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.RecurringDTO;
import com.webelement.taskapp.entity.RecurringEntity;

@Repository
public interface RecurringRepository extends JpaRepository<RecurringEntity, Integer> {

	Optional<RecurringEntity> findById(Integer recurringId);

	@Query("SELECT new com.webelement.taskapp.dto.RecurringDTO(" + "r.recurringId, " + "r.clientId, " + "c.name, "
			+ "r.title, " + "r.description, " + "r.type, " + "r.date, " + "r.day, " + "r.month, " + "r.taskCatId, "
			+ "tc.name, " + "r.status) " + "FROM RecurringEntity r " + "LEFT JOIN ClientEntity c "
			+ "ON r.clientId = c.clientId " + "LEFT JOIN TaskCategoryEntity tc " + "ON r.taskCatId = tc.taskcategoryId "
			+ "WHERE r.recurringId > 0 " + "AND (:status IS NULL OR r.status = :status) "
			+ "AND (:managerId IS NULL OR c.managerId = :managerId) " + "AND (:title IS NULL OR :title = '' "
			+ "OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) ")
	Page<RecurringDTO> findRecurringDetails(Pageable pageable, @Param("status") Short status,
			@Param("managerId") Integer managerId, @Param("title") String title);

	@Modifying
	@Query("UPDATE RecurringEntity u " + "SET u.status = :status " + "WHERE u.recurringId = :recurringId")
	int deleteRecurring(@Param("status") Short status, @Param("recurringId") Integer recurringId);
}
