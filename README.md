# MemberHub | Modern Backend Challenge

A full-stack, containerized Member Management System built with **Spring Boot**, **Angular**, and **MongoDB**. This project demonstrates modern best practices including Microservices architecture, JWT Authentication (RBAC), Reactive State Management, and comprehensive Unit Testing.

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
| **User Interface** | [http://localhost](http://localhost) | Main Application | Register a new user to start |
| **DB Admin** | [http://localhost:8081](http://localhost:8081) | Mongo Express UI | User: `admin` / Pass: `password` |
| **Auth API** | [http://localhost:8082](http://localhost:8082) | Authentication Service | N/A |
| **Member API** | [http://localhost:8083](http://localhost:8083) | Protected Resources | Requires Bearer Token |

### 3. Stop the Application
To stop the containers and remove the active network:
```bash
docker-compose down
```
*(Optional) To stop and also delete the database volume (reset data):* `docker-compose down -v`

---

## 🏗 Architecture

The solution is orchestrated via **Docker Compose** and consists of the following services:

* **`kitchensink-ui`**: Angular 16+ Frontend. Uses Guards for routing protection and Interceptors for JWT handling.
* **`auth-service`**: Spring Boot 3 Service. Handles User Registration, Login, and JWT Issuance (RS256/HS256).
* **`backend-service`**: Spring Boot 3 Service. Manages Member data with strict validation rules.
* **`mongodb`**: MongoDB v7.0. Data is persisted via a Docker Volume (`mongo-data`).

### Key Features
* ✅ **Security:** Stateless Authentication using JWT.
* ✅ **RBAC:** Role-Based Access Control (Admin vs. User capabilities).
* ✅ **Resilience:** Automatic container restart policies and Docker health checks.
* ✅ **Validation:** Regex enforcement for inputs at both UI and Backend Level.
* ✅ **Quality Assurance:** High test coverage (>80%) using **JUnit 5**, **Mockito**, and **MockMvc**.

---

## 🧪 Development & Testing

If you wish to examine the code or run tests manually without Docker, follow these steps.

**Prerequisites:** Java 17+ and Maven.

### 1. Running Unit Tests
Both backend services feature comprehensive unit tests covering Controllers, Services, and Security configurations.

```bash
# Test Auth Service
cd member-auth
mvn test

# Test Backend Service
cd ../member-app
mvn test
```

### 2. Manual Execution (Local Dev)
If running outside Docker, ensure you have a local MongoDB instance running on port `27017`.

**Auth Service:**
```bash
cd member-auth
mvn spring-boot:run
```

**Frontend (UI):**
```bash
cd member-app-ui
npm install
ng serve
```

---

## 📂 Project Structure

```bash
├── member-auth         # Auth Microservice (Login/Signup)
│   ├── src/main        # Source code
│   └── src/test        # JUnit 5 Tests
├── member-app          # KitchenSink Microservice (Business Logic)
│   ├── src/main        # Source code
│   └── src/test        # JUnit 5 Tests
├── member-app-ui       # Angular Frontend
├── docker-compose.yml  # Container Orchestration
└── README.md           # Documentation
```

---
*Developed for the Modern Backend Challenge.*
