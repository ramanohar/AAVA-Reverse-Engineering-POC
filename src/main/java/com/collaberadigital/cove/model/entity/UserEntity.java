package com.collaberadigital.cove.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false, unique = true)
    private Integer userId;

    @Column(name = "email_address", nullable = false, updatable = true, unique = true)
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    @Column(name = "company", nullable = false, unique = false)
    private String company;

    @Column(name = "country", nullable = true, unique = false)
    private String country;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @Column(name = "onboarding_status")
    private String onboardingStatus;
    
    @Column(name = "firstname")
	private String firstname;
    
    @Column(name = "lastname")
	private String lastname;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "role")
	private String role;

    @Column(name = "registration_id")
    private String registrationId;
    
    @Column(name = "designation")
	private String designation;
    
    @Column(name = "client_id")
	private String clientId;
    
    @Column(name = "client_name")
	private String clientName;
    
    @Column(name = "client_one_view")
	private String clientOneView;

	@Column(name = "is_active")
	private Boolean isActive;
    
	
//	public UserEntity()
//	{
//
//	}
//
//
//
//	public UserEntity(Integer userId,
//			@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
//					flags = Flag.CASE_INSENSITIVE)
//			String email,
//			String company, String country, LocalDateTime createdAt, LocalDateTime lastUpdatedAt, String lastUpdatedBy,
//			String onboardingStatus, String firstname, String lastname, String password, String role, String designation,
//			String clientId, String clientName, String clientOneView) {
//		super();
//		this.userId = userId;
//		this.email = email;
//		this.company = company;
//		this.country = country;
//		this.createdAt = createdAt;
//		this.lastUpdatedAt = lastUpdatedAt;
//		this.lastUpdatedBy = lastUpdatedBy;
//		this.onboardingStatus = onboardingStatus;
//		this.firstname = firstname;
//		this.lastname = lastname;
//		this.password = password;
//		this.role = role;
//		this.designation = designation;
//		this.clientId = clientId;
//		this.clientName = clientName;
//		this.clientOneView = clientOneView;
//	}
//
//
//
//	public Integer getUserId() {
//		return userId;
//	}
//
//	public void setUserId(Integer userId) {
//		this.userId = userId;
//	}
//
//	public Boolean getIsActive(){
//		return isActive;
//	}
//
//	public void setIsActive(Boolean isActive){
//		this.isActive = isActive;
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
//	public String getCompany() {
//		return company;
//	}
//
//	public void setCompany(String company) {
//		this.company = company;
//	}
//
//	public String getCountry() {
//		return country;
//	}
//
//	public void setCountry(String country) {
//		this.country = country;
//	}
//
//	public LocalDateTime getCreatedAt() {
//		return createdAt;
//	}
//
//	public void setCreatedAt(LocalDateTime createdAt) {
//		this.createdAt = createdAt;
//	}
//
//	public LocalDateTime getLastUpdatedAt() {
//		return lastUpdatedAt;
//	}
//
//	public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
//		this.lastUpdatedAt = lastUpdatedAt;
//	}
//
//	public String getLastUpdatedBy() {
//		return lastUpdatedBy;
//	}
//
//	public void setLastUpdatedBy(String lastUpdatedBy) {
//		this.lastUpdatedBy = lastUpdatedBy;
//	}
//
//	public String getOnboardingStatus() {
//		return onboardingStatus;
//	}
//
//	public void setOnboardingStatus(String onboardingStatus) {
//		this.onboardingStatus = onboardingStatus;
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
//	public String getPassword() {
//		return password;
//	}
//
//	public void setPassword(String password) {
//		this.password = password;
//	}
//
//	public String getRole() {
//		return role;
//	}
//
//	public void setRole(String role) {
//		this.role = role;
//	}
//
//	public String getDesignation() {
//		return designation;
//	}
//
//	public void setDesignation(String designation) {
//		this.designation = designation;
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
//
    
    

}
