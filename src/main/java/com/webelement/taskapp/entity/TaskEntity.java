package com.webelement.taskapp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {


    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_taskid", nullable = false)
    private Integer taskId;

    @Column(name = "i_clientid")
    private Integer clientId;

    @Column(name = "d_date")
    private LocalDate date;

    @Column(name = "i_taskcategoryid")
    private Integer taskCategoryId;

    @Column(name = "s_description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "s_filename1", length = 100)
    private String fileName1;

    @Column(name = "s_filename2", length = 100)
    private String fileName2;

    @Column(name = "i_addedby")
    private Integer addedBy;

    @Column(name = "i_assignedto")
    private Integer assignedTo;

    @Column(name = "i_priority")
    private Short priority;

    @Column(name = "s_closeremarks", columnDefinition = "LONGTEXT")
    private String closeRemarks;

    @Column(name = "s_filename3", length = 100)
    private String fileName3;

    @Column(name = "s_filename4", length = 100)
    private String fileName4;
    @Column(name = "i_status")
    private Short status;
    @Column(name = "ts_regdate")
    private LocalDateTime registrationDate;
    @Column(name = "ts_moddate")
    private LocalDateTime modificationDate;
    @Column(name = "s_title", length = 100)
    private String title;
//    @Column(name = "i_taskgroupid")
//    private Integer taskGroupId;
    @Column(name = "i_taskstatus")
    private Short taskStatus;
    @Column(name = "i_reopencounts")
    private Integer reopenCount = 0;
}
