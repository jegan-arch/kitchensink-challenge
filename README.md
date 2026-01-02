# MemberHub | Modern Backend Challenge

A full-stack, containerized Member Management System built with **Spring Boot 4**, **Java 21**, **Angular**, and **MongoDB**. This project demonstrates modern best practices including Microservices architecture, JWT Authentication (RBAC), Reactive State Management, and comprehensive Unit Testing.

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

| Service | Container Name | URL | Description | Credentials |
| :--- | :--- | :--- | :--- | :--- |
| **User Interface** | `member-app-ui` | [http://localhost](http://localhost) | Main Application | Login required |
| **DB Admin** | `member-app-db-admin` | [http://localhost:8081](http://localhost:8081) | Mongo Express UI | User: `admin` / Pass: `password` |
| **Auth API** | `member-app-auth` | [http://localhost:8082](http://localhost:8082) | Authentication Service | N/A |
| **Member API** | `member-app-backend` | [http://localhost:8083](http://localhost:8083) | Protected Resources | Requires Bearer Token |

### 3. 🔐 User Setup & RBAC Testing
To test the Role-Based Access Control, you should create two different users.

**Roles Explained:**
* **Admin:** Can View, Register, and **Delete** members.
* **User:** Can View and Register members only (Delete button hidden/blocked).

**Option A: Using Browser Console (Recommended)**
1. Open your browser console (F12).
2. Paste this script to create both users instantly:

```javascript
const createUsers = async () => {
    const users = [
        { username: "testadmin", email: "admin@test.com", password: "password123", roles: ["admin"] },
        { username: "testuser",  email: "user@test.com",  password: "password123", roles: ["user"] }
    ];

    for (const u of users) {
        try {
            const res = await fetch('http://localhost:8082/auth/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(u)
            });
            const data = await res.json();
            console.log(`✅ Created ${u.roles[0]}:`, data);
        } catch (e) { console.error(`❌ Error creating ${u.roles[0]}:`, e); }
    }
    console.log("🎉 Setup Complete! Try logging in with 'testuser' first, then 'testadmin'.");
};
createUsers();
```

**Option B: Using Curl**
```bash
# Create Admin
curl -X POST http://localhost:8082/auth/signup -H "Content-Type: application/json" \
  -d '{"username":"testadmin","email":"admin@test.com","password":"password123","roles":["admin"]}'

# Create Standard User
curl -X POST http://localhost:8082/auth/signup -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"user@test.com","password":"password123","roles":["user"]}'
```

### 4. Stop the Application
To stop the containers and remove the active network:
```bash
docker-compose down
```
*(Optional) To stop and also delete the database volume (reset data):* `docker-compose down -v`

---

## 🏗 Architecture

The solution is orchestrated via **Docker Compose** and consists of the following services:

* **`member-app-ui`**: Angular 16+ Frontend. Uses Guards for routing protection and Interceptors for JWT handling.
* **`auth-service`** (Container: `member-app-auth`): Spring Boot 4 Service. Handles User Registration, Login, and JWT Issuance.
* **`backend-service`** (Container: `member-app-backend`): Spring Boot 4 Service. Manages Member data with strict validation and auditing.
* **`mongodb`** (Container: `member-app-db`): MongoDB v7.0. Data is persisted via a Docker Volume (`mongo-data`).
* **`mongo-express`** (Container: `member-app-db-admin`): Web-based administrative interface for MongoDB.

### Key Features
* ✅ **Modern Stack:** Built on **Java 21 (LTS)** and **Spring Boot 4.0.1**.
* ✅ **Security (RBAC):**
  * **Admins:** Full access including `@PreAuthorize` protected Delete endpoints.
  * **Users:** Restricted access; UI automatically hides "Delete" actions based on JWT claims.
* ✅ **Auditing:** Automated tracking of `CreatedBy`, `UpdatedBy`, and timestamps.
* ✅ **Observability:** Distributed tracing with Correlation IDs (`req-xyz`) in logs.
* ✅ **Strict Validation:**
  * **Names:** Alphabets only (No numbers allowed).
  * **Phone:** Strict Indian 10-digit format (`^[6-9]\d{9}$`).
  * **Email:** DB-level uniqueness via `@Indexed(unique=true)`.
* ✅ **Quality Assurance:** High test coverage (>80%) using **JUnit 5**, **Mockito**, and **MockMvc**.

---

## 🧪 Development & Testing

If you wish to examine the code or run tests manually without Docker, follow these steps.

**Prerequisites:** **Java 21** and Maven.

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
