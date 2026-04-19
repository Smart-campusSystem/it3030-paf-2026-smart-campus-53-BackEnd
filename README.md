# 🏛️ Smart Campus System — Back End

> **Spring Boot 4 · Java 21 · MySQL · JWT + Google OAuth2 · AWS S3**

A RESTful API backend for a university smart campus platform that manages users, resource bookings, support tickets, and real-time notifications — with role-based access control across three user types: **Student (USER)**, **Admin**, and **Technician**.

---

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Features & Completion Status](#-features--completion-status)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [API Endpoints](#-api-endpoints)
- [Roles & Permissions](#-roles--permissions)
- [Database](#-database)
- [Roadmap](#-roadmap)

---

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security · JJWT 0.12.6 · OAuth2 (Google) |
| Database | MySQL 8 (primary) · H2 (dev/test) |
| ORM | Spring Data JPA / Hibernate |
| File Storage | AWS S3 (ap-south-1) |
| Build Tool | Maven |
| Boilerplate | Lombok |

---

## 📁 Project Structure

```
src/main/java/com/smart_campus_system/demo/
├── config/             # Bean configs & sample data seeder
├── controller/         # REST controllers
├── dto/                # Request / Response DTOs
├── exception/          # Global exception handling
├── model/              # JPA entities
├── repository/         # Spring Data repositories
├── security/           # JWT filter, OAuth2 handler, SecurityConfig
├── service/            # Business logic
├── storage/            # AWS S3 profile-image abstraction
└── util/               # Shared utilities
```

---

## ✅ Features & Completion Status

> Update the checkboxes below as features are completed.

### 🔐 Authentication & Authorization
- [x] Email + Password registration & login
- [x] JWT access token generation & validation
- [x] Google OAuth2 social login
- [x] Post-OAuth redirect to frontend with `?token=`
- [x] Role-based access control (`USER`, `ADMIN`, `TECHNICIAN`)
- [x] Protected routes via `JwtAuthenticationFilter`

### 👤 User Management
- [x] User entity (`email`, `firstName`, `lastName`, `role`, `provider`, `active`, `createdAt`)
- [x] Public registration (always creates `USER` role)
- [x] Admin-only user creation (can assign any role)
- [x] Self-service profile update
- [x] Admin CRUD for all users (update role, activate/deactivate)
- [x] Profile image upload → stored in AWS S3 (presigned URL)
- [x] Sample data seeder (ADMIN, USER, TECHNICIAN seed accounts)

### 🏢 Resource Management
- [ ] `Resource` entity (name, type, location, capacity, available)
- [ ] CRUD for resources (Admin only)
- [ ] Availability time-slot management
- [ ] `GET /api/resources` — list all resources
- [ ] `POST /api/resources` — create resource (ADMIN)
- [ ] `PUT /api/resources/{id}` — update resource (ADMIN)
- [ ] `DELETE /api/resources/{id}` — delete resource (ADMIN)

### 📅 Booking System
- [ ] `Booking` entity (user, resource, startTime, endTime, status)
- [ ] Create booking with conflict detection (no double-booking)
- [ ] Cancel / update booking
- [ ] `POST /api/bookings` — book a resource
- [ ] `GET /api/bookings/me` — current user's bookings
- [ ] `GET /api/bookings` — all bookings (ADMIN)
- [ ] `DELETE /api/bookings/{id}` — cancel booking
- [ ] Auto-notification on booking confirmed / cancelled

### 🎫 Ticket System
- [ ] `Ticket` entity (title, description, type, priority, status, submitter, assignee)
- [ ] Submit a new support ticket (USER)
- [ ] Assign ticket to TECHNICIAN (ADMIN)
- [ ] Update ticket status (TECHNICIAN)
- [ ] `POST /api/tickets` — submit ticket
- [ ] `GET /api/tickets/me` — own tickets
- [ ] `GET /api/tickets` — all tickets (ADMIN/TECHNICIAN)
- [ ] `PUT /api/tickets/{id}` — update status / assignee

### 🔔 Notification System
- [ ] `Notification` entity (user, message, link, read, createdAt)
- [ ] Create notifications on domain events
- [ ] `GET /api/notifications/me` — fetch own notifications
- [ ] `PUT /api/notifications/{id}/read` — mark as read
- [ ] `PUT /api/notifications/read-all` — mark all as read

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8 running locally (or configure H2 for dev — see below)
- (Optional) AWS credentials for S3 profile images

### 1. Clone & configure

```bash
git clone https://github.com/isuru666/smart-campus-system-BackEnd.git
cd smart-campus-system-BackEnd
```

Create a `.env` or set environment variables (see [Environment Variables](#-environment-variables)).

### 2. Use H2 for quick local dev (no MySQL needed)

In `src/main/resources/application.properties`, comment out the MySQL block and uncomment the H2 block:

```properties
# spring.datasource.url=jdbc:mysql://...   ← comment this out
spring.datasource.url=jdbc:h2:mem:smartcampus;DB_CLOSE_DELAY=-1;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

### 3. Run

```bash
./mvnw spring-boot:run
```

API available at: **`http://localhost:8080`**
H2 Console (dev only): **`http://localhost:8080/h2-console`**

### 4. Seed Users (auto on startup)

| Email | Password | Role |
|-------|----------|------|
| sample-admin@example.com | SamplePass12 | ADMIN |
| sample-user@example.com | SamplePass12 | USER |
| sample-technician@example.com | SamplePass12 | TECHNICIAN |

---

## 🔑 Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_HOST` | `localhost` | MySQL host |
| `MYSQL_PORT` | `3306` | MySQL port |
| `MYSQL_DB` | `smartcampus` | Database name |
| `MYSQL_USER` | `root` | DB username |
| `MYSQL_PASSWORD` | _(empty)_ | DB password |
| `APP_JWT_SECRET` | `dev-only-...` | JWT signing secret (min 32 chars) |
| `APP_JWT_EXPIRATION_MS` | `86400000` | Token TTL in ms (default 24 h) |
| `APP_OAUTH2_FRONTEND_REDIRECT_URL` | `http://localhost:5173/auth/callback` | Post-OAuth redirect |
| `GOOGLE_CLIENT_ID` | — | Google OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | — | Google OAuth2 Client Secret |
| `AWS_S3_PROFILE_BUCKET` | — | S3 bucket for profile images |
| `AWS_S3_PROFILE_REGION` | `ap-south-1` | S3 region |
| `AWS_S3_PROFILE_PUBLIC_BASE_URL` | — | Public URL prefix for S3 objects |
| `APP_SEED_SAMPLE_USERS` | `true` | Insert seed users on startup |

---

## 📡 API Endpoints

### Auth — `/api/auth`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | Public | Register with email & password |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/oauth2/authorization/google` | Public | Redirect to Google OAuth |

### Users — `/api/users`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/users/me` | Any user | Current user profile |
| GET | `/api/users` | ADMIN | List all users |
| GET | `/api/users/{id}` | Self / ADMIN | Get user by ID |
| POST | `/api/users` | ADMIN | Create user (any role) |
| PUT | `/api/users/{id}` | Self / ADMIN | Update user |
| DELETE | `/api/users/{id}` | ADMIN | Delete user |
| POST | `/api/users/{id}/profile-image` | Self | Upload avatar (multipart) |

### Resources — `/api/resources` *(planned)*
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/resources` | Any user | Browse all resources |
| POST | `/api/resources` | ADMIN | Create resource |
| PUT | `/api/resources/{id}` | ADMIN | Update resource |
| DELETE | `/api/resources/{id}` | ADMIN | Delete resource |

### Bookings — `/api/bookings` *(planned)*
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/bookings` | USER | Create booking |
| GET | `/api/bookings/me` | USER | My bookings |
| GET | `/api/bookings` | ADMIN | All bookings |
| DELETE | `/api/bookings/{id}` | Self / ADMIN | Cancel booking |

### Tickets — `/api/tickets` *(planned)*
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/tickets` | USER | Submit ticket |
| GET | `/api/tickets/me` | USER | My tickets |
| GET | `/api/tickets` | ADMIN / TECH | All tickets |
| PUT | `/api/tickets/{id}` | ADMIN / TECH | Update status |

### Notifications — `/api/notifications` *(planned)*
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/notifications/me` | USER | My notifications |
| PUT | `/api/notifications/{id}/read` | USER | Mark one as read |
| PUT | `/api/notifications/read-all` | USER | Mark all as read |

---

## 🛡️ Roles & Permissions

| Action | USER | TECHNICIAN | ADMIN |
|--------|:----:|:----------:|:-----:|
| Register / Login | ✅ | ✅ | ✅ |
| View own profile | ✅ | ✅ | ✅ |
| Browse resources | ✅ | ✅ | ✅ |
| Create booking | ✅ | ✅ | ✅ |
| Submit ticket | ✅ | ✅ | ✅ |
| Update ticket status | ❌ | ✅ | ✅ |
| Manage resources | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |
| View all bookings | ❌ | ❌ | ✅ |
| View all tickets | ❌ | ✅ | ✅ |

---

## 🗄️ Database

- **Primary**: MySQL 8 with `ddl-auto=update` (auto-creates/updates tables)
- **Dev/Test**: H2 in-memory (see Getting Started)
- Schema auto-generated from JPA entities via Hibernate

> ⚠️ **Production note**: Replace `ddl-auto=update` with a migration tool (Flyway / Liquibase) before deploying to production.

---

## 🗺️ Roadmap

- [ ] Implement `Resource`, `Booking`, `Ticket`, `Notification` JPA entities
- [ ] Implement service layer business logic for all four domains
- [ ] Wire up all planned API controllers
- [ ] Add input validation & global error responses for new endpoints
- [ ] Add pagination & filtering to list endpoints
- [ ] Add unit & integration tests for services and controllers
- [ ] Replace `ddl-auto=update` with Flyway migrations
- [ ] Dockerize application (Dockerfile + docker-compose)
- [ ] CI/CD pipeline (GitHub Actions)

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

*Last updated: April 2026*
