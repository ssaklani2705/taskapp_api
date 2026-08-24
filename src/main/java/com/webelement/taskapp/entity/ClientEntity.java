package com.webelement.taskapp.entity;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_client")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "i_clientid")
    private Integer clientId;

    @Column(name = "s_name")
    private String name;

    @Column(name = "s_code")
    private String code;

    @Column(name = "s_pan")
    private String pan;

    @Column(name = "i_gstflag")
    private Integer gstFlag;

    @Column(name = "s_gstno")
    private String gstNo;

    @Column(name = "i_stateid")
    private Integer stateId;

    @Column(name = "s_addressline1")
    private String addressLine1;

    @Column(name = "s_addressline2")
    private String addressLine2;

    @Column(name = "s_city")
    private String city;

    @Column(name = "s_pincode")
    private String pincode;

    @Column(name = "s_contactname")
    private String contactName;

    @Column(name = "s_contactemail")
    private String contactEmail;

    @Column(name = "s_emails", columnDefinition = "LONGTEXT")
    private String emails;

    @Column(name = "d_startdate")
    private Date startDate;

    @Column(name = "d_monthlycharge")
    private Integer monthlyCharge;

    @Column(name = "d_outstanding")
    private Double outstanding;

    @Column(name = "s_name1")
    private String name1;

    @Column(name = "s_emailid1")
    private String emailId1;

    @Column(name = "s_name2")
    private String name2;

    @Column(name = "s_emailid2")
    private String emailId2;

    @Column(name = "s_name3")
    private String name3;

    @Column(name = "s_emailid3")
    private String emailId3;

    @Column(name = "i_managerid")
    private Integer managerId;

    @Column(name = "i_userid")
    private Integer userId;

    @Column(name = "ts_regdate")
    private Timestamp regdate;

    @Column(name = "ts_moddate")
    private Timestamp moddate;


    public ClientEntity() {
    }


    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }


    public Integer getGstFlag() {
        return gstFlag;
    }

    public void setGstFlag(Integer gstFlag) {
        this.gstFlag = gstFlag;
    }


    public String getGstNo() {
        return gstNo;
    }

    public void setGstNo(String gstNo) {
        this.gstNo = gstNo;
    }


    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }


    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }


    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }


    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }


    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }


    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }


    public Integer getMonthlyCharge() {
        return monthlyCharge;
    }

    public void setMonthlyCharge(Integer monthlyCharge) {
        this.monthlyCharge = monthlyCharge;
    }


    public Double getOutstanding() {
        return outstanding;
    }

    public void setOutstanding(Double outstanding) {
        this.outstanding = outstanding;
    }


    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }


    public String getEmailId1() {
        return emailId1;
    }

    public void setEmailId1(String emailId1) {
        this.emailId1 = emailId1;
    }


    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }


    public String getEmailId2() {
        return emailId2;
    }

    public void setEmailId2(String emailId2) {
        this.emailId2 = emailId2;
    }


    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }


    public String getEmailId3() {
        return emailId3;
    }

    public void setEmailId3(String emailId3) {
        this.emailId3 = emailId3;
    }


    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }


    public Timestamp getRegdate() {
        return regdate;
    }

    public void setRegdate(Timestamp regdate) {
        this.regdate = regdate;
    }


    public Timestamp getModdate() {
        return moddate;
    }

    public void setModdate(Timestamp moddate) {
        this.moddate = moddate;
    }
}
