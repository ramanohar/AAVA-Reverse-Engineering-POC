# COVE User Service — Architecture Summary

**Generated:** 2025-01-16 12:00:00 UTC  
**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main  
**Artifacts Consumed:** repository_summary.json, dependency_graph.json, domain_model.json, integration_catalog.json, data_model.json, security_privacy_assessment.json, business_process_model.json

---

## How to Read This Pack

This document provides a human-readable narrative of the COVE User Service architecture, synthesized from upstream reverse-engineering artifacts. For automation and detailed cross-references, see **architecture_summary.json**. For visual representations, see **architecture_diagrams.md**.

---

## Executive Summary

COVE User Service is a reactive Spring Boot microservice deployed on AWS EKS that provides JWT-based authentication, user registration with admin-approval workflows, and role-based access control for the COVE platform. Primary users include end-users registering for accounts (CUSTOMER role) and administrators managing user onboarding, account activation, and role assignments (ADMIN role). The system integrates with external OneView API for project performance data, uses MySQL for persistence, and sends email notifications via Gmail SMTP. The architecture follows a layered pattern with reactive programming (Spring WebFlux), stateless JWT authentication, and comprehensive audit logging for compliance.

### Key Characteristics

- **Reactive microservice architecture** with Spring Boot 3.2.5 and WebFlux for non-blocking I/O and scalability.
- **JWT-based stateless authentication** with short-lived access tokens (30 minutes) and long-lived refresh tokens (24 hours).
- **Admin-approval onboarding workflow** with email notifications at each stage (pending, approved, rejected).
- **Role-based access control (RBAC)** with CUSTOMER and ADMIN roles; admin endpoints protected via Spring Security.
- **Comprehensive audit logging** for all administrative actions (onboarding, account status, role changes).
- **AWS EKS deployment** with automated CI/CD via AWS CodeBuild; Prometheus metrics for observability.

---

## System Context

### Purpose

Manage user authentication, registration, and onboarding workflows for the COVE platform with role-based access control and integration with external performance data sources.

### Primary Users

| User Type | Description |
|-----------|-------------|
| **End User (CUSTOMER role)** | Individuals registering for COVE accounts, authenticating, and accessing platform features post-approval. |
| **Administrator (ADMIN role)** | Platform administrators who review and approve user registrations, manage account activation/deactivation, assign roles, and access audit logs. |

### External Systems

| System | Role |
|--------|------|
| **OneView API** | External project performance and RAG status data provider |
| **AWS RDS MySQL** | Primary relational database for user entities, tokens, and audit logs |
| **Gmail SMTP** | Email notification delivery for onboarding workflow events |
| **Netflix Eureka** | Service discovery and registration for microservices ecosystem |
| **Prometheus** | Metrics collection and monitoring via scrape endpoint |
| **AWS ECR** | Docker image registry for container storage |
| **AWS EKS** | Kubernetes orchestration platform for container deployment |

### Context Boundaries

**In Scope:**
- User registration, authentication, onboarding approval, account management, role assignment
- Audit logging, JWT token lifecycle management, email notifications
- External performance data retrieval

**Out of Scope:**
- Password reset/forgot password functionality
- Multi-factor authentication (MFA)
- API documentation (Swagger/OpenAPI)
- Database migration tooling (Flyway/Liquibase)
- Caching layer (Redis)
- Application-level rate limiting

---

## Architectural Drivers

### Security and Compliance

JWT-based authentication with token revocation, BCrypt password hashing, role-based access control, and comprehensive audit logging are implemented to meet security and compliance requirements. However, **critical security gaps exist**: hardcoded credentials in source code and configuration files, missing SSL/TLS for database connections, no rate limiting or account lockout for authentication endpoints, and no multi-factor authentication (MFA).

### Scalability and Resilience

Reactive programming with Spring WebFlux, stateless JWT authentication, and Kubernetes deployment with 2 replicas and resource limits support horizontal scaling. Service discovery via Eureka enables dynamic service registration.

### Observability and Monitoring

Prometheus metrics exposed via Spring Actuator enable monitoring of application health, JVM metrics, and custom metrics. Kubernetes deployment includes Prometheus scrape annotations.

### Operational Efficiency

Automated CI/CD pipeline via AWS CodeBuild with Docker image build, ECR push, and EKS deployment reduces manual deployment effort. Kubernetes rollout restart ensures zero-downtime updates.

### User Experience

Email notifications at each onboarding stage (pending, approved, rejected) keep users informed. Admin approval workflow ensures controlled access to the platform.

---

## Logical Architecture

### Containers/Modules

| Module | Responsibility |
|--------|----------------|
| **cove-user-service** | Core Spring Boot application providing REST API endpoints for user management, authentication, and admin functions. Orchestrates service layer, repository layer, and external integrations. |
| **Controller Layer** | REST controllers handling HTTP requests and responses. Includes AuthRestController, AdminController, PerformaceController, Healthcheck. |
| **Service Layer** | Business logic implementation for user management, authentication, admin functions, email notifications, action history, and OneView integration. Includes UserService, AdminService, AuthService, EmailService, ActionHistoryService, OneViewService. |
| **Repository Layer** | Data access layer using Spring Data JPA repositories for CRUD operations on user entities, tokens, and audit logs. Includes UserRepository, AccessTokenRepo, RefreshTokenRepo, ActionHistoryRepo, CustomerDao. |
| **Security Components** | JWT token generation, validation, and authentication filter. Includes JwtAccessTokenUtil, JwtRefreshTokenUtil, JwtTokenAuthenticationFilter, CustomUserDetailsService. |
| **Configuration Components** | Spring Boot configuration classes for security, CORS, application settings, and bean definitions. Includes SecurityConfig, CorsGlobalConfiguration, AppConfig. |
| **Exception Handling** | Global exception handlers for REST and reactive endpoints. Includes GlobalExceptionHandler, CustomControllerAdvice. |

### Notable Patterns

- **Layered Architecture:** Clear separation of concerns with Controller → Service → Repository layers. Dependencies flow downward.
- **Reactive Programming:** Spring WebFlux with Mono and Flux for non-blocking I/O. Reactive custom DAO (CustomerDaoImpl) for database queries.
- **Repository Pattern:** Spring Data JPA repositories abstract database access with custom query methods and pagination support.
- **DTO Pattern:** Data Transfer Objects (AuthUser, AuthenticationRequest, Token, SuccessResponse, etc.) separate API contracts from domain entities.
- **Filter Chain Pattern:** JWT authentication filter (JwtTokenAuthenticationFilter) intercepts requests to validate tokens before reaching controllers.

---

## Integration Architecture

The system integrates with seven external touchpoints:

### External Data Sources
- **OneView API** (outbound REST): Fetch project performance and RAG status data. Custom token-based authentication. Fallback to previous day's data if current day returns empty.

### Persistence Layer
- **AWS RDS MySQL** (outbound JDBC): Primary relational database for user entities, tokens, and audit logs. Basic authentication. No explicit SSL/TLS configuration detected.

### Notification Services
- **Gmail SMTP** (outbound SMTP/SSL): Send email notifications for onboarding workflow events (pending, approved, rejected). Basic authentication.

### Service Discovery and Monitoring
- **Netflix Eureka** (bidirectional REST): Service discovery and registration for microservices ecosystem.
- **Prometheus** (inbound HTTP scrape): Metrics collection and monitoring via /actuator/prometheus endpoint.

### Container Infrastructure
- **AWS ECR** (outbound Docker Registry API): Docker image storage and retrieval.
- **AWS EKS** (bidirectional Kubernetes API): Container orchestration and deployment.

**Cross-Cutting Patterns:**
- **Authentication:** JWT-based for internal API; custom token for OneView; basic auth for MySQL and Gmail.
- **Retry:** No explicit application-level retry logic; relies on library defaults. OneView has fallback to previous day's data.
- **Idempotency:** GET requests are idempotent. POST/PUT operations are not explicitly designed for idempotency.
- **Timeout:** No explicit timeout configuration; relies on library defaults.
- **Observability:** Prometheus metrics exposed via Spring Actuator. No distributed tracing detected.

---

## Data Architecture

The system uses **MySQL 8.0.33 on AWS RDS** as the primary relational database with four tables:

| Table | Purpose | Owner Context |
|-------|---------|---------------|
| **users** | User entities with onboarding status and account activation | User Management Context |
| **access_token** | Short-lived JWT tokens (30 min) | Authentication & Authorization Context |
| **refresh_token** | Long-lived JWT tokens (24 hours) | Authentication & Authorization Context |
| **action_history** | Audit log for admin actions | Audit & Compliance Context |

### Key Data Flows

1. **User Registration:** Creates UserEntity with pending_approval status and isActive=false. Admin approval updates onboardingStatus to approved and isActive=true.
2. **Login:** Generates new access and refresh tokens, revokes old tokens by setting revoked=true. Tokens are stored in database with expiration flags.
3. **Admin Actions:** Logged in action_history table with admin attribution and timestamps.
4. **Email Notifications:** Sent via Gmail SMTP using FreeMarker templates. Email failures do not roll back user registration.
5. **Performance Data:** Retrieved on-demand from OneView API via /performance endpoint. No local caching or persistence.

### Data Relationships

- **users → access_token:** One-to-many (foreign key: userId)
- **users → refresh_token:** One-to-many (foreign key: userId)
- **users → action_history:** One-to-many (logical association via userEmail)

**Gaps:**
- No database migration tooling (Flyway/Liquibase) detected; schema evolution relies on JPA auto-DDL.
- No caching layer or data retention policies implemented.

---

## Security and Privacy Architecture

### Authentication and Authorization

- **JWT-based stateless authentication** with short-lived access tokens (30 min) and long-lived refresh tokens (24 hours).
- **BCrypt password hashing** before storage. Plain text passwords never stored or logged.
- **Role-based access control (RBAC):** Admin endpoints (/api/v1/admin/**) require ADMIN role. JWT token filter validates role claims.
- **Token revocation:** Old tokens are revoked on login by setting revoked=true in database.

### Audit and Compliance

- **Comprehensive audit logging** for all admin actions (onboarding status, account status, role changes) in action_history table with admin email, user email, action details, and timestamps.

### Sensitive Data Handling

| Data Type | Sensitivity | Storage | Flow |
|-----------|-------------|---------|------|
| **Passwords** | Critical | BCrypt hashed in users table | Transmitted over HTTPS during registration/login; never returned in API responses |
| **JWT Tokens** | High | Plain text (signed) in access_token, refresh_token tables | Transmitted over HTTPS; signed with HS256 but not encrypted at rest |
| **Email Addresses** | High | Plain text in users, action_history tables | Transmitted over HTTPS; included in email notifications |
| **Audit Logs** | Medium | Plain text in action_history table | Accessible only to admins via /api/v1/admin/action-history endpoint |

### Critical Security Gaps

| Gap | Severity | Description |
|-----|----------|-------------|
| **Hardcoded Credentials** | Critical | OneView API credentials hardcoded in OneViewServiceImpl.java; database and SMTP credentials in plain text in application-dev.properties |
| **Missing SSL/TLS for Database** | High | MySQL connection does not explicitly configure SSL/TLS encryption |
| **No Rate Limiting** | High | Authentication endpoints (/login, /register) lack rate limiting; vulnerable to brute force attacks |
| **No Account Lockout** | High | No account lockout mechanism after failed login attempts |
| **No MFA** | Medium | System relies solely on username/password authentication |
| **Minimal Test Coverage** | Medium | Test files are commented out; no active unit or integration tests |

---

## Business Alignment

The software architecture directly supports six core business processes:

| Process | Supporting Components |
|---------|----------------------|
| **User Registration** | AuthRestController (/register), UserServiceImpl, EmailServiceImpl, UserRepository |
| **Admin Onboarding Approval** | AdminController (/api/v1/admin/{toEmail}/onboarding-status), AdminServiceImpl, EmailServiceImpl, ActionHistoryServiceImpl, UserRepository |
| **User Login** | AuthRestController (/login), UserServiceImpl, JwtAccessTokenUtil, JwtRefreshTokenUtil, AccessTokenRepo, RefreshTokenRepo |
| **Token Refresh** | AuthRestController (/refresh-token), AuthServiceImpl, JwtRefreshTokenUtil |
| **Admin Account Management** | AdminController (/api/v1/admin/{toEmail}/account-status), AdminServiceImpl, ActionHistoryServiceImpl, UserRepository |
| **Admin Role Management** | AdminController (/api/v1/admin/{toEmail}/role), AdminServiceImpl, ActionHistoryServiceImpl, UserRepository |

Each process is implemented via REST API endpoints, service layer orchestration, and database persistence. Email notifications keep users informed during onboarding. Audit logging ensures compliance and traceability for admin actions. The architecture enables controlled access to the COVE platform with admin oversight.

---

## Operational and Risk Summary

### Tech Debt

**Status:** Not available (tech_debt_risk.json artifact not found; no tech debt analysis performed by Agent 07).

### Residual Risks

| Risk | Severity | Description |
|------|----------|-------------|
| **Hardcoded Credentials** | Critical | OneView API credentials in source code; database and SMTP credentials in configuration files expose credentials to anyone with repository or deployment access |
| **Missing SSL/TLS for Database** | High | Database traffic may be transmitted in plain text, exposing sensitive data to network sniffing |
| **No Rate Limiting or Account Lockout** | High | Authentication endpoints vulnerable to brute force attacks and credential stuffing |
| **No MFA** | Medium | Increased risk of account compromise from credential theft |
| **Minimal Test Coverage** | Medium | Lack of automated testing increases risk of regressions and bugs in production |
| **No Database Migration Tooling** | Medium | Schema changes are not version-controlled or auditable |
| **Email Notification Failures** | Low | User registration succeeds even if email notification fails; users may not receive pending approval emails |
| **No API Documentation** | Low | Lack of Swagger/OpenAPI documentation increases onboarding time for developers and integration partners |

---

## Diagrams

For visual representations of the architecture, see **architecture_diagrams.md**:

1. **System Context Diagram:** Shows COVE User Service and its external systems (OneView API, MySQL, Gmail SMTP, Eureka, Prometheus, ECR, EKS).
2. **Container/Module Diagram:** Shows internal modules (Controller, Service, Repository, Security, Configuration, Exception Handling layers).
3. **Integration Landscape Diagram:** Shows major integration flows with direction and protocol.
4. **Data Flow Diagram (Logical):** Shows primary data entities (users, access_token, refresh_token, action_history) and relationships.
5. **Deployment Diagram (AWS EKS):** Shows Kubernetes deployment with 2 replicas, resource limits, and ClusterIP service.

---

## Limits and Unknowns

- tech_debt_risk.json not found; no tech debt analysis included.
- Deployment diagram based on Kubernetes manifests and repository summary; runtime behavior not observed.
- Integration contract details inferred from configuration files and service code; no API specifications (OpenAPI/Swagger) available.
- Timeout and retry configurations not explicitly set in application code; rely on library defaults.
- Database schema relationships inferred from JPA annotations; no database migration scripts or ER diagrams available.
- Email template content not analyzed; only service-level email sending logic reviewed.
- OneView API integration details limited to implementation code; no API specification or contract documentation available.
- Security controls for infrastructure services (Eureka, Prometheus, ECR, EKS) not visible in application code; may be handled at infrastructure level.

---

**End of Architecture Summary**
