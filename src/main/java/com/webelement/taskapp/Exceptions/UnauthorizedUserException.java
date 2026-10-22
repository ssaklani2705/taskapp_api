package com.webelement.taskapp.Exceptions;

public class UnauthorizedUserException extends RuntimeException{

	public UnauthorizedUserException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedUserException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnauthorizedUserException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedUserException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedUserException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
