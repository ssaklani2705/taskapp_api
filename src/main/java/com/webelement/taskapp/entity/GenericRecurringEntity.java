package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import java.time.LocalTime;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_generic_recurring")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericRecurringEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_genericrecurringid")
	private Integer genericRecurringId;

	@Column(name = "i_for")
	private Integer forValue;

	@Column(name = "i_date")
	private Integer date;

	@Column(name = "i_day")
	private Short day;

	@Column(name = "s_description", columnDefinition = "LONGTEXT")
	private String description;

	@Column(name = "ts_moddate")
	private Timestamp modDate;

	@Column(name = "i_month")
	private Integer month;

	@Column(name = "ts_regdate")
	private Timestamp regDate;

	@Column(name = "i_status")
	private Short status;

	@Column(name = "i_taskcatid")
	private Integer taskCatId;

	@Column(name = "s_title", length = 500)
	private String title;

	@Column(name = "i_type")
	private Short type;

	@Column(name = "i_priority")
	private Short priority;

	@Column(name = "t_time")
	private LocalTime time;
}