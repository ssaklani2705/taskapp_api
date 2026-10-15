package com.webelement.taskapp.Exceptions;

@SuppressWarnings("serial")
public class WorkloadFetchException extends RuntimeException {
	public WorkloadFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}
