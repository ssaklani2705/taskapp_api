package com.webelement.taskapp.dto;

import java.util.Date;
import java.util.List;

import com.webelement.taskapp.entity.TransactionEntity;

public class DepartmentDTO {

	private Integer departmentId;
	private String name;
	private Integer sequence;
	private Integer status;
	private Integer userId;
	private Date regdate;
	private Date moddate;

	private List<TransactionEntity> transactionHistory;

	// =========================================================
	// DEFAULT CONSTRUCTOR
	// =========================================================

	public DepartmentDTO() {
	}

	// =========================================================
	// CONSTRUCTOR USED BY JPQL QUERY
	// =========================================================

	public DepartmentDTO(
			int departmentId,
			String name,
			int sequence,
			int status,
			int userId,
			Date regdate,
			Date moddate) {

		this.departmentId = departmentId;
		this.name = name;
		this.sequence = sequence;
		this.status = status;
		this.userId = userId;
		this.regdate = regdate;
		this.moddate = moddate;
	}

	// =========================================================
	// CONSTRUCTOR FOR ACTIVE DEPARTMENT
	// =========================================================

	public DepartmentDTO(
			Integer departmentId,
			String name) {

		this.departmentId = departmentId;
		this.name = name;
	}

	// =========================================================
	// GETTERS / SETTERS
	// =========================================================

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Date getRegdate() {
		return regdate;
	}

	public void setRegdate(Date regdate) {
		this.regdate = regdate;
	}

	public Date getModdate() {
		return moddate;
	}

	public void setModdate(Date moddate) {
		this.moddate = moddate;
	}

	public List<TransactionEntity> getTransactionHistory() {
		return transactionHistory;
	}

	public void setTransactionHistory(
			List<TransactionEntity> transactionHistory) {

		this.transactionHistory = transactionHistory;
	}
}
