package com.webelement.taskapp.Exceptions;

public class TooManyAttemptsException extends RuntimeException {
	 public TooManyAttemptsException(String message) {
	        super(message);
	    }
}
