package com.webelement.taskapp.dto;


import org.springframework.data.jpa.repository.Query;

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
public class UserInfo {

	private int userId;
	private String firstName;
	private String email;
	private String mobile;
	public UserInfo(int userId, String firstName, String email, String mobile, int status, String permission,String designationName,String isHod) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.email = email;
		this.mobile = mobile;
		this.status = status;
		this.permission = permission;
		this.designationName = designationName;
		this.isHod = isHod;
	}
	private int status;
	private String permission;
	private String departmentName;
	private String designationName;
	private String taskCategoryNames;
	private String isHod;
	private String weeklyOffNames;
	public UserInfo(int userId, String firstName, String email, String mobile, int status, String permission,
			String departmentName,String designationName,String isHod,String weeklyOffNames) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.email = email;
		this.mobile = mobile;
		this.status = status;
		this.permission = permission;
		this.departmentName = departmentName;
		this.designationName = designationName;
		this.isHod =isHod;
		this.weeklyOffNames = weeklyOffNames;
	}
	
	
//	@Query("SELECT new com.webelement.taskapp.dto.UserInfo(" +
//		       "u.userId, " +
//		       "u.firstName, " +
//		       "u.email, " +
//		       "u.mobileNo, " +
//		       "u.status, " +
//		       "u.permission, " +
//		       "u.departmentIdsCsv, " +
//		       "de.name) " +
}
