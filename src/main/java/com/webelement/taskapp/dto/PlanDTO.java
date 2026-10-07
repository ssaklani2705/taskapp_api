package com.webelement.taskapp.dto;
import java.util.List;
import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PlanDTO {
	private Integer planId;

	

	private String name;
	private Integer rate;
	private String description;
	private Short status;
	private Integer userId;

	public PlanDTO(Integer planId, String name, Integer rate, String description, Short status) {
	
		this.planId = planId;
		this.name = name;
		this.rate = rate;
		this.description = description;
		this.status = status;
	}
	private List<TransactionEntity> transactionHistory;
}
