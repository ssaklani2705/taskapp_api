package com.webelement.taskapp.Exceptions;

public class AccountExpiredException  extends RuntimeException {
	public AccountExpiredException(String message) {
        super(message);
    }
}
