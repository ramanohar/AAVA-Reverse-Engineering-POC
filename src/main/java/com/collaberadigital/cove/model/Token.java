package com.collaberadigital.cove.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {

	private String accessToken;
	private String refreshToken;
	private String tokenType;

	private String firstname;
	private String lastname;
	private String email;
	private String account;
	private boolean active;
	private String role;
	private String clientId;
	private String clientName;
	private String clientOneView;

	private String message;
//	private Long code=null;
//
//	public String getToken() {
//		return token;
//	}
//
//	public void setToken(String token) {
//		this.token = token;
//	}
//
//
//	public String getMessage() {
//		return message;
//	}
//
//	public void setMessage(String message) {
//		this.message = message;
//	}
//
//	public Long getCode() {
//		return code;
//	}
//
//	public void setCode(Long code) {
//		this.code = code;
//	}
//
//	public String getFirstname() {
//		return firstname;
//	}
//
//	public void setFirstname(String firstname) {
//		this.firstname = firstname;
//	}
//
//	public String getLastname() {
//		return lastname;
//	}
//
//	public void setLastname(String lastname) {
//		this.lastname = lastname;
//	}
//
//	public String getAccount() {
//		return account;
//	}
//
//	public void setAccount(String account) {
//		this.account = account;
//	}
//
//	public boolean isActive() {
//		return active;
//	}
//
//	public void setActive(boolean active) {
//		this.active = active;
//	}
//
//	public String getRole() {
//		return role;
//	}
//
//	public String getEmail() {
//		return email;
//	}
//
//	public void setEmail(String email) {
//		this.email = email;
//	}
//
//	public void setRole(String role) {
//		this.role = role;
//	}
//
//	public String getClientId() {
//		return clientId;
//	}
//
//	public void setClientId(String clientId) {
//		this.clientId = clientId;
//	}
//
//	public String getClientName() {
//		return clientName;
//	}
//
//	public void setClientName(String clientName) {
//		this.clientName = clientName;
//	}
//
//	public String getClientOneView() {
//		return clientOneView;
//	}
//
//	public void setClientOneView(String clientOneView) {
//		this.clientOneView = clientOneView;
//	}
	
	
}
