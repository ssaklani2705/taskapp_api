package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_company")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_companyid", nullable = false)
	private Integer companyId;

	@Column(name = "s_companyname", length = 100)
	private String companyName;

	@Column(name = "s_companysiteoffice", columnDefinition = "LONGTEXT")
	private String companySiteOffice;
	@Column(name = "i_status")
	private Short status;

	@Column(name = "ts_regdate")
	private LocalDateTime registrationDate;

	@Column(name = "ts_moddate")
	private LocalDateTime modificationDate;
}
