package com.webelement.taskapp.dto;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserAccessLogDTO {

	private Integer logId;
	private Integer userId;
	private String ipAddress;
	private LocalDateTime loginTime;
	private LocalDateTime logoutTime;
	private Integer status;
	private String userName;

}

