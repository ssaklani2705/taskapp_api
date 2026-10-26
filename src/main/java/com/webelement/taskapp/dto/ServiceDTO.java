package com.webelement.taskapp.dto;

import java.util.List;

import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {

    private Integer serviceId;
    private String name;
    private Short status;
    private Integer userId;
    
    private List<TransactionEntity> transactionHistory;

	public ServiceDTO(Integer serviceId, String name, Short status, Integer userId) {
		super();
		this.serviceId = serviceId;
		this.name = name;
		this.status = status;
		this.userId = userId;
	}
    
    
    
}