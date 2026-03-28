# Business Process Model: ramanohar/AAVA-Reverse-Engineering-POC

**Generated:** 2025-01-16T12:00:00Z  
**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main  
**Run Mode:** build

---

## Application Purpose

### Elevator Pitch

COVE User Service is a Spring Boot reactive microservice that manages user authentication, registration, and lifecycle for a multi-tenant system. It implements JWT-based authentication with role-based access control (CUSTOMER and ADMIN roles), an admin-approval onboarding workflow, and integrates with external OneView API for project performance data retrieval.

### Primary Capabilities

#### 1. User Registration and Onboarding

New users can self-register with personal and company information. Registration triggers an admin approval workflow where accounts remain in pending_approval status until an administrator reviews and approves or rejects the request. Email notifications are sent at each workflow transition.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-110`
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:45-75`

**Confidence:** High

---

#### 2. JWT-Based Authentication

Users authenticate with email and password credentials. Upon successful login, the system issues a short-lived access token (30 minutes) and a long-lived refresh token (24 hours). Tokens are stored in the database with expiration and revocation flags for stateless session management.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:125-165`
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java`
- `src/main/resources/application-dev.properties:32-35`

**Confidence:** High

---

#### 3. Role-Based Access Control

The system enforces role-based permissions with two roles: CUSTOMER (standard user with access to public endpoints) and ADMIN (privileged user with access to administrative functions including user management, onboarding approval, and audit log review).

**Evidence:**
- `src/main/java/com/collaberadigital/cove/configuration/SecurityConfig.java:44-51`
- `src/main/java/com/collaberadigital/cove/utils/constant/UserRole.java`

**Confidence:** High

---

#### 4. User Lifecycle Management

Administrators can manage user accounts throughout their lifecycle: approve or reject onboarding requests, activate or deactivate accounts, change user roles, and view detailed user information. All administrative actions are logged in an audit trail.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

**Confidence:** High

---

#### 5. Performance Data Integration

The system integrates with an external OneView API to retrieve project performance metrics and RAG (Red-Amber-Green) status information for specific client accounts. This capability supports performance monitoring and reporting workflows.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java`
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java`

**Confidence:** High

---

### Out of Scope

- **Password reset functionality is not implemented** (Evidence: `aava-demo/reverse-engineering/artifacts/repository_summary.json:gaps_and_unknowns`)
- **Multi-factor authentication (MFA) is not implemented** (Evidence: `aava-demo/reverse-engineering/artifacts/security_privacy_assessment.json:security_smells`)

---

## Business Processes

### Process 1: User Registration and Onboarding

**Process ID:** `proc_user_registration`

**Description:** A new user self-registers by providing personal and company information. The system creates an account in pending_approval status and sends email notifications to the user and potentially administrators. An administrator must review and approve or reject the registration before the user can access the system.

#### Actors

- **Prospective User** (Human): Individual seeking to create an account in the COVE system
- **Administrator** (Human): User with ADMIN role who reviews and approves or rejects registration requests
- **Email Service** (System): Automated email notification system using Gmail SMTP

#### Triggers

- **API request**: User submits registration form via POST /register endpoint

#### Process Steps

##### Step 1: Submit Registration

**Business Description:** Prospective user provides personal information (firstname, lastname, email, password, company, country, designation) and submits registration request.

**System Touchpoint:** AuthRestController.register

**Inputs:**
- AuthUser: Registration form data including email, password, personal and company information (from User input)

**Outputs:**
- SuccessResponse: Confirmation message indicating registration submitted for approval (to User)

**Authorization:** Public endpoint; no authentication required

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-110`

**Confidence:** High

---

##### Step 2: Validate and Create User

**Business Description:** System validates that email is unique, hashes password with BCrypt, generates registration ID, sets onboarding status to pending_approval, and stores user record in database.

**System Touchpoint:** UserServiceImpl.registerUser

**Inputs:**
- AuthUser: Validated registration data (from Step 1)

**Outputs:**
- UserEntity: Persisted user record with pending_approval status (to users table)

**Integrations:**
- AWS RDS MySQL: INSERT into users table

**Authorization:** Password hashed with BCrypt before storage

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-110`
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`

**Confidence:** High

---

##### Step 3: Send Pending Approval Email

**Business Description:** System sends email notification to the newly registered user informing them that their account is pending administrator approval.

**System Touchpoint:** EmailServiceImpl.sendEmailPendingApproval

**Inputs:**
- User email and name: Recipient information from UserEntity (from Step 2)

**Outputs:**
- Email notification: Pending approval email sent to user (to User email address)

**Integrations:**
- Gmail SMTP: Send email via SMTP

**Authorization:** Email service uses configured SMTP credentials

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java:105-135`
- `src/main/resources/templates/email-template-pending-approval.ftl`

**Confidence:** High

---

##### Step 4: Administrator Reviews Registration

**Business Description:** Administrator views list of pending user registrations, reviews user details, and decides to approve or reject the registration request. Administrator may provide comments explaining the decision.

**System Touchpoint:** AdminController.updateOnBoardingStatusController

**Inputs:**
- User email, status (approved/reject), description: Administrator decision and optional comments (from Administrator input)

**Outputs:**
- Updated UserEntity: User record with updated onboarding status and account activation state (to users table)
- ActionHistory: Audit log entry recording the administrative action (to action_history table)

**Integrations:**
- AWS RDS MySQL: UPDATE users table and INSERT into action_history table

**Authorization:** Requires ADMIN role; JWT access token validated

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:45-75`
- `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java:PUT /{toEmail}/onboarding-status`

**Confidence:** High

---

##### Step 5: Send Approval or Rejection Email

**Business Description:** System sends email notification to the user informing them of the administrator's decision. If approved, user is notified they can now log in. If rejected, user receives explanation (if provided) and is informed they cannot access the system.

**System Touchpoint:** EmailServiceImpl.sendEmailApproved or sendEmailReject

**Inputs:**
- User email, name, decision, comments: Notification details based on administrator decision (from Step 4)

**Outputs:**
- Email notification: Approval or rejection email sent to user (to User email address)

**Integrations:**
- Gmail SMTP: Send email via SMTP

**Authorization:** Triggered automatically by administrator action

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java:40-100`
- `src/main/resources/templates/email-template-approved.ftl`
- `src/main/resources/templates/email-template-rejected.ftl`

**Confidence:** High

---

#### Process Variations

- **Email already registered**: If user attempts to register with an email that already exists in the system, registration fails immediately with error message. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-110`)

- **Administrator approves registration**: User onboarding status set to approved, account activated (isActive=true), approval email sent. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:50-55`)

- **Administrator rejects registration**: User onboarding status set to reject, account deactivated (isActive=false), rejection email with comments sent. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:56-60`)

#### Outcomes

- **Success**: User registration approved; user can now log in and access the system
- **Failure**: User registration rejected; user cannot access the system
- **Pending**: User registration awaiting administrator review

---

### Process 2: User Authentication and Session Management

**Process ID:** `proc_user_authentication`

**Description:** An approved user authenticates with email and password credentials. Upon successful authentication, the system issues JWT access and refresh tokens, revokes any existing tokens, and stores new tokens in the database. Users can refresh expired access tokens using refresh tokens or explicitly revoke tokens to log out.

#### Actors

- **Registered User** (Human): User with approved account attempting to log in
- **JWT Token Service** (System): Generates, validates, and manages JWT access and refresh tokens

#### Triggers

- **API request**: User submits login credentials via POST /login endpoint
- **API request**: User requests token refresh via POST /refresh-token endpoint
- **API request**: User requests logout via POST /revoke-access-token or /revoke-refresh-token endpoint

#### Process Steps

##### Step 1: Submit Login Credentials

**Business Description:** User provides email and password credentials via login form or API request.

**System Touchpoint:** AuthRestController.login

**Inputs:**
- AuthenticationRequest: Email and password credentials (from User input)

**Outputs:**
- Token: JWT access token and refresh token (to User client)

**Authorization:** Public endpoint; credentials validated against database

**Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:POST /login`

**Confidence:** High

---

##### Step 2: Validate User Status

**Business Description:** System retrieves user record by email and validates onboarding status (must be approved) and account status (must be active). If user is rejected or pending approval, authentication fails with appropriate error message.

**System Touchpoint:** UserServiceImpl.loginUser

**Inputs:**
- Email: User email from login request (from Step 1)

**Outputs:**
- UserEntity: User record with status validation (to Next step or error response)

**Integrations:**
- AWS RDS MySQL: SELECT from users table

**Authorization:** Validates onboarding status and account activation state

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:135-175`

**Confidence:** High

---

##### Step 3: Verify Password

**Business Description:** System compares provided password with stored BCrypt hash. If passwords do not match, authentication fails with invalid credentials error.

**System Touchpoint:** UserServiceImpl.loginUser

**Inputs:**
- Plain text password: Password from login request (from Step 1)
- Hashed password: BCrypt hash from user record (from Step 2)

**Outputs:**
- Authentication result: Success or failure (to Next step or error response)

**Authorization:** BCrypt password comparison

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:145-150`

**Confidence:** High

---

##### Step 4: Revoke Existing Tokens

**Business Description:** System marks all existing access tokens and refresh tokens for the user as revoked in the database to prevent reuse of old tokens.

**System Touchpoint:** JwtAccessTokenUtil.revokeAccessToken, JwtRefreshTokenUtil.revokeRefreshToken

**Inputs:**
- UserEntity: User record (from Step 2)

**Outputs:**
- Updated token records: Existing tokens marked as revoked (to access_token and refresh_token tables)

**Integrations:**
- AWS RDS MySQL: UPDATE access_token and refresh_token tables

**Authorization:** Ensures single active session per user

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:152-157`

**Confidence:** High

---

##### Step 5: Generate and Store New Tokens

**Business Description:** System generates new JWT access token (30-minute validity) and refresh token (24-hour validity), signs them with secret key, and stores them in database with user association.

**System Touchpoint:** JwtAccessTokenUtil.generateToken, JwtRefreshTokenUtil.generateRefreshToken

**Inputs:**
- User email: User identifier for token payload (from Step 2)

**Outputs:**
- Access token and refresh token: Signed JWT tokens (to User client)
- Token records: Token metadata stored in database (to access_token and refresh_token tables)

**Integrations:**
- AWS RDS MySQL: INSERT into access_token and refresh_token tables

**Authorization:** Tokens signed with HS256 algorithm using configured secret

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:152-160`
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java`

**Confidence:** High

---

##### Step 6: Return Tokens to User

**Business Description:** System returns access token and refresh token to user client. User includes access token in Authorization header for subsequent API requests.

**System Touchpoint:** AuthRestController.login

**Inputs:**
- Token objects: Generated tokens (from Step 5)

**Outputs:**
- Token response: JSON response with access_token and refresh_token (to User client)

**Authorization:** Tokens transmitted over HTTPS

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:160`

**Confidence:** High

---

#### Process Variations

- **User account is deactivated**: If user account is marked as inactive (isActive=false), authentication fails with deactivated account error even if credentials are correct. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:140-145`)

- **User onboarding status is rejected**: If user onboarding status is reject, authentication fails with rejection message. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:170-175`)

- **User onboarding status is pending approval**: If user onboarding status is pending_approval, authentication fails with pending approval message. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:176-180`)

- **Access token expires**: When access token expires (after 30 minutes), user must use refresh token to obtain new access token via POST /refresh-token endpoint. (Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:POST /refresh-token`, `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java:35-40`)

- **User logs out**: User can explicitly revoke tokens by calling POST /revoke-access-token or /revoke-refresh-token endpoints. Revoking refresh token also revokes access token. (Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:POST /revoke-access-token`, `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java:42-60`)

#### Outcomes

- **Success**: User successfully authenticated; access and refresh tokens issued
- **Failure**: Authentication failed due to invalid credentials, deactivated account, or pending/rejected onboarding status

---

### Process 3: Administrator User Lifecycle Management

**Process ID:** `proc_admin_user_management`

**Description:** Administrators manage user accounts throughout their lifecycle: approve or reject onboarding requests, activate or deactivate accounts, change user roles, view user details, and review paginated user lists with filtering. All administrative actions are logged in an audit trail for compliance and traceability.

#### Actors

- **Administrator** (Human): User with ADMIN role performing user management tasks
- **Action History Service** (System): Audit logging service that records all administrative actions
- **Email Service** (System): Sends notifications to users when their account status changes

#### Triggers

- **API request**: Administrator updates user onboarding status via PUT /api/v1/admin/{toEmail}/onboarding-status
- **API request**: Administrator updates user account status via PUT /api/v1/admin/{toEmail}/account-status
- **API request**: Administrator updates user role via PUT /api/v1/admin/{toEmail}/role
- **API request**: Administrator views user details via GET /api/v1/admin/user-details/{email}
- **API request**: Administrator views paginated user list via GET /api/v1/admin/users

#### Process Steps

##### Step 1: Authenticate Administrator

**Business Description:** Administrator authenticates and obtains JWT access token. All admin endpoints require ADMIN role and valid access token in Authorization header.

**System Touchpoint:** JwtTokenAuthenticationFilter

**Inputs:**
- JWT access token: Bearer token in Authorization header (from Administrator client)

**Outputs:**
- Authenticated user context: User identity and role extracted from token (to Security context)

**Authorization:** Requires ADMIN role; token validated against database

**Evidence:**
- `src/main/java/com/collaberadigital/cove/security/JwtTokenAuthenticationFilter.java`
- `src/main/java/com/collaberadigital/cove/configuration/SecurityConfig.java:44-51`

**Confidence:** High

---

##### Step 2: Select User Management Action

**Business Description:** Administrator selects action to perform: approve/reject onboarding, activate/deactivate account, change role, view user details, or browse user list.

**System Touchpoint:** AdminController

**Inputs:**
- Action type and parameters: Endpoint and request parameters (from Administrator input)

**Outputs:**
- Action request: Validated request routed to appropriate service method (to AdminService)

**Authorization:** All admin endpoints protected by ADMIN role requirement

**Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`

**Confidence:** High

---

##### Step 3: Execute User Management Action

**Business Description:** System executes requested action: updates user record in database, sends email notification if applicable, and logs action in audit trail.

**System Touchpoint:** AdminServiceImpl

**Inputs:**
- Action parameters: User email, new status/role, optional comments (from Step 2)

**Outputs:**
- Updated UserEntity: User record with updated status/role (to users table)
- ActionHistory: Audit log entry (to action_history table)
- Email notification: Optional email to user (to User email address)

**Integrations:**
- AWS RDS MySQL: UPDATE users table and INSERT into action_history table
- Gmail SMTP: Send email notification

**Authorization:** Administrator email extracted from JWT token for audit logging

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

**Confidence:** High

---

##### Step 4: Return Success Response

**Business Description:** System returns success response to administrator confirming action completion.

**System Touchpoint:** AdminController

**Inputs:**
- Service result: Success or error response from service layer (from Step 3)

**Outputs:**
- SuccessResponse or error: JSON response with status message (to Administrator client)

**Authorization:** Response transmitted over HTTPS

**Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`

**Confidence:** High

---

#### Process Variations

- **Update onboarding status to approved**: User account activated (isActive=true), onboarding status set to approved, approval email sent. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:50-55`)

- **Update onboarding status to rejected**: User account deactivated (isActive=false), onboarding status set to reject, rejection email with comments sent. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:56-60`)

- **Activate or deactivate account**: User isActive flag updated to true or false, action logged in audit trail. No email notification sent for this action. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:80-110`)

- **Change user role**: User role updated to CUSTOMER or ADMIN, action logged in audit trail. No email notification sent for this action. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:115-135`)

- **View user details**: System retrieves and returns detailed user information for specified email address. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:140-155`)

- **Browse paginated user list**: System returns paginated list of users with filtering by onboarding status, role, submission date, company, username, and active status. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:160-190`)

#### Outcomes

- **Success**: User management action completed successfully; user record updated, audit log created, notification sent if applicable
- **Failure**: User management action failed due to invalid user email or system error

---

### Process 4: Project Performance Data Retrieval

**Process ID:** `proc_performance_data_retrieval`

**Description:** The system retrieves project performance metrics and RAG (Red-Amber-Green) status information from an external OneView API for a specified client account. This process supports performance monitoring and reporting workflows by integrating real-time project health data.

#### Actors

- **User or Administrator** (Human): User requesting performance data for a client account
- **OneView API** (System): External API providing project performance and RAG status data

#### Triggers

- **API request**: User requests performance data via GET /performance endpoint with account and optional performanceType parameters

#### Process Steps

##### Step 1: Receive Performance Data Request

**Business Description:** User submits request for performance data specifying client account name and optional project type filter.

**System Touchpoint:** PerformaceController.getPerformance

**Inputs:**
- account: Client account name (from User input)
- performanceType: Optional project type filter (from User input)

**Outputs:**
- PerformanceResponse: List of projects with RAG status for specified account (to User client)

**Authorization:** Public endpoint; no authentication required (potential security concern)

**Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java`

**Confidence:** High

---

##### Step 2: Authenticate with OneView API

**Business Description:** System authenticates with external OneView API using hardcoded credentials to obtain access token.

**System Touchpoint:** OneViewServiceImpl.getPerFormaceData

**Inputs:**
- Hardcoded credentials: Username and password for OneView API (from Configuration)

**Outputs:**
- X-Ava-Access-Token: OneView API access token (to Next step)

**Integrations:**
- OneView API: POST /user/signin

**Authorization:** Uses hardcoded credentials (critical security risk)

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:30-45`

**Confidence:** High

---

##### Step 3: Retrieve Performance Data for Current Date

**Business Description:** System requests project performance data from OneView API for current date (today) with optional project type filter.

**System Touchpoint:** OneViewServiceImpl.getPerFormaceData

**Inputs:**
- weekEndingDates: Current date in MM/dd/yyyy format (from System-generated)
- projectTypes: Optional project type filter (from Step 1)
- X-Ava-Access-Token: OneView API access token (from Step 2)

**Outputs:**
- PerformaceModel: List of projects with RAG status (to Next step)

**Integrations:**
- OneView API: GET /ava/oneview/internal/api/dashboard/rag/list

**Authorization:** Uses custom X-Ava-Access-Token header

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:47-75`

**Confidence:** High

---

##### Step 4: Fallback to Previous Day if Empty

**Business Description:** If current date returns no data, system automatically retries with previous day's date to ensure data availability.

**System Touchpoint:** OneViewServiceImpl.getPerFormaceData

**Inputs:**
- weekEndingDates: Previous day date in MM/dd/yyyy format (from System-generated)
- X-Ava-Access-Token: OneView API access token (from Step 2)

**Outputs:**
- PerformaceModel: List of projects with RAG status from previous day (to Next step)

**Integrations:**
- OneView API: GET /ava/oneview/internal/api/dashboard/rag/list

**Authorization:** Fallback mechanism for data availability

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:77-95`

**Confidence:** High

---

##### Step 5: Filter by Client Account

**Business Description:** System filters retrieved project list to include only projects matching the requested client account name.

**System Touchpoint:** OneViewServiceImpl.getPerFormaceData

**Inputs:**
- deliveryRagProjectList: Full list of projects from OneView API (from Step 3 or 4)
- account: Client account name filter (from Step 1)

**Outputs:**
- PerformanceResponse: Filtered list of projects with project name and RAG status (to User client)

**Authorization:** Client-side filtering

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:96-105`

**Confidence:** High

---

#### Process Variations

- **Current date returns no data**: System automatically falls back to previous day's data to ensure response contains performance information. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:77-95`)

- **Project type filter specified**: If performanceType parameter is provided and not 'All', system includes projectTypes query parameter in OneView API request. (Evidence: `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:60-65`)

#### Outcomes

- **Success**: Performance data successfully retrieved and filtered for specified client account
- **Failure**: Performance data retrieval failed due to OneView API authentication failure or network error

---

## How Processes Relate

### Relationship 1: User Registration → User Authentication

**Type:** precedes

**Description:** User must complete registration and receive admin approval before they can authenticate and access the system.

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:135-145`

**Confidence:** High

---

### Relationship 2: Administrator User Management → User Registration

**Type:** supports

**Description:** Administrator user management process completes the user registration workflow by approving or rejecting registration requests.

**Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:45-75`

**Confidence:** High

---

### Relationship 3: Administrator User Management → User Authentication

**Type:** supports

**Description:** Administrator can activate or deactivate user accounts, directly affecting user's ability to authenticate.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:80-110`
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:140-145`

**Confidence:** High

---

### Relationship 4: User Authentication → Performance Data Retrieval

**Type:** parallel

**Description:** Performance data retrieval is currently a public endpoint that does not require authentication, though this may be a security concern. In a typical workflow, authenticated users would request performance data.

**Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java`

**Confidence:** Medium

---

## Stakeholder FAQ

### How long does it take for a new user registration to be approved?

New user registrations are placed in pending_approval status immediately upon submission. The approval timeline depends on administrator availability to review the request. Users receive email notifications at each stage: pending approval, approved, or rejected.

---

### What happens if a user's account is deactivated?

When an administrator deactivates a user account (sets isActive to false), the user can no longer log in. Any login attempt will fail with a deactivated account error message, even if the user provides correct credentials. The administrator can reactivate the account at any time.

---

### How long are access tokens valid?

Access tokens are valid for 30 minutes. When an access token expires, users must use their refresh token to obtain a new access token without re-entering credentials. Refresh tokens are valid for 24 hours.

---

### Are administrative actions audited?

Yes, all administrative actions (onboarding status changes, account activation/deactivation, role changes) are logged in the action_history table with timestamps, administrator attribution, and optional comments. Administrators can view paginated audit logs via the /api/v1/admin/action-history endpoint.

---

### What is the difference between onboarding status and account status?

Onboarding status (pending_approval, approved, reject) governs the initial approval workflow for new registrations. Account status (isActive: true/false) governs whether an approved user can currently access the system. An approved user can be temporarily deactivated without changing their onboarding status.

---

## Limits and Unknowns

1. **tech_debt_risk.json artifact is missing** from prerequisite artifacts; no technical debt or risk references included in process model

2. **Performance data retrieval endpoint (/performance) appears to be public** with no authentication requirement; security implications unclear

3. **No explicit workflow engine or state machine detected**; process flows inferred from controller and service code

4. **Batch or scheduled processes not detected** in repository; all processes appear to be API-triggered

5. **Email notification failure handling not explicitly documented**; error behavior inferred from exception handling code

6. **OneView API authentication uses hardcoded credentials** (critical security risk); credential rotation or expiration handling unknown

7. **No evidence of password reset or forgot password workflow**

8. **No evidence of multi-factor authentication (MFA) workflow**

9. **User self-service account management** (profile updates, password changes) not detected

10. **Bulk user import or provisioning workflows** not detected

---

## Evidence and Traceability

All process descriptions, steps, and relationships are grounded in repository evidence. For machine-readable traceability links, refer to the `business_process_model.json` file in this artifacts folder. Each process step includes:

- Evidence paths to source code files
- Integration references to `integration_catalog.json`
- Data entity references to `data_model.json` and `domain_model.json`
- Security control references to `security_privacy_assessment.json`

---

**End of Business Process Model**
