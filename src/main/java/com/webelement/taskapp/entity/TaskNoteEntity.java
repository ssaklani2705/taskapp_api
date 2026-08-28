package com.webelement.taskapp.entity;

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
@Table(name = "t_task_note")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_tasknoteid", nullable = false)
    private Integer taskNoteId;

    @Column(name = "i_taskid")
    private Integer taskId;

    @Column(name = "i_createdby")
    private Integer createdBy;

    @Column(name = "s_note", columnDefinition = "LONGTEXT")
    private String note;

    @Column(name = "ts_regdate")
    private LocalDateTime registrationDate;

    @Column(name = "ts_moddate")
    private LocalDateTime modificationDate;

    @Column(name = "i_status")
    private Short status;
}
