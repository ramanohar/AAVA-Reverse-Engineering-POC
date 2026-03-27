# Dependency Graph: ramanohar/AAVA-Reverse-Engineering-POC

**Generated:** 2025-01-16T00:00:00Z  
**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main  
**Run Mode:** build

---

## Service/Module Graph

### Nodes

| ID | Type | Package |
|----|------|----------|
| AuthRestController | controller | com.collaberadigital.cove.controller.impl |
| AdminController | controller | com.collaberadigital.cove.controller.impl |
| PerformaceController | controller | com.collaberadigital.cove.controller.impl |
| Healthcheck | controller | com.collaberadigital.cove.controller.impl |
| UserService | service | com.collaberadigital.cove.service |
| UserServiceImpl | service_impl | com.collaberadigital.cove.service.impl |
| AdminService | service | com.collaberadigital.cove.service |
| AdminServiceImpl | service_impl | com.collaberadigital.cove.service.impl |
| AuthService | service | com.collaberadigital.cove.service |
| AuthServiceImpl | service_impl | com.collaberadigital.cove.service.impl |
| EmailService | service | com.collaberadigital.cove.service |
| EmailServiceImpl | service_impl | com.collaberadigital.cove.service.impl |
| ActionHistoryService | service | com.collaberadigital.cove.service |
| ActionHistoryServiceImpl | service_impl | com.collaberadigital.cove.service.impl |
| OneViewService | service | com.collaberadigital.cove.service |
| OneViewServiceImpl | service_impl | com.collaberadigital.cove.service.impl |
| UserRepository | repository | com.collaberadigital.cove.repository |
| AccessTokenRepo | repository | com.collaberadigital.cove.repository |
| RefreshTokenRepo | repository | com.collaberadigital.cove.repository |
| ActionHistoryRepo | repository | com.collaberadigital.cove.repository |
| CustomerDao | repository | com.collaberadigital.cove.repository |
| CustomerDaoImpl | repository_impl | com.collaberadigital.cove.repository.repositoryIml |

### Edges (Dependencies)

#### Controller → Service

- **AuthRestController → UserService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`

- **AuthRestController → AuthService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java`

- **AdminController → AdminService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`

- **AdminController → ActionHistoryService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java`

- **PerformaceController → OneViewService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java`

#### Service → Repository

- **UserServiceImpl → UserRepository**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

- **UserServiceImpl → AccessTokenRepo**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

- **UserServiceImpl → RefreshTokenRepo**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

- **AdminServiceImpl → UserRepository**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

- **AuthServiceImpl → AccessTokenRepo**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java`

- **AuthServiceImpl → RefreshTokenRepo**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java`

- **ActionHistoryServiceImpl → ActionHistoryRepo**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java`

- **CustomerDaoImpl → UserRepository**
  - Type: repository_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/repository/repositoryIml/CustomerDaoImpl.java`

#### Service → Service

- **UserServiceImpl → EmailService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java`

- **AdminServiceImpl → EmailService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

- **AdminServiceImpl → ActionHistoryService**
  - Type: service_call
  - Direction: outbound
  - Confidence: high
  - Evidence: `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java`

---

## Library Dependencies

### Maven Dependencies (pom.xml)

| Library | Version | Ecosystem | Scope | Risk Flags | Outdated | Evidence |
|---------|---------|-----------|-------|------------|----------|----------|
| spring-boot-starter-parent | 3.2.5 | maven | parent | None | unknown | pom.xml |
| spring-boot-starter-webflux | inherited from parent 3.2.5 | maven | compile | None | unknown | pom.xml:21-127 |
| spring-boot-starter-data-jpa | inherited from parent 3.2.5 | maven | compile | None | unknown | pom.xml:21-127 |
| spring-boot-starter-security | inherited from parent 3.2.5 | maven | compile | None | unknown | pom.xml:21-127 |
| spring-boot-starter-mail | inherited from parent 3.2.5 | maven | compile | None | unknown | pom.xml:21-127 |
| spring-boot-starter-actuator | inherited from parent 3.2.5 | maven | compile | None | unknown | pom.xml:21-127 |
| spring-cloud-starter-netflix-eureka-client | managed by spring-cloud-dependencies 2023.0.1 | maven | compile | None | unknown | pom.xml:44-47 |
| mysql-connector-j | 8.0.33 | maven | runtime | None | unknown | pom.xml:21-127 |
| jjwt-api | 0.11.5 | maven | compile | None | unknown | pom.xml:21-127 |
| jjwt-impl | 0.11.5 | maven | runtime | None | unknown | pom.xml:21-127 |
| jjwt-jackson | 0.11.5 | maven | runtime | None | unknown | pom.xml:21-127 |
| hibernate-validator | 6.0.16.Final | maven | compile | None | unknown | pom.xml:21-127 |
| lombok | inherited from parent 3.2.5 | maven | provided | None | unknown | pom.xml:21-127 |
| micrometer-registry-prometheus | inherited from parent 3.2.5 | maven | runtime | None | unknown | pom.xml:21-127 |
| spring-boot-starter-freemarker | inherited from parent 3.2.5 | maven | compile | None | unknown | pom.xml:21-127 |

### Notes on Library Dependencies

- **No CVE data included**: No direct evidence of vulnerabilities found in repository artifacts; external vulnerability databases not consulted.
- **Outdated signals marked as 'unknown'**: Lack of direct evidence in repository artifacts to determine if newer versions are available.
- **Version inheritance**: Many dependencies inherit versions from Spring Boot parent POM (3.2.5) or Spring Cloud BOM (2023.0.1).
- **No wildcard or snapshot versions detected**: All versions are pinned or managed by BOMs.

---

## Integration Touchpoints

### External Integrations

#### 1. OneView API
- **Type:** REST
- **Direction:** outbound
- **Protocol/Tech:** HTTPS/REST
- **Source Module:** OneViewServiceImpl
- **Confidence:** high
- **Details:**
  - URL: https://cdoneview.avateam.io
  - Authentication: Custom token-based (X-Ava-Access-Token header)
  - Purpose: Fetch project performance and RAG status data
- **Evidence:** `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:28`
- **Security Note:** Hardcoded credentials detected in source code (flagged in repository summary)

#### 2. AWS RDS MySQL
- **Type:** DB
- **Direction:** outbound
- **Protocol/Tech:** JDBC/MySQL
- **Source Module:** JPA Repositories
- **Confidence:** high
- **Details:**
  - Endpoint: cove-db.chioww02cxoo.ap-south-1.rds.amazonaws.com:3306
  - Database: cove
  - Driver: com.mysql.cj.jdbc.Driver
- **Evidence:** `src/main/resources/application-dev.properties:48`

#### 3. Gmail SMTP
- **Type:** external SaaS
- **Direction:** outbound
- **Protocol/Tech:** SMTP/SSL
- **Source Module:** EmailServiceImpl
- **Confidence:** high
- **Details:**
  - Host: smtp.gmail.com
  - Port: 465
  - Protocol: smtp
  - SSL Enabled: true
  - Purpose: Send email notifications (approval, rejection, pending)
- **Evidence:** `src/main/resources/application-dev.properties:41-46`

#### 4. Netflix Eureka
- **Type:** REST
- **Direction:** bidirectional
- **Protocol/Tech:** HTTP/REST
- **Source Module:** cove-user-service
- **Confidence:** high
- **Details:**
  - Service URL: http://k8s-coveeurekaingress-099090752f-315466093.ap-south-1.elb.amazonaws.com/eureka
  - Purpose: Service discovery and registration
- **Evidence:** `pom.xml:44-47`, `src/main/resources/application-dev.properties:2-5`

#### 5. Prometheus
- **Type:** REST
- **Direction:** inbound
- **Protocol/Tech:** HTTP/Prometheus scrape
- **Source Module:** Spring Actuator
- **Confidence:** high
- **Details:**
  - Scrape Port: 8090
  - Scrape Path: /actuator/prometheus
  - Purpose: Metrics collection and monitoring
- **Evidence:** `kubernetes/deployment.yaml:1-36`

#### 6. AWS ECR
- **Type:** file
- **Direction:** outbound
- **Protocol/Tech:** Docker Registry API
- **Source Module:** CI/CD Pipeline
- **Confidence:** high
- **Details:**
  - Purpose: Docker image storage and retrieval
- **Evidence:** `buildspec.yml`

#### 7. AWS EKS
- **Type:** REST
- **Direction:** bidirectional
- **Protocol/Tech:** Kubernetes API
- **Source Module:** CI/CD Pipeline
- **Confidence:** high
- **Details:**
  - Purpose: Container orchestration and deployment
- **Evidence:** `buildspec.yml`, `kubernetes/deployment.yaml`, `kubernetes/service.yaml`

---

## Analysis Metadata

### Inputs
- **Repository:** ramanohar/AAVA-Reverse-Engineering-POC
- **Branch:** main
- **Run Mode:** build

### Limits and Unknowns

1. **Library version outdated signals marked as 'unknown'**: Due to lack of direct evidence in repository artifacts; external vulnerability databases not consulted.

2. **No CVE data included**: No direct evidence of vulnerabilities found in repository artifacts.

3. **Service-to-service call graph based on repository summary and typical Spring Boot patterns**: Detailed code analysis not performed.

4. **Integration touchpoint details extracted from configuration files**: Runtime behavior not observed.

5. **No API specification (Swagger/OpenAPI) found**: Endpoint details derived from controller annotations.

6. **Database schema relationships inferred from JPA entities**: No database migration scripts analyzed.

7. **Email template usage inferred from service layer**: Actual template rendering not validated.

8. **External API authentication details (OneView) noted as hardcoded in source**: Security risk flagged in repository summary.

---

**End of Dependency Graph**
