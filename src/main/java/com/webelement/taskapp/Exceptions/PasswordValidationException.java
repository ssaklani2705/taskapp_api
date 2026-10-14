package com.webelement.taskapp.Exceptions;

public class PasswordValidationException extends RuntimeException{
	 public PasswordValidationException(String message) {
	        super(message);
	    }

	    public PasswordValidationException(String message, Throwable cause) {
	        super(message, cause);
	    }
}
