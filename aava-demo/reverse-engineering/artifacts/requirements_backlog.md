# Requirements Backlog

## Introduction

This document presents the **Requirements Backlog** for the COVE User Service, derived from reverse-engineered system intelligence. It provides a clean, traceable backlog for downstream autonomous implementation pipelines.

**Purpose:** This backlog translates reverse-engineered artifacts into high-level, testable requirements with stable IDs and explicit links to evidence. Forward engineering agents can validate scope and avoid hallucinated features.

**How to use:**
- Each requirement has a unique **`req_id`** (e.g., `REQ-FUNC-001`) for traceability.
- Each module has a unique **`module_id`** (e.g., `mod_user_management`) for boundary definition.
- Requirements link to **modules**, **processes**, and **artifacts** for validation.
- **Acceptance criteria** define testable outcomes.
- **Assumptions or unknowns** flag gaps in evidence.

---

## Module Breakdown

| Module ID | Module Name | Summary | Key Dependencies | Source Artifacts |
|-----------|-------------|---------|------------------|------------------|
| `mod_user_management` | User Management Module | Handles user registration, profile management, onboarding workflow (pending_approval → approved/reject), account activation/deactivation, and role assignment (CUSTOMER, ADMIN). Provides user search and pagination capabilities. | mod_authentication_authorization, mod_notification, mod_audit_compliance | repository_summary.json, domain_model.json, business_process_model.json |
| `mod_authentication_authorization` | Authentication & Authorization Module | Provides JWT-based stateless authentication with short-lived access tokens (30 minutes) and long-lived refresh tokens (24 hours). Handles user login, token generation, validation, refresh, and revocation. Implements role-based access control (RBAC) with CUSTOMER and ADMIN roles. Includes password encryption with BCrypt. | mod_user_management, AWS RDS MySQL | repository_summary.json, domain_model.json, security_privacy_assessment.json, business_process_model.json |
| `mod_audit_compliance` | Audit & Compliance Module | Records all administrative actions on user accounts (onboarding status changes, account activation/deactivation, role changes) with timestamps and admin attribution. Provides paginated audit log retrieval with filtering by date range, action type, and user email. | mod_user_management, AWS RDS MySQL | repository_summary.json, domain_model.json, data_model.json |
| `mod_notification` | Notification Module | Sends email notifications to users during onboarding workflow transitions (pending approval, approved, rejected). Uses FreeMarker templates for email content and Gmail SMTP for delivery. | Gmail SMTP | repository_summary.json, integration_catalog.json, business_process_model.json |
| `mod_external_integration` | External Integration Module | Integrates with external OneView API to fetch project performance metrics and RAG (Red-Amber-Green) status data. Provides a single REST endpoint for performance data retrieval. Handles OneView authentication with custom token-based mechanism. | OneView API | repository_summary.json, integration_catalog.json, domain_model.json |
| `mod_data_persistence` | Data Persistence Module | Provides data access layer using Spring Data JPA repositories for CRUD operations on user entities, tokens, and audit logs. Manages database schema with JPA auto-DDL. Includes custom queries with pagination support. | AWS RDS MySQL | repository_summary.json, data_model.json |
| `mod_observability` | Observability Module | Exposes Prometheus metrics via Spring Actuator for application health, JVM metrics, and custom metrics. Provides health check endpoint for Kubernetes liveness/readiness probes. Includes Prometheus scrape annotations in Kubernetes deployment. | Prometheus | repository_summary.json, integration_catalog.json |
| `mod_deployment_infrastructure` | Deployment & Infrastructure Module | Manages containerization with Docker, image storage in AWS ECR, and orchestration on AWS EKS. Includes automated CI/CD pipeline via AWS CodeBuild with build, test, Docker image creation, ECR push, and EKS deployment. Provides service discovery via Netflix Eureka. | AWS ECR, AWS EKS, Netflix Eureka | repository_summary.json, integration_catalog.json, architecture_summary.json |

---

## Requirements

### Functional Requirements

#### REQ-FUNC-001: User Registration
**Title:** User Registration  
**Type:** functional  
**Priority:** must  
**Modules:** mod_user_management  
**Processes:** proc_user_registration  

**Description:**  
The system shall allow new users to register by providing email, password, name, company, and optional fields (country, designation, client details). Email must be unique. Password must be hashed with BCrypt before storage. Registration creates a user record with pending_approval status and isActive=false.

**Acceptance Criteria:**
- User submits registration form with required fields (email, password, firstname, lastname, company)
- System validates email uniqueness; rejects duplicate emails with error message
- System hashes password with BCrypt before storage
- System creates UserEntity with onboardingStatus=pending_approval and isActive=false
- System returns HTTP 201 Created with success message on successful registration

**Traceability:**
- business_process_model.json: business_processes.processes[proc_user_registration]
- repository_summary.json: entry_points[AuthRestController].endpoints[POST /register]

---

#### REQ-FUNC-002: User Registration Email Notification
**Title:** User Registration Email Notification  
**Type:** functional  
**Priority:** must  
**Modules:** mod_notification  
**Processes:** proc_user_registration  

**Description:**  
The system shall send a pending approval email notification to the user after successful registration. Email failure shall not roll back user registration.

**Acceptance Criteria:**
- System sends email to user's registered email address with pending approval message
- Email uses FreeMarker template (email-template-pending-approval.ftl)
- Email failure logs error but does not prevent user registration from succeeding

**Traceability:**
- business_process_model.json: business_processes.processes[proc_user_registration].steps[reg_step_5]
- integration_catalog.json: touchpoint_enrichments.enrichments[Gmail SMTP]

---

#### REQ-FUNC-003: Admin Onboarding Approval
**Title:** Admin Onboarding Approval  
**Type:** functional  
**Priority:** must  
**Modules:** mod_user_management  
**Processes:** proc_admin_onboarding_approval  

**Description:**  
The system shall allow administrators to approve or reject user registrations via PUT /api/v1/admin/{toEmail}/onboarding-status endpoint. Approval sets onboardingStatus=approved and isActive=true. Rejection sets onboardingStatus=reject and isActive=false. Admin can provide optional description/comments.

**Acceptance Criteria:**
- Admin submits onboarding status update with status (approved | reject | pending_approval) and optional description
- System validates admin role via JWT token
- System retrieves user by email; returns error if user not found
- System updates user's onboardingStatus and isActive fields based on status
- System persists updated user entity to database
- System returns HTTP 200 OK with success message

**Traceability:**
- business_process_model.json: business_processes.processes[proc_admin_onboarding_approval]
- repository_summary.json: entry_points[AdminController].endpoints[PUT /api/v1/admin/{toEmail}/onboarding-status]

---

#### REQ-FUNC-004: Admin Onboarding Approval Email Notification
**Title:** Admin Onboarding Approval Email Notification  
**Type:** functional  
**Priority:** must  
**Modules:** mod_notification  
**Processes:** proc_admin_onboarding_approval  

**Description:**  
The system shall send an email notification to the user after admin approves or rejects their registration. Approval email uses email-template-approved.ftl. Rejection email uses email-template-rejected.ftl and includes admin comments.

**Acceptance Criteria:**
- System sends approval email if status=approved
- System sends rejection email with admin comments if status=reject
- System does not send email if status=pending_approval
- Email uses appropriate FreeMarker template

**Traceability:**
- business_process_model.json: business_processes.processes[proc_admin_onboarding_approval].steps[onboard_step_5]
- repository_summary.json: service_layer.services[EmailService].email_templates

---

#### REQ-FUNC-005: User Login
**Title:** User Login  
**Type:** functional  
**Priority:** must  
**Modules:** mod_authentication_authorization  
**Processes:** proc_user_login  

**Description:**  
The system shall allow approved and active users to authenticate via POST /login endpoint with email and password. System validates credentials, checks onboarding status and account activation, revokes old tokens, generates new access token (30 min validity) and refresh token (24 hour validity), and returns tokens to user.

**Acceptance Criteria:**
- User submits login request with email and password
- System retrieves user by email; returns error if user not found
- System checks onboardingStatus; rejects login if not approved
- System checks isActive flag; rejects login if account deactivated
- System validates password with BCrypt; rejects login if password does not match
- System revokes all existing access tokens and refresh tokens for user
- System generates new access token (30 min validity) and refresh token (24 hour validity)
- System stores tokens in database with expiration and revocation flags
- System returns HTTP 200 OK with Token payload (email, accessToken, refreshToken)

**Traceability:**
- business_process_model.json: business_processes.processes[proc_user_login]
- repository_summary.json: entry_points[AuthRestController].endpoints[POST /login]

---

#### REQ-FUNC-006: Token Refresh
**Title:** Token Refresh  
**Type:** functional  
**Priority:** must  
**Modules:** mod_authentication_authorization  
**Processes:** proc_token_refresh  

**Description:**  
The system shall allow users to obtain a new access token using a valid refresh token via POST /refresh-token endpoint. System validates refresh token signature, expiration, and revocation status, then generates and returns a new access token.

**Acceptance Criteria:**
- User submits refresh token via POST /refresh-token endpoint
- System validates refresh token signature, expiration, and revocation status
- System generates new access token with 30-minute validity
- System returns HTTP 200 OK with RefreshTokenResponse containing new access token
- System returns error if refresh token is invalid, expired, or revoked

**Traceability:**
- business_process_model.json: business_processes.processes[proc_token_refresh]
- repository_summary.json: entry_points[AuthRestController].endpoints[POST /refresh-token]

**Assumptions or Unknowns:**
- Token refresh implementation details partially inferred; detailed code for JwtRefreshTokenUtil.generateNewRefreshToken not read

---

#### REQ-FUNC-007: Token Revocation
**Title:** Token Revocation  
**Type:** functional  
**Priority:** must  
**Modules:** mod_authentication_authorization  

**Description:**  
The system shall allow users to revoke access tokens and refresh tokens via POST /revoke-access-token and POST /revoke-refresh-token endpoints. Revocation sets the revoked flag to true in the database.

**Acceptance Criteria:**
- User submits token revocation request with token parameter
- System validates token and sets revoked flag to true in database
- System returns success response
- Revoked tokens cannot be used for authentication or token refresh

**Traceability:**
- repository_summary.json: entry_points[AuthRestController].endpoints[POST /revoke-access-token, POST /revoke-refresh-token]
- security_privacy_assessment.json: authorization_model_summary.patterns[JWT-based Authentication with Access and Refresh Tokens]

**Assumptions or Unknowns:**
- Token revocation logic details inferred from login flow; explicit revocation endpoint behavior not fully traced

---

#### REQ-FUNC-008: Admin Account Management
**Title:** Admin Account Management  
**Type:** functional  
**Priority:** must  
**Modules:** mod_user_management  
**Processes:** proc_admin_account_management  

**Description:**  
The system shall allow administrators to activate or deactivate user accounts via PUT /api/v1/admin/{toEmail}/account-status endpoint. Activation sets isActive=true. Deactivation sets isActive=false. Admin can provide optional description/comments.

**Acceptance Criteria:**
- Admin submits account status update with status (true for activate, false for deactivate) and optional description
- System validates admin role via JWT token
- System retrieves user by email; returns error if user not found
- System updates user's isActive field
- System persists updated user entity to database
- System returns HTTP 200 OK with success message

**Traceability:**
- business_process_model.json: business_processes.processes[proc_admin_account_management]
- repository_summary.json: entry_points[AdminController].endpoints[PUT /api/v1/admin/{toEmail}/account-status]

---

#### REQ-FUNC-009: Admin Role Management
**Title:** Admin Role Management  
**Type:** functional  
**Priority:** must  
**Modules:** mod_user_management  
**Processes:** proc_admin_role_management  

**Description:**  
The system shall allow administrators to change user roles (CUSTOMER or ADMIN) via PUT /api/v1/admin/{toEmail}/role endpoint. Admin can provide optional description/comments.

**Acceptance Criteria:**
- Admin submits role update with role (CUSTOMER | ADMIN) and optional description
- System validates admin role via JWT token
- System retrieves user by email; returns error if user not found
- System updates user's role field (converted to uppercase)
- System persists updated user entity to database
- System returns HTTP 200 OK with success message

**Traceability:**
- business_process_model.json: business_processes.processes[proc_admin_role_management]
- repository_summary.json: entry_points[AdminController].endpoints[PUT /api/v1/admin/{toEmail}/role]

---

#### REQ-FUNC-010: Admin User Listing
**Title:** Admin User Listing  
**Type:** functional  
**Priority:** should  
**Modules:** mod_user_management  

**Description:**  
The system shall allow administrators to retrieve paginated user lists via GET /api/v1/admin/users endpoint with filtering by onboarding status and role.

**Acceptance Criteria:**
- Admin submits user listing request with optional pagination parameters (page, size)
- Admin can filter by onboarding status and role
- System validates admin role via JWT token
- System retrieves paginated user list from database
- System returns HTTP 200 OK with user list and pagination metadata

**Traceability:**
- repository_summary.json: entry_points[AdminController].endpoints[GET /api/v1/admin/users]

**Assumptions or Unknowns:**
- Admin user listing process not included in business process model (read-only query, not a state-changing workflow)

---

#### REQ-FUNC-011: Admin User Details Retrieval
**Title:** Admin User Details Retrieval  
**Type:** functional  
**Priority:** should  
**Modules:** mod_user_management  

**Description:**  
The system shall allow administrators to retrieve detailed user information by email via GET /api/v1/admin/user-details/{email} endpoint.

**Acceptance Criteria:**
- Admin submits user details request with user email
- System validates admin role via JWT token
- System retrieves user entity by email from database
- System returns HTTP 200 OK with user details
- System returns error if user not found

**Traceability:**
- repository_summary.json: entry_points[AdminController].endpoints[GET /api/v1/admin/user-details/{email}]

---

#### REQ-FUNC-012: Audit Log Recording
**Title:** Audit Log Recording  
**Type:** functional  
**Priority:** must  
**Modules:** mod_audit_compliance  
**Processes:** proc_admin_onboarding_approval, proc_admin_account_management, proc_admin_role_management  

**Description:**  
The system shall record all administrative actions on user accounts (onboarding status changes, account activation/deactivation, role changes) in the action_history table with timestamps, admin email, user email, action details, and action type.

**Acceptance Criteria:**
- System creates ActionHistory record after each admin action
- ActionHistory includes admin email, user email, action details, action type, and timestamps
- ActionHistory is persisted to action_history table
- Audit log recording does not prevent admin action from succeeding

**Traceability:**
- data_model.json: logical_models.models[ActionHistory]
- business_process_model.json: business_processes.processes[proc_admin_onboarding_approval].steps[onboard_step_6]

---

#### REQ-FUNC-013: Audit Log Retrieval
**Title:** Audit Log Retrieval  
**Type:** functional  
**Priority:** should  
**Modules:** mod_audit_compliance  

**Description:**  
The system shall allow administrators to retrieve paginated audit logs via GET /api/v1/admin/action-history endpoint with filtering by date range, action type, and user email.

**Acceptance Criteria:**
- Admin submits audit log retrieval request with optional pagination parameters (page, size)
- Admin can filter by date range, action type, and user email
- System validates admin role via JWT token
- System retrieves paginated audit log from database
- System returns HTTP 200 OK with audit log entries and pagination metadata

**Traceability:**
- repository_summary.json: entry_points[AdminController].endpoints[GET /api/v1/admin/action-history]

**Assumptions or Unknowns:**
- Action history retrieval process not included in business process model (read-only query, not a state-changing workflow)

---

#### REQ-FUNC-014: Performance Data Retrieval
**Title:** Performance Data Retrieval  
**Type:** functional  
**Priority:** should  
**Modules:** mod_external_integration  

**Description:**  
The system shall allow users to retrieve project performance metrics and RAG status data from external OneView API via GET /performance endpoint.

**Acceptance Criteria:**
- User submits performance data request via GET /performance endpoint
- System authenticates with OneView API using custom token-based mechanism
- System retrieves performance data from OneView API
- System returns HTTP 200 OK with performance data
- System falls back to previous day's data if current day returns empty list

**Traceability:**
- repository_summary.json: entry_points[PerformaceController].endpoints[GET /performance]
- integration_catalog.json: touchpoint_enrichments.enrichments[OneView API]

**Assumptions or Unknowns:**
- OneView API integration process not included as primary business process (external data retrieval, not core user management workflow)

---

#### REQ-FUNC-015: Health Check Endpoint
**Title:** Health Check Endpoint  
**Type:** functional  
**Priority:** must  
**Modules:** mod_observability  

**Description:**  
The system shall provide a health check endpoint at GET /healthcheck for Kubernetes liveness and readiness probes.

**Acceptance Criteria:**
- System responds to GET /healthcheck with HTTP 200 OK if application is healthy
- Health check endpoint is accessible without authentication

**Traceability:**
- repository_summary.json: entry_points[Healthcheck].endpoints[GET /healthcheck]

---

### Non-Functional Requirements

#### REQ-NFR-001: JWT Token Validity
**Title:** JWT Token Validity  
**Type:** non_functional  
**Priority:** must  
**Modules:** mod_authentication_authorization  
**Processes:** proc_user_login, proc_token_refresh  

**Description:**  
Access tokens shall have a validity period of 30 minutes (1800000 milliseconds). Refresh tokens shall have a validity period of 24 hours (86400000 milliseconds).

**Acceptance Criteria:**
- Access tokens expire after 30 minutes
- Refresh tokens expire after 24 hours
- Expired tokens cannot be used for authentication or token refresh

**Traceability:**
- security_privacy_assessment.json: authorization_model_summary.patterns[JWT-based Authentication with Access and Refresh Tokens].evidence

---

#### REQ-NFR-002: Password Hashing
**Title:** Password Hashing  
**Type:** non_functional  
**Priority:** must  
**Modules:** mod_authentication_authorization  
**Processes:** proc_user_registration, proc_user_login  

**Description:**  
User passwords shall be hashed using BCrypt algorithm before storage. Plain text passwords shall never be stored or logged.

**Acceptance Criteria:**
- Passwords are hashed with BCrypt before storage
- Plain text passwords are not stored in database
- Plain text passwords are not logged in application logs

**Traceability:**
- security_privacy_assessment.json: sensitive_data_identification.data_items[Authentication Credentials - Passwords]

---

#### REQ-NFR-003: Role-Based Access Control
**Title:** Role-Based Access Control  
**Type:** non_functional  
**Priority:** must  
**Modules:** mod_authentication_authorization  

**Description:**  
Admin endpoints (/api/v1/admin/**) shall require ADMIN role. JWT token filter shall validate role claims before allowing access.

**Acceptance Criteria:**
- Admin endpoints reject requests without valid JWT token
- Admin endpoints reject requests with CUSTOMER role
- Admin endpoints allow requests with ADMIN role

**Traceability:**
- security_privacy_assessment.json: authorization_model_summary.patterns[Role-Based Access Control (RBAC)]

---

#### REQ-NFR-004: Kubernetes Deployment
**Title:** Kubernetes Deployment  
**Type:** non_functional  
**Priority:** must  
**Modules:** mod_deployment_infrastructure  

**Description:**  
The system shall be deployed on AWS EKS with 2 replicas, resource limits (CPU: 250m, Memory: 1024Mi), and ClusterIP service on port 80.

**Acceptance Criteria:**
- Kubernetes deployment creates 2 replicas
- Each replica has resource requests (CPU: 250m, Memory: 512Mi) and limits (CPU: 250m, Memory: 1024Mi)
- Kubernetes service exposes port 80 with ClusterIP type

**Traceability:**
- repository_summary.json: kubernetes_configuration

---

#### REQ-NFR-005: Prometheus Metrics
**Title:** Prometheus Metrics  
**Type:** non_functional  
**Priority:** must  
**Modules:** mod_observability  

**Description:**  
The system shall expose Prometheus metrics via Spring Actuator at /actuator/prometheus endpoint. Kubernetes deployment shall include Prometheus scrape annotations.

**Acceptance Criteria:**
- System exposes /actuator/prometheus endpoint
- Prometheus metrics include application metrics, JVM metrics, and Spring Boot metrics
- Kubernetes deployment includes Prometheus scrape annotations on port 8090

**Traceability:**
- integration_catalog.json: touchpoint_enrichments.enrichments[Prometheus]

---

#### REQ-NFR-006: Reactive Programming
**Title:** Reactive Programming  
**Type:** non_functional  
**Priority:** should  
**Modules:** mod_data_persistence  

**Description:**  
The system shall use Spring WebFlux for reactive, non-blocking I/O. Reactive custom DAO (CustomerDaoImpl) shall be used for database queries where applicable.

**Acceptance Criteria:**
- System uses Spring WebFlux for REST endpoints
- System uses Mono and Flux for reactive data streams
- CustomerDaoImpl provides reactive database queries

**Traceability:**
- repository_summary.json: application_type (Reactive Web Service)

---

#### REQ-NFR-007: CI/CD Pipeline
**Title:** CI/CD Pipeline  
**Type:** non_functional  
**Priority:** must  
**Modules:** mod_deployment_infrastructure  

**Description:**  
The system shall have an automated CI/CD pipeline via AWS CodeBuild with phases: install (Maven), pre_build (ECR login, kubectl install, EKS cluster config), build (mvn clean install, Docker build/tag), post_build (Docker push to ECR, kubectl apply, rollout restart).

**Acceptance Criteria:**
- CI/CD pipeline installs Maven in install phase
- CI/CD pipeline logs into ECR and configures EKS cluster in pre_build phase
- CI/CD pipeline builds Maven project and Docker image in build phase
- CI/CD pipeline pushes Docker image to ECR and deploys to EKS in post_build phase
- CI/CD pipeline performs rollout restart if deployment exists

**Traceability:**
- repository_summary.json: ci_cd_pipeline

---

### Security Requirements

#### REQ-SEC-001: Externalize Hardcoded Credentials
**Title:** Externalize Hardcoded Credentials  
**Type:** security  
**Priority:** must  
**Modules:** mod_external_integration, mod_data_persistence, mod_notification  

**Description:**  
The system shall externalize all hardcoded credentials (OneView API, database, SMTP) from source code and configuration files to environment variables or AWS Secrets Manager.

**Acceptance Criteria:**
- OneView API credentials removed from OneViewServiceImpl.java source code
- Database and SMTP credentials removed from application-dev.properties
- Credentials stored in environment variables or AWS Secrets Manager
- Application retrieves credentials from secure storage at runtime

**Traceability:**
- security_privacy_assessment.json: security_smells[Hardcoded Credentials, Credentials in Configuration Files]

---

#### REQ-SEC-002: Enable SSL/TLS for Database Connection
**Title:** Enable SSL/TLS for Database Connection  
**Type:** security  
**Priority:** must  
**Modules:** mod_data_persistence  

**Description:**  
The system shall enable SSL/TLS encryption for MySQL database connections by adding SSL parameters to JDBC URL.

**Acceptance Criteria:**
- JDBC URL includes SSL parameters (e.g., useSSL=true, requireSSL=true)
- Database traffic is encrypted with SSL/TLS

**Traceability:**
- security_privacy_assessment.json: security_smells[Missing SSL/TLS for Database Connection]

---

#### REQ-SEC-003: Implement Rate Limiting
**Title:** Implement Rate Limiting  
**Type:** security  
**Priority:** must  
**Modules:** mod_authentication_authorization  

**Description:**  
The system shall implement rate limiting on authentication endpoints (/login, /register) to prevent brute force attacks, credential stuffing, and denial of service.

**Acceptance Criteria:**
- System limits number of login attempts per IP address per time window
- System limits number of registration attempts per IP address per time window
- System returns HTTP 429 Too Many Requests when rate limit exceeded

**Traceability:**
- security_privacy_assessment.json: security_smells[No Rate Limiting]

**Assumptions or Unknowns:**
- Rate limiting may be implemented at API gateway level (not visible in application code)

---

#### REQ-SEC-004: Implement Account Lockout
**Title:** Implement Account Lockout  
**Type:** security  
**Priority:** must  
**Modules:** mod_authentication_authorization  

**Description:**  
The system shall implement account lockout mechanism after multiple failed login attempts to prevent brute force attacks.

**Acceptance Criteria:**
- System tracks failed login attempts per user account
- System locks account after N failed login attempts (e.g., 5)
- System unlocks account after time period (e.g., 30 minutes) or admin intervention
- System notifies user of account lockout via email

**Traceability:**
- security_privacy_assessment.json: security_smells[No Account Lockout]

---

#### REQ-SEC-005: Implement Audit Logging for Authentication Events
**Title:** Implement Audit Logging for Authentication Events  
**Type:** security  
**Priority:** should  
**Modules:** mod_audit_compliance  

**Description:**  
The system shall log all authentication events (successful and failed logins) for security monitoring and incident response.

**Acceptance Criteria:**
- System logs successful login events with timestamp, user email, and IP address
- System logs failed login events with timestamp, attempted email, and IP address
- Logs are stored in centralized logging system for analysis

**Traceability:**
- security_privacy_assessment.json: security_smells[No Audit Logging for Authentication Events]

---

### Integration Requirements

#### REQ-INT-001: OneView API Integration
**Title:** OneView API Integration  
**Type:** integration  
**Priority:** should  
**Modules:** mod_external_integration  

**Description:**  
The system shall integrate with external OneView API to fetch project performance metrics and RAG status data using custom token-based authentication.

**Acceptance Criteria:**
- System authenticates with OneView API using POST /user/signin with username/password
- System retrieves X-Ava-Access-Token header from login response
- System uses X-Ava-Access-Token header for subsequent API calls
- System retrieves performance data via GET /ava/oneview/internal/api/dashboard/rag/list
- System falls back to previous day's data if current day returns empty list

**Traceability:**
- integration_catalog.json: touchpoint_enrichments.enrichments[OneView API]

**Assumptions or Unknowns:**
- OneView API contract details inferred from implementation code; no API specification available

---

#### REQ-INT-002: Gmail SMTP Integration
**Title:** Gmail SMTP Integration  
**Type:** integration  
**Priority:** must  
**Modules:** mod_notification  

**Description:**  
The system shall integrate with Gmail SMTP to send email notifications using FreeMarker templates.

**Acceptance Criteria:**
- System connects to Gmail SMTP server (smtp.gmail.com:465) with SSL
- System authenticates with Gmail SMTP using username and app password
- System sends email notifications using FreeMarker templates
- System logs error and throws CustomException on email send failure

**Traceability:**
- integration_catalog.json: touchpoint_enrichments.enrichments[Gmail SMTP]

---

#### REQ-INT-003: Netflix Eureka Integration
**Title:** Netflix Eureka Integration  
**Type:** integration  
**Priority:** should  
**Modules:** mod_deployment_infrastructure  

**Description:**  
The system shall register with Netflix Eureka for service discovery and send heartbeat updates.

**Acceptance Criteria:**
- System registers with Eureka server on startup
- System sends heartbeat updates to Eureka server
- System updates registration metadata (instance ID, hostname, preferIpAddress)

**Traceability:**
- integration_catalog.json: touchpoint_enrichments.enrichments[Netflix Eureka]

**Assumptions or Unknowns:**
- Eureka authentication mechanism not visible in application code; may be handled by infrastructure

---

#### REQ-INT-004: Prometheus Integration
**Title:** Prometheus Integration  
**Type:** integration  
**Priority:** must  
**Modules:** mod_observability  

**Description:**  
The system shall expose Prometheus metrics endpoint for scraping by Prometheus server.

**Acceptance Criteria:**
- System exposes /actuator/prometheus endpoint
- Prometheus server scrapes metrics from /actuator/prometheus endpoint
- Metrics include application metrics, JVM metrics, and Spring Boot metrics

**Traceability:**
- integration_catalog.json: touchpoint_enrichments.enrichments[Prometheus]

**Assumptions or Unknowns:**
- Prometheus scrape authentication not visible; may be unauthenticated or handled by Kubernetes network policies

---

### Data Requirements

#### REQ-DATA-001: User Entity Persistence
**Title:** User Entity Persistence  
**Type:** data  
**Priority:** must  
**Modules:** mod_data_persistence  

**Description:**  
The system shall persist user entities to the users table in MySQL database with fields: userId (PK), email (unique), password (BCrypt hashed), firstname, lastname, company, country, role, onboardingStatus, isActive, registrationId, designation, clientId, clientName, clientOneView, createdAt, lastUpdatedAt, lastUpdatedBy.

**Acceptance Criteria:**
- UserEntity is persisted to users table
- Email field is unique
- Password field stores BCrypt hash
- All required fields are populated

**Traceability:**
- data_model.json: logical_models.models[UserEntity]

---

#### REQ-DATA-002: Access Token Persistence
**Title:** Access Token Persistence  
**Type:** data  
**Priority:** must  
**Modules:** mod_data_persistence  

**Description:**  
The system shall persist access tokens to the access_token table in MySQL database with fields: id (PK), token (TEXT), tokenType (BEARER), expired (Boolean), revoked (Boolean), createdAt (auto-generated), user (FK to users.user_id).

**Acceptance Criteria:**
- AccessToken is persisted to access_token table
- Token field stores JWT token as TEXT
- Foreign key relationship to users table is enforced

**Traceability:**
- data_model.json: logical_models.models[AccessToken]

---

#### REQ-DATA-003: Refresh Token Persistence
**Title:** Refresh Token Persistence  
**Type:** data  
**Priority:** must  
**Modules:** mod_data_persistence  

**Description:**  
The system shall persist refresh tokens to the refresh_token table in MySQL database with fields: id (PK), token (TEXT), tokenType (BEARER), expired (Boolean), revoked (Boolean), createdAt (auto-generated), user (FK to users.user_id).

**Acceptance Criteria:**
- RefreshToken is persisted to refresh_token table
- Token field stores JWT token as TEXT
- Foreign key relationship to users table is enforced

**Traceability:**
- data_model.json: logical_models.models[RefreshToken]

---

#### REQ-DATA-004: Action History Persistence
**Title:** Action History Persistence  
**Type:** data  
**Priority:** must  
**Modules:** mod_data_persistence  

**Description:**  
The system shall persist action history entries to the action_history table in MySQL database with fields: id (PK), userName, userEmail, userRole, userCompany, updateByAdminName, updateByAdminEmail, registrationId, action, adminComments, actionType, createdAt (auto-generated), updatedAt.

**Acceptance Criteria:**
- ActionHistory is persisted to action_history table
- All required fields are populated
- createdAt is auto-generated on insert

**Traceability:**
- data_model.json: logical_models.models[ActionHistory]

---

### Operational Requirements

#### REQ-OPS-001: Docker Containerization
**Title:** Docker Containerization  
**Type:** operational  
**Priority:** must  
**Modules:** mod_deployment_infrastructure  

**Description:**  
The system shall be containerized using Docker with amazoncorretto:17 base image and expose port 8090.

**Acceptance Criteria:**
- Dockerfile uses amazoncorretto:17 as base image
- Dockerfile exposes port 8090
- Docker image includes cove-user-service-0.0.1-SNAPSHOT.jar artifact

**Traceability:**
- repository_summary.json: deployment.containerization

---

#### REQ-OPS-002: AWS ECR Image Storage
**Title:** AWS ECR Image Storage  
**Type:** operational  
**Priority:** must  
**Modules:** mod_deployment_infrastructure  

**Description:**  
The system shall store Docker images in AWS ECR for retrieval by AWS EKS.

**Acceptance Criteria:**
- CI/CD pipeline pushes Docker image to AWS ECR
- AWS EKS pulls Docker image from AWS ECR for deployment

**Traceability:**
- integration_catalog.json: touchpoint_enrichments.enrichments[AWS ECR]

---

#### REQ-OPS-003: AWS EKS Deployment
**Title:** AWS EKS Deployment  
**Type:** operational  
**Priority:** must  
**Modules:** mod_deployment_infrastructure  

**Description:**  
The system shall be deployed on AWS EKS using Kubernetes manifests (deployment.yaml, service.yaml).

**Acceptance Criteria:**
- CI/CD pipeline applies Kubernetes manifests to AWS EKS cluster
- Kubernetes deployment creates pods with specified replicas and resource limits
- Kubernetes service exposes pods via ClusterIP on port 80

**Traceability:**
- integration_catalog.json: touchpoint_enrichments.enrichments[AWS EKS]

---

### Constraint Requirements

#### REQ-CONSTRAINT-001: Java Version
**Title:** Java Version  
**Type:** constraint  
**Priority:** must  

**Description:**  
The system shall be built and run on Java 17.

**Acceptance Criteria:**
- System is compiled with Java 17
- System runs on Java 17 runtime

**Traceability:**
- repository_summary.json: java_version

---

#### REQ-CONSTRAINT-002: Spring Boot Version
**Title:** Spring Boot Version  
**Type:** constraint  
**Priority:** must  

**Description:**  
The system shall use Spring Boot 3.2.5.

**Acceptance Criteria:**
- System uses Spring Boot 3.2.5 parent POM
- All Spring Boot dependencies are compatible with 3.2.5

**Traceability:**
- repository_summary.json: framework

---

#### REQ-CONSTRAINT-003: MySQL Version
**Title:** MySQL Version  
**Type:** constraint  
**Priority:** must  
**Modules:** mod_data_persistence  

**Description:**  
The system shall use MySQL 8.0.33 for data persistence.

**Acceptance Criteria:**
- System connects to MySQL 8.0.33 database
- JDBC driver is compatible with MySQL 8.0.33

**Traceability:**
- repository_summary.json: technology_stack.database

---

## Traceability Matrix

| Req ID | Artifact File | Pointer |
|--------|---------------|----------|
| REQ-FUNC-001 | business_process_model.json | business_processes.processes[proc_user_registration] |
| REQ-FUNC-001 | repository_summary.json | entry_points[AuthRestController].endpoints[POST /register] |
| REQ-FUNC-002 | business_process_model.json | business_processes.processes[proc_user_registration].steps[reg_step_5] |
| REQ-FUNC-002 | integration_catalog.json | touchpoint_enrichments.enrichments[Gmail SMTP] |
| REQ-FUNC-003 | business_process_model.json | business_processes.processes[proc_admin_onboarding_approval] |
| REQ-FUNC-003 | repository_summary.json | entry_points[AdminController].endpoints[PUT /api/v1/admin/{toEmail}/onboarding-status] |
| REQ-FUNC-004 | business_process_model.json | business_processes.processes[proc_admin_onboarding_approval].steps[onboard_step_5] |
| REQ-FUNC-004 | repository_summary.json | service_layer.services[EmailService].email_templates |
| REQ-FUNC-005 | business_process_model.json | business_processes.processes[proc_user_login] |
| REQ-FUNC-005 | repository_summary.json | entry_points[AuthRestController].endpoints[POST /login] |
| REQ-FUNC-006 | business_process_model.json | business_processes.processes[proc_token_refresh] |
| REQ-FUNC-006 | repository_summary.json | entry_points[AuthRestController].endpoints[POST /refresh-token] |
| REQ-FUNC-007 | repository_summary.json | entry_points[AuthRestController].endpoints[POST /revoke-access-token, POST /revoke-refresh-token] |
| REQ-FUNC-007 | security_privacy_assessment.json | authorization_model_summary.patterns[JWT-based Authentication with Access and Refresh Tokens] |
| REQ-FUNC-008 | business_process_model.json | business_processes.processes[proc_admin_account_management] |
| REQ-FUNC-008 | repository_summary.json | entry_points[AdminController].endpoints[PUT /api/v1/admin/{toEmail}/account-status] |
| REQ-FUNC-009 | business_process_model.json | business_processes.processes[proc_admin_role_management] |
| REQ-FUNC-009 | repository_summary.json | entry_points[AdminController].endpoints[PUT /api/v1/admin/{toEmail}/role] |
| REQ-FUNC-010 | repository_summary.json | entry_points[AdminController].endpoints[GET /api/v1/admin/users] |
| REQ-FUNC-011 | repository_summary.json | entry_points[AdminController].endpoints[GET /api/v1/admin/user-details/{email}] |
| REQ-FUNC-012 | data_model.json | logical_models.models[ActionHistory] |
| REQ-FUNC-012 | business_process_model.json | business_processes.processes[proc_admin_onboarding_approval].steps[onboard_step_6] |
| REQ-FUNC-013 | repository_summary.json | entry_points[AdminController].endpoints[GET /api/v1/admin/action-history] |
| REQ-FUNC-014 | repository_summary.json | entry_points[PerformaceController].endpoints[GET /performance] |
| REQ-FUNC-014 | integration_catalog.json | touchpoint_enrichments.enrichments[OneView API] |
| REQ-FUNC-015 | repository_summary.json | entry_points[Healthcheck].endpoints[GET /healthcheck] |
| REQ-NFR-001 | security_privacy_assessment.json | authorization_model_summary.patterns[JWT-based Authentication with Access and Refresh Tokens].evidence |
| REQ-NFR-002 | security_privacy_assessment.json | sensitive_data_identification.data_items[Authentication Credentials - Passwords] |
| REQ-NFR-003 | security_privacy_assessment.json | authorization_model_summary.patterns[Role-Based Access Control (RBAC)] |
| REQ-NFR-004 | repository_summary.json | kubernetes_configuration |
| REQ-NFR-005 | integration_catalog.json | touchpoint_enrichments.enrichments[Prometheus] |
| REQ-NFR-006 | repository_summary.json | application_type (Reactive Web Service) |
| REQ-NFR-007 | repository_summary.json | ci_cd_pipeline |
| REQ-SEC-001 | security_privacy_assessment.json | security_smells[Hardcoded Credentials, Credentials in Configuration Files] |
| REQ-SEC-002 | security_privacy_assessment.json | security_smells[Missing SSL/TLS for Database Connection] |
| REQ-SEC-003 | security_privacy_assessment.json | security_smells[No Rate Limiting] |
| REQ-SEC-004 | security_privacy_assessment.json | security_smells[No Account Lockout] |
| REQ-SEC-005 | security_privacy_assessment.json | security_smells[No Audit Logging for Authentication Events] |
| REQ-INT-001 | integration_catalog.json | touchpoint_enrichments.enrichments[OneView API] |
| REQ-INT-002 | integration_catalog.json | touchpoint_enrichments.enrichments[Gmail SMTP] |
| REQ-INT-003 | integration_catalog.json | touchpoint_enrichments.enrichments[Netflix Eureka] |
| REQ-INT-004 | integration_catalog.json | touchpoint_enrichments.enrichments[Prometheus] |
| REQ-DATA-001 | data_model.json | logical_models.models[UserEntity] |
| REQ-DATA-002 | data_model.json | logical_models.models[AccessToken] |
| REQ-DATA-003 | data_model.json | logical_models.models[RefreshToken] |
| REQ-DATA-004 | data_model.json | logical_models.models[ActionHistory] |
| REQ-OPS-001 | repository_summary.json | deployment.containerization |
| REQ-OPS-002 | integration_catalog.json | touchpoint_enrichments.enrichments[AWS ECR] |
| REQ-OPS-003 | integration_catalog.json | touchpoint_enrichments.enrichments[AWS EKS] |
| REQ-CONSTRAINT-001 | repository_summary.json | java_version |
| REQ-CONSTRAINT-002 | repository_summary.json | framework |
| REQ-CONSTRAINT-003 | repository_summary.json | technology_stack.database |

---

## Open Questions & Limits

### Open Questions
1. Should password reset functionality be added to the roadmap?
2. Should MFA be implemented for all users or only admin accounts?
3. Should API documentation (Swagger/OpenAPI) be generated automatically?
4. Should database migration tooling (Flyway/Liquibase) be introduced for schema versioning?
5. Should a caching layer (Redis) be added for token storage or performance data?
6. Should rate limiting be implemented at application level or API gateway level?
7. What is the business purpose of client-related fields (clientId, clientName, clientOneView) in UserEntity?
8. What is the registration ID generation and usage pattern?

### Limits or Unknowns
- tech_debt_risk.json artifact not found; no tech debt analysis included
- review_resolution.json artifact not found; no PR review feedback incorporated
- Token refresh implementation details partially inferred; detailed code for JwtRefreshTokenUtil.generateNewRefreshToken not read
- Token revocation logic details inferred from login flow; explicit revocation endpoint behavior not fully traced
- Email template content not analyzed; only service-level email sending logic reviewed
- OneView API integration process not included as primary business process (external data retrieval, not core user management workflow)
- Admin user listing and action history retrieval processes not included (read-only queries, not state-changing workflows)
- Healthcheck endpoint not included as business process (infrastructure monitoring, not business workflow)
- Database schema relationships inferred from JPA annotations; no database migration scripts or ER diagrams available
- Timeout and retry configurations not explicitly set in application code; rely on library defaults
- Security controls for infrastructure services (Eureka, Prometheus, ECR, EKS) not visible in application code; may be handled at infrastructure level
- Client-related fields (clientId, clientName, clientOneView) in UserEntity have unclear business purpose; insufficient evidence to determine their role
- Registration ID generation and usage pattern not fully documented in code

---

## Themes / Epics

| Theme ID | Theme Name | Requirements |
|----------|------------|-------------|
| theme_user_lifecycle | User Lifecycle Management | REQ-FUNC-001, REQ-FUNC-002, REQ-FUNC-003, REQ-FUNC-004, REQ-FUNC-005, REQ-FUNC-006, REQ-FUNC-007, REQ-FUNC-008, REQ-FUNC-009, REQ-FUNC-010, REQ-FUNC-011 |
| theme_audit_compliance | Audit & Compliance | REQ-FUNC-012, REQ-FUNC-013, REQ-SEC-005 |
| theme_external_integration | External System Integration | REQ-FUNC-014, REQ-INT-001, REQ-INT-002, REQ-INT-003, REQ-INT-004 |
| theme_security | Security Hardening | REQ-SEC-001, REQ-SEC-002, REQ-SEC-003, REQ-SEC-004, REQ-NFR-002, REQ-NFR-003 |
| theme_observability | Observability & Monitoring | REQ-FUNC-015, REQ-NFR-005 |
| theme_deployment | Deployment & Infrastructure | REQ-NFR-004, REQ-NFR-007, REQ-OPS-001, REQ-OPS-002, REQ-OPS-003 |
| theme_data_persistence | Data Persistence | REQ-DATA-001, REQ-DATA-002, REQ-DATA-003, REQ-DATA-004, REQ-NFR-006 |

---

**End of Requirements Backlog**
