package com.webelement.taskapp.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_maillog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailLogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_maillogid")
	private Integer mailLogId;

	@Column(name = "i_type")
	private Integer type;

	@Column(name = "s_name")
	private String name;

	@Column(name = "s_to")
	private String to;

	@Column(name = "s_cc")
	private String cc;

	@Column(name = "s_bcc")
	private String bcc;

	@Column(name = "s_from")
	private String from;

	@Column(name = "s_subject")
	private String subject;

	@Column(name = "s_filename")
	private String filename;

	@Column(name = "i_moduleid")
	private Integer moduleId;

	@Column(name = "ts_regdate")
	private LocalDateTime regDate;

	@Column(name = "ts_moddate")
	private LocalDateTime modDate;

	@Column(name = "s_ipaddress")
	private String ipAddress;

	@Column(name = "s_localip")
	private String localIp;

	@Column(name = "i_status")
	private Integer status;
}
