# 🛒 Limited Stock Drop

A high-concurrency reservation system for limited-stock product drops. Built to handle 100+ simultaneous users competing for the same limited inventory without overselling.

[![Live Demo](https://img.shields.io/badge/Live-Demo-brightgreen)](https://lsd-reservation-frontend.onrender.com/products)

## 📋 Table of Contents

- [Overview](#overview)
- [Live Demo](#live-demo)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Database Schema](#database-schema)
- [Concurrency & Race Condition Handling](#concurrency--race-condition-handling)
- [Reservation Flow](#reservation-flow)
- [API Documentation](#api-documentation)
- [Frontend Features](#frontend-features)
- [Security](#security)
- [Observability](#observability)
- [Testing](#testing)
- [Deployment](#deployment)
- [Local Development](#local-development)
- [Trade-offs & Design Decisions](#trade-offs--design-decisions)
- [Scaling Considerations](#scaling-considerations)

---

## Overview

### The Problem

Imagine a limited-edition sneaker drop with only 50 pairs available and 1,000 users trying to buy simultaneously. Without proper concurrency control:

- ❌ Stock goes negative (overselling)
- ❌ Race conditions cause duplicate reservations
- ❌ Users lose items they thought they secured
- ❌ Abandoned carts lock inventory forever

### The Solution

This system implements a **reservation-based checkout flow** with:

- ✅ **Pessimistic locking** to prevent overselling
- ✅ **5-minute reservation window** with automatic expiration
- ✅ **Atomic stock operations** using database transactions
- ✅ **Real-time countdown** showing reservation status
- ✅ **Automatic stock restoration** when reservations expire

---

## Live Demo

| Component | URL |
|-----------|-----|
| 🌐 Frontend | [https://lsd-frontend.onrender.com](https://lsd-reservation-frontend.onrender.com) |
| 🔧 Backend API | [https://lsd-backend.onrender.com](https://lsd-reservation-backend.onrender.com) |
| ❤️ Health Check | [/actuator/health](https://lsd-reservation-backend.onrender.com/actuator/health) |

### Test Credentials

| Username  | Password | Role |
|-----------|----------|------|
| johndoe   | John_01secure! | User |
| janesmith | Jane_010203 | User |
| charlieb  | Charlie_001 | User |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                           FRONTEND                                  │
│                     React + TypeScript + Vite                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐  │
│  │   Products  │  │ Reservation │  │  Checkout   │  │    Auth    │  │
│  │    Page     │  │    Timer    │  │    Flow     │  │   Context  │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTPS + JWT + HTTP-Only Cookies
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                           BACKEND                                    │
│                    Spring Boot 4.0.4 (Java 21)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────┐   │
│  │   Product   │  │ Reservation │  │    Order    │  │    Auth    │   │
│  │  Controller │  │  Controller │  │  Controller │  │ Controller │   │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └─────┬──────┘   │
│         │                │                │                │         │
│  ┌──────▼──────────────────────────────────────────────────▼──────┐  │
│  │                      SERVICE LAYER                             │  │
│  │   • Transactional business logic                               │  │
│  │   • Pessimistic locking for stock operations                   │  │
│  │   • Automatic reservation expiry (scheduled task)              │  │
│  └─────────────────────────┬──────────────────────────────────────┘  │
│                            │                                         │
│  ┌─────────────────────────▼──────────────────────────────────────┐  │
│  │                    REPOSITORY LAYER                            │  │
│  │              Spring Data JPA + Hibernate                       │  │
│  └─────────────────────────┬──────────────────────────────────────┘  │
└────────────────────────────┼─────────────────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   PostgreSQL    │  │  Redis Cloud    │  │   Scheduler     │
│   (Primary DB)  │  │ (Token Cache)   │  │ (Cron Jobs)     │
│                 │  │                 │  │                 │
│ • Users         │  │ • Blacklisted   │  │ • Expire        │
│ • Products      │  │   JWT tokens    │  │   reservations  │
│ • Reservations  │  │ • Rate limiting │  │ • Cleanup       │
│ • Orders        │  │   (future)      │  │   tokens        │
│ • InventoryLogs │  │                 │  │                 │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 4.0.4 | Framework |
| Spring Security | 6.x | Authentication & Authorization |
| Spring Data JPA | 3.x | ORM & Repository |
| PostgreSQL | 16 | Primary Database |
| Redis | 7.x | Token Blacklisting |
| Hibernate | 6.x | JPA Implementation |
| JWT (jjwt) | 0.12.x | Token-based Auth |
| Lombok | 1.18.x | Boilerplate Reduction |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19 | UI Framework |
| TypeScript | 5.9 | Type Safety |
| Vite | 8 | Build Tool |
| Tailwind CSS | 4.2 | Styling |
| React Query | 5.x | Server State Management |
| React Router | 7.x | Routing |
| Axios | 1.x | HTTP Client |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Local Orchestration |
| Render | Cloud Deployment |
| Redis Cloud | Managed Redis |

---

## Database Schema

### Entity Relationship Diagram

```
┌───────────────┐       ┌───────────────────┐       ┌───────────────┐
│     USERS     │       │    RESERVATIONS   │       │   PRODUCTS    │
├───────────────┤       ├───────────────────┤       ├───────────────┤
│ id (PK)       │──┐    │ id (PK)           │    ┌──│ id (PK)       │
│ username      │  │    │ user_id (FK)      │────┘  │ version       │◄── Optimistic Lock
│ email         │  └───►│ product_id (FK)   │───────│ name          │
│ password      │       │ quantity          │       │ price         │
│ role          │       │ status            │       │ total_stock   │
│ active        │       │ expires_at        │       │ reserved_stock│
└───────────────┘       │ created_at        │       │ category      │
        │               └───────────────────┘       └───────────────┘
        │                        │                          │
        │                        │                          │
        ▼                        ▼                          ▼
┌───────────────┐       ┌───────────────────┐       ┌───────────────┐
│ REFRESH_TOKENS│       │      ORDERS       │       │ INVENTORY_LOGS│
├───────────────┤       ├───────────────────┤       ├───────────────┤
│ id (PK)       │       │ id (PK)           │       │ id (PK)       │
│ token         │       │ reservation_id(FK)│       │ product_id(FK)│
│ user_id (FK)  │       │ user_id (FK)      │       │ reservation_id│
│ expires_at    │       │ product_id (FK)   │       │ order_id      │
│ revoked       │       │ quantity          │       │ action        │
│ ip_address    │       │ total_price       │       │ quantity_change│
│ user_agent    │       │ status            │       │ stock_before  │
└───────────────┘       └───────────────────┘       │ stock_after   │
                                                    │ description   │
                                                    └───────────────┘
```

### Schema Design Decisions

#### 1. Separate `reserved_stock` Column
```sql
-- Instead of calculating on-the-fly:
SELECT total_stock - COUNT(*) FROM reservations WHERE status = 'PENDING'

-- We maintain a dedicated column:
reserved_stock INTEGER NOT NULL DEFAULT 0
available_stock = total_stock - reserved_stock
```
**Why?** Avoids expensive JOINs on every stock check. Single column update with row-level lock.

#### 2. Version Column for Optimistic Locking
```java
@Version
private Long version;
```
**Why?** Detects concurrent modifications. If two transactions try to update the same product, one will fail with `OptimisticLockException`.

#### 3. Inventory Audit Log
```sql
CREATE TABLE inventory_logs (
    action VARCHAR(30),      -- STOCK_RESERVED, STOCK_RELEASED, ORDER_CONFIRMED
    quantity_change INTEGER,
    stock_before INTEGER,
    stock_after INTEGER
);
```
**Why?** Complete audit trail for debugging, compliance, and analytics.

---

## Concurrency & Race Condition Handling

### The Problem: Two Users, One Item

```
Time    User A                    User B                    Stock
────────────────────────────────────────────────────────────────────
T1      Read stock = 1            Read stock = 1            1
T2      Stock > 0? ✅             Stock > 0? ✅              1
T3      Reserve item              Reserve item              ???
T4      Stock = 1                 Stock = 0 ❌ OVERSOLD!
```

### Solution: Pessimistic Locking with SELECT FOR UPDATE

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithLock(@Param("id") Long id);
```

```
Time    User A                    User B                    Stock
────────────────────────────────────────────────────────────────────
T1      SELECT FOR UPDATE         (blocked, waiting)        1
        → Acquires row lock
T2      Check stock = 1 ✅        (still waiting...)        1
T3      UPDATE reserved_stock     (still waiting...)        0
T4      COMMIT, release lock      Lock acquired             0
T5                                Check stock = 0 ❌
T6                                Return "Out of stock"
```

### Implementation in Code

```java
@Transactional(rollbackFor = Exception.class)
public ReservationResponse createReservation(Long userId, CreateReservationRequest request) {
   log.info("[RESERVATION-SERVICE] Creating reservation for user {} and product {}", userId, request.getProductId());

   User userRef = userRepository.getReferenceById(userId);

   // Lock the product row to prevent concurrent modifications
   Product product = productQueryService.findByIdWithLockOrThrow(request.getProductId());

   // Prevent duplicate reservations
   boolean hasActive = reservationRepository.hasActiveReservation(userId, request.getProductId(), LocalDateTime.now());
   if (hasActive) {
      log.warn("[RESERVATION-SERVICE] User {} already has active reservation for product {}", userId, request.getProductId());
      throw new ValidationException("You already have an active reservation for this product");
   }

   // Check stock availability
   if (!product.canReserve(request.getQuantity())) {
      log.warn("[RESERVATION-SERVICE] Insufficient stock for product {}. Available: {}, Requested: {}",
              product.getId(), product.getAvailableStock(), request.getQuantity());
      throw new ValidationException("Not enough stock available");
   }

   // Atomically reserve stock at database level
   int stockBefore = product.getAvailableStock();
   product.reserveStock(request.getQuantity());

   // Create reservation
   Reservation reservation = Reservation.builder()
           .product(product)
           .user(userRef)
           .quantity(request.getQuantity())
           .expiresAt(LocalDateTime.now().plusMinutes(5))
           .build();

   Reservation saved = reservationRepository.save(reservation);

   // Log the inventory change
   InventoryLog inventoryLog = InventoryLog.forReservation(product, saved, stockBefore);
   inventoryLogRepository.save(inventoryLog);

   log.info("[RESERVATION-SERVICE] Reservation {} created successfully for user {}", saved.getId(), userId);
   return ReservationMapper.toResponse(saved);
}
```
---

## Reservation Flow

### State Machine

```
                    ┌─────────────────────────────────────┐
                    │                                     │
                    ▼                                     │
┌─────────┐    ┌─────────┐    ┌───────────┐    ┌────────────┐
│  START  │───►│ PENDING │───►│ CONFIRMED │    │  EXPIRED   │
└─────────┘    └─────────┘    └───────────┘    └────────────┘
     │              │                               ▲
     │              │         ┌───────────┐         │
     │              └────────►│ CANCELLED │         │
     │                        └───────────┘         │
     │                                              │
     └──────── (5 min timeout) ─────────────────────┘
```

### API Endpoints

#### POST /api/v1/reservations
Creates a new reservation, locking stock for 5 minutes.

```bash
curl -X POST https://api.example.com/api/v1/reservations \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'
```

Response:
```json
{
  "success": true,
  "message": "Reservation created successfully",
  "data": {
    "id": 123,
    "productId": 1,
    "productName": "Limited Edition Sneakers",
    "quantity": 2,
    "status": "PENDING",
    "expiresAt": "2024-01-15T10:35:00",
    "remainingSeconds": 300,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

#### POST /api/v1/reservations/{id}/checkout
Converts reservation to order.

```bash
curl -X POST https://api.example.com/api/v1/reservations/123/checkout \
  -H "Authorization: Bearer {token}"
```

### Automatic Expiration (Scheduled Task)

```java
@Scheduled(cron = "0 */1 * * * ?") // Every minute
@Transactional
public void expireReservations() {
   LocalDateTime now = LocalDateTime.now();
   log.debug("[RESERVATION-EXPIRY-SCHEDULER] Starting expiration check at {}", now);

   // Find all expired reservations
   List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(now);

   if (expiredReservations.isEmpty()) {
      log.debug("[RESERVATION-EXPIRY-SCHEDULER] No expired reservations found");
      return;
   }

   log.info("[RESERVATION-EXPIRY-SCHEDULER] Found {} expired reservations to process",
           expiredReservations.size());

   // Track statistics
   AtomicInteger successCount = new AtomicInteger(0);
   AtomicInteger failureCount = new AtomicInteger(0);

   // Process each expired reservation
   expiredReservations.forEach(reservation -> {
      try {
         processExpiredReservation(reservation);
         successCount.incrementAndGet();
      } catch (Exception e) {
         failureCount.incrementAndGet();
         log.error("[RESERVATION-EXPIRY-SCHEDULER] Failed to expire reservation {}: {}",
                 reservation.getId(), e.getMessage(), e);
      }
   });

   log.info("[RESERVATION-EXPIRY-SCHEDULER] Expiration completed. Success: {}, Failed: {}",
           successCount.get(), failureCount.get());
}
```

---

## API Documentation

### Authentication

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/auth/register` | POST | Register new user |
| `/api/v1/auth/login` | POST | Login, returns JWT |
| `/api/v1/auth/logout` | POST | Logout, blacklists token |
| `/api/v1/auth/refresh-token` | POST | Refresh access token |

### Products

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/products` | GET | List products (paginated, filtered) |
| `/api/v1/products/{id}` | GET | Get product details |
| `/api/v1/products/{id}/stock` | GET | Get real-time stock |
| `/api/v1/products` | POST | Create product (Admin) |
| `/api/v1/products/{id}` | PUT | Update product (Admin) |
| `/api/v1/products/{id}` | DELETE | Delete product (Admin) |

### Reservations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/reservations` | POST | Create reservation |
| `/api/v1/reservations/me` | GET | Get user's reservations |
| `/api/v1/reservations/{id}` | GET | Get reservation details |
| `/api/v1/reservations/{id}/checkout` | POST | Complete checkout |
| `/api/v1/reservations/{id}/cancel` | POST | Cancel reservation |

### Query Parameters

```
GET /api/v1/products?page=0&size=12&sortBy=price&sortDirection=asc&category=SNEAKERS&inStock=true&minPrice=100&maxPrice=500&search=nike
```

| Parameter | Type | Description |
|-----------|------|-------------|
| page | int | Page number (0-indexed) |
| size | int | Items per page |
| sortBy | string | name, price, createdAt |
| sortDirection | string | asc, desc |
| category | string | ELECTRONICS, SNEAKERS, FASHION, etc. |
| inStock | boolean | Filter by availability |
| minPrice | decimal | Minimum price |
| maxPrice | decimal | Maximum price |
| search | string | Search in name/description |

---

## Frontend Features

### Real-Time Stock Updates
```typescript
// Polls stock every 5 seconds
const { data: stock } = useQuery({
  queryKey: ['stock', productId],
  queryFn: () => productApi.getStock(productId),
  refetchInterval: 5000,
});
```

### Countdown Timer
```typescript
// Uses server-calculated remainingSeconds to avoid timezone issues
export const useCountdown = (initialSeconds: number) => {
  const [seconds, setSeconds] = useState(Math.max(0, initialSeconds));
  
  useEffect(() => {
    const interval = setInterval(() => {
      setSeconds(prev => Math.max(0, prev - 1));
    }, 1000);
    return () => clearInterval(interval);
  }, [initialSeconds]);
  
  return { seconds, isExpired: seconds === 0 };
};
```

### Optimistic UI Updates
```typescript
const reserveMutation = useMutation({
  mutationFn: reservationApi.create,
  onMutate: async (newReservation) => {
    // Cancel outgoing refetches
    await queryClient.cancelQueries(['stock', productId]);
    
    // Optimistically update stock
    queryClient.setQueryData(['stock', productId], (old) => ({
      ...old,
      availableStock: old.availableStock - newReservation.quantity
    }));
  },
  onError: (err, variables, context) => {
    // Rollback on error
    queryClient.setQueryData(['stock', productId], context.previousStock);
  }
});
```

---

## Security

### Authentication Flow

```
┌────────┐         ┌────────┐         ┌────────┐         ┌────────┐
│ Client │         │  API   │         │  JWT   │         │ Redis  │
└───┬────┘         └───┬────┘         └───┬────┘         └───┬────┘
    │   POST /login    │                  │                  │
    │─────────────────►│                  │                  │
    │                  │ Generate tokens  │                  │
    │                  │─────────────────►│                  │
    │                  │                  │                  │
    │  accessToken +   │                  │                  │
    │  refreshToken    │                  │                  │
    │◄─────────────────│                  │                  │
    │  (cookie)        │                  │                  │
    │                  │                  │                  │
    │ GET /products    │                  │                  │
    │ + Bearer token   │                  │                  │
    │─────────────────►│                  │                  │
    │                  │ Validate token   │                  │
    │                  │─────────────────►│                  │
    │                  │                  │ Check blacklist  │
    │                  │                  │─────────────────►│
    │                  │                  │     Not found    │
    │                  │                  │◄─────────────────│
    │                  │     Valid ✅     │                  │
    │                  │◄─────────────────│                  │
    │    Products      │                  │                  │
    │◄─────────────────│                  │                  │
```

### Security Features

| Feature | Implementation |
|---------|----------------|
| Password Hashing | BCrypt with strength 10 |
| JWT Tokens | Short-lived access (15min), long-lived refresh (7d) |
| Token Blacklisting | Redis-based for logout/revocation |
| HTTP-Only Cookies | Refresh token stored securely |
| CORS | Whitelist-based origin validation |
| Input Validation | Bean Validation (Jakarta) |
| SQL Injection | Parameterized queries via JPA |
| XSS Protection | React's built-in escaping |

---

## Observability

### Logging

```java
// Structured logging with context
log.info("[RESERVATION-SERVICE] Creating reservation | userId={}, productId={}, quantity={}", 
    userId, request.getProductId(), request.getQuantity());

log.error("[RESERVATION-SERVICE] Failed to create reservation | userId={}, error={}", 
    userId, e.getMessage());
```

### Health Check

```bash
GET /actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

### Metrics

```bash
GET /actuator/prometheus
```

Exposes metrics for:
- JVM memory/GC
- HTTP request latency
- Database connection pool
- Custom business metrics

---

## Testing

### Unit Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ReservationServiceImplTest
```

#### Test Coverage

| Service | Tests | Coverage |
|---------|-------|----------|
| ReservationServiceImpl | 26 | Stock locking, checkout, cancellation, expiration |
| ProductServiceImpl | 22 | CRUD, stock validation, delete guards |
| AuthenticationServiceImpl | 15 | Login, logout, token refresh |

### Test Factories

```java
// Clean test data creation
Product product = TestProductFactory.createDefaultProduct();
User user = TestUserFactory.createDefaultUser();
Reservation reservation = TestReservationFactory.createPendingReservation(user, product);
```

### Concurrency Test

```java
@Test
void shouldPreventOverselling_whenConcurrentReservations() throws Exception {
    // Given: Product with 1 unit in stock
    Product product = createProductWithStock(1);
    
    // When: 10 concurrent reservation attempts
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<Boolean>> futures = new ArrayList<>();
    
    for (int i = 0; i < 10; i++) {
        futures.add(executor.submit(() -> {
            try {
                reservationService.createReservation(userId, request);
                return true;
            } catch (InsufficientStockException e) {
                return false;
            }
        }));
    }
    
    // Then: Exactly 1 succeeds, 9 fail
    long successCount = futures.stream().filter(Future::get).count();
    assertThat(successCount).isEqualTo(1);
    assertThat(product.getReservedStock()).isEqualTo(1);
}
```

---

## Deployment

### Render Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                        RENDER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   Frontend   │  │   Backend    │  │   PostgreSQL     │  │
│  │ (Static Site)│  │ (Web Service)│  │   (Database)     │  │
│  │              │  │              │  │                  │  │
│  │  React/Vite  │  │ Spring Boot  │  │  Managed DB      │  │
│  │  via Nginx   │  │  Docker      │  │                  │  │
│  └──────────────┘  └──────┬───────┘  └──────────────────┘  │
└─────────────────────────────┼───────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │   Redis Cloud    │
                    │   (RedisLabs)    │
                    └──────────────────┘
```

### Environment Variables

```bash
# Backend
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<render-postgres-host>
DB_PORT=5432
DB_NAME=reservation_db
DB_USER=<user>
DB_PASSWORD=<password>
REDIS_HOST=<redislabs-host>
REDIS_PORT=<port>
REDIS_PASSWORD=<password>
JWT_SECRET=<base64-encoded-secret>
CORS_ALLOWED_ORIGINS=https://lsd-reservation-frontend.onrender.com

# Frontend
VITE_API_BASE_URL=https:https://lsd-reservation-backend.onrender.com
```

---

## Local Development

### Prerequisites

- Java 21+
- Node.js 22+
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker)
- Redis (or use cloud)

### Quick Start

```bash
# 1. Clone repository
git clone https://github.com/LastCoderBoy/Reservation_Stock_Validator.git
cd limited-stock-drop

# 2. Copy environment file
cp .env.example .env
# Edit .env with your values

# 3. Start with Docker Compose
docker compose up -d

# 4. Access the app
# Frontend: http://localhost:3000
# Backend:  http://localhost:8080
# API:      http://localhost:8080/api/v1
```

### Development Mode

```bash
# Backend (with hot reload)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend (with hot reload)
cd frontend
npm install
npm run dev
```

---

## Trade-offs & Design Decisions

### 1. Pessimistic vs Optimistic Locking

| Approach | Pros | Cons |
|----------|------|------|
| **Pessimistic (chosen)** | Guarantees no conflicts | Blocks concurrent reads |
| Optimistic | Better read throughput | Requires retry logic |

**Decision:** Pessimistic locking for reservation creation (stock is critical), optimistic locking (`@Version`) for product updates (less critical).

### 2. Separate Reserved Stock Column

**Trade-off:** Denormalization adds write overhead but eliminates expensive JOINs.

**Decision:** Acceptable trade-off for read-heavy stock checks (100:1 read/write ratio expected).

### 3. JWT + Redis vs Session-Based Auth

| Approach | Pros | Cons |
|----------|------|------|
| **JWT + Redis (chosen)** | Stateless, scalable | Redis dependency |
| Sessions | Simple | Sticky sessions needed |

**Decision:** JWT for scalability, Redis for token blacklisting (logout/revocation).

### 4. Polling vs WebSocket for Stock Updates

| Approach | Pros | Cons |
|----------|------|------|
| **Polling (chosen)** | Simple, reliable | Higher bandwidth |
| WebSocket | Real-time, efficient | Complex, connection management |

**Decision:** 5-second polling is sufficient for stock updates. WebSocket adds complexity without significant benefit.

---

## Scaling Considerations

### What Breaks at 10,000 Concurrent Users?

#### 1. Database Connection Pool
```
Current: 5 connections
Problem: 10k users → connection exhaustion
Solution: PgBouncer connection pooling, read replicas
```

#### 2. Single Database Lock Contention
```
Current: SELECT FOR UPDATE on single row
Problem: All users blocked waiting for same product
Solution: Distributed locking with Redis, inventory sharding
```

#### 3. Scheduler Single Instance
```
Current: Spring @Scheduled runs on one instance
Problem: Duplicate execution or missed execution
Solution: Distributed scheduler (ShedLock, Quartz cluster)
```

### Scaling Roadmap

```
┌─────────────────────────────────────────────────────────────────────┐
│                        LEVEL 1 (100 users)                          │
│                        Current Architecture                         │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        LEVEL 2 (1,000 users)                        │
│  • PgBouncer for connection pooling                                 │
│  • Redis caching for product catalog                                │
│  • CDN for static assets                                            │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        LEVEL 3 (10,000 users)                       │
│  • Read replicas for product queries                                │
│  • Redis distributed locks (Redisson)                               │
│  • Message queue for reservation processing                         │
│  • Horizontal scaling with load balancer                            │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        LEVEL 4 (100,000 users)                      │
│  • Database sharding by product category                            │
│  • Event sourcing for inventory                                     │
│  • Kubernetes for container orchestration                           │
│  • Global CDN + edge caching                                        │
└─────────────────────────────────────────────────────────────────────┘
```

### Recommended Improvements for Scale

1. **Queue-Based Reservations**
   ```
   User → API → Queue → Worker → Database
   ```
   Decouples request handling from database writes.

2. **Redis for Hot Products**
   ```java
   // Cache popular products in Redis
   @Cacheable(value = "products", key = "#id")
   public Product getProduct(Long id) { ... }
   ```

3. **Inventory Pre-allocation**
   ```
   Instead of: Check stock → Lock → Reserve
   Do: Pre-allocate inventory buckets → Claim from bucket
   ```

---

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

---

## License

MIT License - see [LICENSE](LICENSE) for details.

---

## Author

**Jasurbek Khamroev**

- GitHub: [@LastCoderBoy](https://github.com/LastCoderBoy)

---

<p align="center">
  Built with ☕ and determination
</p>
