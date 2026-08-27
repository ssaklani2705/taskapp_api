package com.webelement.taskapp.entity;

import java.sql.Timestamp;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_recurring")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_recurringid")
	private Integer recurringId;

	@Column(name = "i_clientid")
	private Integer clientId;

	@Column(name = "s_title", length = 500)
	private String title;

	@Column(name = "s_description", columnDefinition = "LONGTEXT")
	private String description;

	@Column(name = "i_type")
	private Short type;

	@Column(name = "i_date")
	private Integer date;

	@Column(name = "i_day")
	private Short day;

	@Column(name = "i_month")
	private Integer month;

	@Column(name = "i_taskcatid")
	private Integer taskCatId;

	@Column(name = "i_status")
	private Short status;

	@Column(name = "ts_regdate")
	private Timestamp regDate;

	@Column(name = "ts_moddate")
	private Timestamp modDate;

}
