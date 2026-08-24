package com.webelement.taskapp.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.webelement.taskapp.entity.TransactionEntity;

public class TaskCategoryDTO {

    private Integer taskcategoryId;
    private Integer departmentId;
    private String departmentName;
    private String name;
    private Integer status;
    private Integer userId;
    private Timestamp regdate;
    private Timestamp moddate;

    private List<TransactionEntity> transactionHistory;

    public TaskCategoryDTO() {
    }

    /*
     * Constructor used by JPQL
     *
     * Hibernate is resolving:
     * int, int, String, int, int, Date, Date
     */
    public TaskCategoryDTO(
            int taskcategoryId,
            int departmentId,
            String departmentName,
            String name,
            int status,
            int userId,
            Date regdate,
            Date moddate) {

        this.taskcategoryId = taskcategoryId;
        this.departmentId = departmentId;
        this.departmentName= departmentName; 
        this.name = name;
        this.status = status;
        this.userId = userId;

        if (regdate != null) {
            this.regdate =
                    new Timestamp(regdate.getTime());
        }

        if (moddate != null) {
            this.moddate =
                    new Timestamp(moddate.getTime());
        }
    }

    /*
     * Constructor for active dropdown
     */
    public TaskCategoryDTO(
            Integer taskcategoryId,
            String name) {

        this.taskcategoryId = taskcategoryId;
        this.name = name;
    }

    public Integer getTaskcategoryId() {
        return taskcategoryId;
    }

    public void setTaskcategoryId(
            Integer taskcategoryId) {

        this.taskcategoryId = taskcategoryId;
    }
    
    

    public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(
            Integer departmentId) {

        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name) {

        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(
            Integer status) {

        this.status = status;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(
            Integer userId) {

        this.userId = userId;
    }

    public Timestamp getRegdate() {
        return regdate;
    }

    public void setRegdate(
            Timestamp regdate) {

        this.regdate = regdate;
    }

    public Timestamp getModdate() {
        return moddate;
    }

    public void setModdate(
            Timestamp moddate) {

        this.moddate = moddate;
    }

    public List<TransactionEntity> getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(
            List<TransactionEntity> transactionHistory) {

        this.transactionHistory =
                transactionHistory;
    }
}
