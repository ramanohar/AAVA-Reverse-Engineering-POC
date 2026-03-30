# COVE User Service - Architecture Diagrams

## Introduction

This document contains Mermaid diagrams visualizing the architecture of the COVE User Service. All diagrams are derived from upstream artifacts and represent the system as evidenced in the codebase. For detailed explanations, refer to `architecture_summary.md` and `architecture_summary.json`.

---

## System Context Diagram

This diagram shows the COVE User Service in its operational context, including primary users and external systems.

```mermaid
flowchart TB
    User["End User (CUSTOMER)"] -->|Register, Login, Refresh Token| CoveUserService["COVE User Service"]
    Admin["Administrator (ADMIN)"] -->|Approve/Reject Users, Manage Accounts, Manage Roles| CoveUserService
    CoveUserService -->|Fetch Performance Data| OneViewAPI["OneView API"]
    CoveUserService -->|Persist User, Token, Audit Data| MySQL["AWS RDS MySQL"]
    CoveUserService -->|Send Email Notifications| GmailSMTP["Gmail SMTP"]
    CoveUserService <-->|Service Discovery| Eureka["Netflix Eureka"]
    Prometheus["Prometheus"] -->|Scrape Metrics| CoveUserService
    CoveUserService -->|Push Docker Images| ECR["AWS ECR"]
    CoveUserService <-->|Deploy and Orchestrate| EKS["AWS EKS"]
```

*Sources: integration_catalog.json (touchpoint_enrichments), business_process_model.json (application_purpose_summary)*

---

## Container/Module Diagram

This diagram shows the major internal modules and layers of the COVE User Service.

```mermaid
flowchart TB
    subgraph CoveUserService["COVE User Service (Spring Boot)"]
        Controllers["Controller Layer<br/>(AuthRestController, AdminController, PerformaceController, Healthcheck)"]
        Services["Service Layer<br/>(UserService, AdminService, AuthService, EmailService, ActionHistoryService, OneViewService)"]
        Repositories["Repository Layer<br/>(UserRepository, AccessTokenRepo, RefreshTokenRepo, ActionHistoryRepo, CustomerDao)"]
        Security["Security Components<br/>(JwtAccessTokenUtil, JwtRefreshTokenUtil, JwtTokenAuthenticationFilter, CustomUserDetailsService)"]
        EmailService["Email Service<br/>(EmailServiceImpl, FreeMarker Templates)"]
        OneViewIntegration["OneView Integration<br/>(OneViewServiceImpl, RestTemplate)"]
        
        Controllers --> Services
        Services --> Repositories
        Controllers --> Security
        Services --> Security
        Services --> EmailService
        Services --> OneViewIntegration
    end
    
    Repositories -->|JDBC/JPA| MySQL["AWS RDS MySQL"]
    EmailService -->|SMTP| GmailSMTP["Gmail SMTP"]
    OneViewIntegration -->|REST| OneViewAPI["OneView API"]
```

*Sources: dependency_graph.json (service_module_graph), repository_summary.json (modules)*

---

## Integration Landscape Diagram

This diagram shows the major integration flows between COVE User Service and external systems.

```mermaid
flowchart LR
    CoveUserService["COVE User Service"]
    
    CoveUserService -->|"HTTPS/REST<br/>Custom Token Auth<br/>Fetch Performance Data"| OneViewAPI["OneView API"]
    CoveUserService -->|"JDBC/MySQL<br/>Basic Auth<br/>Persist Data"| MySQL["AWS RDS MySQL"]
    CoveUserService -->|"SMTP/SSL<br/>Basic Auth<br/>Send Emails"| GmailSMTP["Gmail SMTP"]
    CoveUserService <-->|"HTTP/REST<br/>Service Discovery"| Eureka["Netflix Eureka"]
    Prometheus["Prometheus"] -->|"HTTP Scrape<br/>Metrics Collection"| CoveUserService
    CoveUserService -->|"Docker Registry API<br/>Push Images"| ECR["AWS ECR"]
    CoveUserService <-->|"Kubernetes API<br/>Orchestration"| EKS["AWS EKS"]
```

*Sources: integration_catalog.json (touchpoint_enrichments)*

---

## Data Flow Diagram (Logical)

This diagram shows the primary data entities and their relationships in the COVE User Service.

```mermaid
erDiagram
    UserEntity ||--o{ AccessToken : "has many"
    UserEntity ||--o{ RefreshToken : "has many"
    UserEntity ||--o{ ActionHistory : "logged in (via email)"
    
    UserEntity {
        Integer userId PK
        String email UK
        String password
        String role
        String onboardingStatus
        Boolean isActive
    }
    
    AccessToken {
        Long id PK
        String token
        Boolean expired
        Boolean revoked
        Integer user_id FK
    }
    
    RefreshToken {
        Long id PK
        String token
        Boolean expired
        Boolean revoked
        Integer user_id FK
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

*Sources: data_model.json (logical_models, key_relationships)*

---

## Deployment Diagram (AWS EKS)

This diagram shows the deployment architecture of COVE User Service on AWS EKS.

```mermaid
flowchart TB
    subgraph AWS["AWS Cloud"]
        subgraph EKS["AWS EKS Cluster"]
            subgraph Deployment["cove-user-backend Deployment"]
                Pod1["Pod 1<br/>cove-user-service:latest<br/>Port 8090"] 
                Pod2["Pod 2<br/>cove-user-service:latest<br/>Port 8090"]
            end
            Service["ClusterIP Service<br/>Port 80 -> 8090"]
            Service --> Pod1
            Service --> Pod2
        end
        
        ECR["AWS ECR<br/>Docker Image Registry"]
        RDS["AWS RDS MySQL<br/>cove-db.chioww02cxoo.ap-south-1.rds.amazonaws.com:3306"]
        
        Pod1 --> RDS
        Pod2 --> RDS
        ECR -.->|Pull Image| Pod1
        ECR -.->|Pull Image| Pod2
    end
    
    CodeBuild["AWS CodeBuild<br/>CI/CD Pipeline"] -->|Build & Push| ECR
    CodeBuild -->|Deploy| EKS
    
    Prometheus["Prometheus"] -->|Scrape Metrics :8090| Pod1
    Prometheus -->|Scrape Metrics :8090| Pod2
    
    Eureka["Netflix Eureka<br/>Service Registry"] <--> Pod1
    Eureka <--> Pod2
```

*Sources: repository_summary.json (deployment, kubernetes_configuration)*

---

## Notes

- All diagrams are generated from upstream artifacts and represent the system as evidenced in the codebase.
- Diagrams use Mermaid syntax for GitHub rendering.
- For detailed explanations of each component and integration, refer to `architecture_summary.md` and `architecture_summary.json`.
- Security risks (hardcoded credentials, missing SSL/TLS, no rate limiting) are documented in `architecture_summary.json` under `operational_and_risk.residual_risks`.
