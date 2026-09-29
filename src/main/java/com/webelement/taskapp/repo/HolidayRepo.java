package com.webelement.taskapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webelement.taskapp.dto.HolidayDTO;
import com.webelement.taskapp.entity.HolidayEntity;

@Repository
public interface HolidayRepo extends JpaRepository<HolidayEntity, Integer> {

	@Transactional
	@Modifying
	@Query("UPDATE HolidayEntity h " + "SET h.status = :status, " + "h.moddate = CURRENT_TIMESTAMP "
			+ "WHERE h.holidayId = :holidayId")
	int softDelete(@Param("status") Integer status, @Param("holidayId") Integer holidayId);

	@Query("SELECT new com.webelement.taskapp.dto.HolidayDTO(" + "h.holidayId, " + "h.name, " + "h.startDate, "
			+ "h.endDate, " + "h.status, " + "COALESCE(h.userId, 0), " + "h.regdate, " + "h.moddate) "
			+ "FROM HolidayEntity h " + "WHERE h.holidayId > 0 " + "AND (:statusIndex = 0 OR h.status = :statusIndex) "
			+ "AND (:search IS NULL OR :search = '' " + "OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "AND (:fromDate IS NULL OR h.startDate >= :fromDate) AND (:toDate IS NULL OR h.startDate <= :toDate) "
			+ "ORDER BY h.status, h.startDate desc")
	Page<HolidayDTO> findHolidayDetails(Pageable pageable, @Param("statusIndex") int statusIndex,
			@Param("search") String search, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	List<HolidayEntity> findByStatus(Integer status);

	boolean existsByNameIgnoreCase(String name);

	HolidayEntity findByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndStatusNot(String name, Integer status);

	boolean existsByNameIgnoreCaseAndHolidayIdNotAndStatusNot(String name, Integer holidayId, Integer status);

	boolean existsByStartDateAndStatusNot(LocalDate startDate, Integer status);

	boolean existsByStartDateAndHolidayIdNotAndStatusNot(LocalDate startDate, Integer holidayId, Integer status);

	Optional<HolidayEntity> findByNameIgnoreCaseAndStatusNot(String name, Integer status);

}
