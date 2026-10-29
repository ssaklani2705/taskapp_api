package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserActiveDTO {
	private Integer userId;
	private String firstName;
	private String isHod;

	public UserActiveDTO(Integer userId, String firstName, String isHod) {
		super();
		this.userId = userId;
		this.firstName = firstName;
		this.isHod = isHod;
	}

	public UserActiveDTO(Integer userId, String firstName) {
		super();
		this.userId = userId;
		this.firstName = firstName;
	}
}