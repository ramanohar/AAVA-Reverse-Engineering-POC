package com.collaberadigital.cove.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Logout {

	private String status;
	private String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String data;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String account_id;
	
	
	
	public String getAccount_id() {
		return account_id;
	}
	public void setAccount_id(String account_id) {
		this.account_id = account_id;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
