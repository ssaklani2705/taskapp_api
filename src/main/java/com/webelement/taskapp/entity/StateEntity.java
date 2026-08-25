package com.webelement.taskapp.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_stateid", nullable = false)
	private Integer stateId;

	@Column(name = "s_name", length = 200)
	private String name;

	@Column(name = "s_statecode", length = 2)
	private String stateCode;

	@Column(name = "i_status")
	private Short status;

	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "ts_regdate")
	private String regDate;

	@Column(name = "ts_moddate")
	private String modDate;
	
	@Transient
	private List<TransactionEntity> transactionhistory;
}
