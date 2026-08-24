package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_permisson")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PermissionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_perid")
	private Integer perId;

	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "i_moduleid")
	private Integer moduleId;

	@Column(name = "s_view")
	private String view;

	@Column(name = "s_add")
	private String add;

	@Column(name = "s_edit")
	private String edit;

	@Column(name = "s_delete")
	private String delete;

	@Column(name = "s_approve")
	private String approve;

	@Column(name = "s_adminapprove")
	private String adminApprove;

	@Column(name = "s_doc")
	private String doc;

	@Column(name = "s_admin")
	private String admin;

	@Column(name = "i_status")
	private Integer status;

	@Column(name = "ts_regdate")
	private Timestamp regDate;

	@Column(name = "ts_moddate")
	private Timestamp modDate;
	
	@Column(name = "s_exportexcel")
	private String exportExcel;
}
