package com.webelement.taskapp.Exceptions;

public class CaptchaValidationException extends RuntimeException {
	public CaptchaValidationException(String message) {
		super(message);
	}
}
