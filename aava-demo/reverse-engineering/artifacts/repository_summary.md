# Repository Summary: ramanohar/AAVA-Reverse-Engineering-POC

**Analysis Date:** 2025-01-16  
**Primary Language:** Java  
**Build System:** Maven  
**Framework:** Spring Boot 3.2.5  
**Java Version:** 17  
**Application Type:** Reactive Web Service

---

## Overview

This repository contains **cove-user-service**, a Spring Boot 3.2.5 reactive web application built with Java 17. It is a comprehensive user authentication and management service featuring JWT-based security, MySQL database integration, and AWS EKS deployment capabilities. The service implements a complete user lifecycle management system with role-based access control, email notifications, and audit logging.

---

## Modules

### cove-user-service
- **Type:** Spring Boot Application
- **Description:** User authentication and management service with JWT-based security
- **Evidence:** `pom.xml:13-15`, `src/main/java/com/collaberadigital/cove/CoveUserServiceApplication.java`

---

## Technology Stack

### Backend Framework
- **Spring Boot:** 3.2.5
- **Reactive Stack:** Spring WebFlux
- **Security:** Spring Security with JWT (jjwt 0.11.5)
- **Database:** MySQL 8.0.33 with JPA/Hibernate
- **Service Discovery:** Netflix Eureka Client
- **Monitoring:** Micrometer + Prometheus
- **Email:** Spring Mail + FreeMarker templates
- **Validation:** Hibernate Validator 6.0.16
- **Utilities:** Lombok, Jackson

**Evidence:** `pom.xml:21-127`

---

## Build Configuration

- **Build Tool:** Maven
- **Wrapper Version:** 3.9.6
- **Parent POM:** spring-boot-starter-parent:3.2.5
- **Spring Cloud Version:** 2023.0.1
- **Plugins:**
  - spring-boot-maven-plugin
  - maven-resources-plugin:3.1.0

**Evidence:** `pom.xml`, `.mvn/wrapper/maven-wrapper.properties`

---

## Deployment

### Containerization
- **Platform:** Docker
- **Base Image:** amazoncorretto:17
- **Exposed Port:** 8090
- **Artifact:** cove-user-service-0.0.1-SNAPSHOT.jar

### Orchestration
- **Platform:** Kubernetes (AWS EKS)
- **CI/CD:** AWS CodeBuild

**Evidence:** `Dockerfile`, `buildspec.yml`, `kubernetes/deployment.yaml`, `kubernetes/service.yaml`

### Kubernetes Configuration
- **Deployment Name:** cove-user-backend
- **Replicas:** 2
- **Service Type:** ClusterIP
- **Service Port:** 80 → Container Port: 8090
- **Resources:**
  - Requests: CPU 250m, Memory 512Mi
  - Limits: CPU 250m, Memory 1024Mi
- **Monitoring:** Prometheus scraping enabled on port 8090

**Evidence:** `kubernetes/deployment.yaml:1-36`, `kubernetes/service.yaml`

---

## Entry Points

### 1. Main Application Class
- **Class:** `CoveUserServiceApplication`
- **Package:** `com.collaberadigital.cove`
- **Annotations:** `@SpringBootApplication`, `@ComponentScan`, `@EnableJpaRepositories`
- **Evidence:** `src/main/java/com/collaberadigital/cove/CoveUserServiceApplication.java:8-17`

### 2. Authentication Controller
- **Class:** `AuthRestController`
- **Endpoints:**
  - `POST /login` - User authentication
  - `POST /register` - User registration
  - `POST /check-access-token` - Validate access token
  - `POST /refresh-token` - Refresh JWT token
  - `POST /revoke-access-token` - Revoke access token
  - `POST /revoke-refresh-token` - Revoke refresh token
- **Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`

### 3. Admin Controller
- **Class:** `AdminController`
- **Base Path:** `/api/v1/admin`
- **Security:** Requires ADMIN role
- **Endpoints:**
  - `PUT /{toEmail}/onboarding-status` - Update user onboarding status
  - `PUT /{toEmail}/account-status` - Update user account status
  - `PUT /{toEmail}/role` - Update user role
  - `GET /user-details/{email}` - Get user details
  - `GET /users` - Get paginated user list
  - `GET /action-history` - Get action history with pagination
- **Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`, `src/main/java/com/collaberadigital/cove/configuration/SecurityConfig.java:44-51`

### 4. Performance Controller
- **Class:** `PerformaceController`
- **Endpoints:**
  - `GET /performance` - Get performance data from OneView
- **Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java`

### 5. Health Check
- **Class:** `Healthcheck`
- **Endpoints:**
  - `GET /healthcheck` - Application health check
- **Evidence:** `src/main/java/com/collaberadigital/cove/controller/impl/Healthcheck.java`

---

## Security Configuration

### Framework
- **Security:** Spring Security (WebFlux)
- **Authentication:** JWT (Access Token + Refresh Token)
- **Token Storage:** Database (access_token, refresh_token tables)
- **Password Encoding:** BCrypt
- **JWT Library:** jjwt 0.11.5

### Token Configuration
- **Access Token Validity:** 30 minutes (1800000ms)
- **Refresh Token Validity:** 24 hours (86400000ms)
- **Security Context:** NoOpServerSecurityContextRepository (stateless)

### Endpoint Protection
- **Protected:** `/api/v1/admin/**` (requires ADMIN role)
- **Public:** All other endpoints

**Evidence:**
- `src/main/java/com/collaberadigital/cove/configuration/SecurityConfig.java`
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java:31-33`
- `src/main/java/com/collaberadigital/cove/security/JwtRefreshTokenUtil.java:33-35`
- `src/main/resources/application-dev.properties:32-35`

---

## Database Schema

### Entities

#### 1. UserEntity
- **Table:** `users`
- **Primary Key:** `user_id` (auto-increment)
- **Key Fields:**
  - `email` (unique)
  - `registration_id`
  - `role` (CUSTOMER, ADMIN)
  - `onboarding_status` (pending_approval, approved, reject)
  - `is_active` (boolean)
- **Evidence:** `src/main/java/com/collaberadigital/cove/model/entity/UserEntity.java:14-75`

#### 2. AccessToken
- **Table:** `access_token`
- **Primary Key:** `id` (auto-increment)
- **Foreign Keys:** `user_id` → `users.user_id`
- **Key Fields:** `token` (TEXT), `expired`, `revoked`, `created_at`
- **Evidence:** `src/main/java/com/collaberadigital/cove/model/entity/AccessToken.java`

#### 3. RefreshToken
- **Table:** `refresh_token`
- **Primary Key:** `id` (auto-increment)
- **Foreign Keys:** `user_id` → `users.user_id`
- **Key Fields:** `token` (TEXT), `expired`, `revoked`, `created_at`
- **Evidence:** `src/main/java/com/collaberadigital/cove/model/entity/RefreshToken.java`

#### 4. ActionHistory
- **Table:** `action_history`
- **Primary Key:** `id` (auto-increment)
- **Key Fields:** `user_email`, `action`, `action_type`, `update_by_admin_email`, `created_at`
- **Evidence:** `src/main/java/com/collaberadigital/cove/model/entity/ActionHistory.java`

### Database Connection
- **Driver:** com.mysql.cj.jdbc.Driver
- **URL:** jdbc:mysql://cove-db.chioww02cxoo.ap-south-1.rds.amazonaws.com:3306/cove
- **JPA Settings:** generate-ddl=true, hibernate.order_inserts=true
- **Evidence:** `src/main/resources/application-dev.properties:47-55`

---

## Service Layer

### 1. UserService
- **Implementation:** `UserServiceImpl`
- **Responsibilities:**
  - User registration
  - User login
  - User approval
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/UserService.java`, `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

### 2. AdminService
- **Implementation:** `AdminServiceImpl`
- **Responsibilities:**
  - Update onboarding status
  - Update account status
  - Update user role
  - Get user details
  - Paginated user listing
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/AdminService.java`, `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

### 3. AuthService
- **Implementation:** `AuthServiceImpl`
- **Responsibilities:**
  - Check access token
  - Refresh token
  - Revoke access token
  - Revoke refresh token
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/AuthService.java`, `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java`

### 4. EmailService
- **Implementation:** `EmailServiceImpl`
- **Responsibilities:**
  - Send rejection email
  - Send approval email
  - Send pending approval email
- **Email Templates:**
  - email-template-rejected.ftl
  - email-template-approved.ftl
  - email-template-pending-approval.ftl
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/EmailService.java`, `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java`, `src/main/resources/templates/`

### 5. ActionHistoryService
- **Implementation:** `ActionHistoryServiceImpl`
- **Responsibilities:**
  - Save action history
  - Get paginated action history
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/ActionHistoryService.java`, `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java`

### 6. OneViewService
- **Implementation:** `OneViewServiceImpl`
- **Responsibilities:**
  - Fetch performance data from external OneView API
- **External Integration:** https://cdoneview.avateam.io
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/OneViewService.java`, `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:28`

---

## Repository Layer

### 1. UserRepository
- **Extends:** `JpaRepository<UserEntity, Integer>`
- **Custom Queries:**
  - findByEmail
  - existsByRegistrationId
  - findByOnboardingStatusInAndSubmissionDate (with pagination)
  - findByOnboardingStatusInAndRoleIn (with pagination)
- **Evidence:** `src/main/java/com/collaberadigital/cove/repository/UserRepository.java`

### 2. AccessTokenRepo
- **Extends:** `JpaRepository<AccessToken, Long>`
- **Custom Queries:**
  - findAllByUserUserIdAndExpiredFalseAndRevokedFalse
  - findFirstByTokenOrderByCreatedAtDesc
  - findByToken
- **Evidence:** `src/main/java/com/collaberadigital/cove/repository/AccessTokenRepo.java`

### 3. RefreshTokenRepo
- **Extends:** `JpaRepository<RefreshToken, Long>`
- **Custom Queries:**
  - findAllByUserUserIdAndExpiredFalseAndRevokedFalse
  - findFirstByTokenOrderByCreatedAtDesc
  - findByToken
- **Evidence:** `src/main/java/com/collaberadigital/cove/repository/RefreshTokenRepo.java`

### 4. ActionHistoryRepo
- **Extends:** `JpaRepository<ActionHistory, Long>`
- **Custom Queries:**
  - findByActionHistoryWithDate (complex JPQL with pagination)
  - findByActionHistoryWithoutDate (complex JPQL with pagination)
- **Evidence:** `src/main/java/com/collaberadigital/cove/repository/ActionHistoryRepo.java`

### 5. CustomerDao (Reactive)
- **Implementation:** `CustomerDaoImpl`
- **Type:** Reactive custom DAO
- **Methods:** findByEmailReactive (returns Mono<UserEntity>)
- **Evidence:** `src/main/java/com/collaberadigital/cove/repository/CustomerDao.java`, `src/main/java/com/collaberadigital/cove/repository/repositoryIml/CustomerDaoImpl.java`

---

## Configuration

### Application Settings
- **Application Name:** cove-user-service
- **Server Port:** 8090
- **Active Profile:** dev
- **Web Application Type:** reactive

### Service Discovery
- **Eureka Service URL:** http://k8s-coveeurekaingress-099090752f-315466093.ap-south-1.elb.amazonaws.com/eureka

### Actuator Endpoints
- health
- info
- prometheus
- metrics

### CORS
- **Enabled:** true

### Mail Configuration
- **Host:** smtp.gmail.com
- **Port:** 465
- **Protocol:** smtp
- **SSL Enabled:** true

**Evidence:** `src/main/resources/application.properties`, `src/main/resources/application-dev.properties`

---

## Exception Handling

### Global Handler
- **Class:** `GlobalExceptionHandler`

### Custom Exceptions
- CoveCustomException
- UserCreationException
- UserNotFoundException
- CustomException (reactive)

### Reactive Advice
- **Class:** `CustomControllerAdvice`

**Evidence:**
- `src/main/java/com/collaberadigital/cove/exception/GlobalExceptionHandler.java`
- `src/main/java/com/collaberadigital/cove/exception/reactive/CustomControllerAdvice.java`

---

## Testing

- **Framework:** JUnit 5 + Spring Boot Test
- **Test Files:** `src/test/java/com/collaberadigital/cove/CoveUserServiceApplicationTests.java` (commented out)
- **Status:** Minimal test coverage - tests are commented out
- **Evidence:** `src/test/java/com/collaberadigital/cove/CoveUserServiceApplicationTests.java`

---

## CI/CD Pipeline

### Platform
- **AWS CodeBuild**

### Phases

#### 1. Install
- **Runtime:** corretto17
- **Actions:** Install Maven

#### 2. Pre-Build
- **Actions:**
  - ECR login
  - Install kubectl
  - Configure EKS cluster

#### 3. Build
- **Actions:**
  - mvn clean install
  - Docker build
  - Docker tag

#### 4. Post-Build
- **Actions:**
  - Docker push to ECR
  - Deploy to EKS
  - Apply Kubernetes manifests
  - Rollout restart if exists

### Configuration
- **Parameter Store:** /cove/staging/cove-backend

**Evidence:** `buildspec.yml`

---

## Key Features

1. JWT-based authentication with access and refresh tokens
2. User registration with admin approval workflow
3. Role-based access control (CUSTOMER, ADMIN)
4. User onboarding status management (pending_approval, approved, reject)
5. Account activation/deactivation
6. Action history tracking for audit
7. Email notifications (approval, rejection, pending)
8. Token revocation and refresh mechanism
9. Reactive programming with Spring WebFlux
10. Integration with external OneView API for performance data
11. Prometheus metrics and health checks
12. AWS EKS deployment with auto-scaling

---

## Architectural Patterns

- Layered Architecture (Controller → Service → Repository)
- Reactive Programming (WebFlux, Mono, Flux)
- Repository Pattern with JPA
- DTO Pattern for data transfer
- Builder Pattern (Lombok @Builder)
- Dependency Injection (Spring)
- Filter Chain Pattern (JWT authentication filter)
- Template Method Pattern (FreeMarker email templates)

---

## Security Features

- JWT token-based authentication
- BCrypt password hashing
- Token expiration and revocation
- Stateless session management
- Role-based endpoint protection
- CSRF disabled (stateless API)
- Custom authentication filter
- User account status validation

---

## External Integrations

### 1. OneView API
- **URL:** https://cdoneview.avateam.io
- **Purpose:** Fetch project performance and RAG status data
- **Authentication:** Custom token-based (X-Ava-Access-Token header)
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:28-75`

### 2. Gmail SMTP
- **Purpose:** Send email notifications
- **Configuration:** smtp.gmail.com:465 with SSL
- **Evidence:** `src/main/resources/application-dev.properties:41-46`

### 3. Netflix Eureka
- **Purpose:** Service discovery and registration
- **Evidence:** `pom.xml:44-47`, `src/main/resources/application-dev.properties:2-5`

### 4. AWS RDS MySQL
- **Purpose:** Primary database
- **Endpoint:** cove-db.chioww02cxoo.ap-south-1.rds.amazonaws.com:3306
- **Evidence:** `src/main/resources/application-dev.properties:48`

---

## Code Quality Observations

### Strengths
- Extensive use of Lombok reduces boilerplate
- Reactive programming with proper Mono/Flux usage
- Comprehensive exception handling
- Separation of concerns (DTOs, entities, services)
- Custom validation and error responses

### Areas for Improvement
- Commented-out code blocks should be removed
- Test coverage is minimal (tests commented out)
- Hardcoded credentials in OneViewServiceImpl (security risk)
- Large commented code blocks in entity classes

---

## Potential Improvements

1. Add comprehensive unit and integration tests
2. Remove commented-out code
3. Externalize OneView credentials to configuration
4. Add API documentation (Swagger/OpenAPI)
5. Implement request/response logging
6. Add rate limiting for authentication endpoints
7. Implement password reset functionality
8. Add database migration tool (Flyway/Liquibase)
9. Enhance error messages with i18n support
10. Add caching layer (Redis) for tokens

---

## Dependencies Summary

- **Total Dependencies:** 15
- **Spring Boot Starters:** 8
- **Security Libraries:** 4
- **Database Libraries:** 2
- **Monitoring Libraries:** 2
- **Utility Libraries:** 3
- **Test Libraries:** 2

---

## File Statistics

- **Total Files:** 104
- **Java Files:** 85
- **Configuration Files:** 7
- **Template Files:** 3
- **Kubernetes Manifests:** 2
- **Build Files:** 4
- **Documentation Files:** 0

---

## Gaps and Unknowns

### 1. API Documentation
- **Description:** No Swagger/OpenAPI specification found
- **Confidence:** High

### 2. Database Migrations
- **Description:** No Flyway or Liquibase migrations detected; relies on JPA auto-DDL
- **Confidence:** High

### 3. Logging Configuration
- **Description:** No explicit logback.xml or log4j2.xml configuration
- **Confidence:** Medium

### 4. Caching Strategy
- **Description:** No caching implementation (Redis, Caffeine) detected
- **Confidence:** High

### 5. Rate Limiting
- **Description:** No rate limiting or throttling mechanism found
- **Confidence:** High

### 6. Password Reset
- **Description:** No password reset or forgot password functionality
- **Confidence:** High

---

## Notes

- This is a production-ready Spring Boot reactive microservice for user management
- Deployed on AWS EKS with automated CI/CD pipeline
- Uses JWT for stateless authentication with token storage in database
- Integrates with external OneView API for performance metrics
- Email notifications handled via Gmail SMTP with FreeMarker templates
- Prometheus metrics enabled for monitoring
- **Security concern:** Hardcoded credentials in OneViewServiceImpl should be externalized
- Test coverage needs significant improvement
- Consider adding API documentation and database migration tools

---

**End of Repository Summary**