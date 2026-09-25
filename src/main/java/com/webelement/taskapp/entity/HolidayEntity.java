package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
@Entity
@Table(name = "t_holiday")
public class HolidayEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_holidayid")
	private Integer holidayId;

	@Column(name = "s_name")
	private String name;

	@Column(name = "d_startdate")
	private LocalDate startDate;

	@Column(name = "d_enddate")
	private LocalDate endDate;

	@Column(name = "i_status")
	private Integer status;

	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "ts_regdate")
	private Timestamp regdate;

	@Column(name = "ts_moddate")
	private Timestamp moddate;

}