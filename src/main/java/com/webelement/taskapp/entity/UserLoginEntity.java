package com.webelement.taskapp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webelement.taskapp.dto.ModulePermissionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "t_userlogin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserLoginEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "s_firstname")
	private String firstName;

	@Column(name = "s_mobileno")
	private String mobileNo;

	@Column(name = "s_email")
	private String email;

	@Column(name = "s_password")
	private String password;

	@Column(name = "s_telephone")
	private String telephone;

	@Column(name = "d_expirydate")
	@Temporal(TemporalType.DATE)
	private Date expiryDate;

	@Column(name = "s_permission")
	private String permission;

	@Column(name = "i_status")
	private int status;

	@Column(name = "ts_regdate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Timestamp regDate;

	@Column(name = "ts_moddate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Timestamp modDate;

	@Column(name = "ts_sentlinkdate")
	private Timestamp sentLinkDate;

	@Column(name = "i_qcflag")
	private Short qcFlag;

	@Column(name = "s_pcb")
	private String pcb;

	@Transient
	private String pcbName;

	@Transient
	private int createdBy;

	@Transient
	private List<ModulePermissionDTO> module;

	@Transient
	private List<TransactionEntity> transactionhistory;
	
	@Column(name = "i_departmentid")
	private Integer departmentId;

	public UserLoginEntity(Integer userId, String firstName, String mobileNo, String email, String password,
			String telephone, Date expiryDate, String permission, int status, Timestamp regDate, Timestamp modDate,
			Timestamp sentLinkDate, Short qcFlag, String pcb, String pcbName, int createdBy) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.mobileNo = mobileNo;
		this.email = email;
		this.password = password;
		this.telephone = telephone;
		this.expiryDate = expiryDate;
		this.permission = permission;
		this.status = status;
		this.regDate = regDate;
		this.modDate = modDate;
		this.sentLinkDate = sentLinkDate;
		this.qcFlag = qcFlag;
		this.pcb = pcb;
		this.pcbName = pcbName;
		this.createdBy = createdBy;
	}

}
