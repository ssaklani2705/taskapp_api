package com.webelement.taskapp.Exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.webelement.taskapp.common.ResponseApi;

import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.common.util.impl.LoggerFactory;
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
       // log.error("Workload fetch failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseApi<Object>(false, ex.getMessage(), null));
    }
}
