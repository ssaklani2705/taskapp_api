package com.webelement.taskapp.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ChangePassWordDTO {
	private String username;
	private String oldPassword;
	private String newPassword;
	private String userId;
}
