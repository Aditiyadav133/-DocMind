package com.project.docmind.docmind.exception;


public class DocumentProcessingException extends RuntimeException{

	public DocumentProcessingException(String message) {
		super(message);
	}

	public DocumentProcessingException() {
		super("error processing documents !!");
	}

	public DocumentProcessingException(String message,Throwable ex) {
		super(message,ex);
	}
}
