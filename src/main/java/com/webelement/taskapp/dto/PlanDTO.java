package com.webelement.taskapp.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.webelement.taskapp.entity.TransactionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PlanDTO {

    private Integer planId;
    private String name;
    private Integer rate;
    private String description;
    private Short status;
    private Integer userId;

    // Selected services
    private List<Integer> serviceIds;

    private List<TransactionEntity> transactionHistory;

    public PlanDTO(
            Integer planId,
            String name,
            Integer rate,
            String description,
            Short status) {
        this.planId = planId;
        this.name = name;
        this.rate = rate;
        this.description = description;
        this.status = status;
    }
    
    public PlanDTO(
            Integer planId,
            String name,
            Integer rate,
            String description,
            Short status,
            String serviceIds) {

        this.planId = planId;
        this.name = name;
        this.rate = rate;
        this.description = description;
        this.status = status;

        this.serviceIds = serviceIds == null || serviceIds.trim().isEmpty()
                ? new ArrayList<>()
                : Arrays.stream(serviceIds.split(","))
                        .map(Integer::valueOf)
                        .collect(Collectors.toList());
    }
}