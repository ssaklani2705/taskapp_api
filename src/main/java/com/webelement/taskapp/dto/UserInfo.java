package com.webelement.taskapp.dto;


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
	public UserInfo(int userId, String firstName, String email, String mobile, int status, String permission,String designationName) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.email = email;
		this.mobile = mobile;
		this.status = status;
		this.permission = permission;
		this.designationName = designationName;
	}
	private int status;
	private String permission;
	private String departmentName;
	private String designationName;
	private String taskCategoryNames;
	public UserInfo(int userId, String firstName, String email, String mobile, int status, String permission,
			String departmentName, String designationName) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.email = email;
		this.mobile = mobile;
		this.status = status;
		this.permission = permission;
		this.departmentName = departmentName;
		this.designationName = designationName;
	}
	
	

}
