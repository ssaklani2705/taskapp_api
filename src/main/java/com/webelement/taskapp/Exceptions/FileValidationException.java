package com.webelement.taskapp.Exceptions;

@SuppressWarnings("serial")
public class FileValidationException extends RuntimeException {
	public FileValidationException(String message) {
		super(message);
	}
}
