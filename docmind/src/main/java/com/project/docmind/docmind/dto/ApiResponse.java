package com.project.docmind.docmind.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public class ApiResponse<T> {

	private Boolean success;
	private String message;
	private T data;
	private LocalDateTime timestamp;

	public ApiResponse() {
		super();
	}
	public ApiResponse(Boolean success, String message, T data, LocalDateTime timestamp) {
		super();
		this.success = success;
		this.message = message;
		this.data = data;
		this.timestamp = timestamp;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

}
