# Requirements Backlog

## Introduction

This document presents the **requirements backlog** for the COVE User Service, derived from reverse-engineered system intelligence. It provides a clean, traceable backlog for downstream autonomous implementation pipelines.

**Purpose:** This backlog serves as the authoritative source of high-level requirements for forward engineering agents. Each requirement is linked to source artifacts and includes stable IDs (`req_id`, `module_id`) for traceability.

**How to use:**
- **Module IDs** (`module_id`) identify coarse-grained system boundaries (e.g., `mod_user_management`, `mod_authentication_authorization`).
- **Requirement IDs** (`req_id`) uniquely identify each requirement (e.g., `REQ-USR-001`, `REQ-AUTH-001`).
- **Traceability** links each requirement to source artifacts (e.g., `business_process_model.json`, `security_privacy_assessment.json`).
- **Acceptance Criteria** provide testable conditions for each requirement.
- **Assumptions/Unknowns** flag gaps or speculative elements.

---

## Module Breakdown

| Module ID | Module Name | Summary | Key Dependencies | Source Artifacts |
|-----------|-------------|---------|------------------|------------------|
| `mod_user_management` | User Management | Handles user registration, profile management, onboarding workflow (pending_approval → approved/reject), account activation/deactivation, role assignment (CUSTOMER, ADMIN), and user search with pagination. | `mod_authentication_authorization`, `mod_email_notifications`, `mod_audit_compliance` | `domain_model.json` (bounded_contexts[0]), `business_process_model.json` (proc_user_registration, proc_admin_onboarding_approval), `data_model.json` (UserEntity) |
| `mod_authentication_authorization` | Authentication & Authorization | Provides JWT-based stateless authentication with short-lived access tokens (30 min) and long-lived refresh tokens (24 hours). Handles user login, token generation, validation, refresh, and revocation. Enforces role-based access control (RBAC) for admin endpoints. | `mod_user_management` | `domain_model.json` (bounded_contexts[1]), `business_process_model.json` (proc_user_login, proc_token_refresh), `security_privacy_assessment.json` (authorization_model_summary), `data_model.json` (AccessToken, RefreshToken) |
| `mod_audit_compliance` | Audit & Compliance | Records all administrative actions on user accounts (onboarding status changes, account activation/deactivation, role changes) with timestamps and admin attribution. Provides paginated audit log retrieval with filtering. | `mod_user_management` | `domain_model.json` (bounded_contexts[2]), `data_model.json` (ActionHistory), `business_process_model.json` (proc_admin_onboarding_approval, proc_admin_account_management, proc_admin_role_management) |
| `mod_email_notifications` | Email Notifications | Sends automated email notifications to users during onboarding workflow transitions (pending approval, approved, rejected) using FreeMarker templates and Gmail SMTP. | Gmail SMTP (external) | `integration_catalog.json` (Gmail SMTP touchpoint), `business_process_model.json` (proc_user_registration, proc_admin_onboarding_approval), `repository_summary.json` (EmailService) |
| `mod_external_integration` | External Integration | Integrates with external OneView API to fetch project performance metrics and RAG (Red-Amber-Green) status data. Provides a single endpoint for performance data retrieval. | OneView API (external) | `integration_catalog.json` (OneView API touchpoint), `business_process_model.json` (application_purpose_summary.primary_capabilities[4]), `repository_summary.json` (OneViewService) |
| `mod_infrastructure_observability` | Infrastructure & Observability | Provides service discovery via Netflix Eureka, metrics collection via Prometheus, health checks, and AWS EKS deployment with automated CI/CD via AWS CodeBuild. | Netflix Eureka (external), Prometheus (external), AWS ECR (external), AWS EKS (external) | `integration_catalog.json` (Netflix Eureka, Prometheus, AWS ECR, AWS EKS touchpoints), `repository_summary.json` (deployment, kubernetes_configuration, ci_cd_pipeline), `architecture_summary.json` (architectural_drivers) |

---

## Requirements

### Theme: User Lifecycle Management

#### REQ-USR-001: User Registration with Email Validation

**Type:** Functional | **Priority:** Must | **Modules:** `mod_user_management`, `mod_email_notifications`

**Description:** The system shall allow new users to register by providing email, password, name, company, and optional fields (country, designation, client details). Email must be unique. Password must be hashed with BCrypt before storage. A pending approval email notification must be sent upon successful registration.

**Process Refs:** `proc_user_registration`

**Traceability:**
- `business_process_model.json` → `processes[0] (proc_user_registration)`
- `data_model.json` → `logical_models[0] (UserEntity)`
- `security_privacy_assessment.json` → `sensitive_data_identification[0] (BCrypt password hashing)`

**Acceptance Criteria:**
- POST /register endpoint accepts AuthUser payload with required fields (email, password, firstname, lastname, company)
- System validates email uniqueness; returns 400 error if email already exists
- Password is hashed with BCrypt before storage in users table
- User record created with onboardingStatus=pending_approval and isActive=false
- Pending approval email sent to user email address
- System returns 201 Created with success message on successful registration

**Assumptions/Unknowns:**
- Email validation format not specified; assume standard email regex
- Password complexity requirements not specified; assume BCrypt hashing is sufficient

---

#### REQ-USR-002: Admin Onboarding Approval Workflow

**Type:** Functional | **Priority:** Must | **Modules:** `mod_user_management`, `mod_email_notifications`, `mod_audit_compliance`

**Description:** The system shall allow administrators to approve or reject user registrations via PUT /api/v1/admin/{toEmail}/onboarding-status endpoint. Approval sets onboardingStatus=approved and isActive=true. Rejection sets onboardingStatus=reject and isActive=false. Appropriate email notifications (approval or rejection with admin comments) must be sent. All actions must be logged in action_history table.

**Process Refs:** `proc_admin_onboarding_approval`

**Traceability:**
- `business_process_model.json` → `processes[1] (proc_admin_onboarding_approval)`
- `domain_model.json` → `bounded_contexts[0] (User Management)`
- `data_model.json` → `logical_models[3] (ActionHistory)`

**Acceptance Criteria:**
- PUT /api/v1/admin/{toEmail}/onboarding-status endpoint requires ADMIN role
- Admin provides status (approved | reject | pending_approval) and optional description
- System updates user onboardingStatus and isActive fields based on status
- Approval email sent if status=approved; rejection email with description sent if status=reject
- ActionHistory record created with admin email, user email, status, description, actionType=OnBoarding Status
- System returns 200 OK with success message
- System returns 400 error if user not found

---

#### REQ-ADM-001: Admin Account Activation/Deactivation

**Type:** Functional | **Priority:** Must | **Modules:** `mod_user_management`, `mod_audit_compliance`

**Description:** The system shall allow administrators to activate or deactivate user accounts via PUT /api/v1/admin/{toEmail}/account-status endpoint. Activation sets isActive=true; deactivation sets isActive=false. All actions must be logged in action_history table. No email notification is sent for account status changes.

**Process Refs:** `proc_admin_account_management`

**Traceability:**
- `business_process_model.json` → `processes[4] (proc_admin_account_management)`
- `data_model.json` → `logical_models[0] (UserEntity.isActive)`

**Acceptance Criteria:**
- PUT /api/v1/admin/{toEmail}/account-status endpoint requires ADMIN role
- Admin provides status (true for activate, false for deactivate) and optional description
- System updates user isActive field
- ActionHistory record created with admin email, user email, account status, description, actionType=Account Status
- System returns 200 OK with success message
- System returns 400 error if user not found

---

#### REQ-ADM-002: Admin Role Management

**Type:** Functional | **Priority:** Must | **Modules:** `mod_user_management`, `mod_audit_compliance`

**Description:** The system shall allow administrators to change user roles (CUSTOMER or ADMIN) via PUT /api/v1/admin/{toEmail}/role endpoint. All actions must be logged in action_history table. No email notification is sent for role changes.

**Process Refs:** `proc_admin_role_management`

**Traceability:**
- `business_process_model.json` → `processes[5] (proc_admin_role_management)`
- `data_model.json` → `logical_models[0] (UserEntity.role)`

**Acceptance Criteria:**
- PUT /api/v1/admin/{toEmail}/role endpoint requires ADMIN role
- Admin provides role (CUSTOMER | ADMIN) and optional description
- System updates user role field (converted to uppercase)
- ActionHistory record created with admin email, user email, role, description, actionType=Role Status
- System returns 200 OK with success message
- System returns 400 error if user not found

---

#### REQ-ADM-003: Admin User Listing with Pagination

**Type:** Functional | **Priority:** Should | **Modules:** `mod_user_management`

**Description:** The system shall allow administrators to retrieve a paginated list of users via GET /api/v1/admin/users endpoint with optional filtering by onboarding status and role.

**Traceability:**
- `repository_summary.json` → `entry_points[2] (AdminController endpoints)`
- `repository_summary.json` → `repository_layer (UserRepository custom queries)`

**Acceptance Criteria:**
- GET /api/v1/admin/users endpoint requires ADMIN role
- Endpoint accepts pagination parameters (page, size) and optional filters (onboardingStatus, role)
- System returns paginated list of users matching filters
- System returns 200 OK with user list

**Assumptions/Unknowns:**
- Pagination and filtering details inferred from repository summary; not traced in business process model

---

#### REQ-ADM-004: Admin User Details Retrieval

**Type:** Functional | **Priority:** Should | **Modules:** `mod_user_management`

**Description:** The system shall allow administrators to retrieve detailed information for a specific user via GET /api/v1/admin/user-details/{email} endpoint.

**Traceability:**
- `repository_summary.json` → `entry_points[2] (AdminController endpoints)`

**Acceptance Criteria:**
- GET /api/v1/admin/user-details/{email} endpoint requires ADMIN role
- System retrieves user entity by email
- System returns 200 OK with user details
- System returns 404 error if user not found

---

#### REQ-EMAIL-001: Email Notifications for Onboarding Workflow

**Type:** Functional | **Priority:** Must | **Modules:** `mod_email_notifications`

**Description:** The system shall send automated email notifications to users at each stage of the onboarding workflow: pending approval (after registration), approved (after admin approval), and rejected (after admin rejection with comments). Emails shall be sent via Gmail SMTP using FreeMarker templates.

**Process Refs:** `proc_user_registration`, `proc_admin_onboarding_approval`

**Traceability:**
- `integration_catalog.json` → `touchpoint_enrichments[2] (Gmail SMTP)`
- `repository_summary.json` → `service_layer (EmailService)`

**Acceptance Criteria:**
- Pending approval email sent to user email after successful registration
- Approval email sent to user email after admin approves onboarding
- Rejection email with admin comments sent to user email after admin rejects onboarding
- Emails rendered using FreeMarker templates (email-template-pending-approval.ftl, email-template-approved.ftl, email-template-rejected.ftl)
- Emails sent via Gmail SMTP (smtp.gmail.com:465) with SSL
- Email send failures throw CustomException with code 451 but do not roll back user registration

**Assumptions/Unknowns:**
- Email template content not analyzed; assume templates exist and are correctly formatted

---

### Theme: Authentication & Security

#### REQ-AUTH-001: JWT-Based User Login

**Type:** Functional | **Priority:** Must | **Modules:** `mod_authentication_authorization`

**Description:** The system shall authenticate users via POST /login endpoint with email and password. Upon successful authentication, the system shall generate a new access token (30-minute validity) and refresh token (24-hour validity), revoke all existing tokens for the user, store new tokens in database, and return them in the response. Login must fail if user is not approved, account is deactivated, or credentials are invalid.

**Process Refs:** `proc_user_login`

**Traceability:**
- `business_process_model.json` → `processes[2] (proc_user_login)`
- `security_privacy_assessment.json` → `authorization_model_summary (JWT-based Authentication)`
- `data_model.json` → `logical_models[1,2] (AccessToken, RefreshToken)`

**Acceptance Criteria:**
- POST /login endpoint accepts AuthenticationRequest payload with email and password
- System validates user exists; returns 403 error if not found
- System checks onboardingStatus; returns 403 error if pending_approval or reject
- System checks isActive flag; returns 403 error if false
- System validates password against BCrypt hash; returns 403 error if invalid
- System revokes all existing access and refresh tokens for user (sets revoked=true)
- System generates new access token (30-minute validity) and refresh token (24-hour validity) signed with HS256
- New tokens stored in access_token and refresh_token tables with expired=false, revoked=false
- System returns 200 OK with Token payload (email, accessToken, refreshToken)

**Assumptions/Unknowns:**
- JWT secret key management not specified; assume configured in application properties

---

#### REQ-AUTH-002: Token Refresh Workflow

**Type:** Functional | **Priority:** Must | **Modules:** `mod_authentication_authorization`

**Description:** The system shall allow users to obtain a new access token using a valid refresh token via POST /refresh-token endpoint. The system shall validate the refresh token signature, expiration, and revocation status before generating a new access token.

**Process Refs:** `proc_token_refresh`

**Traceability:**
- `business_process_model.json` → `processes[3] (proc_token_refresh)`
- `security_privacy_assessment.json` → `authorization_model_summary (JWT-based Authentication)`

**Acceptance Criteria:**
- POST /refresh-token endpoint accepts refresh token parameter
- System validates refresh token signature, expiration, and revocation status
- System returns error if refresh token is invalid, expired, or revoked
- System generates new access token (30-minute validity) with user email from refresh token
- System returns 200 OK with RefreshTokenResponse containing new access token

**Assumptions/Unknowns:**
- Refresh token validation logic details partially inferred; assume JwtRefreshTokenUtil handles validation

---

#### REQ-AUTH-003: Token Revocation

**Type:** Functional | **Priority:** Should | **Modules:** `mod_authentication_authorization`

**Description:** The system shall allow users to revoke access tokens and refresh tokens via POST /revoke-access-token and POST /revoke-refresh-token endpoints. Revocation sets the revoked flag to true in the database, preventing further use of the token.

**Traceability:**
- `repository_summary.json` → `entry_points[1] (AuthRestController endpoints)`
- `data_model.json` → `logical_models[1,2] (AccessToken, RefreshToken)`

**Acceptance Criteria:**
- POST /revoke-access-token endpoint accepts access token parameter
- POST /revoke-refresh-token endpoint accepts refresh token parameter
- System sets revoked=true for the specified token in database
- System returns success response
- Revoked tokens cannot be used for authentication or refresh

**Assumptions/Unknowns:**
- Token revocation endpoint behavior not fully traced in business process model; inferred from repository summary

---

#### REQ-AUTH-004: Role-Based Access Control (RBAC)

**Type:** Security | **Priority:** Must | **Modules:** `mod_authentication_authorization`

**Description:** The system shall enforce role-based access control for admin endpoints (/api/v1/admin/**). Only users with ADMIN role shall be allowed to access these endpoints. JWT token filter shall validate role claims before allowing access.

**Traceability:**
- `security_privacy_assessment.json` → `authorization_model_summary (Role-Based Access Control)`
- `repository_summary.json` → `security_configuration (protected_endpoints)`

**Acceptance Criteria:**
- JwtTokenAuthenticationFilter validates JWT token for all requests
- Admin endpoints (/api/v1/admin/**) require ADMIN role in JWT token claims
- System returns 403 Forbidden if user does not have ADMIN role
- Public endpoints (login, register, etc.) do not require authentication

---

#### REQ-SEC-001: Secure Credential Storage

**Type:** Security | **Priority:** Must | **Modules:** `mod_authentication_authorization`, `mod_email_notifications`, `mod_external_integration`

**Description:** The system shall store all credentials (database, SMTP, JWT secrets, external API credentials) securely using environment variables or AWS Secrets Manager. Credentials shall not be hardcoded in source code or configuration files.

**Traceability:**
- `security_privacy_assessment.json` → `security_smells (Hardcoded Credentials, Credentials in Configuration Files)`
- `security_privacy_assessment.json` → `data_handling_recommendations[0,1]`

**Acceptance Criteria:**
- Database credentials (username, password) stored in environment variables or AWS Secrets Manager
- SMTP credentials (username, password) stored in environment variables or AWS Secrets Manager
- JWT secret key stored in environment variables or AWS Secrets Manager
- OneView API credentials (username, password) stored in environment variables or AWS Secrets Manager
- No credentials hardcoded in source code or configuration files

**Assumptions/Unknowns:**
- Current implementation has hardcoded credentials in OneViewServiceImpl and application-dev.properties; must be refactored

---

#### REQ-SEC-002: SSL/TLS for Database Connection

**Type:** Security | **Priority:** Must | **Modules:** `mod_user_management`, `mod_authentication_authorization`, `mod_audit_compliance`

**Description:** The system shall enable SSL/TLS encryption for MySQL database connections by adding SSL parameters to JDBC URL.

**Traceability:**
- `security_privacy_assessment.json` → `security_smells (Missing SSL/TLS for Database Connection)`
- `security_privacy_assessment.json` → `data_handling_recommendations[2]`

**Acceptance Criteria:**
- JDBC URL includes SSL parameters (e.g., useSSL=true, requireSSL=true)
- Database traffic encrypted with SSL/TLS

**Assumptions/Unknowns:**
- Current implementation does not explicitly configure SSL/TLS for database connection

---

#### REQ-SEC-003: Rate Limiting for Authentication Endpoints

**Type:** Security | **Priority:** Must | **Modules:** `mod_authentication_authorization`, `mod_user_management`

**Description:** The system shall implement rate limiting on authentication endpoints (/login, /register) to prevent brute force attacks, credential stuffing, and denial of service.

**Traceability:**
- `security_privacy_assessment.json` → `security_smells (No Rate Limiting)`
- `security_privacy_assessment.json` → `data_handling_recommendations[3]`

**Acceptance Criteria:**
- Rate limiting applied to POST /login endpoint (e.g., max 5 attempts per minute per IP)
- Rate limiting applied to POST /register endpoint (e.g., max 3 registrations per hour per IP)
- System returns 429 Too Many Requests when rate limit exceeded

**Assumptions/Unknowns:**
- Rate limiting may be implemented at API gateway level or application level; implementation approach to be determined

---

#### REQ-SEC-004: Account Lockout Mechanism

**Type:** Security | **Priority:** Must | **Modules:** `mod_authentication_authorization`

**Description:** The system shall implement account lockout mechanism after multiple failed login attempts to prevent brute force attacks.

**Traceability:**
- `security_privacy_assessment.json` → `security_smells (No Account Lockout)`
- `security_privacy_assessment.json` → `data_handling_recommendations[4]`

**Acceptance Criteria:**
- System tracks failed login attempts per user account
- Account locked after N failed attempts (e.g., 5 attempts)
- Locked account cannot authenticate until unlocked by admin or after timeout period
- System logs failed login attempts for security monitoring

**Assumptions/Unknowns:**
- Lockout threshold and timeout duration to be determined

---

#### REQ-SEC-005: Audit Logging for Authentication Events

**Type:** Security | **Priority:** Should | **Modules:** `mod_authentication_authorization`

**Description:** The system shall log all authentication events (successful and failed logins, token refresh, token revocation) for security monitoring and incident response.

**Traceability:**
- `security_privacy_assessment.json` → `security_smells (No Audit Logging for Authentication Events)`
- `security_privacy_assessment.json` → `data_handling_recommendations[5]`

**Acceptance Criteria:**
- Successful login events logged with user email, timestamp, IP address
- Failed login events logged with attempted email, timestamp, IP address, failure reason
- Token refresh events logged with user email, timestamp
- Token revocation events logged with user email, timestamp, token type

**Assumptions/Unknowns:**
- Log storage and retention policy to be determined

---

### Theme: Audit & Compliance

#### REQ-AUD-001: Audit Logging for Admin Actions

**Type:** Non-Functional | **Priority:** Must | **Modules:** `mod_audit_compliance`

**Description:** The system shall record all administrative actions on user accounts (onboarding status changes, account activation/deactivation, role changes) in the action_history table with timestamps, admin attribution (admin email and name), user details, action type, and optional admin comments.

**Process Refs:** `proc_admin_onboarding_approval`, `proc_admin_account_management`, `proc_admin_role_management`

**Traceability:**
- `data_model.json` → `logical_models[3] (ActionHistory)`
- `security_privacy_assessment.json` → `sensitive_data_identification[4] (Audit and Compliance Data)`

**Acceptance Criteria:**
- ActionHistory record created for every admin action on user accounts
- Record includes: userName, userEmail, userRole, userCompany, updateByAdminName, updateByAdminEmail, registrationId, action, adminComments, actionType, createdAt, updatedAt
- ActionType values: OnBoarding Status, Account Status, Role Status
- Timestamps auto-generated on record creation

---

#### REQ-AUD-002: Audit Log Retrieval with Pagination

**Type:** Functional | **Priority:** Should | **Modules:** `mod_audit_compliance`

**Description:** The system shall allow administrators to retrieve a paginated audit log via GET /api/v1/admin/action-history endpoint with optional filtering by date range, user email, admin email, and action type.

**Traceability:**
- `repository_summary.json` → `entry_points[2] (AdminController endpoints)`
- `repository_summary.json` → `repository_layer (ActionHistoryRepo custom queries)`

**Acceptance Criteria:**
- GET /api/v1/admin/action-history endpoint requires ADMIN role
- Endpoint accepts pagination parameters (page, size) and optional filters (date range, user email, admin email, action type)
- System returns paginated list of ActionHistory records matching filters
- System returns 200 OK with audit log

**Assumptions/Unknowns:**
- Filtering details inferred from repository summary; not traced in business process model

---

### Theme: Data Persistence

#### REQ-DATA-001: User Data Persistence

**Type:** Data | **Priority:** Must | **Modules:** `mod_user_management`

**Description:** The system shall persist user entities in MySQL database (users table) with fields: userId (PK, auto-increment), email (unique, required), password (BCrypt hashed), firstname, lastname, company (required), country, role (CUSTOMER | ADMIN), onboardingStatus (pending_approval | approved | reject), isActive (boolean), registrationId, designation, clientId, clientName, clientOneView, createdAt, lastUpdatedAt, lastUpdatedBy.

**Traceability:**
- `data_model.json` → `logical_models[0] (UserEntity)`
- `repository_summary.json` → `database_schema (users table)`

**Acceptance Criteria:**
- users table created with specified fields and constraints
- email field has unique constraint
- password field stores BCrypt hashed values
- userId is auto-incremented primary key
- createdAt and lastUpdatedAt timestamps auto-generated

**Assumptions/Unknowns:**
- Database schema evolution relies on JPA auto-DDL; no migration scripts detected

---

#### REQ-DATA-002: Token Data Persistence

**Type:** Data | **Priority:** Must | **Modules:** `mod_authentication_authorization`

**Description:** The system shall persist access tokens and refresh tokens in MySQL database (access_token and refresh_token tables) with fields: id (PK, auto-increment), token (TEXT), tokenType (BEARER), expired (boolean), revoked (boolean), createdAt (auto-generated), user (FK to users.userId).

**Traceability:**
- `data_model.json` → `logical_models[1,2] (AccessToken, RefreshToken)`
- `repository_summary.json` → `database_schema (access_token, refresh_token tables)`

**Acceptance Criteria:**
- access_token and refresh_token tables created with specified fields and constraints
- token field stores JWT token strings (TEXT type)
- user field is foreign key to users.userId
- id is auto-incremented primary key
- createdAt timestamp auto-generated

---

#### REQ-DATA-003: Audit Log Data Persistence

**Type:** Data | **Priority:** Must | **Modules:** `mod_audit_compliance`

**Description:** The system shall persist audit log entries in MySQL database (action_history table) with fields: id (PK, auto-increment), userName, userEmail, userRole, userCompany, updateByAdminName, updateByAdminEmail, registrationId, action, adminComments, actionType (OnBoarding Status | Account Status | Role Status), createdAt (auto-generated), updatedAt.

**Traceability:**
- `data_model.json` → `logical_models[3] (ActionHistory)`
- `repository_summary.json` → `database_schema (action_history table)`

**Acceptance Criteria:**
- action_history table created with specified fields
- id is auto-incremented primary key
- createdAt timestamp auto-generated
- actionType values restricted to: OnBoarding Status, Account Status, Role Status

---

### Theme: External Integration

#### REQ-INT-001: OneView API Integration for Performance Data

**Type:** Integration | **Priority:** Should | **Modules:** `mod_external_integration`

**Description:** The system shall integrate with external OneView API to fetch project performance metrics and RAG (Red-Amber-Green) status data via GET /performance endpoint. The system shall authenticate with OneView API using custom token-based authentication (X-Ava-Access-Token header).

**Traceability:**
- `integration_catalog.json` → `touchpoint_enrichments[0] (OneView API)`
- `repository_summary.json` → `service_layer (OneViewService)`

**Acceptance Criteria:**
- GET /performance endpoint retrieves performance data from OneView API
- System authenticates with OneView API via POST /user/signin with username and password
- System obtains X-Ava-Access-Token from login response and includes it in subsequent requests
- System fetches RAG list via GET /ava/oneview/internal/api/dashboard/rag/list with query params
- System returns performance data in PerformanceResponse format
- System falls back to previous day's data if current day returns empty list

**Assumptions/Unknowns:**
- OneView API contract details inferred from implementation code; no API specification available
- Hardcoded credentials in OneViewServiceImpl flagged as critical security risk

---

### Theme: Operations & Infrastructure

#### REQ-OPS-001: Health Check Endpoint

**Type:** Operational | **Priority:** Must | **Modules:** `mod_infrastructure_observability`

**Description:** The system shall provide a health check endpoint (GET /healthcheck) for monitoring application availability and readiness.

**Traceability:**
- `repository_summary.json` → `entry_points[4] (Healthcheck controller)`

**Acceptance Criteria:**
- GET /healthcheck endpoint returns 200 OK when application is healthy
- Endpoint accessible without authentication

---

#### REQ-OPS-002: Prometheus Metrics Exposure

**Type:** Operational | **Priority:** Must | **Modules:** `mod_infrastructure_observability`

**Description:** The system shall expose Prometheus metrics via Spring Actuator endpoint (/actuator/prometheus) for monitoring application health, JVM metrics, and custom metrics.

**Traceability:**
- `integration_catalog.json` → `touchpoint_enrichments[4] (Prometheus)`
- `repository_summary.json` → `configuration (actuator_endpoints)`

**Acceptance Criteria:**
- /actuator/prometheus endpoint returns metrics in Prometheus text format
- Metrics include application metrics, JVM metrics, Spring Boot metrics
- Kubernetes deployment includes Prometheus scrape annotations on port 8090

---

#### REQ-OPS-003: Service Discovery via Netflix Eureka

**Type:** Operational | **Priority:** Should | **Modules:** `mod_infrastructure_observability`

**Description:** The system shall register with Netflix Eureka service discovery for dynamic service registration and discovery in microservices ecosystem.

**Traceability:**
- `integration_catalog.json` → `touchpoint_enrichments[3] (Netflix Eureka)`
- `repository_summary.json` → `configuration (eureka_service_url)`

**Acceptance Criteria:**
- Application registers with Eureka server on startup
- Application sends heartbeat to Eureka server
- Application deregisters from Eureka server on shutdown

---

#### REQ-OPS-004: Kubernetes Deployment with Auto-Scaling

**Type:** Operational | **Priority:** Must | **Modules:** `mod_infrastructure_observability`

**Description:** The system shall be deployed on AWS EKS with Kubernetes deployment manifest specifying 2 replicas, resource limits (CPU: 250m, Memory: 1024Mi), and ClusterIP service on port 80.

**Traceability:**
- `repository_summary.json` → `kubernetes_configuration`
- `integration_catalog.json` → `touchpoint_enrichments[6] (AWS EKS)`

**Acceptance Criteria:**
- Kubernetes Deployment resource created with 2 replicas
- Resource requests: CPU 250m, Memory 512Mi
- Resource limits: CPU 250m, Memory 1024Mi
- ClusterIP service exposes port 80 (maps to container port 8090)
- Prometheus scrape annotations included in deployment manifest

---

#### REQ-OPS-005: Automated CI/CD Pipeline

**Type:** Operational | **Priority:** Must | **Modules:** `mod_infrastructure_observability`

**Description:** The system shall have an automated CI/CD pipeline via AWS CodeBuild that builds Docker image, pushes to AWS ECR, and deploys to AWS EKS with zero-downtime rollout.

**Traceability:**
- `repository_summary.json` → `ci_cd_pipeline`
- `integration_catalog.json` → `touchpoint_enrichments[5,6] (AWS ECR, AWS EKS)`

**Acceptance Criteria:**
- AWS CodeBuild pipeline triggered on code commit
- Pipeline phases: install (Maven), pre_build (ECR login, kubectl config), build (mvn clean install, Docker build/tag), post_build (Docker push, kubectl apply, rollout restart)
- Docker image pushed to AWS ECR
- Kubernetes manifests applied to EKS cluster
- Rollout restart ensures zero-downtime deployment

---

#### REQ-NFR-001: Reactive Programming for Scalability

**Type:** Non-Functional | **Priority:** Must | **Modules:** `mod_user_management`, `mod_authentication_authorization`

**Description:** The system shall use Spring WebFlux reactive programming model for non-blocking I/O to support high concurrency and scalability.

**Traceability:**
- `repository_summary.json` → `application_type (Reactive Web Service)`
- `architecture_summary.json` → `logical_architecture.notable_patterns (Reactive Programming)`

**Acceptance Criteria:**
- Application uses Spring WebFlux with Mono and Flux for reactive streams
- Reactive custom DAO (CustomerDaoImpl) for database queries
- Non-blocking I/O for external integrations (OneView API, Gmail SMTP)

---

#### REQ-NFR-002: Stateless Authentication for Horizontal Scaling

**Type:** Non-Functional | **Priority:** Must | **Modules:** `mod_authentication_authorization`

**Description:** The system shall use stateless JWT authentication to enable horizontal scaling without session affinity requirements.

**Traceability:**
- `security_privacy_assessment.json` → `authorization_model_summary (JWT-based Authentication)`
- `repository_summary.json` → `security_configuration (security_context: NoOpServerSecurityContextRepository)`

**Acceptance Criteria:**
- JWT tokens contain all necessary authentication and authorization information
- No server-side session state maintained
- Tokens validated independently on each request
- Application can scale horizontally without session replication

---

### Theme: Technology Constraints

#### REQ-CONSTRAINT-001: Java 17 and Spring Boot 3.2.5 Technology Stack

**Type:** Constraint | **Priority:** Must | **Modules:** All modules

**Description:** The system shall be built using Java 17, Spring Boot 3.2.5, Spring WebFlux, Spring Security, Spring Data JPA, and MySQL 8.0.33.

**Traceability:**
- `repository_summary.json` → `java_version, framework, technology_stack`

**Acceptance Criteria:**
- Application built with Java 17
- Spring Boot version 3.2.5
- Spring WebFlux for reactive web
- Spring Security for authentication and authorization
- Spring Data JPA for database access
- MySQL 8.0.33 for database

---

#### REQ-CONSTRAINT-002: AWS Cloud Infrastructure

**Type:** Constraint | **Priority:** Must | **Modules:** `mod_infrastructure_observability`

**Description:** The system shall be deployed on AWS cloud infrastructure using AWS EKS for container orchestration, AWS RDS for MySQL database, AWS ECR for Docker image registry, and AWS CodeBuild for CI/CD.

**Traceability:**
- `repository_summary.json` → `deployment (orchestration: Kubernetes (AWS EKS), ci_cd: AWS CodeBuild)`
- `integration_catalog.json` → `touchpoint_enrichments[1,5,6] (AWS RDS MySQL, AWS ECR, AWS EKS)`

**Acceptance Criteria:**
- Application deployed on AWS EKS
- MySQL database hosted on AWS RDS
- Docker images stored in AWS ECR
- CI/CD pipeline runs on AWS CodeBuild

---

## Traceability Matrix

| Req ID | Artifact File | Pointer |
|--------|---------------|----------|
| REQ-USR-001 | business_process_model.json | processes[0] (proc_user_registration) |
| REQ-USR-001 | data_model.json | logical_models[0] (UserEntity) |
| REQ-USR-001 | security_privacy_assessment.json | sensitive_data_identification[0] (BCrypt password hashing) |
| REQ-USR-002 | business_process_model.json | processes[1] (proc_admin_onboarding_approval) |
| REQ-USR-002 | domain_model.json | bounded_contexts[0] (User Management) |
| REQ-USR-002 | data_model.json | logical_models[3] (ActionHistory) |
| REQ-AUTH-001 | business_process_model.json | processes[2] (proc_user_login) |
| REQ-AUTH-001 | security_privacy_assessment.json | authorization_model_summary (JWT-based Authentication) |
| REQ-AUTH-001 | data_model.json | logical_models[1,2] (AccessToken, RefreshToken) |
| REQ-AUTH-002 | business_process_model.json | processes[3] (proc_token_refresh) |
| REQ-AUTH-002 | security_privacy_assessment.json | authorization_model_summary (JWT-based Authentication) |
| REQ-AUTH-003 | repository_summary.json | entry_points[1] (AuthRestController endpoints) |
| REQ-AUTH-003 | data_model.json | logical_models[1,2] (AccessToken, RefreshToken) |
| REQ-AUTH-004 | security_privacy_assessment.json | authorization_model_summary (Role-Based Access Control) |
| REQ-AUTH-004 | repository_summary.json | security_configuration (protected_endpoints) |
| REQ-ADM-001 | business_process_model.json | processes[4] (proc_admin_account_management) |
| REQ-ADM-001 | data_model.json | logical_models[0] (UserEntity.isActive) |
| REQ-ADM-002 | business_process_model.json | processes[5] (proc_admin_role_management) |
| REQ-ADM-002 | data_model.json | logical_models[0] (UserEntity.role) |
| REQ-ADM-003 | repository_summary.json | entry_points[2] (AdminController endpoints) |
| REQ-ADM-003 | repository_summary.json | repository_layer (UserRepository custom queries) |
| REQ-ADM-004 | repository_summary.json | entry_points[2] (AdminController endpoints) |
| REQ-AUD-001 | data_model.json | logical_models[3] (ActionHistory) |
| REQ-AUD-001 | security_privacy_assessment.json | sensitive_data_identification[4] (Audit and Compliance Data) |
| REQ-AUD-002 | repository_summary.json | entry_points[2] (AdminController endpoints) |
| REQ-AUD-002 | repository_summary.json | repository_layer (ActionHistoryRepo custom queries) |
| REQ-EMAIL-001 | integration_catalog.json | touchpoint_enrichments[2] (Gmail SMTP) |
| REQ-EMAIL-001 | repository_summary.json | service_layer (EmailService) |
| REQ-INT-001 | integration_catalog.json | touchpoint_enrichments[0] (OneView API) |
| REQ-INT-001 | repository_summary.json | service_layer (OneViewService) |
| REQ-DATA-001 | data_model.json | logical_models[0] (UserEntity) |
| REQ-DATA-001 | repository_summary.json | database_schema (users table) |
| REQ-DATA-002 | data_model.json | logical_models[1,2] (AccessToken, RefreshToken) |
| REQ-DATA-002 | repository_summary.json | database_schema (access_token, refresh_token tables) |
| REQ-DATA-003 | data_model.json | logical_models[3] (ActionHistory) |
| REQ-DATA-003 | repository_summary.json | database_schema (action_history table) |
| REQ-SEC-001 | security_privacy_assessment.json | security_smells (Hardcoded Credentials, Credentials in Configuration Files) |
| REQ-SEC-001 | security_privacy_assessment.json | data_handling_recommendations[0,1] |
| REQ-SEC-002 | security_privacy_assessment.json | security_smells (Missing SSL/TLS for Database Connection) |
| REQ-SEC-002 | security_privacy_assessment.json | data_handling_recommendations[2] |
| REQ-SEC-003 | security_privacy_assessment.json | security_smells (No Rate Limiting) |
| REQ-SEC-003 | security_privacy_assessment.json | data_handling_recommendations[3] |
| REQ-SEC-004 | security_privacy_assessment.json | security_smells (No Account Lockout) |
| REQ-SEC-004 | security_privacy_assessment.json | data_handling_recommendations[4] |
| REQ-SEC-005 | security_privacy_assessment.json | security_smells (No Audit Logging for Authentication Events) |
| REQ-SEC-005 | security_privacy_assessment.json | data_handling_recommendations[5] |
| REQ-OPS-001 | repository_summary.json | entry_points[4] (Healthcheck controller) |
| REQ-OPS-002 | integration_catalog.json | touchpoint_enrichments[4] (Prometheus) |
| REQ-OPS-002 | repository_summary.json | configuration (actuator_endpoints) |
| REQ-OPS-003 | integration_catalog.json | touchpoint_enrichments[3] (Netflix Eureka) |
| REQ-OPS-003 | repository_summary.json | configuration (eureka_service_url) |
| REQ-OPS-004 | repository_summary.json | kubernetes_configuration |
| REQ-OPS-004 | integration_catalog.json | touchpoint_enrichments[6] (AWS EKS) |
| REQ-OPS-005 | repository_summary.json | ci_cd_pipeline |
| REQ-OPS-005 | integration_catalog.json | touchpoint_enrichments[5,6] (AWS ECR, AWS EKS) |
| REQ-NFR-001 | repository_summary.json | application_type (Reactive Web Service) |
| REQ-NFR-001 | architecture_summary.json | logical_architecture.notable_patterns (Reactive Programming) |
| REQ-NFR-002 | security_privacy_assessment.json | authorization_model_summary (JWT-based Authentication) |
| REQ-NFR-002 | repository_summary.json | security_configuration (security_context: NoOpServerSecurityContextRepository) |
| REQ-CONSTRAINT-001 | repository_summary.json | java_version, framework, technology_stack |
| REQ-CONSTRAINT-002 | repository_summary.json | deployment (orchestration: Kubernetes (AWS EKS), ci_cd: AWS CodeBuild) |
| REQ-CONSTRAINT-002 | integration_catalog.json | touchpoint_enrichments[1,5,6] (AWS RDS MySQL, AWS ECR, AWS EKS) |

---

## Open Questions & Limits

### Open Questions

1. Should rate limiting be implemented at application level or API gateway level?
2. What is the desired account lockout threshold and timeout duration?
3. What is the desired log retention policy for authentication events and audit logs?
4. Should JWT tokens be encrypted at rest in database?
5. What is the desired data retention and deletion policy for user data and audit logs (GDPR compliance)?
6. Should MFA be implemented in a future phase?
7. Should password reset functionality be implemented in a future phase?

### Limits and Unknowns

- Email template content not analyzed; only service-level email sending logic reviewed
- OneView API contract details inferred from implementation code; no API specification available
- Timeout and retry configurations not explicitly set in application code; rely on library defaults
- Database schema relationships inferred from JPA annotations; no database migration scripts or ER diagrams available
- Security controls for infrastructure services (Eureka, Prometheus, ECR, EKS) not visible in application code; may be handled at infrastructure level
- Rate limiting may be implemented at API gateway level (not visible in application code)
- MFA implementation may be planned but not yet implemented
- CORS configuration details not fully visible; marked as low confidence in security assessment

---

## Non-Goals

- Password reset or forgot password functionality
- Multi-factor authentication (MFA)
- API documentation (Swagger/OpenAPI)
- Database migration tooling (Flyway/Liquibase)
- Caching layer (Redis)
- Application-level rate limiting (may be implemented at API gateway level)

---

**End of Requirements Backlog**
