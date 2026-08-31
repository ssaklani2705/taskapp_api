package com.webelement.taskapp.dto;

import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webelement.taskapp.entity.TransactionEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {

	private Integer clientId;
	private String name;
	private String code;
	private String pan;
	private Short status;
	private Short gstFlag;
	private String gstNo;
	private Integer stateId;
	private String stateName;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String pincode;
	private String contactName;
	private String contactEmail;
	private String emails;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate startDate;

	private Double monthlyCharge;
	private Double outstanding;
	private String name1;
	private String emailId1;
	private String name2;
	private String emailId2;
	private String name3;
	private String emailId3;
	private Integer managerId;
	private String managerName;
	private Integer userId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date regdate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date moddate;

	private Short taxFlag;
	private String location;
	private Integer planId;
	private String planName;

	private List<TransactionEntity> transactionHistory;

	// Get by ClientId
	public ClientDTO(Integer clientId, String name, String code, String pan, Short status, Short gstFlag, String gstNo,
			Integer stateId, String stateName, String addressLine1, String addressLine2, String city, String pincode,
			String contactName, String contactEmail, String emails, LocalDate startDate, Double monthlyCharge,
			Double outstanding, String name1, String emailId1, String name2, String emailId2, String name3,
			String emailId3, Integer managerId, String managerName, Integer userId, Date regdate, Date moddate,
			Short taxFlag, String location, Integer planId, String planName) {

		this.clientId = clientId;
		this.name = name;
		this.code = code;
		this.pan = pan;
		this.status = status;
		this.gstFlag = gstFlag;
		this.gstNo = gstNo;
		this.stateId = stateId;
		this.stateName = stateName;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.pincode = pincode;
		this.contactName = contactName;
		this.contactEmail = contactEmail;
		this.emails = emails;
		this.startDate = startDate;
		this.monthlyCharge = monthlyCharge;
		this.outstanding = outstanding;
		this.name1 = name1;
		this.emailId1 = emailId1;
		this.name2 = name2;
		this.emailId2 = emailId2;
		this.name3 = name3;
		this.emailId3 = emailId3;
		this.managerId = managerId;
		this.managerName = managerName;
		this.userId = userId;
		this.regdate = regdate;
		this.moddate = moddate;
		this.taxFlag = taxFlag;
		this.location = location;
		this.planId = planId;
		this.planName = planName;
	}

	// Get details
	public ClientDTO(Integer clientId, String name, String code, String pan, Short status, Short gstFlag, String gstNo,
			Integer stateId, String stateName, String addressLine1, String addressLine2, String city, String pincode,
			String contactName, String contactEmail, String emails, LocalDate startDate, Double monthlyCharge,
			Double outstanding, String name1, String emailId1, String name2, String emailId2, String name3,
			String emailId3, Integer managerId, String managerName, Integer userId, Timestamp regdate,
			Timestamp moddate, Short taxFlag, String location, Integer planId, String planName) {

		this.clientId = clientId;
		this.name = name;
		this.code = code;
		this.pan = pan;
		this.status = status;
		this.gstFlag = gstFlag;
		this.gstNo = gstNo;
		this.stateId = stateId;
		this.stateName = stateName;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.pincode = pincode;
		this.contactName = contactName;
		this.contactEmail = contactEmail;
		this.emails = emails;
		this.startDate = startDate;
		this.monthlyCharge = monthlyCharge;
		this.outstanding = outstanding;
		this.name1 = name1;
		this.emailId1 = emailId1;
		this.name2 = name2;
		this.emailId2 = emailId2;
		this.name3 = name3;
		this.emailId3 = emailId3;
		this.managerId = managerId;
		this.managerName = managerName;
		this.userId = userId;
		this.regdate = regdate;
		this.moddate = moddate;
		this.taxFlag = taxFlag;
		this.location = location;
		this.planId = planId;
		this.planName = planName;
	}

	// For recurring
	public ClientDTO(Integer clientId, String name) {
		this.clientId = clientId;
		this.name = name;
	}
	
}
