# Integration Catalog: ramanohar/AAVA-Reverse-Engineering-POC

**Generated:** 2025-01-16T12:00:00Z  
**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main  
**Run Mode:** build

---

## Touchpoints Source

**Source Artifact:** `aava-demo/reverse-engineering/artifacts/dependency_graph.json`  
**Source Touchpoints Count:** 7  
**Notes:** Integration Catalog enriches Dependency Graph touchpoints; no separate inventory produced.

---

## Touchpoint Enrichments

### 1. OneView API

**Direction:** outbound  
**Protocol/Tech:** HTTPS/REST  
**Auth Pattern:** custom_token  
**Confidence:** high

#### Contract Summary
- **Inputs:** POST /user/signin with username/password JSON body; GET /ava/oneview/internal/api/dashboard/rag/list with query params (weekEndingDates, projectTypes)
- **Outputs:** Login returns X-Ava-Access-Token header; RAG list returns JSON with deliveryRagProjectList array containing project, client, ragStatus fields
- **Schema Hint:** LoginRequestOneView (userName, password), PerformaceModel (data.deliveryRagProjectList[]), PerformanceResponse (data[])

#### Failure Behavior
- **Retry Policy:** Fallback to previous day's data if current day returns empty list; no explicit retry on HTTP failure
- **Idempotency:** GET requests are idempotent; POST /signin is not idempotent but stateless
- **Timeout:** unknown
- **Error Handling:** No explicit try-catch around RestTemplate calls; relies on Spring default exception handling

#### Evidence
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:30-45` (hardcoded credentials: userName=Nagahemanthkn, password=Digital@$2458)
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:47` (custom header: X-Ava-Access-Token)
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:70-90` (fallback to previous day if empty response)

---

### 2. AWS RDS MySQL

**Direction:** outbound  
**Protocol/Tech:** JDBC/MySQL  
**Auth Pattern:** basic  
**Confidence:** high

#### Contract Summary
- **Inputs:** JPA repository method calls (findByEmail, save, findById, etc.) with entity objects or query parameters
- **Outputs:** Entity objects (UserEntity, AccessToken, RefreshToken, ActionHistory) or Optional<Entity>
- **Schema Hint:** Tables: users, access_token, refresh_token, action_history; JPA entities define schema via annotations

#### Failure Behavior
- **Retry Policy:** Spring Data JPA default retry (none); connection pool handles transient failures
- **Idempotency:** Reads are idempotent; writes (save) are not idempotent without application-level checks
- **Timeout:** unknown
- **Error Handling:** Repository methods throw exceptions on failure; caught by service layer or global exception handler

#### Evidence
- `src/main/resources/application-dev.properties:48-52` (datasource URL, username=admin, password=coveadmin2024)
- `src/main/java/com/collaberadigital/cove/repository/UserRepository.java` (custom queries with pagination)
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-100` (save with no explicit error handling)

---

### 3. Gmail SMTP

**Direction:** outbound  
**Protocol/Tech:** SMTP/SSL  
**Auth Pattern:** basic  
**Confidence:** high

#### Contract Summary
- **Inputs:** Email address, subject, FreeMarker template name, template model (name, description)
- **Outputs:** Boolean success/failure; throws CustomException on failure
- **Schema Hint:** Templates: email-template-rejected.ftl, email-template-approved.ftl, email-template-pending-approval.ftl

#### Failure Behavior
- **Retry Policy:** No retry; throws CustomException immediately on send failure
- **Idempotency:** Not idempotent; duplicate sends possible on retry at caller level
- **Timeout:** unknown
- **Error Handling:** Catches Exception, logs stack trace, throws CustomException with code 451

#### Evidence
- `src/main/resources/application-dev.properties:41-46` (host=smtp.gmail.com, port=465, username=covecollaberadigital@gmail.com, password=abmedqpfrirzecrl)
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java:40-70` (sendEmailReject method with try-catch)
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java:64` (throws CustomException on failure)

---

### 4. Netflix Eureka

**Direction:** bidirectional  
**Protocol/Tech:** HTTP/REST  
**Auth Pattern:** unknown  
**Confidence:** medium

#### Contract Summary
- **Inputs:** Service registration metadata (instance ID, hostname, preferIpAddress)
- **Outputs:** Service registry updates; heartbeat responses
- **Schema Hint:** Eureka client library handles protocol; no explicit request/response models in application code

#### Failure Behavior
- **Retry Policy:** Eureka client library default retry and failover
- **Idempotency:** Registration and heartbeat are idempotent
- **Timeout:** unknown
- **Error Handling:** Eureka client library handles failures; application does not explicitly handle Eureka errors

#### Evidence
- `src/main/resources/application-dev.properties:2-5` (service URL, instance ID, hostname, preferIpAddress)
- `pom.xml:44-47` (spring-cloud-starter-netflix-eureka-client dependency)

---

### 5. Prometheus

**Direction:** inbound  
**Protocol/Tech:** HTTP/Prometheus scrape  
**Auth Pattern:** unknown  
**Confidence:** high

#### Contract Summary
- **Inputs:** HTTP GET /actuator/prometheus from Prometheus server
- **Outputs:** Prometheus text format metrics (application metrics, JVM metrics, Spring Boot metrics)
- **Schema Hint:** Micrometer registry exports metrics in Prometheus format

#### Failure Behavior
- **Retry Policy:** Prometheus server retries scrape on failure
- **Idempotency:** Scrape endpoint is idempotent
- **Timeout:** unknown
- **Error Handling:** Spring Actuator handles endpoint errors; application does not explicitly handle scrape failures

#### Evidence
- `src/main/resources/application-dev.properties:10-15` (actuator endpoints enabled: health, info, prometheus, metrics)
- `kubernetes/deployment.yaml:1-36` (Prometheus scrape annotations on port 8090)

---

### 6. AWS ECR

**Direction:** outbound  
**Protocol/Tech:** Docker Registry API  
**Auth Pattern:** unknown  
**Confidence:** medium

#### Contract Summary
- **Inputs:** Docker image push/pull commands via AWS CLI and kubectl
- **Outputs:** Image manifest and layer storage confirmation
- **Schema Hint:** Docker Registry HTTP API V2

#### Failure Behavior
- **Retry Policy:** AWS CLI and Docker client default retry
- **Idempotency:** Image push is idempotent (same tag overwrites); pull is idempotent
- **Timeout:** unknown
- **Error Handling:** CI/CD pipeline (buildspec.yml) does not explicitly handle ECR errors; relies on AWS CLI exit codes

#### Evidence
- `buildspec.yml:pre_build phase` (ECR login command)
- `buildspec.yml:post_build phase` (docker push command)

---

### 7. AWS EKS

**Direction:** bidirectional  
**Protocol/Tech:** Kubernetes API  
**Auth Pattern:** unknown  
**Confidence:** medium

#### Contract Summary
- **Inputs:** kubectl apply commands with deployment.yaml and service.yaml manifests
- **Outputs:** Kubernetes resource creation/update confirmation; pod status
- **Schema Hint:** Kubernetes Deployment and Service resource schemas

#### Failure Behavior
- **Retry Policy:** kubectl retries on transient API failures; Kubernetes controller retries pod creation on failure
- **Idempotency:** kubectl apply is idempotent; Kubernetes reconciliation loop ensures desired state
- **Timeout:** unknown
- **Error Handling:** CI/CD pipeline (buildspec.yml) does not explicitly handle kubectl errors; relies on exit codes

#### Evidence
- `buildspec.yml:pre_build phase` (kubectl configuration)
- `buildspec.yml:post_build phase` (kubectl apply and rollout restart commands)
- `kubernetes/deployment.yaml` (Deployment resource with 2 replicas, resource limits)
- `kubernetes/service.yaml` (ClusterIP service on port 80)

---

## Cross-Cutting Patterns

### Auth Pattern

**Description:** JWT-based authentication with access tokens (30-minute validity) and refresh tokens (24-hour validity). Tokens stored in database with expiration and revocation flags. BCrypt password hashing for user credentials.

**Applies To:** (Internal authentication; not directly tied to external touchpoints)

**Evidence:**
- `src/main/resources/application-dev.properties:32-35` (jwt.secret, jwt.token.validity=1800000, jwt.refresh.token.validity=86400000)
- `src/main/java/com/collaberadigital/cove/security/JwtAccessTokenUtil.java` (token generation and validation)
- `src/main/java/com/collaberadigital/cove/security/JwtRefreshTokenUtil.java` (refresh token generation and validation)
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:145-155` (token generation on login with revocation of old tokens)

**Confidence:** high

---

### Retry Pattern

**Description:** No explicit application-level retry logic for most integrations. OneView API has fallback to previous day's data if current day returns empty. Relies on library defaults (Eureka client, Spring Data JPA connection pool, AWS CLI, kubectl).

**Applies To:** OneView API, Netflix Eureka, AWS RDS MySQL, AWS ECR, AWS EKS

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:70-90` (fallback to previous day)
- No explicit retry annotations or libraries (e.g., Spring Retry) detected in service implementations

**Confidence:** high

---

### Idempotency Pattern

**Description:** GET requests are idempotent. POST/PUT operations (user registration, login, admin updates) are not explicitly designed for idempotency; duplicate requests may cause duplicate side effects (e.g., multiple emails sent).

**Applies To:** OneView API, AWS RDS MySQL, Gmail SMTP

**Evidence:**
- `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:95-110` (registerUser saves without idempotency check)
- `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java:40-70` (sendEmail does not prevent duplicate sends)

**Confidence:** high

---

### Timeout Pattern

**Description:** No explicit timeout configuration detected in application code. Relies on library and infrastructure defaults (RestTemplate, JDBC driver, JavaMailSender, Eureka client).

**Applies To:** OneView API, AWS RDS MySQL, Gmail SMTP, Netflix Eureka

**Evidence:**
- No timeout properties in application-dev.properties for RestTemplate, JDBC, or mail
- No custom RestTemplate bean with timeout configuration detected

**Confidence:** high

---

### Observability Pattern

**Description:** Prometheus metrics exposed via Spring Actuator on /actuator/prometheus. Logging with SLF4J/Logback (commented out debug logging in properties). No distributed tracing (e.g., Sleuth, Zipkin) detected.

**Applies To:** Prometheus

**Evidence:**
- `src/main/resources/application-dev.properties:10-15` (actuator endpoints: health, info, prometheus, metrics)
- `src/main/resources/application-dev.properties:60-63` (commented out logging levels)
- `pom.xml` (micrometer-registry-prometheus dependency)

**Confidence:** high

---

## Analysis Metadata

### Inputs
- **Repository:** ramanohar/AAVA-Reverse-Engineering-POC
- **Branch:** main
- **Run Mode:** build
- **Prerequisite Artifacts:**
  - aava-demo/reverse-engineering/artifacts/repository_summary.json
  - aava-demo/reverse-engineering/artifacts/dependency_graph.json

### Limits and Unknowns

1. **Timeout configuration not explicitly set in application code**; relies on library defaults.

2. **Auth patterns for AWS ECR and AWS EKS not visible in application code**; handled by AWS CLI and kubectl.

3. **Eureka authentication mechanism not visible in application code**; may be handled by infrastructure or Eureka server configuration.

4. **Prometheus scrape authentication not visible**; may be unauthenticated or handled by Kubernetes network policies.

5. **No API specification (OpenAPI/Swagger) found**; contract details inferred from controller and service code.

6. **Hardcoded credentials detected in OneViewServiceImpl** (security risk flagged in repository summary).

7. **No distributed tracing or correlation ID propagation detected**.

8. **Error handling for external integrations is minimal**; relies on Spring default exception handling and global exception handler.

---

**End of Integration Catalog**
