# MemberHub | Modern Backend Challenge

A full-stack, containerized Member Management System built with **Spring Boot 4**, **Java 21**, **Angular**, and **MongoDB**.

This project demonstrates modern best practices including **Role-Based Access Control (RBAC)**, **JWT Authentication**, **Strict Data Validation**, and **High Test Coverage (>90%)**.

---

## 🚀 Quick Start (Docker)

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed.

You do **not** need Java, Node.js, or Maven installed locally to run this demo. The build process is fully containerized.

### 1. Clone & Run
```bash
git clone <your-repo-url>
cd mongodb-challenge

# Build and start all services in the background
docker-compose up --build -d
```

### 2. Access the Application
*Please wait approx. 30 seconds for the backend services to fully initialize.*

| Service | URL | Description | Credentials |
| :--- | :--- | :--- | :--- |
| **User Interface** | [http://localhost:4200](http://localhost:4200) | Main Dashboard | Login required |
| **Backend API** | [http://localhost:8083](http://localhost:8083) | API Root | Requires Token |
| **Swagger UI** | [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) | API Documentation | N/A |
| **System Health** | [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health) | Monitoring Status | Public |
| **DB Admin** | [http://localhost:8081](http://localhost:8081) | Mongo Express UI | User: `admin` / Pass: `password` |

### 3. 🔐 Login & RBAC Testing

**Default Super Admin**
The system automatically initializes a Super Admin account on the first run.
* **Username:** `superadmin`
* **Password:** `admin123`
* **Role:** `ADMIN` (Full Access)

**Create a Standard User**
To test the "User" role (restricted permissions), use the Sign-up page or this Curl command:

```bash
curl -X POST http://localhost:8083/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "testuser",
    "name": "Standard User",
    "email": "user@test.com",
    "password": "password123",
    "phoneNumber": "9876543210",
    "role": "ROLE_USER"
  }'
```

### 4. Stop the Application
To stop the containers and remove the active network:
```bash
docker-compose down
```

---

## 🏗 Architecture & Tech Stack

The solution is orchestrated via **Docker Compose** as a cohesive unit.

### Core Stack
* **Backend:** Java 21, Spring Boot 3.2+
* **Frontend:** Angular 16+, TypeScript
* **Database:** MongoDB v7.0
* **Containerization:** Docker & Docker Compose

### Key Features
* ✅ **Security (RBAC):**
    * **Admins:** Full CRUD access.
    * **Users:** Read/Write own profile only.
    * **Privilege Escalation Protection:** Users cannot assign themselves `ADMIN` roles.
* ✅ **JWT Authentication:** Stateless security with custom `AuthTokenFilter`.
* ✅ **Observability:** Spring Actuator enabled for Health & Metrics.
* ✅ **API Docs:** Auto-generated OpenAPI 3.0 documentation via Swagger.
* ✅ **Strict Validation:**
    * **Phone:** Indian 10-digit format (`^[6-9]\d{9}$`).
    * **Email:** DB-level uniqueness via `@Indexed(unique=true)`.
* ✅ **Quality Assurance:** High test coverage (**>90%**) using **JUnit 5**, **Mockito**, and **MockMvc**.

---

## 🧪 Development & Testing

If you wish to examine the code or run tests manually without Docker:

**Prerequisites:** **Java 21** and Maven.

### 1. Running Unit Tests
The backend features comprehensive unit tests covering Controllers, Services, Security, and Exception Handling.

```bash
cd member-backend
mvn test
```
*Current Coverage: ~92%*

### 2. Manual Execution
**Backend:**
```bash
cd member-backend
mvn spring-boot:run
```

**Frontend (UI):**
```bash
cd member-app-ui
npm install
ng serve
```
*Access UI at `http://localhost:4200`*

---

## 📂 Project Structure

```bash
├── member-backend      # Spring Boot Monolith
│   ├── src/main        # Source code
│   └── src/test        # JUnit 5 Tests (>90% coverage)
├── member-app-ui       # Angular Frontend
├── docker-compose.yml  # Orchestration
└── README.md           # Documentation
```

---
*Developed for the Modern Backend Challenge.*