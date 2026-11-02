package com.project.docmind.docmind.exception;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.project.docmind.docmind.dto.ApiResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static  final Logger logger=LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex){
		logger.warn("Resource not found : {} " ,ex.getMessage());
		return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(new ApiResponse(false,ex.getMessage(),null,LocalDateTime.now()));
	}

	@ExceptionHandler(DocumentProcessingException.class)
	public ResponseEntity<ApiResponse<Object>> handleProcessingError(DocumentProcessingException ex){
		logger.error("Document processing error: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE).body(new ApiResponse(false,"Failed to process document: "+ex.getMessage(),null,LocalDateTime.now()));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<Object>> handleMaxSize(MaxUploadSizeExceededException ex){
		logger.warn("File size limit exceeded : {}",ex.getMessage());
		return ResponseEntity.status(HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE).body(new ApiResponse(false,"File size exceeds the allowed limit(250MB)",null,LocalDateTime.now()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex){
		Map<String,String> errors=new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST)
                .body(new ApiResponse<Map<String, String>>(false,"Validation failed",errors,LocalDateTime.now()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST)
                .body(new ApiResponse(false,ex.getMessage(),null,LocalDateTime.now()));
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        logger.error("Unexpected error occurred: ", ex);

        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST)
                .body(new ApiResponse(false,"An unexpected error occurred: " + ex.getMessage(),null,LocalDateTime.now()));
    }
}
