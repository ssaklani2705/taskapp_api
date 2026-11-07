package com.webelement.taskapp.dto;

import java.time.LocalDateTime;

public interface TransactionHistoryProjection {

	Integer getTransactionId();

	String getModuleName();
	
    Integer getUserId();

	String getActionBy();

	LocalDateTime getUpdatedOn();

	String getAction();
}
