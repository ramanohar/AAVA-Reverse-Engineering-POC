# High-Level Requirements and Module Breakdown

## Executive Summary

This document provides high-level requirements and module breakdown for the COVE User Service, derived from comprehensive reverse engineering artifacts. It serves as a handoff from reverse engineering to forward engineering, providing a **minimal sufficient** requirements baseline for elaboration into detailed requirements, user stories, low-level design, and implementation plans.

**Purpose:** Seed forward engineering agents with traceable, high-level requirements and clear module boundaries aligned to the as-is system architecture.

**Recommended Next Agents:** `elaborate_requirements`, `story_breakdown`, `future_state_architecture`, `lld`, `implementation`

---

## Module Breakdown

The COVE User Service is decomposed into six logical modules based on bounded contexts, technology signals, and interface boundaries identified in reverse engineering artifacts.

### Module 1: Authentication & Authorization Module (`mod_authentication_authorization`)

**Responsibility:** Manages JWT-based stateless authentication with short-lived access tokens (30 min) and long-lived refresh tokens (24 hours). Provides token generation, validation, revocation, and refresh workflows. Enforces role-based access control (RBAC) with CUSTOMER and ADMIN roles. Stores tokens in database with expiration and revocation flags.

**Mapped Artifacts:**
- `domain_model.json`: bounded_contexts[1] (Authentication & Authorization)
- `security_privacy_assessment.json`: authorization_model_summary
- `business_process_model.json`: proc_user_login, proc_token_refresh
- `data_model.json`: logical_models (AccessToken, RefreshToken)

**Interfaces:**
- **Inbound:** REST API endpoints: POST /login, POST /check-access-token, POST /refresh-token, POST /revoke-access-token, POST /revoke-refresh-token
- **Outbound:** Database access via AccessTokenRepo, RefreshTokenRepo, UserRepository

**Primary Technologies:** Spring Security, JWT (jjwt 0.11.5), BCrypt, Spring Data JPA

---

### Module 2: User Management Module (`mod_user_management`)

**Responsibility:** Handles user registration, profile management, onboarding workflow (pending_approval → approved/reject), account activation/deactivation, and role assignment. Integrates with email service for onboarding notifications. Supports paginated user listing and search.

**Mapped Artifacts:**
- `domain_model.json`: bounded_contexts[0] (User Management)
- `business_process_model.json`: proc_user_registration, proc_admin_onboarding_approval, proc_admin_account_management, proc_admin_role_management
- `data_model.json`: logical_models (UserEntity)

**Interfaces:**
- **Inbound:** REST API endpoints: POST /register, PUT /api/v1/admin/{toEmail}/onboarding-status, PUT /api/v1/admin/{toEmail}/account-status, PUT /api/v1/admin/{toEmail}/role, GET /api/v1/admin/user-details/{email}, GET /api/v1/admin/users
- **Outbound:** Database access via UserRepository; Email notifications via EmailService

**Primary Technologies:** Spring Boot, Spring Data JPA, MySQL, BCrypt

---

### Module 3: Audit & Compliance Module (`mod_audit_compliance`)

**Responsibility:** Records all administrative actions on user accounts (onboarding status changes, account activation/deactivation, role changes) with timestamps and admin attribution. Provides paginated audit log retrieval with filtering by date range, action type, and user email.

**Mapped Artifacts:**
- `domain_model.json`: bounded_contexts[2] (Audit & Compliance)
- `business_process_model.json`: proc_admin_onboarding_approval (step: Log Action in Audit Trail)
- `data_model.json`: logical_models (ActionHistory)

**Interfaces:**
- **Inbound:** REST API endpoint: GET /api/v1/admin/action-history
- **Outbound:** Database access via ActionHistoryRepo

**Primary Technologies:** Spring Data JPA, MySQL

---

### Module 4: Email Notifications Module (`mod_email_notifications`)

**Responsibility:** Sends automated email notifications to users during onboarding workflow transitions (pending approval, approved, rejected). Uses FreeMarker templates for email content. Integrates with Gmail SMTP for email delivery.

**Mapped Artifacts:**
- `domain_model.json`: bounded_contexts[0] (User Management) - Email Notifications interface
- `integration_catalog.json`: touchpoint_enrichments (Gmail SMTP)
- `business_process_model.json`: proc_user_registration (step: Send Pending Approval Email), proc_admin_onboarding_approval (step: Send Email Notification)

**Interfaces:**
- **Inbound:** Service layer calls from UserService, AdminService
- **Outbound:** SMTP integration with Gmail (smtp.gmail.com:465)

**Primary Technologies:** Spring Mail, FreeMarker, SMTP/SSL

---

### Module 5: External Integration Module (`mod_external_integration`)

**Responsibility:** Integrates with external OneView API to fetch project performance metrics and RAG (Red-Amber-Green) status data. Provides a single endpoint for performance data retrieval. Handles OneView authentication and token management.

**Mapped Artifacts:**
- `domain_model.json`: bounded_contexts[3] (External Integration)
- `integration_catalog.json`: touchpoint_enrichments (OneView API)
- `business_process_model.json`: application_purpose_summary.primary_capabilities (External Performance Data Integration)

**Interfaces:**
- **Inbound:** REST API endpoint: GET /performance
- **Outbound:** HTTPS REST integration with OneView API (https://cdoneview.avateam.io)

**Primary Technologies:** RestTemplate, HTTPS/REST

---

### Module 6: Infrastructure & Observability Module (`mod_infrastructure_observability`)

**Responsibility:** Provides health check endpoint, Prometheus metrics exposure via Spring Actuator, and service discovery registration with Netflix Eureka. Supports monitoring, alerting, and service mesh integration.

**Mapped Artifacts:**
- `integration_catalog.json`: touchpoint_enrichments (Prometheus, Netflix Eureka)
- `repository_summary.json`: entry_points (Healthcheck), configuration (actuator_endpoints)

**Interfaces:**
- **Inbound:** REST API endpoints: GET /healthcheck, GET /actuator/health, GET /actuator/prometheus, GET /actuator/metrics
- **Outbound:** Service registration with Netflix Eureka

**Primary Technologies:** Spring Actuator, Micrometer, Prometheus, Netflix Eureka

---

## Requirement Themes

Requirements are organized into five epic-level themes aligned with business objectives and architectural drivers.

### Theme 1: Security & Compliance (`theme_security_compliance`)

**Description:** Ensure secure authentication, authorization, and audit logging to meet security and compliance requirements. Address critical security gaps identified in reverse engineering.

**Business Objectives:**
- Protect user credentials and authentication tokens from unauthorized access
- Enforce role-based access control to prevent privilege escalation
- Maintain comprehensive audit trail for compliance and incident response
- Mitigate security vulnerabilities (hardcoded credentials, missing SSL/TLS, no rate limiting)

**Related Modules:** Authentication & Authorization, User Management, Audit & Compliance

**Source:** `security_privacy_assessment.json`, `architecture_summary.json` (operational_and_risk.residual_risks)

---

### Theme 2: User Onboarding & Lifecycle Management (`theme_user_onboarding`)

**Description:** Provide controlled user registration and onboarding workflow with admin approval, email notifications, and account lifecycle management (activation, deactivation, role changes).

**Business Objectives:**
- Enable self-service user registration with admin oversight
- Ensure users are informed at each onboarding stage via email notifications
- Allow administrators to manage user accounts and roles efficiently
- Maintain audit trail for all user lifecycle events

**Related Modules:** User Management, Email Notifications, Audit & Compliance

**Source:** `business_process_model.json` (proc_user_registration, proc_admin_onboarding_approval), `domain_model.json`

---

### Theme 3: Authentication & Session Management (`theme_authentication_session`)

**Description:** Implement stateless JWT-based authentication with token lifecycle management (generation, validation, refresh, revocation) to support secure and scalable user sessions.

**Business Objectives:**
- Provide seamless authentication experience with short-lived access tokens and long-lived refresh tokens
- Enable token revocation for logout and security incident response
- Support horizontal scaling with stateless authentication
- Minimize session hijacking risk with token expiration and revocation

**Related Modules:** Authentication & Authorization

**Source:** `business_process_model.json` (proc_user_login, proc_token_refresh), `security_privacy_assessment.json`

---

### Theme 4: External Data Integration (`theme_external_integration`)

**Description:** Integrate with external OneView API to provide project performance and RAG status data to authorized users.

**Business Objectives:**
- Enable users to access project performance metrics from OneView within COVE platform
- Ensure secure and reliable integration with external API
- Handle authentication and error scenarios gracefully

**Related Modules:** External Integration

**Source:** `integration_catalog.json` (OneView API), `business_process_model.json`

---

### Theme 5: Observability & Operational Excellence (`theme_observability_operations`)

**Description:** Provide comprehensive monitoring, health checks, and service discovery to ensure system reliability, performance, and operational visibility.

**Business Objectives:**
- Enable proactive monitoring and alerting via Prometheus metrics
- Support automated health checks for deployment and orchestration
- Facilitate service discovery and load balancing in microservices ecosystem
- Ensure operational visibility for incident response and troubleshooting

**Related Modules:** Infrastructure & Observability

**Source:** `integration_catalog.json` (Prometheus, Eureka), `architecture_summary.json` (architectural_drivers)

---

## High-Level Requirements

The following table summarizes 20 high-level requirements organized by theme. Each requirement includes traceability to source artifacts.

| Req ID | Title | Type | Priority | Module(s) | Theme(s) |
|--------|-------|------|----------|-----------|----------|
| HLR-001 | User Registration with Email Validation | Functional | Must | User Management, Email Notifications | User Onboarding |
| HLR-002 | Admin Onboarding Approval Workflow | Functional | Must | User Management, Email Notifications, Audit & Compliance | User Onboarding |
| HLR-003 | JWT-Based User Authentication | Functional | Must | Authentication & Authorization | Authentication & Session |
| HLR-004 | Token Refresh Workflow | Functional | Must | Authentication & Authorization | Authentication & Session |
| HLR-005 | Token Revocation for Logout | Functional | Must | Authentication & Authorization | Authentication & Session |
| HLR-006 | Role-Based Access Control (RBAC) | Functional | Must | Authentication & Authorization | Security & Compliance |
| HLR-007 | Admin Account Activation and Deactivation | Functional | Must | User Management, Audit & Compliance | User Onboarding |
| HLR-008 | Admin Role Management | Functional | Must | User Management, Audit & Compliance | User Onboarding |
| HLR-009 | Comprehensive Audit Logging for Admin Actions | Functional | Must | Audit & Compliance | Security & Compliance |
| HLR-010 | Email Notifications for Onboarding Workflow | Functional | Must | Email Notifications | User Onboarding |
| HLR-011 | OneView API Integration for Performance Data | Integration | Should | External Integration | External Integration |
| HLR-012 | Health Check and Actuator Endpoints | Non-Functional | Must | Infrastructure & Observability | Observability & Operations |
| HLR-013 | Service Discovery with Netflix Eureka | Non-Functional | Should | Infrastructure & Observability | Observability & Operations |
| HLR-014 | Externalize Credentials to Secrets Manager | Security | Must | Authentication & Authorization, Email Notifications, External Integration | Security & Compliance |
| HLR-015 | Enable SSL/TLS for Database Connection | Security | Must | User Management, Authentication & Authorization, Audit & Compliance | Security & Compliance |
| HLR-016 | Implement Rate Limiting for Authentication Endpoints | Security | Must | Authentication & Authorization, User Management | Security & Compliance |
| HLR-017 | Implement Account Lockout Mechanism | Security | Must | Authentication & Authorization | Security & Compliance |
| HLR-018 | Comprehensive Audit Logging for Authentication Events | Security | Should | Authentication & Authorization, Audit & Compliance | Security & Compliance |
| HLR-019 | Reactive Programming with Spring WebFlux | Non-Functional | Should | Authentication & Authorization, User Management, External Integration | Observability & Operations |
| HLR-020 | Kubernetes Deployment with Auto-Scaling | Non-Functional | Must | Infrastructure & Observability | Observability & Operations |

---

## Detailed Requirements

### HLR-001: User Registration with Email Validation

**Description:** The system shall allow new users to register by providing email, password, name, company, and optional profile details. Email must be unique. Password must be hashed with BCrypt before storage. Registration creates a user record with pending_approval status and triggers a pending approval email notification.

**Type:** Functional  
**Priority:** Must  
**Module(s):** User Management, Email Notifications  
**Theme(s):** User Onboarding

**Traceability:**
- `business_process_model.json`: proc_user_registration
- `domain_model.json`: business_entities[0] (User)
- `security_privacy_assessment.json`: sensitive_data_identification (Authentication Credentials - Passwords)

**Acceptance Criteria (High-Level):**
- User can submit registration form with required fields (email, password, firstname, lastname, company)
- System validates email uniqueness and rejects duplicate registrations
- Password is hashed with BCrypt before storage; plain text password is never stored
- User record is created with onboardingStatus=pending_approval and isActive=false
- Pending approval email is sent to user email address

**Forward Work Notes:** Elaborate validation rules for email format, password strength, and required fields. Define error messages for duplicate email and validation failures. Consider adding CAPTCHA for bot prevention.

---

### HLR-002: Admin Onboarding Approval Workflow

**Description:** The system shall allow administrators to review pending user registrations and update onboarding status to approved or rejected. Approved users have accounts activated (isActive=true) and receive approval email. Rejected users remain inactive and receive rejection email with admin comments. All actions are logged in audit trail.

**Type:** Functional  
**Priority:** Must  
**Module(s):** User Management, Email Notifications, Audit & Compliance  
**Theme(s):** User Onboarding

**Traceability:**
- `business_process_model.json`: proc_admin_onboarding_approval
- `domain_model.json`: business_entities[0] (User), business_entities[3] (ActionHistory)
- `security_privacy_assessment.json`: authorization_model_summary (Onboarding Workflow with Admin Approval)

**Acceptance Criteria (High-Level):**
- Admin can view list of pending user registrations with pagination and filtering
- Admin can update onboarding status to approved, rejected, or pending_approval with optional comments
- Approved users have isActive set to true and receive approval email
- Rejected users remain inactive (isActive=false) and receive rejection email with admin comments
- All onboarding status changes are logged in action_history table with admin attribution and timestamp

**Forward Work Notes:** Define admin UI/UX for reviewing pending registrations. Specify email template content for approval and rejection notifications. Consider bulk approval/rejection for efficiency.

---

### HLR-003: JWT-Based User Authentication

**Description:** The system shall authenticate users via email and password credentials. Successful authentication generates a short-lived access token (30 min) and a long-lived refresh token (24 hours). Tokens are signed with HS256 and stored in database with expiration and revocation flags. Old tokens are revoked on login. Authentication fails if user is not approved, account is deactivated, or credentials are invalid.

**Type:** Functional  
**Priority:** Must  
**Module(s):** Authentication & Authorization  
**Theme(s):** Authentication & Session

**Traceability:**
- `business_process_model.json`: proc_user_login
- `security_privacy_assessment.json`: authorization_model_summary (JWT-based Authentication with Access and Refresh Tokens)
- `data_model.json`: logical_models (AccessToken, RefreshToken)

**Acceptance Criteria (High-Level):**
- User can submit login request with email and password
- System validates credentials against stored BCrypt hash
- System checks user onboarding status (must be approved) and account activation (must be active)
- Successful login generates new access token (30 min validity) and refresh token (24 hours validity)
- Old access and refresh tokens are revoked (revoked=true) on login
- Login fails with appropriate error message if user not found, not approved, deactivated, or invalid password

**Forward Work Notes:** Elaborate error messages for different failure scenarios. Define token payload structure (claims). Consider adding login attempt tracking for security monitoring.

---

### HLR-004: Token Refresh Workflow

**Description:** The system shall allow users to obtain a new access token using a valid refresh token without re-authenticating. Refresh token must be validated for signature, expiration, and revocation status. Successful refresh generates a new access token with 30-minute validity.

**Type:** Functional  
**Priority:** Must  
**Module(s):** Authentication & Authorization  
**Theme(s):** Authentication & Session

**Traceability:**
- `business_process_model.json`: proc_token_refresh
- `security_privacy_assessment.json`: authorization_model_summary (JWT-based Authentication with Access and Refresh Tokens)

**Acceptance Criteria (High-Level):**
- User can submit refresh token to obtain new access token
- System validates refresh token signature, expiration, and revocation status
- Valid refresh token generates new access token with 30-minute validity
- Invalid, expired, or revoked refresh token returns error; user must re-authenticate

**Forward Work Notes:** Define error messages for invalid refresh token scenarios. Consider refresh token rotation (issue new refresh token on each refresh) for enhanced security.

---

### HLR-005: Token Revocation for Logout

**Description:** The system shall allow users to revoke their access tokens and refresh tokens to log out. Revoked tokens are marked with revoked=true in database and cannot be used for authentication.

**Type:** Functional  
**Priority:** Must  
**Module(s):** Authentication & Authorization  
**Theme(s):** Authentication & Session

**Traceability:**
- `repository_summary.json`: entry_points (AuthRestController: /revoke-access-token, /revoke-refresh-token)
- `security_privacy_assessment.json`: authorization_model_summary (JWT-based Authentication with Access and Refresh Tokens)

**Acceptance Criteria (High-Level):**
- User can submit access token or refresh token to revoke
- System marks token as revoked (revoked=true) in database
- Revoked tokens cannot be used for authentication or token refresh
- Revocation is idempotent (revoking already revoked token succeeds)

**Forward Work Notes:** Define logout flow (revoke both access and refresh tokens). Consider global logout (revoke all tokens for user) for security incident response.

---

### HLR-006: Role-Based Access Control (RBAC)

**Description:** The system shall enforce role-based access control with CUSTOMER and ADMIN roles. Admin endpoints (/api/v1/admin/**) require ADMIN role. JWT token filter validates role claims before allowing access. Unauthorized access attempts return 403 Forbidden.

**Type:** Functional  
**Priority:** Must  
**Module(s):** Authentication & Authorization  
**Theme(s):** Security & Compliance

**Traceability:**
- `security_privacy_assessment.json`: authorization_model_summary (Role-Based Access Control)
- `repository_summary.json`: security_configuration (protected_endpoints, public_endpoints)

**Acceptance Criteria (High-Level):**
- Admin endpoints (/api/v1/admin/**) require ADMIN role in JWT token
- Public endpoints (login, register, health check, etc.) do not require authentication
- JWT token filter validates role claims before allowing access to protected endpoints
- Unauthorized access attempts (missing token, invalid role) return 403 Forbidden with error message

**Forward Work Notes:** Define granular permissions within ADMIN role if needed (e.g., read-only admin). Consider adding more roles (e.g., MANAGER) for future requirements.

---

### HLR-007: Admin Account Activation and Deactivation

**Description:** The system shall allow administrators to activate or deactivate user accounts. Deactivated accounts cannot authenticate. Account status changes are logged in audit trail. No email notification is sent for account status changes.

**Type:** Functional  
**Priority:** Must  
**Module(s):** User Management, Audit & Compliance  
**Theme(s):** User Onboarding

**Traceability:**
- `business_process_model.json`: proc_admin_account_management
- `domain_model.json`: business_entities[0] (User: isActive field)
- `data_model.json`: logical_models (ActionHistory)

**Acceptance Criteria (High-Level):**
- Admin can update user account status to activated (isActive=true) or deactivated (isActive=false)
- Deactivated users cannot authenticate (login fails with error message)
- Account status changes are logged in action_history table with admin attribution and timestamp
- No email notification is sent to user on account status change

**Forward Work Notes:** Consider adding email notification for account deactivation to inform users. Define admin UI/UX for account management.

---

### HLR-008: Admin Role Management

**Description:** The system shall allow administrators to change user roles (CUSTOMER or ADMIN). Role changes are logged in audit trail. No email notification is sent for role changes.

**Type:** Functional  
**Priority:** Must  
**Module(s):** User Management, Audit & Compliance  
**Theme(s):** User Onboarding

**Traceability:**
- `business_process_model.json`: proc_admin_role_management
- `domain_model.json`: business_entities[0] (User: role field)
- `data_model.json`: logical_models (ActionHistory)

**Acceptance Criteria (High-Level):**
- Admin can update user role to CUSTOMER or ADMIN
- Role changes take effect immediately (user's next token refresh reflects new role)
- Role changes are logged in action_history table with admin attribution and timestamp
- No email notification is sent to user on role change

**Forward Work Notes:** Consider adding email notification for role changes to inform users. Define role change validation rules (e.g., prevent self-demotion).

---

### HLR-009: Comprehensive Audit Logging for Admin Actions

**Description:** The system shall record all administrative actions on user accounts (onboarding status changes, account activation/deactivation, role changes) in an audit log with timestamps, admin attribution, user details, action type, and optional comments. Audit logs are retrievable via paginated API with filtering by date range, action type, and user email.

**Type:** Functional  
**Priority:** Must  
**Module(s):** Audit & Compliance  
**Theme(s):** Security & Compliance

**Traceability:**
- `business_process_model.json`: proc_admin_onboarding_approval (step: Log Action in Audit Trail), proc_admin_account_management (step: Log Action in Audit Trail), proc_admin_role_management (step: Log Action in Audit Trail)
- `data_model.json`: logical_models (ActionHistory)
- `security_privacy_assessment.json`: sensitive_data_identification (Audit and Compliance Data)

**Acceptance Criteria (High-Level):**
- All admin actions (onboarding status, account status, role changes) are logged in action_history table
- Audit log entries include: admin email, user email, action type, action value, admin comments, timestamp
- Audit logs are retrievable via GET /api/v1/admin/action-history with pagination and filtering
- Filtering supports date range, action type, and user email

**Forward Work Notes:** Define audit log retention policy. Consider adding audit log export functionality for compliance reporting.

---

### HLR-010: Email Notifications for Onboarding Workflow

**Description:** The system shall send automated email notifications to users at each onboarding stage: pending approval (after registration), approved (after admin approval), rejected (after admin rejection with comments). Emails use FreeMarker templates and are sent via Gmail SMTP.

**Type:** Functional  
**Priority:** Must  
**Module(s):** Email Notifications  
**Theme(s):** User Onboarding

**Traceability:**
- `business_process_model.json`: proc_user_registration (step: Send Pending Approval Email), proc_admin_onboarding_approval (step: Send Email Notification)
- `integration_catalog.json`: touchpoint_enrichments (Gmail SMTP)
- `repository_summary.json`: service_layer (EmailService)

**Acceptance Criteria (High-Level):**
- Pending approval email is sent to user after registration
- Approval email is sent to user after admin approves registration
- Rejection email with admin comments is sent to user after admin rejects registration
- Emails use FreeMarker templates (email-template-pending-approval.ftl, email-template-approved.ftl, email-template-rejected.ftl)
- Email failures are logged but do not roll back user registration or status updates

**Forward Work Notes:** Define email template content and branding. Consider adding email delivery status tracking. Implement retry logic for transient email failures.

---

### HLR-011: OneView API Integration for Performance Data

**Description:** The system shall integrate with external OneView API to fetch project performance metrics and RAG (Red-Amber-Green) status data. Integration includes OneView authentication (username/password) and token-based API calls. Performance data is retrieved on-demand via GET /performance endpoint.

**Type:** Integration  
**Priority:** Should  
**Module(s):** External Integration  
**Theme(s):** External Integration

**Traceability:**
- `integration_catalog.json`: touchpoint_enrichments (OneView API)
- `business_process_model.json`: application_purpose_summary.primary_capabilities (External Performance Data Integration)
- `repository_summary.json`: entry_points (PerformaceController)

**Acceptance Criteria (High-Level):**
- System authenticates with OneView API using username/password to obtain access token
- System fetches performance data via GET /ava/oneview/internal/api/dashboard/rag/list with query params
- Performance data is returned to user via GET /performance endpoint
- System handles OneView API failures gracefully (fallback to previous day's data if current day returns empty)

**Forward Work Notes:** Externalize OneView credentials to environment variables or secrets manager (critical security fix). Define error handling and retry logic for OneView API failures. Consider caching performance data to reduce API calls.

---

### HLR-012: Health Check and Actuator Endpoints

**Description:** The system shall provide health check endpoint (/healthcheck) and Spring Actuator endpoints (/actuator/health, /actuator/info, /actuator/prometheus, /actuator/metrics) for monitoring, alerting, and operational visibility.

**Type:** Non-Functional  
**Priority:** Must  
**Module(s):** Infrastructure & Observability  
**Theme(s):** Observability & Operations

**Traceability:**
- `repository_summary.json`: entry_points (Healthcheck), configuration (actuator_endpoints)
- `integration_catalog.json`: touchpoint_enrichments (Prometheus)

**Acceptance Criteria (High-Level):**
- GET /healthcheck returns 200 OK with application health status
- GET /actuator/health returns Spring Boot health indicators
- GET /actuator/prometheus returns Prometheus-formatted metrics
- GET /actuator/metrics returns available metrics list
- Prometheus scrape endpoint is accessible on port 8090

**Forward Work Notes:** Define custom health indicators for database, external API, and email service. Configure Prometheus scrape interval and retention.

---

### HLR-013: Service Discovery with Netflix Eureka

**Description:** The system shall register with Netflix Eureka service registry on startup and send periodic heartbeats. Service discovery enables dynamic service registration and load balancing in microservices ecosystem.

**Type:** Non-Functional  
**Priority:** Should  
**Module(s):** Infrastructure & Observability  
**Theme(s):** Observability & Operations

**Traceability:**
- `integration_catalog.json`: touchpoint_enrichments (Netflix Eureka)
- `repository_summary.json`: configuration (eureka_service_url)

**Acceptance Criteria (High-Level):**
- System registers with Eureka server on startup
- System sends periodic heartbeats to Eureka server
- Service metadata (instance ID, hostname, port) is registered in Eureka
- Eureka client handles failover and retry on connection failures

**Forward Work Notes:** Verify Eureka server availability and configuration. Consider alternative service discovery mechanisms (e.g., Kubernetes service discovery) if Eureka is deprecated.

---

### HLR-014: Externalize Credentials to Secrets Manager

**Description:** The system shall externalize all credentials (database, SMTP, JWT secrets, OneView API) from source code and configuration files to environment variables or AWS Secrets Manager. This addresses critical security vulnerability of hardcoded credentials.

**Type:** Security  
**Priority:** Must  
**Module(s):** Authentication & Authorization, Email Notifications, External Integration  
**Theme(s):** Security & Compliance

**Traceability:**
- `security_privacy_assessment.json`: security_smells (Hardcoded Credentials, Credentials in Configuration Files), data_handling_recommendations
- `architecture_summary.json`: operational_and_risk.residual_risks (Hardcoded Credentials in Source Code and Configuration Files)

**Acceptance Criteria (High-Level):**
- OneView API credentials are removed from OneViewServiceImpl.java and stored in environment variables or AWS Secrets Manager
- Database credentials are removed from application-dev.properties and stored in environment variables or AWS Secrets Manager
- SMTP credentials are removed from application-dev.properties and stored in environment variables or AWS Secrets Manager
- JWT secret is removed from application-dev.properties and stored in environment variables or AWS Secrets Manager
- Application reads credentials from environment variables or AWS Secrets Manager at runtime

**Forward Work Notes:** Implement AWS Secrets Manager integration or use Kubernetes secrets. Update deployment pipeline to inject secrets at runtime. Rotate credentials after externalization.

---

### HLR-015: Enable SSL/TLS for Database Connection

**Description:** The system shall enable SSL/TLS encryption for MySQL database connections to protect sensitive data in transit. This addresses high-severity security gap of missing SSL/TLS for database.

**Type:** Security  
**Priority:** Must  
**Module(s):** User Management, Authentication & Authorization, Audit & Compliance  
**Theme(s):** Security & Compliance

**Traceability:**
- `security_privacy_assessment.json`: security_smells (Missing SSL/TLS for Database Connection), data_handling_recommendations
- `architecture_summary.json`: operational_and_risk.residual_risks (Missing SSL/TLS for Database Connection)

**Acceptance Criteria (High-Level):**
- JDBC URL includes SSL parameters (e.g., useSSL=true, requireSSL=true)
- Database connection uses SSL/TLS encryption
- Application verifies database server certificate

**Forward Work Notes:** Update JDBC URL in application properties. Verify AWS RDS MySQL SSL/TLS configuration. Test database connection with SSL/TLS enabled.

---

### HLR-016: Implement Rate Limiting for Authentication Endpoints

**Description:** The system shall implement rate limiting on authentication endpoints (/login, /register) to prevent brute force attacks, credential stuffing, and denial of service. This addresses high-severity security gap of no rate limiting.

**Type:** Security  
**Priority:** Must  
**Module(s):** Authentication & Authorization, User Management  
**Theme(s):** Security & Compliance

**Traceability:**
- `security_privacy_assessment.json`: security_smells (No Rate Limiting), data_handling_recommendations
- `architecture_summary.json`: operational_and_risk.residual_risks (No Rate Limiting or Account Lockout)

**Acceptance Criteria (High-Level):**
- Login endpoint (/login) is rate-limited to N requests per IP address per time window (e.g., 5 requests per minute)
- Registration endpoint (/register) is rate-limited to N requests per IP address per time window (e.g., 3 requests per hour)
- Exceeded rate limit returns 429 Too Many Requests with retry-after header
- Rate limiting is configurable via application properties

**Forward Work Notes:** Implement rate limiting using Spring framework (e.g., Bucket4j) or API gateway. Define rate limit thresholds based on expected traffic patterns. Consider distributed rate limiting for multi-instance deployments.

---

### HLR-017: Implement Account Lockout Mechanism

**Description:** The system shall implement account lockout mechanism after N failed login attempts to prevent brute force attacks. Locked accounts are automatically unlocked after a time period or manually unlocked by admin. This addresses high-severity security gap of no account lockout.

**Type:** Security  
**Priority:** Must  
**Module(s):** Authentication & Authorization  
**Theme(s):** Security & Compliance

**Traceability:**
- `security_privacy_assessment.json`: security_smells (No Account Lockout), data_handling_recommendations
- `architecture_summary.json`: operational_and_risk.residual_risks (No Rate Limiting or Account Lockout)

**Acceptance Criteria (High-Level):**
- System tracks failed login attempts per user account
- Account is locked after N failed login attempts (e.g., 5 attempts)
- Locked account cannot authenticate until unlocked
- Account is automatically unlocked after time period (e.g., 30 minutes) or manually unlocked by admin
- Failed login attempts and account lockout events are logged in audit trail

**Forward Work Notes:** Define lockout threshold and duration. Implement failed login attempt tracking in database. Add admin endpoint for manual account unlock. Consider CAPTCHA after N failed attempts before lockout.

---

### HLR-018: Comprehensive Audit Logging for Authentication Events

**Description:** The system shall log all authentication events (successful logins, failed login attempts, token refresh, token revocation) with timestamps, user email, IP address, and user agent for security monitoring and incident response. This addresses medium-severity security gap of no audit logging for authentication events.

**Type:** Security  
**Priority:** Should  
**Module(s):** Authentication & Authorization, Audit & Compliance  
**Theme(s):** Security & Compliance

**Traceability:**
- `security_privacy_assessment.json`: security_smells (No Audit Logging for Authentication Events), data_handling_recommendations
- `architecture_summary.json`: operational_and_risk.residual_risks (Minimal Test Coverage)

**Acceptance Criteria (High-Level):**
- Successful login events are logged with user email, timestamp, IP address, user agent
- Failed login attempts are logged with user email (if provided), timestamp, IP address, user agent, failure reason
- Token refresh events are logged with user email, timestamp
- Token revocation events are logged with user email, timestamp
- Authentication logs are retrievable via admin API with pagination and filtering

**Forward Work Notes:** Define authentication log schema and storage (separate table or extend action_history). Implement log retrieval API. Consider integration with SIEM for security monitoring.

---

### HLR-019: Reactive Programming with Spring WebFlux

**Description:** The system shall use Spring WebFlux for reactive, non-blocking I/O to support high concurrency and scalability. Reactive programming is applied to REST controllers, service layer, and database access where applicable.

**Type:** Non-Functional  
**Priority:** Should  
**Module(s):** Authentication & Authorization, User Management, External Integration  
**Theme(s):** Observability & Operations

**Traceability:**
- `repository_summary.json`: application_type (Reactive Web Service), technology_stack (reactive_stack)
- `architecture_summary.json`: logical_architecture.notable_patterns (Reactive Programming)

**Acceptance Criteria (High-Level):**
- REST controllers use Mono and Flux for reactive responses
- Service layer methods return Mono or Flux where applicable
- Database access uses reactive repositories (e.g., CustomerDaoImpl with Mono<UserEntity>)
- External API calls (OneView) use reactive WebClient or RestTemplate with reactive support

**Forward Work Notes:** Ensure all blocking operations are offloaded to separate thread pools. Verify reactive programming best practices (e.g., avoid blocking in reactive chains). Consider full reactive stack (R2DBC for database) for maximum performance.

---

### HLR-020: Kubernetes Deployment with Auto-Scaling

**Description:** The system shall be deployed on AWS EKS with Kubernetes deployment manifests. Deployment includes 2 replicas, resource limits (CPU, memory), health checks, and Prometheus scrape annotations. Auto-scaling is configured based on CPU/memory utilization.

**Type:** Non-Functional  
**Priority:** Must  
**Module(s):** Infrastructure & Observability  
**Theme(s):** Observability & Operations

**Traceability:**
- `repository_summary.json`: deployment (orchestration: Kubernetes (AWS EKS)), kubernetes_configuration
- `architecture_summary.json`: architectural_drivers (Scalability and Resilience)

**Acceptance Criteria (High-Level):**
- Kubernetes deployment manifest defines 2 replicas
- Resource requests and limits are configured (CPU: 250m, Memory: 512Mi-1024Mi)
- Liveness and readiness probes are configured for health checks
- Prometheus scrape annotations are configured on deployment
- Horizontal Pod Autoscaler (HPA) is configured to scale based on CPU/memory utilization

**Forward Work Notes:** Define auto-scaling thresholds (e.g., scale up at 70% CPU utilization). Verify resource limits are appropriate for expected load. Test rolling updates and zero-downtime deployments.

---

## Non-Functional Requirements and Constraints

The following cross-cutting non-functional requirements and constraints apply to all modules:

| NFR ID | Category | Description | Priority |
|--------|----------|-------------|----------|
| NFR-001 | Security | All credentials (database, SMTP, JWT secrets, external API) must be externalized to environment variables or AWS Secrets Manager. No credentials in source code or configuration files. | Must |
| NFR-002 | Security | All database connections must use SSL/TLS encryption. JDBC URL must include SSL parameters. | Must |
| NFR-003 | Security | Authentication endpoints (/login, /register) must be rate-limited to prevent brute force attacks and denial of service. | Must |
| NFR-004 | Security | Account lockout mechanism must be implemented after N failed login attempts to prevent brute force attacks. | Must |
| NFR-005 | Security | All authentication events (successful logins, failed attempts, token refresh, revocation) must be logged with timestamps, user email, IP address, and user agent. | Should |
| NFR-006 | Performance | System must support reactive, non-blocking I/O with Spring WebFlux to handle high concurrency and scalability. | Should |
| NFR-007 | Scalability | System must support horizontal scaling with stateless JWT authentication. No server-side session state. | Must |
| NFR-008 | Observability | System must expose Prometheus metrics via /actuator/prometheus endpoint for monitoring and alerting. | Must |
| NFR-009 | Observability | System must provide health check endpoints (/healthcheck, /actuator/health) for deployment orchestration and monitoring. | Must |
| NFR-010 | Compliance | All administrative actions on user accounts must be logged in audit trail with timestamps, admin attribution, and action details. | Must |
| NFR-011 | Deployment | System must be deployed on AWS EKS with Kubernetes deployment manifests. Deployment must include resource limits, health checks, and auto-scaling configuration. | Must |
| NFR-012 | Data Retention | Data retention and deletion policies must be implemented for audit logs and user data to comply with GDPR and minimize data exposure risk. | Should |

---

## Traceability Appendix

This section provides a compact mapping of requirements to source artifacts for traceability.

| Req ID | Source Artifacts |
|--------|------------------|
| HLR-001 | business_process_model.json (proc_user_registration), domain_model.json (User), security_privacy_assessment.json (Passwords) |
| HLR-002 | business_process_model.json (proc_admin_onboarding_approval), domain_model.json (User, ActionHistory), security_privacy_assessment.json (Onboarding Workflow) |
| HLR-003 | business_process_model.json (proc_user_login), security_privacy_assessment.json (JWT Authentication), data_model.json (AccessToken, RefreshToken) |
| HLR-004 | business_process_model.json (proc_token_refresh), security_privacy_assessment.json (JWT Authentication) |
| HLR-005 | repository_summary.json (AuthRestController), security_privacy_assessment.json (JWT Authentication) |
| HLR-006 | security_privacy_assessment.json (RBAC), repository_summary.json (security_configuration) |
| HLR-007 | business_process_model.json (proc_admin_account_management), domain_model.json (User), data_model.json (ActionHistory) |
| HLR-008 | business_process_model.json (proc_admin_role_management), domain_model.json (User), data_model.json (ActionHistory) |
| HLR-009 | business_process_model.json (proc_admin_onboarding_approval, proc_admin_account_management, proc_admin_role_management), data_model.json (ActionHistory), security_privacy_assessment.json (Audit Data) |
| HLR-010 | business_process_model.json (proc_user_registration, proc_admin_onboarding_approval), integration_catalog.json (Gmail SMTP), repository_summary.json (EmailService) |
| HLR-011 | integration_catalog.json (OneView API), business_process_model.json (External Performance Data Integration), repository_summary.json (PerformaceController) |
| HLR-012 | repository_summary.json (Healthcheck, actuator_endpoints), integration_catalog.json (Prometheus) |
| HLR-013 | integration_catalog.json (Netflix Eureka), repository_summary.json (eureka_service_url) |
| HLR-014 | security_privacy_assessment.json (Hardcoded Credentials, data_handling_recommendations), architecture_summary.json (residual_risks) |
| HLR-015 | security_privacy_assessment.json (Missing SSL/TLS, data_handling_recommendations), architecture_summary.json (residual_risks) |
| HLR-016 | security_privacy_assessment.json (No Rate Limiting, data_handling_recommendations), architecture_summary.json (residual_risks) |
| HLR-017 | security_privacy_assessment.json (No Account Lockout, data_handling_recommendations), architecture_summary.json (residual_risks) |
| HLR-018 | security_privacy_assessment.json (No Audit Logging for Authentication Events, data_handling_recommendations), architecture_summary.json (residual_risks) |
| HLR-019 | repository_summary.json (Reactive Web Service, reactive_stack), architecture_summary.json (Reactive Programming) |
| HLR-020 | repository_summary.json (deployment, kubernetes_configuration), architecture_summary.json (Scalability and Resilience) |

---

## Limits and Unknowns

The following limits and unknowns were identified during reverse engineering and should be addressed by forward engineering agents:

1. **Tech debt analysis not available:** No `tech_debt_risk.json` artifact found. Forward engineering agents should assess technical debt independently.
2. **Review resolution not available:** No `review_resolution.json` artifact found. Forward engineering agents should validate requirements with stakeholders.
3. **Password reset/forgot password functionality is out of scope:** Not implemented in as-is system. Forward engineering agents should consider adding this feature.
4. **Multi-factor authentication (MFA) is out of scope:** Not implemented in as-is system. Forward engineering agents should consider adding MFA for enhanced security.
5. **API documentation (Swagger/OpenAPI) is out of scope:** Not implemented in as-is system. Forward engineering agents should add API documentation.
6. **Database migration tooling (Flyway/Liquibase) is out of scope:** Not implemented in as-is system. Forward engineering agents should add database migration tooling for schema evolution.
7. **Caching layer (Redis) is out of scope:** Not implemented in as-is system. Forward engineering agents should consider adding caching for performance optimization.
8. **Distributed tracing (Sleuth, Zipkin) is out of scope:** Not implemented in as-is system. Forward engineering agents should consider adding distributed tracing for observability.
9. **Test coverage is minimal in as-is system:** Forward engineering agents must implement comprehensive unit and integration tests.
10. **Email notification failures do not roll back transactions in as-is system:** Forward engineering agents should consider transactional email sending or compensating transactions.
11. **OneView API integration details are limited:** No API specification available. Forward engineering agents should obtain OneView API documentation for detailed integration design.
12. **Client-related fields (clientId, clientName, clientOneView) in UserEntity have unclear business purpose:** Forward engineering agents should clarify business requirements for these fields.

---

## Metadata

**Generated At (UTC):** 2025-01-16T12:00:00Z  
**Read Branch:** aava/reverse-index  
**Write Branch:** aava/reverse-index  
**Inputs Consumed:** repository_summary.json, dependency_graph.json, domain_model.json, integration_catalog.json, data_model.json, security_privacy_assessment.json, business_process_model.json, architecture_summary.json  
**Missing Optional Inputs:** tech_debt_risk.json, review_resolution.json

---

**End of Document**