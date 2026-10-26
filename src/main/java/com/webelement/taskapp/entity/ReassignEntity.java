package com.webelement.taskapp.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_reassign")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReassignEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_reassignid")
	private Integer reassignId;
	
	@Column(name = "i_taskid", nullable = false)
	private Integer taskId;

	@Column(name = "i_currentuserid", nullable = false)
	private Integer currentUserId;
	
	@Column(name = "i_newuserid", nullable = false)
	private Integer newUserId;
	@Column(name = "i_status")
	private Short status;

	@Column(name = "ts_regdate")
	private LocalDateTime registrationDate;

	@Column(name = "ts_moddate")
	private LocalDateTime modificationDate;

	
}
