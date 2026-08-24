package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_module")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ModuleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_moduleid")
	private Integer moduleId;

	@Column(name = "s_name")
	private String name;

	@Column(name = "i_type")
	private Short type;

	@Column(name = "i_sequence")
	private Short sequence;

	@Column(name = "i_userid")
	private Integer userId;

	@Column(name = "i_status")
	private Integer status;

	@Column(name = "ts_regdate")
	private Timestamp regDate;

	@Column(name = "ts_moddate")
	private Timestamp modDate;
}
