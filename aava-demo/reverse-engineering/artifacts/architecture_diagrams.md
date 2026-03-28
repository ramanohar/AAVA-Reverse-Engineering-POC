# COVE User Service — Architecture Diagrams

**Generated:** 2025-01-16 12:00:00 UTC  
**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main

---

## Introduction

This document contains Mermaid diagrams for the COVE User Service architecture. All diagrams are synthesized from upstream reverse-engineering artifacts (repository_summary.json, dependency_graph.json, domain_model.json, integration_catalog.json, data_model.json, security_privacy_assessment.json, business_process_model.json). Diagrams are GitHub-renderable and cite source artifacts in comments.

---

## System Context Diagram

**Purpose:** Shows COVE User Service and its external systems (actors, integrations, and data sources).

<!-- sources: integration_catalog.json (touchpoints), business_process_model.json (actors), domain_model.json (bounded_contexts) -->

```mermaid
flowchart LR
    EndUser["End User<br/>(CUSTOMER role)"]
    Admin["Administrator<br/>(ADMIN role)"]
    CoveUserService["COVE User Service<br/>(Spring Boot Microservice)"]
    OneViewAPI["OneView API<br/>(Performance Data)"]
    MySQL["AWS RDS MySQL<br/>(Database)"]
    GmailSMTP["Gmail SMTP<br/>(Email Notifications)"]
    Eureka["Netflix Eureka<br/>(Service Discovery)"]
    Prometheus["Prometheus<br/>(Metrics Monitoring)"]
    ECR["AWS ECR<br/>(Docker Registry)"]
    EKS["AWS EKS<br/>(Kubernetes)"]

    EndUser -->|"Register, Login, Refresh Token"| CoveUserService
    Admin -->|"Approve/Reject Users, Manage Accounts"| CoveUserService
    CoveUserService -->|"Fetch Performance Data (HTTPS/REST)"| OneViewAPI
    CoveUserService -->|"Persist User Data, Tokens, Audit Logs (JDBC)"| MySQL
    CoveUserService -->|"Send Email Notifications (SMTP/SSL)"| GmailSMTP
    CoveUserService <-->|"Service Registration (HTTP/REST)"| Eureka
    Prometheus -->|"Scrape Metrics (HTTP)"| CoveUserService
    CoveUserService -->|"Push Docker Images"| ECR
    EKS <-->|"Deploy and Orchestrate Containers"| CoveUserService
```

*Caption:* COVE User Service interacts with end users and administrators via REST API, integrates with OneView API for performance data, persists data in MySQL, sends email notifications via Gmail SMTP, registers with Eureka for service discovery, exposes metrics to Prometheus, and is deployed on AWS EKS with images stored in ECR.

---

## Container/Module Diagram

**Purpose:** Shows internal modules and layers within COVE User Service.

<!-- sources: dependency_graph.json (service_module_graph.nodes), repository_summary.json (modules) -->

```mermaid
flowchart TB
    subgraph CoveUserService["COVE User Service"]
        Controllers["Controller Layer<br/>(AuthRestController, AdminController, PerformaceController, Healthcheck)"]
        Services["Service Layer<br/>(UserService, AdminService, AuthService, EmailService, ActionHistoryService, OneViewService)"]
        Repositories["Repository Layer<br/>(UserRepository, AccessTokenRepo, RefreshTokenRepo, ActionHistoryRepo, CustomerDao)"]
        Security["Security Components<br/>(JwtAccessTokenUtil, JwtRefreshTokenUtil, JwtTokenAuthenticationFilter, CustomUserDetailsService)"]
        Config["Configuration Components<br/>(SecurityConfig, CorsGlobalConfiguration, AppConfig)"]
        ExceptionHandlers["Exception Handling<br/>(GlobalExceptionHandler, CustomControllerAdvice)"]
    end

    Controllers --> Services
    Services --> Repositories
    Security --> Controllers
    Config --> Security
    ExceptionHandlers --> Controllers
```

*Caption:* COVE User Service follows a layered architecture with clear separation of concerns. Controllers handle HTTP requests, Services implement business logic, Repositories manage data access, Security Components enforce authentication and authorization, Configuration Components define application settings, and Exception Handlers manage error responses.

---

## Integration Landscape Diagram

**Purpose:** Shows major integration flows with direction and protocol.

<!-- sources: integration_catalog.json (touchpoint_enrichments) -->

```mermaid
flowchart LR
    CoveUserService["COVE User Service"]
    OneViewAPI["OneView API<br/>(HTTPS/REST)"]
    MySQL["AWS RDS MySQL<br/>(JDBC/MySQL)"]
    GmailSMTP["Gmail SMTP<br/>(SMTP/SSL)"]
    Eureka["Netflix Eureka<br/>(HTTP/REST)"]
    Prometheus["Prometheus<br/>(HTTP Scrape)"]
    ECR["AWS ECR<br/>(Docker Registry API)"]
    EKS["AWS EKS<br/>(Kubernetes API)"]

    CoveUserService -->|"Outbound: Fetch Performance Data<br/>(Custom Token Auth)"| OneViewAPI
    CoveUserService -->|"Outbound: Persist Data<br/>(Basic Auth, No SSL/TLS)"| MySQL
    CoveUserService -->|"Outbound: Send Emails<br/>(Basic Auth)"| GmailSMTP
    CoveUserService <-->|"Bidirectional: Service Registration<br/>(Auth Unknown)"| Eureka
    Prometheus -->|"Inbound: Scrape Metrics<br/>(Auth Unknown)"| CoveUserService
    CoveUserService -->|"Outbound: Push Images<br/>(Auth Unknown)"| ECR
    EKS <-->|"Bidirectional: Deploy Containers<br/>(Auth Unknown)"| CoveUserService
```

*Caption:* COVE User Service integrates with seven external systems. Outbound integrations include OneView API (performance data), MySQL (persistence), Gmail SMTP (email notifications), and ECR (Docker images). Bidirectional integrations include Eureka (service discovery) and EKS (container orchestration). Inbound integration includes Prometheus (metrics scraping). Authentication patterns vary; critical gaps include missing SSL/TLS for MySQL and hardcoded credentials for OneView and Gmail.

---

## Data Flow Diagram (Logical)

**Purpose:** Shows primary data entities and relationships.

<!-- sources: data_model.json (logical_models, key_relationships), business_process_model.json (processes) -->

```mermaid
erDiagram
    UserEntity ||--o{ AccessToken : "has many"
    UserEntity ||--o{ RefreshToken : "has many"
    UserEntity ||--o{ ActionHistory : "logged in (via email)"

    UserEntity {
        Integer userId PK
        String email UK
        String password
        String firstname
        String lastname
        String company
        String role
        String onboardingStatus
        Boolean isActive
        LocalDateTime createdAt
        LocalDateTime lastUpdatedAt
    }

    AccessToken {
        Long id PK
        String token
        Boolean expired
        Boolean revoked
        Date createdAt
        Integer userId FK
    }

    RefreshToken {
        Long id PK
        String token
        Boolean expired
        Boolean revoked
        Date createdAt
        Integer userId FK
    }

    ActionHistory {
        Long id PK
        String userEmail
        String updateByAdminEmail
        String action
        String actionType
        LocalDateTime createdAt
    }
```

*Caption:* COVE User Service data model includes four primary entities: UserEntity (user accounts with onboarding status and activation state), AccessToken (short-lived JWT tokens), RefreshToken (long-lived JWT tokens), and ActionHistory (audit log for admin actions). Relationships are one-to-many from UserEntity to tokens (via foreign key userId) and logical association from UserEntity to ActionHistory (via userEmail).

---

## Deployment Diagram (AWS EKS)

**Purpose:** Shows Kubernetes deployment with replicas, resource limits, and service configuration.

<!-- sources: repository_summary.json (deployment, kubernetes_configuration), integration_catalog.json (AWS EKS touchpoint) -->

```mermaid
flowchart TB
    subgraph EKS["AWS EKS Cluster"]
        subgraph Deployment["Deployment: cove-user-backend"]
            Pod1["Pod 1<br/>(cove-user-service)<br/>CPU: 250m, Mem: 512Mi-1024Mi<br/>Port: 8090"]
            Pod2["Pod 2<br/>(cove-user-service)<br/>CPU: 250m, Mem: 512Mi-1024Mi<br/>Port: 8090"]
        end
        Service["Service: cove-user-backend<br/>(ClusterIP)<br/>Port: 80 -> 8090"]
    end

    ECR["AWS ECR<br/>(Docker Image: cove-user-service)"]
    MySQL["AWS RDS MySQL<br/>(cove-db.chioww02cxoo.ap-south-1.rds.amazonaws.com:3306)"]
    Prometheus["Prometheus<br/>(Scrape Port: 8090)"]

    ECR -->|"Pull Image"| Deployment
    Service --> Pod1
    Service --> Pod2
    Pod1 -->|"JDBC Connection"| MySQL
    Pod2 -->|"JDBC Connection"| MySQL
    Prometheus -->|"Scrape Metrics"| Pod1
    Prometheus -->|"Scrape Metrics"| Pod2
```

*Caption:* COVE User Service is deployed on AWS EKS with 2 replicas (Pods) in a Deployment named cove-user-backend. Each Pod runs the cove-user-service container with resource requests (250m CPU, 512Mi memory) and limits (250m CPU, 1024Mi memory). A ClusterIP Service exposes port 80 internally, routing traffic to Pod port 8090. Docker images are pulled from AWS ECR. Pods connect to AWS RDS MySQL for persistence. Prometheus scrapes metrics from each Pod on port 8090.

---

**End of Architecture Diagrams**
