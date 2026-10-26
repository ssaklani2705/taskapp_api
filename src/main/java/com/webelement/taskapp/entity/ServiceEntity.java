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
@Table(name = "t_service")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_serviceid", nullable = false)
    private Integer serviceId;

    @Column(name = "s_name", length = 100)
    private String name;

    @Column(name = "i_status")
    private Short status;

    @Column(name = "ts_regdate")
    private LocalDateTime registrationDate;

    @Column(name = "ts_moddate")
    private LocalDateTime modificationDate;
}