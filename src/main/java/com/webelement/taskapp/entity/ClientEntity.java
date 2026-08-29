package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.*;

@Entity
@Table(name = "t_client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_clientid")
	private Integer clientId;

	@Column(name = "s_name")
	private String name;

	@Column(name = "s_code")
	private String code;

	@Column(name = "s_pan")
	private String pan;

	@Column(name = "i_status")
	private Short status;

	@Column(name = "i_gstflag")
	private Short gstFlag;

	@Column(name = "s_gstno")
	private String gstNo;

	@Column(name = "i_stateid")
	private Integer stateId;

	@Column(name = "s_addressline1")
	private String addressLine1;

	@Column(name = "s_addressline2")
	private String addressLine2;

	@Column(name = "s_city")
	private String city;

	@Column(name = "s_pincode")
	private String pincode;

	@Column(name = "s_contactname")
	private String contactName;

	@Column(name = "s_contactemail")
	private String contactEmail;

	@Column(name = "s_emails", columnDefinition = "LONGTEXT")
	private String emails;

	@Column(name = "d_startdate")
	private LocalDate startDate;

	@Column(name = "d_monthlycharge")
	private Double monthlyCharge;

	@Column(name = "d_outstanding")
	private Double outstanding;

	@Column(name = "s_name1")
	private String name1;

	@Column(name = "s_emailid1")
	private String emailId1;

	@Column(name = "s_name2")
	private String name2;

	@Column(name = "s_emailid2")
	private String emailId2;

	@Column(name = "s_name3")
	private String name3;

	@Column(name = "s_emailid3")
	private String emailId3;

	@Column(name = "i_managerid")
	private Integer managerId;

	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "ts_regdate")
	private Timestamp regdate;

	@Column(name = "ts_moddate")
	private Timestamp moddate;

	@Column(name = "i_taxflag")
	private Short taxFlag;

	@Column(name = "s_location")
	private String location;
	
	@Column(name = "i_planid")
	private Integer planId;

	@Transient
	private Integer excelRowNumber;

	@Transient
	private String stateNameForExcel;

	@Transient
	private String managerNameForExcel;
	
	@Transient
	private String planNameForExcel;
}
