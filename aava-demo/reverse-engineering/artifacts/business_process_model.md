# Business Process Model

## Application Purpose

**COVE User Service** is a reactive Spring Boot microservice that manages user authentication, registration, and onboarding workflows for the COVE platform. It provides JWT-based authentication with role-based access control (CUSTOMER and ADMIN roles), an admin-approval workflow for new user registrations, and integrates with external systems for performance data retrieval and email notifications.

### Primary Capabilities

- **User Registration and Onboarding**: New users register with personal and company information. Registrations enter a pending_approval state and trigger email notifications. Admins review and approve or reject registrations, with automated email notifications sent at each status change.
- **JWT-Based Authentication**: Users authenticate with email and password. The system issues short-lived access tokens (30 minutes) and long-lived refresh tokens (24 hours). Tokens are stored in the database with expiration and revocation tracking. Token refresh and revocation endpoints support session management.
- **Admin User Management**: Admins can update user onboarding status (approve/reject), activate or deactivate user accounts, change user roles (CUSTOMER/ADMIN), retrieve user details, and view paginated user lists with filtering by status, role, submission date, company, and active state.
- **Audit and Action History**: All administrative actions on user accounts (onboarding status changes, account activation/deactivation, role changes) are logged with timestamps, admin attribution, and optional comments. Admins can retrieve paginated action history with filtering by status, role, date, company, name, and email.
- **External Performance Data Integration**: The system integrates with the OneView API to retrieve project performance metrics and RAG (Red-Amber-Green) status data for specified accounts and project types. This capability supports performance monitoring and reporting.

### Out of Scope

- No password reset or forgot password functionality detected
- No multi-factor authentication (MFA) implementation found
- No API documentation (Swagger/OpenAPI) detected

---

## Business Processes

### User Registration

**Process ID**: `proc_user_registration`

**Description**: A new user submits registration information including personal details, company, and credentials. The system validates the email uniqueness, hashes the password, stores the user record with pending_approval status, and sends a pending approval email notification.

**Actors**:
- **Prospective User** (human): Individual registering for COVE platform access
- **COVE User Service** (system): Handles registration logic, validation, and email notification
- **Gmail SMTP** (system): Sends pending approval email notification

**Triggers**:
- API request: POST /register with user registration payload

**Steps**:

1. **Validate Email Uniqueness**: System checks if the provided email address is already registered. If duplicate, registration fails. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-100`)
2. **Hash Password**: System hashes the user's password using BCrypt before storage. (Evidence: `security_privacy_assessment.json`)
3. **Store User Record**: System creates a new user record with onboarding status set to pending_approval and isActive set to false. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-110`)
4. **Send Pending Approval Email**: System sends an email notification to the user informing them that their registration is pending admin approval. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java`)

**Variations**:
- If email already exists, registration fails with error
- If email sending fails, registration still succeeds but user does not receive notification

**Outcomes**:
- Registration successful; user awaits admin approval
- Registration failed due to duplicate email

---

### User Login

**Process ID**: `proc_user_login`

**Description**: An approved and active user submits email and password credentials. The system validates credentials, checks onboarding status and account activation, revokes old tokens, generates new access and refresh tokens, stores them in the database, and returns tokens to the user.

**Actors**:
- **Registered User** (human): User with approved onboarding status and active account
- **COVE User Service** (system): Handles authentication logic, token generation, and validation

**Triggers**:
- API request: POST /login with email and password

**Steps**:

1. **Validate User Exists**: System checks if a user record exists for the provided email. If not found, login fails. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:130-135`)
2. **Check Onboarding Status**: System checks the user's onboarding status. Only users with approved status can proceed. Users with pending_approval or reject status receive specific error messages. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:135-175`)
3. **Check Account Active Status**: System checks if the user account is active. Deactivated accounts cannot log in. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:140-145`)
4. **Validate Password**: System compares the provided password with the stored BCrypt hash. If mismatch, login fails. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:145-155`)
5. **Revoke Old Tokens**: System revokes all existing access and refresh tokens for the user to enforce single-session behavior. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:145-155`)
6. **Generate Access Token**: System generates a new JWT access token with 30-minute validity and stores it in the database. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:145-155`, `repository_summary.json`)
7. **Generate Refresh Token**: System generates a new JWT refresh token with 24-hour validity and stores it in the database. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:145-155`, `repository_summary.json`)
8. **Return Tokens**: System returns the access token and refresh token to the user in the response payload. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:145-155`)

**Variations**:
- If user does not exist, login fails with 'Invalid username or password' error
- If onboarding status is pending_approval, login fails with 'Please wait for approval' error
- If onboarding status is reject, login fails with 'You have not been accepted' error
- If account is deactivated, login fails with 'Account has been deactivated' error
- If password does not match, login fails with 'Invalid username or password' error

**Outcomes**:
- Login successful; user receives access and refresh tokens
- Login failed due to invalid credentials, inactive account, or unapproved status

**Related Risks**:
- No rate limiting on login endpoint (security_privacy_assessment.json: No Rate Limiting)
- No account lockout after failed login attempts (security_privacy_assessment.json: No Account Lockout)

---

### Admin Approve or Reject User

**Process ID**: `proc_admin_approve_user`

**Description**: An admin reviews a pending user registration and updates the onboarding status to approved or reject. If approved, the user account is activated and an approval email is sent. If rejected, the account remains inactive and a rejection email with optional comments is sent. The action is logged in the action history.

**Actors**:
- **Admin** (human): User with ADMIN role authorized to approve or reject registrations
- **COVE User Service** (system): Handles onboarding status update, email notification, and audit logging
- **Gmail SMTP** (system): Sends approval or rejection email notification

**Triggers**:
- API request: PUT /api/v1/admin/{toEmail}/onboarding-status with status parameter (approved or reject) and optional description

**Steps**:

1. **Extract Admin Identity**: System extracts the admin's email from the JWT access token in the request header. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:44-50`)
2. **Retrieve User Record**: System retrieves the user record by email. If not found, the operation fails. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:50-55`)
3. **Update Onboarding Status**: System updates the user's onboarding status to the specified value (approved or reject). If approved, isActive is set to true. If rejected, isActive is set to false. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:55-70`)
4. **Send Email Notification**: System sends an email notification to the user. If approved, sends approval email. If rejected, sends rejection email with optional description. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:55-70`)
5. **Log Action History**: System creates an action history record with admin email, user email, new onboarding status, optional comments, and action type (UpdateOnBoardingStatus). (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:70-75`)

**Variations**:
- If user does not exist, operation fails with 'User does not exist' error
- If status is approved, user account is activated and approval email is sent
- If status is reject, user account remains inactive and rejection email with description is sent
- If status is pending_approval, user account is set to inactive

**Outcomes**:
- User onboarding status updated successfully; email sent and action logged
- Operation failed due to non-existent user

---

### Admin Manage User Account

**Process ID**: `proc_admin_manage_account`

**Description**: An admin activates or deactivates a user account, or changes a user's role. Each action is logged in the action history with admin attribution and optional comments.

**Actors**:
- **Admin** (human): User with ADMIN role authorized to manage user accounts
- **COVE User Service** (system): Handles account status and role updates, and audit logging

**Triggers**:
- API request: PUT /api/v1/admin/{toEmail}/account-status with status parameter (true for activate, false for deactivate) and optional description
- API request: PUT /api/v1/admin/{toEmail}/role with role parameter (CUSTOMER or ADMIN) and optional description

**Steps**:

1. **Extract Admin Identity**: System extracts the admin's email from the JWT access token in the request header. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:80-85`)
2. **Retrieve User Record**: System retrieves the user record by email. If not found, the operation fails. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:85-90`)
3. **Update Account Status or Role**: System updates the user's isActive field (for account status) or role field (for role change) based on the request. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:90-110`)
4. **Log Action History**: System creates an action history record with admin email, user email, new status or role, optional comments, and action type (UpdateUserAccountStatus or UpdateUserRoleStatus). (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:100-110`)

**Variations**:
- If user does not exist, operation fails with 'User does not exist' error
- For account status update, isActive is set to true (activated) or false (deactivated)
- For role update, role is set to CUSTOMER or ADMIN

**Outcomes**:
- User account status or role updated successfully; action logged
- Operation failed due to non-existent user

---

### Token Refresh

**Process ID**: `proc_token_refresh`

**Description**: A user with a valid refresh token requests a new access token. The system validates the refresh token, generates a new access token, and optionally generates a new refresh token. Old tokens are revoked.

**Actors**:
- **Authenticated User** (human): User with a valid refresh token
- **COVE User Service** (system): Handles token validation, generation, and revocation

**Triggers**:
- API request: POST /refresh-token with refresh token parameter

**Steps**:

1. **Validate Refresh Token**: System validates the refresh token signature, expiration, and revocation status. If invalid, the operation fails. (Evidence: `repository_summary.json`)
2. **Generate New Access Token**: System generates a new JWT access token with 30-minute validity and stores it in the database. (Evidence: `repository_summary.json`)
3. **Optionally Generate New Refresh Token**: System may generate a new refresh token and revoke the old one (token rotation strategy). (Evidence: `repository_summary.json`)
4. **Return New Tokens**: System returns the new access token and optionally new refresh token to the user. (Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`)

**Variations**:
- If refresh token is invalid, expired, or revoked, operation fails

**Outcomes**:
- New access token (and optionally new refresh token) issued successfully
- Token refresh failed due to invalid or expired refresh token

---

### Retrieve Performance Data

**Process ID**: `proc_retrieve_performance_data`

**Description**: A user requests project performance metrics and RAG status data for a specified account and optional project type. The system authenticates with the OneView API, retrieves the data, and returns filtered results.

**Actors**:
- **Authenticated User** (human): User requesting performance data
- **COVE User Service** (system): Handles OneView API integration and data filtering
- **OneView API** (system): External API providing project performance and RAG status data

**Triggers**:
- API request: GET /performance with account and optional performanceType query parameters

**Steps**:

1. **Authenticate with OneView API**: System sends a POST request to OneView API /user/signin with hardcoded credentials to obtain an access token. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:30-45`)
2. **Request Performance Data**: System sends a GET request to OneView API /ava/oneview/internal/api/dashboard/rag/list with current date and optional project type filter. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:50-75`)
3. **Fallback to Previous Day**: If current day returns no data, system retries with previous day's date. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:70-90`)
4. **Filter by Account**: System filters the performance data to include only projects matching the specified account. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:90-100`)
5. **Return Performance Data**: System returns the filtered performance data to the user. (Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java`)

**Variations**:
- If current day returns no data, system retries with previous day
- If project type is specified and not 'All', system filters by project type

**Outcomes**:
- Performance data retrieved and returned successfully
- Performance data retrieval failed due to OneView API error

**Related Risks**:
- Hardcoded OneView API credentials in source code (security_privacy_assessment.json: Hardcoded Credentials)

---

## How Processes Relate

- **User Registration** precedes **Admin Approve or Reject User**: User registration creates a pending_approval record that triggers the admin approval process.
- **Admin Approve or Reject User** precedes **User Login**: Admin approval (with approved status) enables the user to log in.
- **User Login** supports **Token Refresh**: User login issues a refresh token that can be used in the token refresh process.
- **Admin Approve or Reject User** and **Admin Manage User Account** are parallel: Admin approval and account management are independent administrative processes that can occur in any order after registration.

---

## Limits and Unknowns

- `tech_debt_risk.json` artifact was not found on aava/reverse-index branch; no technical debt or risk signals incorporated.
- Token refresh process details (step ordering, token rotation strategy) inferred from repository summary and integration catalog; detailed implementation not visible in read source.
- OneView API contract details (request/response schemas, error handling) inferred from service implementation; no API specification available.
- Email notification failure handling not explicitly documented; assumed to throw exception but registration still succeeds.
- No workflow engine or batch scheduling detected; all processes are synchronous API-driven.
- No password reset or forgot password process found; marked as out of scope.
- No multi-factor authentication (MFA) process found; marked as out of scope.
- Admin authorization enforcement (ADMIN role requirement) inferred from security configuration; detailed filter chain not analyzed.
- Action history pagination and filtering logic inferred from repository method signatures; detailed JPQL queries not analyzed.
- Performance data filtering logic (by account and project type) inferred from service implementation; no business rules documentation available.

---

## Evidence and Traceability

All process steps, variations, and outcomes are linked to source code paths, configuration files, and upstream artifact references in the JSON artifact (`business_process_model.json`). For machine-readable traceability, refer to the JSON file.

---

**Generated**: 2025-01-16T12:00:00Z  
**Repository**: ramanohar/AAVA-Reverse-Engineering-POC  
**Branch**: main  
**Run Mode**: build
