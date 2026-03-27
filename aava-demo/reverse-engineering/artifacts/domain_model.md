# Domain Model: ramanohar/AAVA-Reverse-Engineering-POC

**Generated:** 2025-01-16T12:00:00Z  
**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main  
**Run Mode:** build

---

## Domain Glossary

### User
**Definition:** An individual who registers and interacts with the COVE user management system. Users have roles (CUSTOMER or ADMIN), onboarding status, and account activation state.

**Aliases:** Customer, Admin, Registered User

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`
- `src/main/java/com/collaberadigital/cove/service/UserService.java`

**Confidence:** High

---

### Onboarding
**Definition:** The process by which a newly registered user is reviewed and approved or rejected by an administrator before gaining full system access.

**Aliases:** User Approval, Registration Approval

**Evidence:**
- `src/main/java/com/collaberadigital/cove/utils/constant/OnboardingStatus.java`
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

**Confidence:** High

---

### Onboarding Status
**Definition:** The current state of a user's onboarding process. Valid values: pending_approval, approved, reject.

**Aliases:** Approval Status

**Evidence:**
- `src/main/java/com/collaberadigital/cove/utils/constant/OnboardingStatus.java`
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`

**Confidence:** High

---

### Access Token
**Definition:** A short-lived JWT token (30 minutes validity) used to authenticate API requests. Stored in the database with expiration and revocation flags.

**Aliases:** JWT Access Token, Auth Token

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java`

**Confidence:** High

---

### Refresh Token
**Definition:** A long-lived JWT token (24 hours validity) used to obtain new access tokens without re-authentication. Stored in the database with expiration and revocation flags.

**Aliases:** JWT Refresh Token

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`
- `src/main/java/com/collaberadigital/cove/security/JwtRefreshTokenUtil.java`

**Confidence:** High

---

### Action History
**Definition:** An audit log entry recording administrative actions performed on user accounts, including onboarding status changes, role updates, and account activation/deactivation.

**Aliases:** Audit Log, Admin Action Log

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`
- `src/main/java/com/collaberadigital/cove/service/ActionHistoryService.java`

**Confidence:** High

---

### Role
**Definition:** A user's permission level within the system. Valid values: CUSTOMER (standard user) or ADMIN (administrative privileges).

**Aliases:** User Role, Permission Level

**Evidence:**
- `src/main/java/com/collaberadigital/cove/utils/constant/UserRole.java`
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`

**Confidence:** High

---

### Account Status
**Definition:** Indicates whether a user account is currently active or deactivated. Active accounts can authenticate; deactivated accounts cannot.

**Aliases:** Active Status, Account State

**Evidence:**
- `src/main/java/com/collaberadigital/cove/utils/constant/AccountStatus.java`
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`

**Confidence:** High

---

### Registration
**Definition:** The process by which a new user creates an account in the system, providing personal and company information. Registration triggers the onboarding workflow.

**Aliases:** Sign Up, User Creation

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

**Confidence:** High

---

### Authentication
**Definition:** The process of verifying a user's identity using email and password credentials, resulting in the issuance of access and refresh tokens.

**Aliases:** Login, Sign In

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

**Confidence:** High

---

### Token Revocation
**Definition:** The process of invalidating access or refresh tokens, preventing their further use for authentication.

**Aliases:** Token Invalidation, Logout

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java`
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java`

**Confidence:** High

---

### Email Notification
**Definition:** Automated email messages sent to users during onboarding workflow transitions (pending approval, approved, rejected).

**Aliases:** Email Alert, Notification

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/EmailService.java`
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java`

**Confidence:** High

---

### Performance Data
**Definition:** Project performance metrics and RAG (Red-Amber-Green) status information retrieved from the external OneView API.

**Aliases:** Project Metrics, RAG Status

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/OneViewService.java`
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java`

**Confidence:** High

---

## Business Entities

### User
**Purpose:** Represents an individual registered in the COVE system with authentication credentials, role-based permissions, and onboarding workflow state.

**Key Attributes:**
- `userId` (Integer, primary key)
- `email` (String, unique, required)
- `password` (String, BCrypt hashed)
- `firstname` (String)
- `lastname` (String)
- `company` (String, required)
- `country` (String)
- `role` (String: CUSTOMER | ADMIN)
- `onboardingStatus` (String: pending_approval | approved | reject)
- `isActive` (Boolean)
- `registrationId` (String)
- `designation` (String)
- `clientId` (String)
- `clientName` (String)
- `clientOneView` (String)
- `createdAt` (LocalDateTime)
- `lastUpdatedAt` (LocalDateTime)
- `lastUpdatedBy` (String)

**Owned By:** User Management Context

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`

**Confidence:** High

---

### AccessToken
**Purpose:** Represents a short-lived JWT token used for API authentication. Tracks token validity, expiration, and revocation status.

**Key Attributes:**
- `id` (Long, primary key)
- `token` (String, TEXT)
- `tokenType` (TokenType enum: BEARER)
- `expired` (Boolean)
- `revoked` (Boolean)
- `createdAt` (Date, auto-generated)
- `user` (UserEntity, foreign key)

**Owned By:** Authentication & Authorization Context

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`

**Confidence:** High

---

### RefreshToken
**Purpose:** Represents a long-lived JWT token used to obtain new access tokens without re-authentication. Tracks token validity, expiration, and revocation status.

**Key Attributes:**
- `id` (Long, primary key)
- `token` (String, TEXT)
- `tokenType` (TokenType enum: BEARER)
- `expired` (Boolean)
- `revoked` (Boolean)
- `createdAt` (Date, auto-generated)
- `user` (UserEntity, foreign key)

**Owned By:** Authentication & Authorization Context

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`

**Confidence:** High

---

### ActionHistory
**Purpose:** Audit log entry recording administrative actions performed on user accounts for compliance and traceability.

**Key Attributes:**
- `id` (Long, primary key)
- `userName` (String)
- `userEmail` (String)
- `userRole` (String)
- `userCompany` (String)
- `updateByAdminName` (String)
- `updateByAdminEmail` (String)
- `registrationId` (String)
- `action` (String: onboarding status | account status | role)
- `adminComments` (String)
- `actionType` (String: OnBoarding Status | Account Status | Role Status)
- `createdAt` (LocalDateTime, auto-generated)
- `updatedAt` (LocalDateTime)

**Owned By:** Audit & Compliance Context

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`

**Confidence:** High

---

## Entity Relationships

### User → AccessToken (one-to-many)
**Description:** A User can have multiple AccessTokens over time. Each AccessToken belongs to exactly one User. Tokens are revoked and regenerated on each login.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

**Confidence:** High

---

### User → RefreshToken (one-to-many)
**Description:** A User can have multiple RefreshTokens over time. Each RefreshToken belongs to exactly one User. Tokens are revoked and regenerated on each login.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

**Confidence:** High

---

### User → ActionHistory (one-to-many)
**Description:** A User (as subject) can have multiple ActionHistory entries recording administrative actions performed on their account. ActionHistory is not directly linked via foreign key but associated by userEmail.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`
- `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java`

**Confidence:** High

---

### User (Admin) → ActionHistory (one-to-many)
**Description:** A User with ADMIN role can create multiple ActionHistory entries by performing administrative actions on other users. ActionHistory records the admin's email and name.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

**Confidence:** High

---

### User → User (self-referential, audit trail)
**Description:** Users track who last updated their record via lastUpdatedBy field, creating an implicit relationship between users for audit purposes.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

**Confidence:** Medium

---

## Bounded Contexts

### User Management
**Responsibilities:**
- User registration and profile management
- User onboarding workflow (pending_approval → approved/reject)
- Account activation and deactivation
- Role assignment (CUSTOMER, ADMIN)
- User search and pagination

**Core Entities:**
- User

**Interfaces:**

#### REST API
- `POST /register`
- `PUT /api/v1/admin/{toEmail}/onboarding-status`
- `PUT /api/v1/admin/{toEmail}/account-status`
- `PUT /api/v1/admin/{toEmail}/role`
- `GET /api/v1/admin/user-details/{email}`
- `GET /api/v1/admin/users`

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`
- `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`

#### Service Layer
- UserService
- AdminService

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/UserService.java`
- `src/main/java/com/collaberadigital/cove/service/AdminService.java`

#### Email Notifications
Sends onboarding status notifications (pending, approved, rejected)

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/EmailService.java`

**Context Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

**Confidence:** High

---

### Authentication & Authorization
**Responsibilities:**
- User authentication (login)
- JWT access token generation and validation (30-minute validity)
- JWT refresh token generation and validation (24-hour validity)
- Token revocation (logout)
- Token refresh workflow
- Password encryption (BCrypt)
- Role-based access control enforcement

**Core Entities:**
- AccessToken
- RefreshToken
- User

**Interfaces:**

#### REST API
- `POST /login`
- `POST /check-access-token`
- `POST /refresh-token`
- `POST /revoke-access-token`
- `POST /revoke-refresh-token`

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`

#### Service Layer
- AuthService
- UserService

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/AuthService.java`
- `src/main/java/com/collaberadigital/cove/service/UserService.java`

#### Security Components
- JwtAccessTokenUtil
- JwtRefreshTokenUtil
- JwtTokenAuthenticationFilter
- CustomUserDetailsService

**Evidence:**
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java`
- `src/main/java/com/collaberadigital/cove/security/JwtRefreshTokenUtil.java`

**Context Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java`
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java`

**Confidence:** High

---

### Audit & Compliance
**Responsibilities:**
- Recording administrative actions on user accounts
- Tracking onboarding status changes
- Tracking account activation/deactivation
- Tracking role changes
- Providing audit trail with admin attribution
- Paginated audit log retrieval with filtering

**Core Entities:**
- ActionHistory

**Interfaces:**

#### REST API
- `GET /api/v1/admin/action-history`

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`

#### Service Layer
- ActionHistoryService

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/ActionHistoryService.java`

**Context Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java`
- `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`

**Confidence:** High

---

### External Integration
**Responsibilities:**
- Integration with OneView API for project performance data
- Fetching RAG (Red-Amber-Green) status for projects
- OneView authentication and token management
- Email delivery via Gmail SMTP

**Core Entities:** (None - integration context)

**Interfaces:**

#### REST API
- `GET /performance`

**Evidence:**
- `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java`

#### Service Layer
- OneViewService
- EmailService

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/OneViewService.java`
- `src/main/java/com/collaberadigital/cove/service/EmailService.java`

#### External APIs
- OneView API (https://cdoneview.avateam.io)
- Gmail SMTP (smtp.gmail.com:465)

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java`
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java`

**Context Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java`
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java`

**Confidence:** High

---

## Ubiquitous Language Recommendations

### Recommended Terms

#### Onboarding
**Usage:** Prefer 'onboarding' over 'approval' or 'registration approval' to describe the user review process.

**Rationale:** Consistently used in code (OnboardingStatus, updateUserOnboardingStatus) and aligns with business process terminology.

---

#### Access Token
**Usage:** Use 'access token' for short-lived authentication tokens; avoid 'auth token' or 'JWT token'.

**Rationale:** Distinguishes from refresh tokens and matches entity naming (AccessToken).

---

#### Refresh Token
**Usage:** Use 'refresh token' for long-lived tokens used to obtain new access tokens.

**Rationale:** Matches entity naming (RefreshToken) and clearly differentiates from access tokens.

---

#### Action History
**Usage:** Use 'action history' for audit logs; avoid 'audit log' or 'admin log'.

**Rationale:** Matches entity naming (ActionHistory) and service naming (ActionHistoryService).

---

#### Account Status
**Usage:** Use 'account status' to refer to active/deactivated state; avoid 'active status' or 'account state'.

**Rationale:** Matches constant naming (AccountStatus) and API endpoint naming.

---

### Naming Conflicts

#### User vs Customer
**Conflict:** The term 'Customer' is used as a role value (UserRole.Customer) but 'User' is the entity name (UserEntity). This creates ambiguity.

**Recommendation:** Clarify that 'Customer' is a role type, not a separate entity. Use 'User with CUSTOMER role' in documentation.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/utils/constant/UserRole.java`
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`

---

#### Onboarding Status vs Account Status
**Conflict:** Two similar-sounding status fields: onboardingStatus (pending_approval/approved/reject) and isActive (boolean account status).

**Recommendation:** Clearly distinguish: 'Onboarding Status' governs approval workflow; 'Account Status' governs active/deactivated state post-approval.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`
- `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

---

#### Action vs Action Type
**Conflict:** ActionHistory entity has both 'action' (the status value) and 'actionType' (the category: OnBoarding Status, Account Status, Role Status).

**Recommendation:** Rename 'action' to 'actionValue' or 'statusValue' and keep 'actionType' for category. This clarifies the distinction.

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`
- `src/main/java/com/collaberadigital/cove/utils/constant/ActionType.java`

---

## Analysis Metadata

### Inputs
- **Repository:** ramanohar/AAVA-Reverse-Engineering-POC
- **Branch:** main
- **Run Mode:** build
- **Prerequisite Artifacts:**
  - repository_summary.json
  - dependency_graph.json

### Limits and Unknowns

1. **Database schema relationships inferred from JPA entity annotations**: No database migration scripts or ER diagrams available.

2. **Bounded context boundaries derived from package structure and service responsibilities**: No explicit domain-driven design documentation found.

3. **OneView API integration details limited to implementation code**: No API specification or contract documentation available.

4. **Email template content not analyzed**: Only service-level email sending logic reviewed.

5. **Performance data domain model (RAG status, project metrics) not fully elaborated**: Limited evidence in OneView integration code.

6. **Client-related fields (clientId, clientName, clientOneView) in UserEntity have unclear business purpose**: Insufficient evidence to determine their role in the domain.

7. **Registration ID generation and usage pattern not fully documented in code**.

---

**End of Domain Model**
