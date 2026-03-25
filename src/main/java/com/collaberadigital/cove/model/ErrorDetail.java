package com.collaberadigital.cove.model;



public class ErrorDetail {
	private int code;
	private String message;
	private String vendorErrorMessage;

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public String getVendorErrorMessage() {
		return vendorErrorMessage;
	}
	public void setVendorErrorMessage(String vendorErrorMessage) {
		this.vendorErrorMessage = vendorErrorMessage;
	}	
	
}
