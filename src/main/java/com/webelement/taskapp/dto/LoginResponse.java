package com.webelement.taskapp.dto;


import java.util.List;

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
public class LoginResponse {
	private String token;
	private int userId;
	private String username;
	private String refreshToken;
	private int sessionId;
	private String isAdmin;
	private List<ModulePermissionDTO> modules;

	public LoginResponse(String token, int userId, String username, int sessionId, String isAdmin,
			List<ModulePermissionDTO> modules) {
		this.token = token;
		this.userId = userId;
		this.username = username;
		this.sessionId = sessionId;
		this.isAdmin = isAdmin;
		this.modules = modules;
	}

}