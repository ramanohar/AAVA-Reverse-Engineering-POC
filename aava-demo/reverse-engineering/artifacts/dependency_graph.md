# Dependency Graph

**Repository:** ramanohar/AAVA-Reverse-Engineering-POC  
**Branch:** main  
**Run Mode:** build  
**Generated:** 2025-01-16T00:00:00Z

---

## 1. Service/Module Graph

### Nodes

| ID | Type | Package |
|----|------|----------|
| AuthRestController | controller | com.collaberadigital.cove.controller.impl |
| AdminController | controller | com.collaberadigital.cove.controller.impl |
| PerformaceController | controller | com.collaberadigital.cove.controller.impl |
| Healthcheck | controller | com.collaberadigital.cove.controller.impl |
| UserService | service | com.collaberadigital.cove.service |
| AdminService | service | com.collaberadigital.cove.service |
| AuthService | service | com.collaberadigital.cove.service |
| EmailService | service | com.collaberadigital.cove.service |
| ActionHistoryService | service | com.collaberadigital.cove.service |
| OneViewService | service | com.collaberadigital.cove.service |
| UserRepository | repository | com.collaberadigital.cove.repository |
| AccessTokenRepo | repository | com.collaberadigital.cove.repository |
| RefreshTokenRepo | repository | com.collaberadigital.cove.repository |
| ActionHistoryRepo | repository | com.collaberadigital.cove.repository |
| CustomerDao | repository | com.collaberadigital.cove.repository |

### Edges

#### AuthRestController → UserService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:95` - userService.loginUser()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:102` - userService.registerUser()

#### AuthRestController → AuthService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:127` - authService.checkAccessToken()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:132` - authService.refreshToken()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:138` - authService.revokeAccessToken()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java:142` - authService.revokeRefreshToken()

#### AdminController → AdminService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java:39` - adminService.updateUserOnboardingStatus()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java:50` - adminService.updateUserAccountStatus()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java:61` - adminService.updateUserRole()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java:68` - adminService.getUserDetails()
  - `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java:88` - adminService.getAllUserPagination()

#### AdminController → ActionHistoryService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java:115` - actionHistoryService.getActionHistoryPagination()

#### PerformaceController → OneViewService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java:23` - oneViewService.getPerFormaceData()

#### UserService → UserRepository
- **Dependency Type:** repository_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:97` - userRepository.findByEmail()
  - `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:103` - userRepository.save()
  - `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:133` - userRepository.findByEmail()

#### UserService → EmailService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/UserServiceImpl.java:105` - emailService.sendEmailPendingApproval()

#### AdminService → UserRepository
- **Dependency Type:** repository_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:49` - userRepository.findByEmail()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:68` - userRepository.save()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:87` - userRepository.findByEmail()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:119` - userRepository.findByEmail()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:138` - userRepository.findByEmail()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:154` - userRepository.findByOnboardingStatusInAndRoleIn()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:157` - userRepository.findByOnboardingStatusInAndSubmissionDate()

#### AdminService → EmailService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:53` - emailService.sendEmailApproved()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:58` - emailService.sendEmailReject()

#### AdminService → ActionHistoryService
- **Dependency Type:** service_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:65` - actionHistoryService.saveActionHistory()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:103` - actionHistoryService.saveActionHistory()
  - `src/main/java/com/collaberadigital/cove/service/impl/AdminServiceImpl.java:124` - actionHistoryService.saveActionHistory()

#### AuthService → AccessTokenRepo
- **Dependency Type:** repository_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java:35` - jwtAccessTokenUtil.validateAccessTokenV2() which uses accessTokenRepo

#### AuthService → RefreshTokenRepo
- **Dependency Type:** repository_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java:40` - jwtRefreshTokenUtil.generateNewRefreshToken() which uses refreshTokenRepo

#### AuthService → UserRepository
- **Dependency Type:** repository_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java:46` - userRepository.findByEmail()
  - `src/main/java/com/collaberadigital/cove/service/impl/AuthServiceImpl.java:54` - userRepository.findByEmail()

#### ActionHistoryService → ActionHistoryRepo
- **Dependency Type:** repository_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java:60` - actionHistoryRepo.save()
  - `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java:76` - actionHistoryRepo.findByActionHistoryWithoutDate()
  - `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java:80` - actionHistoryRepo.findByActionHistoryWithDate()

#### ActionHistoryService → UserRepository
- **Dependency Type:** repository_call
- **Direction:** outbound
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java:43` - userRepository.findByEmail()
  - `src/main/java/com/collaberadigital/cove/service/impl/ActionHistoryServiceImpl.java:44` - userRepository.findByEmail()

---

## 2. Library Dependencies

| Name | Version | Ecosystem | Scope | Risk Flags | Outdated | Evidence |
|------|---------|-----------|-------|------------|----------|----------|
| spring-boot-starter-parent | 3.2.5 | maven | parent | - | unknown | pom.xml:6-10 |
| spring-boot-starter-web | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:22-25 |
| spring-boot-starter-webflux | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:26-29 |
| lombok | inherited from parent 3.2.5 | maven | provided | - | unknown | pom.xml:30-33 |
| hibernate-validator | 6.0.16.Final | maven | compile | explicit_old_version | yes | pom.xml:34-38 |
| spring-cloud-starter-netflix-eureka-client | inherited from spring-cloud-dependencies 2023.0.1 | maven | compile | - | unknown | pom.xml:39-42, pom.xml:117-125 |
| jaxb-api | 2.3.1 | maven | compile | - | unknown | pom.xml:43-47 |
| spring-boot-starter-data-jpa | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:48-51 |
| mysql-connector-java | 8.0.33 | maven | runtime | - | unknown | pom.xml:52-56 |
| spring-boot-starter-security | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:57-60 |
| jjwt-api | 0.11.5 | maven | compile | - | unknown | pom.xml:68-72 |
| jjwt-impl | 0.11.5 | maven | runtime | - | unknown | pom.xml:74-78 |
| jjwt-jackson | 0.11.5 | maven | runtime | - | unknown | pom.xml:80-84 |
| spring-boot-starter-mail | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:86-89 |
| spring-boot-starter-freemarker | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:90-93 |
| spring-boot-starter-test | inherited from parent 3.2.5 | maven | test | - | unknown | pom.xml:95-99 |
| reactor-test | inherited from parent 3.2.5 | maven | test | - | unknown | pom.xml:100-104 |
| spring-boot-starter-actuator | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:106-109 |
| micrometer-registry-prometheus | inherited from parent 3.2.5 | maven | compile | - | unknown | pom.xml:110-113 |

### Risk Flag Details

- **hibernate-validator 6.0.16.Final**: Version 6.0.16.Final is significantly older than current Hibernate Validator releases (8.x). Recommend upgrade review.

---

## 3. Integration Touchpoints

### MySQL Database (AWS RDS)
- **Type:** database
- **Direction:** outbound
- **Protocol/Tech:** JDBC / MySQL 8.0.33
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/resources/application-dev.properties:48` - spring.datasource.url=jdbc:mysql://cove-db.chioww02cxoo.ap-south-1.rds.amazonaws.com:3306/cove
  - `pom.xml:52-56` - mysql-connector-java:8.0.33

### OneView API
- **Type:** REST
- **Direction:** outbound
- **Protocol/Tech:** HTTPS / RestTemplate
- **Source Module:** OneViewServiceImpl
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:31` - https://cdoneview.avateam.io/user/signin
  - `src/main/java/com/collaberadigital/cove/service/impl/OneViewServiceImpl.java:48` - https://cdoneview.avateam.io/ava/oneview/internal/api/dashboard/rag/list

### Gmail SMTP
- **Type:** email
- **Direction:** outbound
- **Protocol/Tech:** SMTP over SSL (port 465)
- **Source Module:** EmailServiceImpl
- **Confidence:** high
- **Evidence:**
  - `src/main/resources/application-dev.properties:41-46` - spring.mail.host=smtp.gmail.com, spring.mail.port=465
  - `src/main/java/com/collaberadigital/cove/service/impl/EmailServiceImpl.java:44-60` - sendingEmail.send(message)

### Netflix Eureka Service Registry
- **Type:** service_discovery
- **Direction:** outbound
- **Protocol/Tech:** HTTP / Eureka Client
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/resources/application-dev.properties:2` - eureka.client.service-url.defaultZone=http://k8s-coveeurekaingress-099090752f-315466093.ap-south-1.elb.amazonaws.com/eureka
  - `pom.xml:39-42` - spring-cloud-starter-netflix-eureka-client

### Prometheus Metrics Endpoint
- **Type:** monitoring
- **Direction:** inbound
- **Protocol/Tech:** HTTP / Actuator
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/resources/application-dev.properties:12-14` - management.endpoint.prometheus.enabled=true, management.endpoints.web.exposure.include=health,info,prometheus,metrics
  - `pom.xml:110-113` - micrometer-registry-prometheus
  - `kubernetes/deployment.yaml:20-21` - prometheus.io/scrape: true, prometheus.io/port: 8090

### REST API Endpoints
- **Type:** REST
- **Direction:** inbound
- **Protocol/Tech:** HTTP / Spring WebFlux
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `src/main/java/com/collaberadigital/cove/controller/impl/AuthRestController.java` - /login, /register, /check-access-token, /refresh-token, /revoke-access-token, /revoke-refresh-token
  - `src/main/java/com/collaberadigital/cove/controller/impl/AdminController.java` - /api/v1/admin/*
  - `src/main/java/com/collaberadigital/cove/controller/impl/PerformaceController.java` - /performance
  - `src/main/java/com/collaberadigital/cove/controller/impl/Healthcheck.java` - /healthcheck

### Kubernetes Service (ClusterIP)
- **Type:** orchestration
- **Direction:** inbound
- **Protocol/Tech:** TCP / Kubernetes
- **Source Module:** cove-user-service
- **Confidence:** high
- **Evidence:**
  - `kubernetes/service.yaml:8-12` - type: ClusterIP, port: 80, targetPort: 8090
  - `kubernetes/deployment.yaml:28-29` - containerPort: 8090

---

## 4. Analysis Metadata

- **Generated At (UTC):** 2025-01-16T00:00:00Z
- **Inputs:**
  - `repo_url`: ramanohar/AAVA-Reverse-Engineering-POC
  - `branch`: main
  - `run_mode`: build

### Limits and Unknowns

1. No lock file detected; transitive dependencies not captured
2. Outdated signals for most libraries marked unknown due to lack of external version comparison data
3. No CVE database access; vulnerability claims require external validation
4. Reactive call chains (Mono/Flux) may obscure some runtime dependencies
5. OneView API credentials hardcoded in source (security risk noted in repository summary)
6. Test coverage minimal; integration test dependencies not analyzed

---

**End of Dependency Graph**
