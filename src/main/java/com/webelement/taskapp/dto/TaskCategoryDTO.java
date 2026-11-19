package com.webelement.taskapp.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webelement.taskapp.entity.TransactionEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString

public class TaskCategoryDTO {
	private Integer taskcategoryId;
	private Integer departmentId;
	private String departmentName;
	private String name;
	private Integer status;
	private Integer userId;
	private Timestamp regdate;
	private Timestamp moddate;
	
	private Integer serviceId;
	private Integer categoryType;   // 1 = Internal, 2 = External

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private String dueDateTime;

	private List<TransactionEntity> transactionHistory;
	private Integer sequence;

	public TaskCategoryDTO() {
	}
	public TaskCategoryDTO(Integer taskcategoryId, String name, Integer departmentId, String departmentName) {
	    this.taskcategoryId = taskcategoryId;
	    this.name = name;
	    this.departmentId = departmentId;
	    this.departmentName = departmentName;
	}

	/*
	 * Constructor used by JPQL
	 *
	 * Hibernate is resolving: int, int, String, int, int, Date, Date
	 */
	public TaskCategoryDTO(int taskcategoryId, int departmentId, String departmentName, String name, int status,
			int userId, Date regdate, Date moddate) {

		this.taskcategoryId = taskcategoryId;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.name = name;
		this.status = status;
		this.userId = userId;

		if (regdate != null) {
			this.regdate = new Timestamp(regdate.getTime());
		}

		if (moddate != null) {
			this.moddate = new Timestamp(moddate.getTime());
		}
	}

	/*
	 * Constructor for active dropdown
	 */
	public TaskCategoryDTO(Integer taskcategoryId, String name) {

		this.taskcategoryId = taskcategoryId;
		this.name = name;
	}

	

	/*
	 * Constructor for active dropdown with department
	 */
	public TaskCategoryDTO(Integer taskcategoryId, Integer departmentId, String name, String departmentName,
			String dueDateTime) {

		this.taskcategoryId = taskcategoryId;
		this.departmentId = departmentId;
		this.name = name;
		this.departmentName = departmentName;
		this.dueDateTime = dueDateTime;
	}

	public TaskCategoryDTO(Integer taskcategoryId, String name, Integer departmentId, String departmentName,
			String dueDateTime) {
		this.taskcategoryId = taskcategoryId;
		this.name = name;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.dueDateTime = dueDateTime;
	}
	
	public TaskCategoryDTO(Integer taskcategoryId, String name, Integer departmentId, String departmentName,
			String dueDateTime,Integer categoryType) {
		this.taskcategoryId = taskcategoryId;
		this.name = name;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.dueDateTime = dueDateTime;
		this.categoryType = categoryType;
	}
	
	public TaskCategoryDTO(
	        int taskcategoryId,
	        int departmentId,
	        String departmentName,
	        String name,
	        int status,
	        int userId,
	        Date regdate,
	        Date moddate,
	        Integer serviceId) {

	    this.taskcategoryId = taskcategoryId;
	    this.departmentId = departmentId;
	    this.departmentName = departmentName;
	    this.name = name;
	    this.status = status;
	    this.userId = userId;
	    this.serviceId = serviceId;

	    if (regdate != null) {
	        this.regdate = new Timestamp(regdate.getTime());
	    }

	    if (moddate != null) {
	        this.moddate = new Timestamp(moddate.getTime());
	    }
	}
	
	public TaskCategoryDTO(Integer taskcategoryId, Integer departmentId, String name, String departmentName,
			String dueDateTime, Integer sequence) {
		this.taskcategoryId = taskcategoryId;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.name = name;
		this.dueDateTime = dueDateTime;
		this.sequence = sequence;
	}
}
