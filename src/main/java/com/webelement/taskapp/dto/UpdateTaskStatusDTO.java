package com.webelement.taskapp.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class UpdateTaskStatusDTO {
	private Integer taskId;

	private String description;
	private String selectedTaskStatusId;
	private MultipartFile fileName1;

	private MultipartFile fileName2;
	private Integer userId;
}
