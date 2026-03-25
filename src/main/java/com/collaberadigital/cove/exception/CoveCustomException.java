package com.collaberadigital.cove.exception;

import org.springframework.http.HttpStatus;

public class CoveCustomException extends Exception {
	private static final long serialVersionUID = 1L;
	private String errorMessage;
	private HttpStatus statusCode;
	private String vendorError;
	public CoveCustomException(String errorMessage, HttpStatus httpStatus, Throwable throwable, String vendorError) {
        super(throwable);
        this.errorMessage = errorMessage;
        this.statusCode = httpStatus;
    }
	public CoveCustomException(String errorMessage) {
		this.errorMessage = errorMessage;
		this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
	}
	public CoveCustomException(String errorMessage, HttpStatus status, String vendorError) {
		this.vendorError = vendorError;
		this.errorMessage = errorMessage;
		this.statusCode = status;
	}
	
	public HttpStatus getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getVendorError() {
		return vendorError;
	}
	public void setVendorError(String vendorError) {
		this.vendorError = vendorError;
	}
	
}
