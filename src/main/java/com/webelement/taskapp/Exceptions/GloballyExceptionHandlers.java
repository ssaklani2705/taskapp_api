package com.webelement.taskapp.Exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.webelement.taskapp.common.ResponseApi;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GloballyExceptionHandlers {

	@ExceptionHandler(ClientValidationException.class)
	public ResponseEntity<ResponseApi<String>> handleClientValidationException(ClientValidationException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseApi<>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(FileValidationException.class)
	public ResponseEntity<ResponseApi<String>> handleFileValidationException(FileValidationException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseApi<>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseApi<String>> handleException(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ResponseApi<>(false, "Something went wrong", ex.getMessage()));
	}

	@ExceptionHandler(FileDownloadException.class)
	public ResponseEntity<Map<String, Object>> handleFileDownloadException(FileDownloadException ex) {

		Map<String, Object> response = new HashMap<>();
		response.put("status", false);
		response.put("message", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ResponseApi<Object>> handleInvalidRequest(InvalidRequestException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ResponseApi<Object>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(WorkloadFetchException.class)
	public ResponseEntity<ResponseApi<Object>> handleWorkloadFetch(WorkloadFetchException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ResponseApi<Object>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ResponseApi<String>> handleUpdateError(IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ResponseApi<String>(false, "Unable to change password. Please try again.", null));
	}

	

	@ExceptionHandler(UnauthorizedUserException.class)
	public ResponseEntity<ResponseApi<Object>> handleUnauthorized(UnauthorizedUserException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseApi<>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(TooManyAttemptsException.class)
	public ResponseEntity<ResponseApi<Object>> handleTooManyAttempts(TooManyAttemptsException ex) {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(new ResponseApi<>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(InvalidCaptchaException.class)
	public ResponseEntity<ResponseApi<Void>> handleInvalidCaptcha(InvalidCaptchaException ex) {

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseApi<>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ResponseApi<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseApi<>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ResponseApi<Object>> handleApi(ApiException ex) {

		return ResponseEntity.status(ex.getStatus()).body(new ResponseApi<>(false, ex.getMessage(), null));
	}
}
