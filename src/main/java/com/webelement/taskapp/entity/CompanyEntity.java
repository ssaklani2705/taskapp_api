package com.webelement.taskapp.entity;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "t_company")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "i_companyid", nullable = false)
	private Integer companyId;

	@Column(name = "s_companyname", length = 100)
	private String companyName;

//	@Column(name = "s_companysiteoffice", columnDefinition = "LONGTEXT")
//	private String companySiteOffice;

	@Column(name = "s_addressline1",columnDefinition = "LONGTEXT")
	private String addressLine1;

	@Column(name = "s_addressline2",columnDefinition = "LONGTEXT")
	private String addressLine2;

	@Column(name = "s_city")
	private String city;

	@Column(name = "s_pincode")
	private String pincode;

	@Column(name = "s_location")
	private String location;

	@Column(name = "i_stateid")
	private Integer stateId;

	@Column(name = "s_gstno")
	private String gstNo;

	@Column(name = "s_pan")
	private String pan;

	@Column(name = "i_status")
	private Short status;

	@Column(name = "ts_regdate")
	private LocalDateTime registrationDate;

	@Column(name = "ts_moddate")
	private LocalDateTime modificationDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "i_stateid", insertable = false, updatable = false)
	private StateEntity state;
	
	
	@OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<CompanyShiftEntity> shifts = new ArrayList<>();
	
	@Column(name = "s_workingdays", length = 50)
	private String workingDays;
	
	@Column(name = "i_companysiteoffice")
	private Integer companySiteOffice;

}
