package com.webelement.taskapp.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StateDTO {
	
	
	public StateDTO(Integer stateId, String name, String code, Short status, Integer userId) {
		super();
		this.stateId = stateId;
		this.name = name;
		this.code = code;
		this.status = status;
		this.userId = userId;
	}

	private Integer stateId;
	private String name;
	private String code;
	private Short status;
	private Integer userId;

	private LocalDateTime registrationDate;

	private LocalDateTime modificationDate;
	
	 private List<TransactionEntity> transactionHistory;
	 
	

}
