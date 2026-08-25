package com.webelement.taskapp.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_state")
@Data
@Builder(toBuilder = true)   
@NoArgsConstructor
@AllArgsConstructor
public class StateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_stateid", nullable = false)
    private Integer stateId;

    @Column(name = "s_name", length = 100)
    private String name;

    @Column(name = "s_code", length = 2)
    private String code;

    @Column(name = "i_status")
    private Short status;

    @Column(name = "i_userid")
    private Integer userId;

    @Column(name = "ts_regdate")
    private LocalDateTime registrationDate;

    @Column(name = "ts_moddate")
    private LocalDateTime modificationDate;
}
