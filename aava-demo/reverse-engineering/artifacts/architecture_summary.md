# COVE User Service - Architecture Summary

## How to Read This Pack

This architecture summary provides a human-readable narrative of the COVE User Service system architecture. For detailed technical specifications, refer to `architecture_summary.json`. For visual representations of the architecture, see `architecture_diagrams.md` which contains Mermaid diagrams for system context, containers, integrations, data flow, and deployment.

---

## Executive Summary

COVE User Service is a reactive Spring Boot microservice deployed on AWS EKS that provides JWT-based authentication, user registration with admin-approval workflows, and role-based access control for the COVE platform. Primary users include end-users registering for accounts (CUSTOMER role) and administrators managing user onboarding, account activation, and role assignments (ADMIN role). The system integrates with external OneView API for project performance data, uses AWS RDS MySQL for persistence, and sends email notifications via Gmail SMTP. The architecture follows a layered pattern with Spring WebFlux for reactive programming, stateless JWT authentication, and comprehensive audit logging for compliance.

### Key Characteristics

- **Reactive microservice architecture** with Spring WebFlux for non-blocking I/O and high concurrency
- **JWT-based stateless authentication** with access tokens (30-minute validity) and refresh tokens (24-hour validity)
- **Admin-approval workflow** for user onboarding with email notifications at each stage (pending, approved, rejected)
- **Role-based access control** with CUSTOMER and ADMIN roles enforcing endpoint-level authorization
- **Comprehensive audit logging** for all administrative actions with timestamps and admin attribution
- **AWS EKS deployment** with automated CI/CD via AWS CodeBuild, 2 replicas, and resource limits for auto-scaling

---

## System Context

### Purpose

The COVE User Service manages user authentication, registration, and onboarding workflows for the COVE platform with role-based access control and audit compliance.

### Primary Users

| User Type | Description |
|-----------|-------------|
| **End User (CUSTOMER role)** | Individuals registering for COVE accounts, authenticating, and accessing platform features |
| **Administrator (ADMIN role)** | Users managing user onboarding approvals, account activation/deactivation, role assignments, and audit log review |

### External Systems

| System | Role |
|--------|------|
| **OneView API** | External project performance and RAG status data provider |
| **AWS RDS MySQL** | Primary relational database for user, token, and audit data persistence |
| **Gmail SMTP** | Email notification delivery for onboarding workflow events |
| **Netflix Eureka** | Service discovery and registration |
| **Prometheus** | Metrics collection and monitoring |
| **AWS ECR** | Docker image registry |
| **AWS EKS** | Container orchestration and deployment platform |

### Context Boundaries

**In Scope:**
- User registration, authentication, onboarding approval
- Account management, role management
- Audit logging
- JWT token lifecycle management

**Out of Scope:**
- Password reset functionality
- Multi-factor authentication (MFA)
- API documentation generation (Swagger/OpenAPI)
- Database migration tooling (Flyway/Liquibase)

---

## Architectural Drivers

The architecture is shaped by the following key drivers:

### 1. Security and Compliance

The system handles sensitive user credentials, personally identifiable information (PII), and requires audit trails for administrative actions. Security measures include:
- JWT-based authentication with token revocation
- BCrypt password hashing
- Comprehensive action history logging for audit compliance
- Role-based access control with protected admin endpoints

### 2. Scalability and Resilience

Reactive programming with Spring WebFlux enables non-blocking I/O for high concurrency. Stateless JWT authentication allows horizontal scaling without session affinity. AWS EKS deployment with 2 replicas and resource limits supports auto-scaling based on load.

### 3. Operational Observability

Prometheus metrics are exposed via Spring Actuator for monitoring application health and performance. Kubernetes health checks and readiness probes ensure reliable deployments. Audit logging for administrative actions supports compliance and incident investigation.

### 4. Integration with External Systems

The system integrates with multiple external systems:
- **OneView API** for project performance data (outbound REST)
- **Gmail SMTP** for email notifications (outbound SMTP)
- **Netflix Eureka** for service discovery (bidirectional REST)
- **Prometheus** for metrics collection (inbound HTTP scrape)
- **AWS ECR** and **AWS EKS** for container infrastructure

### 5. Admin-Controlled User Lifecycle

Business requirement for admin approval of new user registrations before account activation. The onboarding workflow with `pending_approval`, `approved`, and `reject` states enforces this control, ensuring only vetted users gain access to the platform.

---

## Logical Architecture

### Containers and Modules

The system follows a **layered architecture** with clear separation of concerns:

| Layer/Module | Responsibility | Key Components |
|--------------|----------------|----------------|
| **cove-user-service** | Spring Boot application providing REST API for user management, authentication, and admin operations | Main application class, configuration beans |
| **Controller Layer** | REST controllers handling HTTP requests and responses | AuthRestController, AdminController, PerformaceController, Healthcheck |
| **Service Layer** | Business logic implementation | UserService, AdminService, AuthService, EmailService, ActionHistoryService, OneViewService |
| **Repository Layer** | Data access via JPA repositories | UserRepository, AccessTokenRepo, RefreshTokenRepo, ActionHistoryRepo, CustomerDao |
| **Security Components** | JWT token generation, validation, and authentication filtering | JwtAccessTokenUtil, JwtRefreshTokenUtil, JwtTokenAuthenticationFilter, CustomUserDetailsService |
| **Email Service** | Email notification delivery using FreeMarker templates | EmailServiceImpl, FreeMarker template engine |
| **OneView Integration** | External API integration for project performance data | OneViewServiceImpl, RestTemplate |

### Notable Architectural Patterns

- **Layered Architecture**: Controller → Service → Repository separation of concerns
- **Reactive Programming**: Spring WebFlux with Mono and Flux for non-blocking I/O
- **Repository Pattern**: JPA repositories abstract data access logic
- **DTO Pattern**: Data transfer objects for API request/response payloads
- **Filter Chain Pattern**: JWT authentication filter in Spring Security filter chain
- **Template Method Pattern**: FreeMarker email templates for notification rendering

---

## Integration Architecture

The system integrates with **7 external touchpoints**:

### External Data and Services
- **OneView API** (outbound HTTPS/REST): Fetches project performance and RAG status data with custom token-based authentication
- **Gmail SMTP** (outbound SMTP/SSL): Sends email notifications for onboarding workflow events with basic authentication

### Persistence and State
- **AWS RDS MySQL** (outbound JDBC/MySQL): Primary relational database for user, token, and audit data persistence with basic authentication

### Service Discovery and Monitoring
- **Netflix Eureka** (bidirectional HTTP/REST): Service discovery and registration
- **Prometheus** (inbound HTTP scrape): Metrics collection and monitoring via Spring Actuator

### Container Infrastructure
- **AWS ECR** (outbound Docker Registry API): Docker image storage and retrieval
- **AWS EKS** (bidirectional Kubernetes API): Container orchestration and deployment platform

### Integration Patterns

- **Authentication**: Custom token-based (OneView), basic auth (MySQL, Gmail), unknown for infrastructure services
- **Retry Logic**: No explicit application-level retry logic for most integrations; OneView API has fallback to previous day's data if current day returns empty; relies on library defaults (Eureka client, Spring Data JPA connection pool, AWS CLI, kubectl)
- **Idempotency**: GET requests are idempotent; POST/PUT operations (user registration, login, admin updates) are not explicitly designed for idempotency
- **Timeout**: No explicit timeout configuration detected in application code; relies on library and infrastructure defaults
- **Observability**: Prometheus metrics exposed via Spring Actuator; no distributed tracing (e.g., Sleuth, Zipkin) detected

---

## Data Architecture

### Persistence

Data persistence uses **AWS RDS MySQL 8.0.33** with four primary tables:

| Table | Purpose | Key Fields |
|-------|---------|------------|
| **users** | User profiles and credentials | userId (PK), email (unique), password (BCrypt hashed), role, onboardingStatus, isActive |
| **access_token** | Short-lived JWT tokens (30 min) | id (PK), token (TEXT), expired, revoked, user_id (FK) |
| **refresh_token** | Long-lived JWT tokens (24 hours) | id (PK), token (TEXT), expired, revoked, user_id (FK) |
| **action_history** | Audit log for admin actions | id (PK), userEmail, updateByAdminEmail, action, actionType, createdAt |

### Data Relationships

- **One-to-Many**: User → AccessToken (a user can have multiple access tokens over time)
- **One-to-Many**: User → RefreshToken (a user can have multiple refresh tokens over time)
- **Logical Association**: User → ActionHistory (via userEmail field; no direct foreign key)

### Data Handling Notes

- **User credentials** stored with BCrypt hashed passwords; email field is unique index
- **JWT tokens** stored in plain text (signed but not encrypted at rest) with expiration and revocation flags
- **Action history** records all admin actions with timestamps and admin attribution for audit compliance
- **No caching layer** detected; all data access goes directly to MySQL
- **Database connection** does not explicitly configure SSL/TLS encryption (security risk)

---

## Security and Privacy Architecture

### Security Model

The security model uses **JWT-based stateless authentication** with:
- **Access tokens**: 30-minute validity for API authentication
- **Refresh tokens**: 24-hour validity for obtaining new access tokens without re-authentication
- **Password hashing**: BCrypt hashing before storage
- **Role-based access control**: CUSTOMER and ADMIN roles with protected `/api/v1/admin/**` endpoints
- **Token revocation**: On logout and login, old tokens are revoked
- **Audit logging**: All admin actions logged with timestamps and attribution

### Security Controls

| Control | Description |
|---------|-------------|
| **JWT Authentication and Authorization** | Stateless JWT tokens with signature validation, expiration checks, and database-backed revocation |
| **Password Hashing** | BCrypt hashing for user passwords before storage |
| **Role-Based Access Control** | CUSTOMER and ADMIN roles with endpoint-level authorization enforcement |
| **Audit Logging** | Comprehensive action history for all admin operations with timestamps and attribution |
| **HTTPS Enforcement** | Assumed based on production deployment context; not explicitly verified in code |

### Critical Security Risks

| Risk | Severity | Description |
|------|----------|-------------|
| **Hardcoded Credentials in Source Code** | Critical | OneView API credentials (username and password) are hardcoded in `OneViewServiceImpl.java` |
| **Plain Text Credentials in Configuration Files** | Critical | Database and SMTP credentials stored in plain text in `application-dev.properties` |
| **Missing SSL/TLS for Database Connection** | High | MySQL database connection does not explicitly configure SSL/TLS encryption |
| **No Rate Limiting on Authentication Endpoints** | High | Authentication endpoints (login, register) lack rate limiting, vulnerable to brute force attacks |
| **No Account Lockout Mechanism** | High | No account lockout mechanism after failed login attempts |
| **No Multi-Factor Authentication (MFA)** | Medium | No MFA implementation for user authentication |

---

## Business Alignment

The software architecture directly supports **six primary business processes**:

### 1. User Registration
- **Components**: AuthRestController, UserServiceImpl, EmailServiceImpl, UserRepository
- **Flow**: User submits registration → System validates email uniqueness → Password hashed → User entity created with `pending_approval` status → Pending approval email sent

### 2. Admin Onboarding Approval
- **Components**: AdminController, AdminServiceImpl, EmailServiceImpl, ActionHistoryServiceImpl, UserRepository
- **Flow**: Admin reviews registration → Updates onboarding status (approved/rejected) → Account activated (if approved) → Email notification sent → Action logged

### 3. User Login
- **Components**: AuthRestController, UserServiceImpl, JwtAccessTokenUtil, JwtRefreshTokenUtil, UserRepository, AccessTokenRepo, RefreshTokenRepo
- **Flow**: User submits credentials → System validates credentials, onboarding status, and account activation → Old tokens revoked → New access and refresh tokens generated → Tokens returned

### 4. Token Refresh
- **Components**: AuthRestController, AuthServiceImpl, JwtRefreshTokenUtil
- **Flow**: User submits refresh token → System validates token → New access token generated → Token returned

### 5. Admin Account Management
- **Components**: AdminController, AdminServiceImpl, ActionHistoryServiceImpl, UserRepository
- **Flow**: Admin updates account status (activate/deactivate) → User entity updated → Action logged

### 6. Admin Role Management
- **Components**: AdminController, AdminServiceImpl, ActionHistoryServiceImpl, UserRepository
- **Flow**: Admin updates user role (CUSTOMER/ADMIN) → User entity updated → Action logged

---

## Operational and Risk Considerations

### Tech Debt Summary

**Status**: Not available (tech_debt_risk.json artifact not found)

### Residual Risks

| Risk | Severity | Mitigation Recommendation |
|------|----------|---------------------------|
| **Hardcoded Credentials in Source Code** | Critical | Immediately remove hardcoded OneView API credentials and store in environment variables or AWS Secrets Manager |
| **Plain Text Credentials in Configuration Files** | Critical | Move all credentials (database, SMTP, JWT secrets) to environment variables or AWS Secrets Manager |
| **Missing SSL/TLS for Database Connection** | High | Enable SSL/TLS for MySQL database connections by adding SSL parameters to JDBC URL |
| **No Rate Limiting on Authentication Endpoints** | High | Implement rate limiting on authentication endpoints (/login, /register) to prevent brute force attacks |
| **No Account Lockout Mechanism** | High | Implement account lockout mechanism after failed login attempts |
| **Minimal Test Coverage** | Medium | Add comprehensive unit and integration tests; remove commented-out test code |
| **No Database Migration Tool** | Medium | Implement Flyway or Liquibase for database schema versioning and migration |
| **No API Documentation** | Low | Add Swagger/OpenAPI specification for API documentation |

---

## Limits and Unknowns

- **tech_debt_risk.json** artifact not found; tech debt summary marked as not_available
- **HTTPS enforcement** not explicitly verified in code; assumed based on production deployment context
- **Network security controls** (firewalls, security groups) not visible in application code
- **AWS RDS encryption at rest** configuration not visible in application code
- **Secrets manager integration** not detected; may be configured at deployment level
- **Rate limiting** may be implemented at API gateway level (not visible in application code)
- **MFA implementation** may be planned but not yet implemented
- **CORS configuration** details not fully visible; marked as low confidence
- **Timeout configuration** not explicitly set in application code; relies on library defaults
- **Auth patterns for AWS ECR and AWS EKS** not visible in application code; handled by AWS CLI and kubectl
- **Eureka authentication mechanism** not visible in application code; may be handled by infrastructure or Eureka server configuration
- **Prometheus scrape authentication** not visible; may be unauthenticated or handled by Kubernetes network policies
- **No API specification** (OpenAPI/Swagger) found; contract details inferred from controller and service code
- **No distributed tracing** or correlation ID propagation detected
- **Error handling for external integrations** is minimal; relies on Spring default exception handling and global exception handler

---

## References

- **architecture_summary.json**: Machine-consumable architecture specification
- **architecture_diagrams.md**: Mermaid diagrams for system context, containers, integrations, data flow, and deployment
- **Upstream artifacts**: repository_summary.json, dependency_graph.json, domain_model.json, integration_catalog.json, data_model.json, security_privacy_assessment.json, business_process_model.json
