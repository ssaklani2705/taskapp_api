package com.webelement.taskapp.dto;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.webelement.taskapp.entity.TransactionEntity;

@Data
@NoArgsConstructor
public class HolidayDTO {

	private Integer holidayId;
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private Integer status;
	private Integer userId;
	private Timestamp regdate;
	private Timestamp moddate;

	private List<TransactionEntity> transactionHistory;

	/**
	 * Used by the JPQL "new" projection in
	 * HolidayRepository.findHolidayDetails().
	 *
	 * Hibernate resolves the projected column types for this
	 * query as: int, String, LocalDate, LocalDate, int, int,
	 * java.util.Date, java.util.Date — NOT the boxed/Timestamp
	 * types on the entity. The signature below must match that
	 * exactly, or Hibernate throws QuerySyntaxException at startup.
	 */
	public HolidayDTO(
			int holidayId,
			String name,
			LocalDate startDate,
			LocalDate endDate,
			int status,
			int userId,
			Date regdate,
			Date moddate) {

		this.holidayId = holidayId;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
		this.userId = userId;
		this.regdate = regdate != null ? new Timestamp(regdate.getTime()) : null;
		this.moddate = moddate != null ? new Timestamp(moddate.getTime()) : null;
	}

	// Lightweight projection used by getActiveHolidays()
	public HolidayDTO(Integer holidayId, String name) {
		this.holidayId = holidayId;
		this.name = name;
	}
}