package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_taskcategory")
public class TaskCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_taskcategoryid")
    private Integer taskcategoryId;

    @Column(name = "i_departmentid")
    private Integer departmentId;

    @Column(name = "s_name")
    private String name;

    @Column(name = "i_status")
    private Integer status;

    @Column(name = "i_userid")
    private Integer userId;

    @Column(name = "ts_regdate")
    private Timestamp regdate;

    @Column(name = "ts_moddate")
    private Timestamp moddate;

    public TaskCategoryEntity() {
    }

    public Integer getTaskcategoryId() {
        return taskcategoryId;
    }

    public void setTaskcategoryId(Integer taskcategoryId) {
        this.taskcategoryId = taskcategoryId;
    }

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

    public Timestamp getRegdate() {
        return regdate;
    }

    public void setRegdate(Timestamp regdate) {
        this.regdate = regdate;
    }

    public Timestamp getModdate() {
        return moddate;
    }

    public void setModdate(Timestamp moddate) {
        this.moddate = moddate;
    }
}
