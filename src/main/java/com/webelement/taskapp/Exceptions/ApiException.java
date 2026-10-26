package com.webelement.taskapp.Exceptions;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException{
	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

    public ApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);   // keeps the original error for logs
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException tooManyRequests(String message) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    public static ApiException internal(String message, Throwable cause) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
    
    
    public static ApiException accountExpiredException(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }
    
    public static ApiException conflictedException(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }
}
