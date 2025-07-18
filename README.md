# 🏨 Booking System (Test Task)

A mini version of booking.com — monolithic Spring Boot application for unit listing, booking, filtering, and payments with availability and cache support.

---

## 🛠 Tech Stack

- Java 21
- Spring Boot 3
- Spring Data JPA + PostgreSQL
- Liquibase (SQL changelogs)
- Redis (caching available unit count)
- JUnit 5 + Mockito (unit tests)
- Docker & Docker Compose
- Swagger/OpenAPI for documentation

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Gradle
- Docker + Docker Compose

---

### 1️⃣ Start Dependencies (Postgres + Redis)

```bash
docker-compose up -d
```

### 2️⃣ Run the Application

```bash
./gradlew bootRun
```

### 📚 API Documentation

Once the app is running, open:

Swagger UI:
http://localhost:8080/swagger-ui/index.html

Provides interactive access to:
-	POST /api/units – Add a new unit
-	POST /api/units/search – Filter & paginate units
-	GET /api/units/available-count – Count of available units
-	POST /api/bookings – Book a unit
-	POST /api/bookings/{id}/pay – Simulate payment
-	POST /api/bookings/{id}/cancel – Cancel a booking
-	POST /api/bookings/auto-expire – Automatically expire unpaid bookings