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
@Table(name = "t_plan")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PlanEntity {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "i_planid", nullable = false)
	    private Integer planId;

	    @Column(name = "s_name", length = 100)
	    private String name;

	    @Column(name = "i_rate")
	    private Integer rate; // Monthly Rate

	    @Column(name = "i_description", columnDefinition = "LONGTEXT")
	    private String description;

	    @Column(name = "i_status")
	    private Short status;

	    @Column(name = "ts_regdate")
	    private LocalDateTime registrationDate;

	    @Column(name = "ts_moddate")
	    private LocalDateTime modificationDate;
}
