package com.webelement.taskapp.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	@Column(name = "i_assignedbyid", nullable = false)
	private Integer assignedById;

	@Column(name = "i_status")
	private Short status;

	@Column(name = "ts_regdate")
	private LocalDateTime registrationDate;

	@Column(name = "ts_moddate")
	private LocalDateTime modificationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "i_taskid", referencedColumnName = "i_taskid", insertable = false, updatable = false)
	private TaskEntity taskEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "i_newuserid", referencedColumnName = "i_userid", insertable = false, updatable = false)
	private UserLoginEntity newUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "i_currentuserid", referencedColumnName = "i_userid", insertable = false, updatable = false)
	private UserLoginEntity currentUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "i_assignedbyid", referencedColumnName = "i_userid", insertable = false, updatable = false)
	private UserLoginEntity assignedBy;

}
