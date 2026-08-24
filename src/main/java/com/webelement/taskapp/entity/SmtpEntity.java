package com.webelement.taskapp.entity;

import java.sql.Date;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_smtp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SmtpEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_smtpid")
	private Integer smtpId;

	@Column(name = "s_host")
	private String host;

	@Column(name = "s_port")
	private String port;

	@Column(name = "s_serverusername")
	private String serverUsername;

	@Column(name = "s_serverpassword")
	private String serverPassword;

	@Column(name = "s_from")
	private String from;

	@Column(name = "s_displayname")
	private String displayName;

	@Column(name = "i_status")
	private Short status;

	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "ts_regdate")
	private Date regDate;

	@Column(name = "ts_moddate")
	private Date modDate;
}
