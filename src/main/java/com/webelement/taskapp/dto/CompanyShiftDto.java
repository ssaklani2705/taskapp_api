package com.webelement.taskapp.dto;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyShiftDto {
    private Integer shiftId;
    private String shiftName;
    private String fromTime;   // "HH:mm"
    private String toTime;     // "HH:mm"
    private Short status;
    private Integer companyId;
    private Integer userId;
    // getters & setters
}
