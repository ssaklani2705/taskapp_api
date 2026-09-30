package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserInfo {

	private static final String[] WEEK_DAYS = {
	        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
	};

	private int userId;
	private String firstName;
	private String email;
	private String mobile;
	private int status;
	private String permission;
	private String departmentName;
	private String designationName;
	private String taskCategoryNames;
	private String isHod;
	private String weeklyOffNames;

	public UserInfo(int userId, String firstName, String email, String mobile, int status, String permission,
			String designationName, String isHod) {
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

	// Used by findBasicUserInfo query (last argument is u.weeklyOff, a Short)
	public UserInfo(int userId, String firstName, String email, String mobile, int status, String permission,
			String departmentName, String designationName, String isHod, Short weeklyOff) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.email = email;
		this.mobile = mobile;
		this.status = status;
		this.permission = permission;
		this.departmentName = departmentName;
		this.designationName = designationName;
		this.isHod = isHod;
		this.weeklyOffNames = (weeklyOff != null && weeklyOff >= 1 && weeklyOff <= 7)
				? WEEK_DAYS[weeklyOff - 1]
				: "";
	}
}