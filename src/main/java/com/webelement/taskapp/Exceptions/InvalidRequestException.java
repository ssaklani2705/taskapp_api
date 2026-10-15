package com.webelement.taskapp.Exceptions;

@SuppressWarnings("serial")
public class InvalidRequestException extends RuntimeException {
	public InvalidRequestException(String message) {
		super(message);
	}
}
