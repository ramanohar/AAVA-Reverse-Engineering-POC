package com.collaberadigital.cove.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.NON_EMPTY,content = JsonInclude.Include.NON_NULL)
public class ActionHistoryDto {



    private String userName;
    private String userEmail;
    private String userRole;
    private String userCompany;
    private String updateByAdminName;
    private String updateByAdminEmail;
    private String registrationId;
    private String action;
    private String adminComments;
    private String actionType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
