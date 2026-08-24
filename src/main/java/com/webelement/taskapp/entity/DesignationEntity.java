package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
@Entity
@Table(name = "t_designation")
public class DesignationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_designationid")
    private Integer designationId;

    @Column(name = "s_name")
    private String name;

    @Column(name = "i_sequence")
    private Integer sequence;

    @Column(name = "i_status")
    private Integer status;

    @Column(name = "i_userid")
    private Integer userId;

    @Column(name = "ts_regdate")
    private Timestamp regdate;

    @Column(name = "ts_moddate")
    private Timestamp moddate;

}
