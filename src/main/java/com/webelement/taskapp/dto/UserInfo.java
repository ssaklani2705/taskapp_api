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
	private int status;
	private String permission;

}
