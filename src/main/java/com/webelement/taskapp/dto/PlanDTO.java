package com.webelement.taskapp.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
	public PlanDTO(Integer planId, String name, Integer rate, Short status) {
		super();
		this.planId = planId;
		this.name = name;
		this.rate = rate;
		this.status = status;
	}
	private List<TransactionEntity> transactionHistory;
}
