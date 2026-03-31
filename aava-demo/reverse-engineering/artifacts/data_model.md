# Data Model: ramanohar/AAVA-Reverse-Engineering-POC

**Generated:** 2025-01-16T12:00:00Z  
**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main  
**Run Mode:** build

---

## Data Assets

### 1. users
- **Store Type:** table
- **Technology:** MySQL 8.0.33
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`
  - `src/main/resources/application-dev.properties:48`

### 2. access_token
- **Store Type:** table
- **Technology:** MySQL 8.0.33
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`

### 3. refresh_token
- **Store Type:** table
- **Technology:** MySQL 8.0.33
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`

### 4. action_history
- **Store Type:** table
- **Technology:** MySQL 8.0.33
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`

---

## Logical Models

### 1. UserEntity
- **Kind:** table
- **Source Path:** `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`
- **Confidence:** high

**Attributes:**
- userId (Integer, PK, auto-increment)
- email (String, unique, required)
- password (String, BCrypt hashed)
- firstname (String)
- lastname (String)
- company (String, required)
- country (String)
- role (String: CUSTOMER | ADMIN)
- onboardingStatus (String: pending_approval | approved | reject)
- isActive (Boolean)
- registrationId (String)
- designation (String)
- clientId (String)
- clientName (String)
- clientOneView (String)
- createdAt (LocalDateTime)
- lastUpdatedAt (LocalDateTime)
- lastUpdatedBy (String)

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java`

---

### 2. AccessToken
- **Kind:** table
- **Source Path:** `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`
- **Confidence:** high

**Attributes:**
- id (Long, PK, auto-increment)
- token (String, TEXT)
- tokenType (TokenType enum: BEARER)
- expired (Boolean)
- revoked (Boolean)
- createdAt (Date, auto-generated)
- user (UserEntity, FK)

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`

---

### 3. RefreshToken
- **Kind:** table
- **Source Path:** `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`
- **Confidence:** high

**Attributes:**
- id (Long, PK, auto-increment)
- token (String, TEXT)
- tokenType (TokenType enum: BEARER)
- expired (Boolean)
- revoked (Boolean)
- createdAt (Date, auto-generated)
- user (UserEntity, FK)

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`

---

### 4. ActionHistory
- **Kind:** table
- **Source Path:** `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`
- **Confidence:** high

**Attributes:**
- id (Long, PK, auto-increment)
- userName (String)
- userEmail (String)
- userRole (String)
- userCompany (String)
- updateByAdminName (String)
- updateByAdminEmail (String)
- registrationId (String)
- action (String)
- adminComments (String)
- actionType (String: OnBoarding Status | Account Status | Role Status)
- createdAt (LocalDateTime, auto-generated)
- updatedAt (LocalDateTime)

**Evidence:**
- `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`

---

## Ownership Mapping

### 1. users
- **Owning Service:** cove-user-service
- **Steward Hint:** User Management Context
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/repository/UserRepository.java`
  - `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

### 2. access_token
- **Owning Service:** cove-user-service
- **Steward Hint:** Authentication & Authorization Context
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/repository/AccessTokenRepo.java`
  - `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java`

### 3. refresh_token
- **Owning Service:** cove-user-service
- **Steward Hint:** Authentication & Authorization Context
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/repository/RefreshTokenRepo.java`
  - `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java`

### 4. action_history
- **Owning Service:** cove-user-service
- **Steward Hint:** Audit & Compliance Context
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/repository/ActionHistoryRepo.java`
  - `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java`

---

## Key Relationships

### 1. UserEntity → AccessToken (one-to-many)
- **Key Fields:** userId
- **Reference Type:** foreign_key
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`

### 2. UserEntity → RefreshToken (one-to-many)
- **Key Fields:** userId
- **Reference Type:** foreign_key
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`

### 3. UserEntity → ActionHistory (one-to-many)
- **Key Fields:** userEmail
- **Reference Type:** logical_association
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`

---

## Event and Contract Summary

### 1. Email Notification (Onboarding)
- **Producer:** cove-user-service
- **Consumer:** Gmail SMTP
- **Payload Summary:** Email address, subject, FreeMarker template (approved/rejected/pending), template model (name, description)
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java`
  - `src/main/resources/templates/email-template-approved.ftl`

### 2. Performance Data Request
- **Producer:** cove-user-service
- **Consumer:** OneView API
- **Payload Summary:** POST /user/signin (userName, password); GET /ava/oneview/internal/api/dashboard/rag/list (weekEndingDates, projectTypes)
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java`

---

## Analysis Metadata

### Inputs
- **Repository:** ramanohar/AAVA-Reverse-Engineering-POC
- **Branch:** main
- **Run Mode:** build

### Limits and Unknowns

1. **Database schema relationships inferred from JPA entity annotations**: No database migration scripts or ER diagrams available.

2. **No explicit foreign key constraints visible in entity code**: Relationships inferred from @ManyToOne and @OneToMany annotations.

3. **Event contracts limited to email notifications and external API calls**: No internal event bus or message queue detected.

4. **Client-related fields (clientId, clientName, clientOneView) in UserEntity have unclear business purpose**: Insufficient evidence to determine their role.

5. **No database migration tool (Flyway/Liquibase) detected**: Schema evolution strategy unknown.

---

**End of Data Model**
