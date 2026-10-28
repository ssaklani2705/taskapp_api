package com.webelement.taskapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;

import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyDTO {

	private Integer companyId;
	private String companyName;
//	private String companySiteOffice;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String pincode;
	private String location;
	private Integer stateId;
	private String stateName;    
	private String gstNo;
	private String pan;
	private Short status;
	private Integer userId;
	
	private Integer companySiteOffice;
	private String workingDays;

	public CompanyDTO(Integer companyId, String companyName, String city, String pincode, String location,
			Integer stateId, String gstNo, String pan, Short status) {
		this.companyId = companyId;
		this.companyName = companyName;
		this.city = city;
		this.pincode = pincode;
		this.location = location;
		this.stateId = stateId;
		this.gstNo = gstNo;
		this.pan = pan;
		this.status = status;
	}
	
	public CompanyDTO(Integer companyId, String companyName, Integer companySiteOffice, String addressLine1,
			String addressLine2, String city, String pincode, String location, Integer stateId,
			String gstNo, String pan, Short status) {
		this.companyId = companyId;
		this.companyName = companyName;
		this.companySiteOffice = companySiteOffice;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.pincode = pincode;
		this.location = location;
		this.stateId = stateId;
		this.gstNo = gstNo;
		this.pan = pan;
		this.status = status;
	}
	
	private List<TransactionEntity> transactionHistory;
}
