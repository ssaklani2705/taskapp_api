package com.webelement.taskapp.entity;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_task")
public class TaskEntity {

    @Id
    @Column(name = "i_taskid")
    private Integer taskId;

    @Column(name = "i_clientid")
    private Integer clientId;

    @Column(name = "d_date")
    private Date date;

    @Column(name = "i_taskgroupid")
    private Integer taskGroupId;

    @Column(name = "s_description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "s_filename1")
    private String fileName1;

    @Column(name = "s_filename2")
    private String fileName2;

    @Column(name = "i_addedby")
    private Integer addedBy;

    @Column(name = "i_assignedto")
    private Integer assignedTo;

    @Column(name = "i_priority")
    private Integer priority;

    @Column(name = "s_closeremarks", columnDefinition = "LONGTEXT")
    private String closeRemarks;

    @Column(name = "s_filename3")
    private String fileName3;

    @Column(name = "s_filename4")
    private String fileName4;

    @Column(name = "i_status")
    private Integer status;

    @Column(name = "ts_regdate")
    private Timestamp regdate;

    @Column(name = "ts_moddate")
    private Timestamp moddate;

    public TaskEntity() {
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(Integer taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName1() {
        return fileName1;
    }

    public void setFileName1(String fileName1) {
        this.fileName1 = fileName1;
    }

    public String getFileName2() {
        return fileName2;
    }

    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
    }

    public Integer getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Integer addedBy) {
        this.addedBy = addedBy;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCloseRemarks() {
        return closeRemarks;
    }

    public void setCloseRemarks(String closeRemarks) {
        this.closeRemarks = closeRemarks;
    }

    public String getFileName3() {
        return fileName3;
    }

    public void setFileName3(String fileName3) {
        this.fileName3 = fileName3;
    }

    public String getFileName4() {
        return fileName4;
    }

    public void setFileName4(String fileName4) {
        this.fileName4 = fileName4;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
