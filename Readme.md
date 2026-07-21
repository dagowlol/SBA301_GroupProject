# Annexe Auction System

A full-stack online auction platform enabling users to list, browse, bid on, and purchase auction items with real-time bidding, VNPay payment integration, and role-based access control.

## Project Management

- **Jira Board**: https://doanchauviet.atlassian.net/jira/software/projects/SBA3/boards/1

## Team Members

| Full Name | Student ID |
|-----------|-----------|
| Châu Viết Doãn | DE190484 |
| Trần Thanh Hưng | DE190481 |
| Nguyễn Tiến Quân | DE190728 |
| Lê Hoàng | DE190435 |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 4.0.6, Spring Security, Spring Data JPA, WebSocket (STOMP) |
| Frontend | React 19, Vite 8, React Bootstrap, Ant Design, React Query |
| Database | Microsoft SQL Server |
| Object Storage | MinIO (S3-compatible) |
| Authentication | JWT (HS256) + OTP via Email + CSRF (HttpOnly cookies) |
| Payment | VNPay Sandbox |

## Prerequisites

- **Java 21** and Maven
- **Node.js 18+** and npm
- **SQL Server** running on `localhost:1433` with database `Auction_System_DB`
- **Docker Desktop** (for MinIO image storage)

## Getting Started

### 1. Start MinIO (Object Storage)

```bash
cd Auction_System_BE
docker compose -f docker-compose.storage.yml up -d
```

MinIO Console: http://localhost:9001 (login: `minioadmin` / `minioadmin`)

### 2. Start the Backend

```bash
cd Auction_System_BE
mvn spring-boot:run
```

Runs on http://localhost:8080. The database seeder runs automatically on first startup.

### 3. Start the Frontend

```bash
cd Auction_System_FE
npm install
npm run dev
```

Runs on http://localhost:5173

## Project Structure

```
Auction_System_BE/
├── src/main/java/hoang/com/auction_system_be/
│   ├── config/          # Security, CORS, storage, validation configs
│   ├── controller/      # REST controllers (18 endpoints)
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities (17 tables)
│   ├── enums/           # Domain enums
│   ├── event/           # Spring Application Events
│   ├── exception/       # Custom exceptions and error codes
│   ├── mapper/          # Entity <-> DTO mappers
│   ├── repository/      # Spring Data JPA repositories
│   ├── scheduler/       # Scheduled tasks (session lifecycle)
│   ├── service/         # Business logic services
│   └── validator/       # Custom validation annotations
├── docker-compose.storage.yml
└── pom.xml

Auction_System_FE/
├── src/
│   ├── api/             # API client modules
│   ├── context/         # React context (AppContext)
│   ├── features/        # Feature modules
│   │   ├── auth/        # Login, Register, OTP verification
│   │   ├── auction/     # Live auction room, bidding
│   │   ├── catalog/     # Home, catalog, product detail
│   │   ├── staff/       # Admin/staff management pages
│   │   └── user/        # Account, items, earnings
│   ├── layouts/         # Main and Admin layouts
│   ├── router/          # Route configuration
│   └── utils/           # Utilities (image resolver, etc.)
└── package.json
```

## Features

### Bidding & Auctions
- Real-time bidding via WebSocket/STOMP with live bid updates
- Automatic bidding with configurable max bid amounts
- Anti-snipe protection (automatic time extension on late bids)
- Suspicious bid detection
- Idempotent bid submissions (prevents duplicates)

### Item Management
- Seller item submission with image upload
- Staff approval/rejection workflow with reasons
- Item lifecycle: PENDING > APPROVED > ACTIVE > SOLD > PAID > SHIPPING > DELIVERED

### Session Management
- Automated session scheduling via background scheduler
- Session lifecycle: SCHEDULED > ACTIVE > ENDED > PAID
- Reserve price enforcement
- Session creation, postponement, and cancellation

### Payment
- VNPay sandbox integration
- Automatic winner payment record creation
- Payment status tracking (PENDING > PAID / FAILED)

### User & Roles
- Three roles: User, Auction Manager (Staff), Admin
- OTP-based login (passwordless)
- JWT access token (24h) + refresh token (7 days, HttpOnly cookie)
- CSRF protection
- Account lockout after 5 failed attempts

### Staff Dashboard
- Item approval/rejection with filters and search
- Auction session management
- Bid monitoring with suspicious activity flags
- Dispute management
- User management

### Reporting
- Earning reports for sellers (daily/monthly charts)
- Revenue and transaction tracking
- Excel and PDF export
- Admin analytics dashboard

## API Documentation

Once the backend is running, access the Swagger UI at:

http://localhost:8080/swagger-ui.html

## Configuration

Key settings in `Auction_System_BE/src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `app.storage.bucket` | `auction-images` | MinIO bucket name |
| `app.storage.endpoint` | `http://localhost:9000` | MinIO endpoint |
| `app.jwt.expiration-ms` | `86400000` | Access token expiry (24h) |
| `app.jwt.refresh-token.expiration-ms` | `604800000` | Refresh token expiry (7 days) |
| `app.security.lockout.max-attempts` | `5` | Failed login attempts before lockout |
| `app.seeder.enabled` | `true` | Seed database on startup |

## License

Academic project for SBA301 course.
