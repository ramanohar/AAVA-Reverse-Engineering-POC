package com.collaberadigital.cove.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDto {

    private String email;
    private String company;
    private String country;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedBy;
    private String onboardingStatus;
    private String firstname;
    private String lastname;
    private String role;
    private String designation;
    private String registrationId;
    private String clientId;
    private String clientName;
    private String clientOneView;
    private Boolean isActive;
}
