package com.webelement.taskapp.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ModulePermissionDTO {

	@JsonIgnore
	private Integer userId;
	private Integer moduleId;
	private String name;
	private Integer type;
	private String addPer;
	private String editPer;
	private String deletePer;
	private String approvePer;
	private String adminApprovePer;
	private String viewPer;
	private String exportExcel;

}
