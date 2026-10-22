package com.webelement.taskapp.Exceptions;

public class InvalidCaptchaException  extends RuntimeException {
	public InvalidCaptchaException(String message) {
        super(message);
    }
}
