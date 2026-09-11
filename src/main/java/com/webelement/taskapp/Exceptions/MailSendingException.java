package com.webelement.taskapp.Exceptions;

public class MailSendingException extends RuntimeException{
	 private static final long serialVersionUID = 1L;

	    public MailSendingException(String message) {
	        super(message);
	    }

		public MailSendingException(String message, Throwable cause) {
			super(message, cause);
		}
}
