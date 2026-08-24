package com.webelement.taskapp.dto;

import java.time.LocalDateTime;

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
public class MailLogDTO {

	private Integer mailLogId;
	private Integer type;
	private String name;
	private String to;
	private String cc;
	private String bcc;
	private String from;
	private String subject;
	private String filename;
	private Integer moduleId;
	private LocalDateTime regDate;
	private LocalDateTime modDate;
	private String ipAddress;
	private String localIp;
	private Integer status;

}