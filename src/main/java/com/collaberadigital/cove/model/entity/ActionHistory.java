package com.collaberadigital.cove.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "action_history")
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class ActionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "user_company")
    private String userCompany;

    @Column(name = "update_by_admin_name")
    private String updateByAdminName;

    @Column(name = "update_by_admin_email")
    private String updateByAdminEmail;

    @Column(name = "registration_id")
    private String registrationId;

    @Column(name = "action")
    private String action;

    @Column(name = "admin_comments")
    private String adminComments;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
