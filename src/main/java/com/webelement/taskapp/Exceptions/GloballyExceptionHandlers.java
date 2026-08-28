package com.webelement.taskapp.Exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.webelement.taskapp.common.ResponseApi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GloballyExceptionHandlers {
	  @ExceptionHandler(FileValidationException.class)
	    public ResponseEntity<ResponseApi<String>> handleFileValidationException(
	            FileValidationException ex) {

	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(new ResponseApi<>(
	                        false,
	                        ex.getMessage(),
	                        null));
	    }
		@ExceptionHandler(Exception.class)
		public ResponseEntity<ResponseApi<String>> handleException(Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseApi<>(false, "Something went wrong", null));
		}
}
