package com.webelement.taskapp.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "t_company_shift")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CompanyShiftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_shiftid", nullable = false)
    private Integer shiftId;

    @Column(name = "i_companyid")
    private Integer companyId;

    @Column(name = "s_shiftname", length = 100)
    private String shiftName;

    @Column(name = "t_fromtime")
    private LocalTime fromTime;

    @Column(name = "t_totime")
    private LocalTime toTime;

    @Column(name = "i_status")
    private Short status;

    @Column(name = "ts_regdate")
    private LocalDateTime registrationDate;

    @Column(name = "ts_moddate")
    private LocalDateTime modificationDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "i_companyid", insertable = false, updatable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CompanyEntity company;
}
