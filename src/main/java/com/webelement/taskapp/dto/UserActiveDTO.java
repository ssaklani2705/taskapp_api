package com.webelement.taskapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserActiveDTO {
	private Integer userId;
	private String firstName;
}