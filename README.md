# Medical Store POS System

## 🎥 Project Demo Video

[![MediPOS Demo Video](https://img.youtube.com/vi/pzBrswZ8kcM/maxresdefault.jpg)](https://youtu.be/pzBrswZ8kcM)

---


## 📋 Table of Contents
- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Frontend Modules](#frontend-modules)
- [Role-Based Access Control](#role-based-access-control)
- [Setup Instructions](#setup-instructions)
- [Deployment](#deployment)
- [Development](#development)

---

## 🎯 Overview

A production-grade Point of Sale (POS) system designed specifically for medical stores and pharmacies. The system provides comprehensive functionality for billing, inventory management, medicine tracking, returns processing, and detailed reporting with GST compliance.

### Key Features
- **Billing & POS**: Real-time bill generation with GST calculation and PDF export
- **Inventory Management**: Batch tracking with expiry date monitoring and low stock alerts
- **Medicine Management**: Complete medicine catalog with barcode support (GTIN/EAN)
- **Returns Processing**: Customer return and refund management with audit trail
- **Reporting**: Sales reports, GST reports, and cashier performance analytics
- **Audit Logging**: Complete audit trail of all system activities with IP tracking
- **User Management**: Role-based access control with multiple user roles
- **PDF Generation**: Automatic bill PDF generation using iText 7
- **Barcode Scanning**: Support for barcode scanning in frontend using ZXing library
- **Login History**: Track user login/logout events
- **User Activity**: Monitor all user actions across the system

---

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with JWT (io.jsonwebtoken 0.12.3)
- **API Documentation**: Swagger/OpenAPI 3 (springdoc-openapi 2.3.0)
- **PDF Generation**: iText 7 (8.0.2)
- **Build Tool**: Maven 3.9+
- **Utilities**: Lombok, MapStruct 1.5.5
- **Validation**: Jakarta Validation

### Frontend
- **Framework**: Angular 17
- **Language**: TypeScript 5.2
- **UI Library**: Bootstrap 5.3
- **Barcode Scanner**: ZXing Library 0.21.3
- **Build Tool**: Angular CLI 17
- **State Management**: RxJS 7.8

### Database
- **RDBMS**: PostgreSQL 12+
- **Connection Pool**: HikariCP
- **Migration**: SQL scripts in `DBQ/` directory

### Deployment Platforms
- Docker
- Railway (railway.json)
- Render (render.yaml)
- Vercel (vercel.json)
- Heroku (Procfile)

---

## 🏗 System Architecture

### Architecture Overview

![Medical Store POS System Architecture](Diagrams/System-Archietecture.png)






### Request Flow

1. **Client Request** → Angular frontend makes HTTP request via API service
2. **JWT Authentication** → JwtAuthenticationFilter validates JWT token
3. **Role Authorization** → SecurityConfig checks user permissions via @PreAuthorize
4. **Controller** → REST controller receives request (@RestController)
5. **Service Layer** → Business logic processing with transaction management
6. **Repository** → Data access via Spring Data JPA
7. **Database** → PostgreSQL executes query with connection pooling
8. **Audit Logging** → AuditService logs critical operations
9. **Response** → JSON response sent back to client

---

## 🗄 Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          USERS                                  │
│  ┌─────────────┬──────────────────────────────────────────┐    │
│  │ id (PK)     │ BIGSERIAL                                │    │
│  │ username    │ VARCHAR(50) UNIQUE                       │    │
│  │ password    │ VARCHAR(255) [BCrypt]                    │    │
│  │ email       │ VARCHAR(100) UNIQUE                      │    │
│  │ full_name   │ VARCHAR(100)                             │    │
│  │ role        │ VARCHAR(20) [ADMIN, CASHIER, etc.]      │    │
│  │ active      │ BOOLEAN                                   │    │
│  │ created_at  │ TIMESTAMP                                 │    │
│  │ updated_at  │ TIMESTAMP                                 │    │
│  └─────────────┴──────────────────────────────────────────┘    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ 1:N
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                        MEDICINES                                │
│  ┌─────────────┬──────────────────────────────────────────┐    │
│  │ id (PK)     │ BIGSERIAL                                │    │
│  │ name        │ VARCHAR(200)                             │    │
│  │ manufacturer│ VARCHAR(200)                              │    │
│  │ category    │ VARCHAR(100)                             │    │
│  │ barcode     │ VARCHAR(50) [GTIN/EAN]                   │    │
│  │ hsn_code    │ VARCHAR(20) UNIQUE                       │    │
│  │ gst_percent │ NUMERIC(5,2)                              │    │
│  │ presc_req   │ BOOLEAN                                   │    │
│  │ status      │ VARCHAR(20) [ACTIVE, DISCONTINUED]      │    │
│  │ version     │ BIGINT [Optimistic Locking]               │    │
│  │ created_at  │ TIMESTAMP                                 │    │
│  │ updated_at  │ TIMESTAMP                                 │    │
│  └─────────────┴──────────────────────────────────────────┘    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ 1:N
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                         BATCHES                                 │
│  ┌─────────────┬──────────────────────────────────────────┐    │
│  │ id (PK)     │ BIGSERIAL                                │    │
│  │ medicine_id │ BIGINT FK → medicines.id                  │    │
│  │ batch_num   │ VARCHAR(50)                               │    │
│  │ expiry_date │ DATE                                       │    │
│  │ purch_price │ NUMERIC(10,2)                              │    │
│  │ sell_price  │ NUMERIC(10,2)                              │    │
│  │ qty_avail   │ INTEGER                                    │    │
│  │ version     │ BIGINT [Optimistic Locking]               │    │
│  │ created_at  │ TIMESTAMP                                  │    │
│  │ updated_at  │ TIMESTAMP                                  │    │
│  └─────────────┴──────────────────────────────────────────┘    │
└───────────┬───────────────────────────────┬─────────────────────┘
            │                               │
            │ 1:N                           │ 1:N
            │                               │
┌───────────▼───────────┐   ┌───────────────▼─────────────────────┐
│    STOCK_BARCODES     │   │            BILLS                   │
│  ┌─────────────────┐  │   │  ┌─────────────────────────────┐  │
│  │ id (PK)         │  │   │  │ id (PK)                     │  │
│  │ batch_id (FK)   │  │   │  │ bill_number UNIQUE           │  │
│  │ barcode UNIQUE  │  │   │  │ bill_date                    │  │
│  │ sold            │  │   │  │ cashier_id (FK) → users.id   │  │
│  │ created_at      │  │   │  │ customer_name                │  │
│  │ updated_at      │  │   │  │ customer_phone               │  │
│  └─────────────────┘  │   │  │ subtotal                     │  │
└───────────────────────┘   │  │ total_gst                     │  │
                            │  │ total_amount                  │  │
                            │  │ payment_status                │  │
                            │  │ cancelled                     │  │
                            │  │ cancellation_reason           │  │
                            │  │ created_at                    │  │
                            │  │ updated_at                    │  │
                            │  └─────────────────────────────┘  │
                            └───────────┬─────────────────────────┘
                                        │
                                        │ 1:N
                                        │
                    ┌───────────────────┴───────────────────┐
                    │                                       │
        ┌───────────▼───────────┐   ┌───────────▼───────────┐
        │      BILL_ITEMS       │   │       PAYMENTS         │
        │  ┌─────────────────┐  │   │  ┌─────────────────┐   │
        │  │ id (PK)         │  │   │  │ id (PK)         │   │
        │  │ bill_id (FK)    │  │   │  │ bill_id (FK)    │   │
        │  │ medicine_id (FK)│  │   │  │ payment_ref     │   │
        │  │ batch_id (FK)   │  │   │  │ mode            │   │
        │  │ batch_number    │  │   │  │ amount          │   │
        │  │ quantity        │  │   │  │ status          │   │
        │  │ unit_price      │  │   │  │ payment_date    │   │
        │  │ gst_percentage  │  │   │  │ created_at      │   │
        │  │ gst_amount      │  │   │  └─────────────────┘   │
        │  │ total_amount    │  │   └─────────────────────────┘
        │  └─────────────────┘  │
        └───────────────────────┘
                                        │
                                        │ 1:N (via originalBill_id)
                                        │
                            ┌───────────▼───────────┐
                            │        RETURNS        │
                            │  ┌─────────────────┐  │
                            │  │ id (PK)         │  │
                            │  │ return_number   │  │
                            │  │ originalBill_id │  │
                            │  │ processedBy_id  │  │
                            │  │ return_date     │  │
                            │  │ refund_amount   │  │
                            │  │ reason          │  │
                            │  │ return_type      │  │
                            │  │ created_at      │  │
                            │  └─────────────────┘  │
                            └───────────┬───────────┘
                                        │
                                        │ 1:N
                                        │
                            ┌───────────▼───────────┐
                            │     RETURN_ITEMS      │
                            │  ┌─────────────────┐  │
                            │  │ id (PK)         │  │
                            │  │ return_id (FK)  │  │
                            │  │ medicine_id (FK)│  │
                            │  │ batch_id (FK)   │  │
                            │  │ batch_number    │  │
                            │  │ quantity        │  │
                            │  │ refund_amount   │  │
                            │  └─────────────────┘  │
                            └───────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      AUDIT_LOGS                                  │
│  ┌─────────────┬──────────────────────────────────────────┐    │
│  │ id (PK)     │ BIGSERIAL                                │    │
│  │ user_id (FK)│ BIGINT → users.id                        │    │
│  │ action      │ VARCHAR(50)                               │    │
│  │ entity_type │ VARCHAR(100)                              │    │
│  │ entity_id   │ VARCHAR(100)                              │    │
│  │ description │ TEXT                                       │    │
│  │ old_value   │ TEXT                                       │    │
│  │ new_value   │ TEXT                                       │    │
│  │ timestamp   │ TIMESTAMP                                  │    │
│  │ ip_address  │ VARCHAR(50)                                │    │
│  └─────────────┴──────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### Table Descriptions

#### 1. **users**
Stores all system users with authentication and authorization information.
- **Primary Key**: `id`
- **Unique Constraints**: `username`, `email`
- **Indexes**: `username`, `email`
- **Relationships**: Referenced by `bills.cashier_id`, `returns.processedBy_id`, `audit_logs.user_id`
- **Security**: Passwords stored as BCrypt hashes

#### 2. **medicines**
Master catalog of all medicines/products in the system.
- **Primary Key**: `id`
- **Unique Constraints**: `hsn_code`
- **Indexes**: `name`, `hsn_code`, `status`, `barcode`
- **Special Features**: Optimistic locking via `version` column
- **Relationships**: One-to-Many with `batches`

#### 3. **batches**
Tracks inventory batches with expiry dates and pricing.
- **Primary Key**: `id`
- **Foreign Keys**: `medicine_id` → `medicines.id`
- **Indexes**: `medicine_id`, `expiry_date`, `batch_number`, composite `(medicine_id, expiry_date)`
- **Special Features**: Optimistic locking via `version` column
- **Relationships**: Many-to-One with `medicines`, One-to-Many with `bill_items`, `return_items`, `stock_barcodes`

#### 4. **stock_barcodes**
Optional table for serial number/individual unit tracking.
- **Primary Key**: `id`
- **Foreign Keys**: `batch_id` → `batches.id`
- **Unique Constraints**: `barcode`
- **Indexes**: `barcode`, `batch_id`, `sold`
- **Relationships**: Many-to-One with `batches`

#### 5. **bills**
Main billing/transaction table.
- **Primary Key**: `id`
- **Unique Constraints**: `bill_number`
- **Foreign Keys**: `cashier_id` → `users.id`
- **Indexes**: `bill_number`, `bill_date`, `cashier_id`
- **Relationships**: One-to-Many with `bill_items`, `payments`, `returns`

#### 6. **bill_items**
Line items for each bill.
- **Primary Key**: `id`
- **Foreign Keys**: 
  - `bill_id` → `bills.id`
  - `medicine_id` → `medicines.id`
  - `batch_id` → `batches.id`
- **Indexes**: `bill_id`, `batch_id`
- **Relationships**: Many-to-One with `bills`, `medicines`, `batches`

#### 7. **payments**
Payment records for bills.
- **Primary Key**: `id`
- **Foreign Keys**: `bill_id` → `bills.id`
- **Unique Constraints**: `payment_reference`
- **Indexes**: `bill_id`, `payment_reference`, `payment_date`
- **Relationships**: Many-to-One with `bills`

#### 8. **returns**
Customer return transactions.
- **Primary Key**: `id`
- **Unique Constraints**: `return_number`
- **Foreign Keys**: 
  - `originalBill_id` → `bills.id`
  - `processedBy_id` → `users.id`
- **Indexes**: `originalBill_id`, `return_date`, `return_number`
- **Relationships**: One-to-Many with `return_items`

#### 9. **return_items**
Line items for returns.
- **Primary Key**: `id`
- **Foreign Keys**: 
  - `return_id` → `returns.id`
  - `medicine_id` → `medicines.id`
  - `batch_id` → `batches.id`
- **Indexes**: `return_id`, `batch_id`
- **Relationships**: Many-to-One with `returns`, `medicines`, `batches`

#### 10. **audit_logs**
System-wide audit trail.
- **Primary Key**: `id`
- **Foreign Keys**: `user_id` → `users.id`
- **Indexes**: `user_id`, `action`, `timestamp`
- **Relationships**: Many-to-One with `users`

### Key Design Decisions

1. **Optimistic Locking**: `medicines` and `batches` tables use `version` column to prevent concurrent update conflicts
2. **Barcode Strategy**: 
   - `medicines.barcode`: GTIN/EAN (product identifier, not unique per unit)
   - `stock_barcodes.barcode`: Serial numbers (unique per unit, optional)
3. **Soft Deletes**: Bills support cancellation via `cancelled` flag instead of hard deletion
4. **Audit Trail**: All critical operations are logged in `audit_logs` with before/after values and IP addresses
5. **Indexing Strategy**: Comprehensive indexes on foreign keys, search fields, and date columns for performance
6. **Connection Pooling**: HikariCP configured for optimal database connection management

---

## 🔌 API Endpoints

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | User login (returns JWT token) | No |
| POST | `/auth/logout` | User logout | Yes |

### Billing Endpoints (`/cashier/bills`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/cashier/bills` | Create new bill | CASHIER, ADMIN |
| GET | `/api/cashier/bills/{id}` | Get bill by ID | CASHIER, ADMIN |
| GET | `/api/cashier/bills/number/{billNumber}` | Get bill by bill number | CASHIER, ADMIN |
| GET | `/api/cashier/bills` | Get all bills (purchase history) | CASHIER, ADMIN |
| POST | `/api/cashier/bills/{id}/cancel` | Cancel bill | CASHIER, ADMIN |
| GET | `/api/cashier/bills/{id}/pdf` | Download bill PDF | CASHIER, ADMIN |

### Medicine Management Endpoints (`/pharmacist/medicines`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/pharmacist/medicines` | Create medicine | STOCK_KEEPER, ADMIN |
| GET | `/api/pharmacist/medicines/{id}` | Get medicine by ID | STOCK_KEEPER, ADMIN |
| GET | `/api/pharmacist/medicines` | Get all medicines | STOCK_KEEPER, ADMIN |
| GET | `/api/pharmacist/medicines/search?name={name}` | Search medicines | STOCK_KEEPER, ADMIN |
| GET | `/api/pharmacist/medicines/barcode/{barcode}` | Get medicine by barcode | STOCK_KEEPER, ADMIN |
| PUT | `/api/pharmacist/medicines/{id}` | Update medicine | STOCK_KEEPER, ADMIN |
| PATCH | `/api/pharmacist/medicines/{id}/status?status={status}` | Update medicine status | STOCK_KEEPER, ADMIN |

### Batch Management Endpoints (`/pharmacist/batches`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/pharmacist/batches` | Create batch | STOCK_KEEPER, ADMIN |
| GET | `/api/pharmacist/batches/medicine/{medicineId}` | Get batches for medicine | STOCK_KEEPER, ADMIN |
| GET | `/api/pharmacist/batches/expired` | Get expired batches | STOCK_KEEPER, ADMIN |
| GET | `/api/pharmacist/batches/low-stock?threshold={threshold}` | Get low stock batches | STOCK_KEEPER, ADMIN |
| PUT | `/api/pharmacist/batches/{id}` | Update batch | STOCK_KEEPER, ADMIN |
| PATCH | `/api/pharmacist/batches/{id}/stock` | Update stock quantity | STOCK_KEEPER, ADMIN |

### Returns Endpoints (`/cashier/returns`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/cashier/returns` | Process return | CUSTOMER_SUPPORT, ADMIN |

### Reports Endpoints (`/admin/reports`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/admin/reports/sales?startDate={date}&endDate={date}` | Sales report | ANALYST, MANAGER, ADMIN |
| GET | `/api/admin/reports/gst?startDate={date}&endDate={date}` | GST report | ANALYST, MANAGER, ADMIN |
| GET | `/api/admin/reports/cashier/{cashierId}?startDate={date}&endDate={date}` | Cashier performance | ANALYST, MANAGER, ADMIN |
| GET | `/api/admin/reports/stock` | Stock report | ANALYST, MANAGER, ADMIN |

### User Management Endpoints (`/admin/users`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/admin/users` | Get all users | ADMIN |
| GET | `/api/admin/users/{id}` | Get user by ID | ADMIN |
| POST | `/api/admin/users` | Create user | ADMIN |
| PUT | `/api/admin/users/{id}` | Update user | ADMIN |
| PATCH | `/api/admin/users/{id}/status` | Update user status | ADMIN |
| POST | `/api/admin/users/{id}/change-password` | Change user password | ADMIN |

### Audit Logs Endpoints (`/admin/audit-logs`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/admin/audit-logs` | Get audit logs | ADMIN |
| GET | `/api/admin/audit-logs/user/{userId}` | Get logs by user | ADMIN |

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 🎨 Frontend Modules

### Module Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── auth/                    # Authentication module
│   │   │   ├── login/               # Login component
│   │   │   ├── unauthorized/       # Unauthorized access component
│   │   │   ├── auth.guard.ts        # Authentication guard
│   │   │   ├── role.guard.ts        # Role-based access guard
│   │   │   ├── token.interceptor.ts # JWT token interceptor
│   │   │   └── auth.service.ts      # Authentication service
│   │   │
│   │   ├── core/                    # Core shared components
│   │   │   ├── components/          # Reusable components
│   │   │   │   ├── app-shell/       # Main app shell
│   │   │   │   ├── dialog/          # Dialog component
│   │   │   │   └── svg-icon/        # SVG icon component
│   │   │   ├── constants/           # API endpoints
│   │   │   ├── models/              # TypeScript models
│   │   │   ├── pipes/               # Custom pipes
│   │   │   └── services/            # Core services
│   │   │       ├── api.service.ts   # Base API service
│   │   │       ├── auth.service.ts  # Auth service
│   │   │       ├── billing.service.ts
│   │   │       ├── inventory.service.ts
│   │   │       ├── report.service.ts
│   │   │       ├── return.service.ts
│   │   │       ├── user.service.ts
│   │   │       └── audit-log.service.ts
│   │   │
│   │   └── modules/                 # Feature modules
│   │       ├── billing/             # Billing/POS module
│   │       ├── inventory/           # Inventory monitoring
│   │       ├── medicines/           # Medicine management
│   │       ├── returns/             # Returns processing
│   │       ├── reports/             # Reports & analytics
│   │       ├── purchase-history/    # Purchase history
│   │       ├── user-activity/       # User activity logs
│   │       ├── login-history/       # Login history
│   │       └── user-management/     # User management
```

### Route Configuration

| Route | Module | Accessible Roles |
|-------|--------|------------------|
| `/auth/login` | Authentication | Public |
| `/billing` | Billing | CASHIER, ADMIN |
| `/inventory` | Inventory | STOCK_MONITOR, ADMIN |
| `/medicines` | Medicines | STOCK_KEEPER, ADMIN |
| `/returns` | Returns | CUSTOMER_SUPPORT, ADMIN |
| `/reports` | Reports | ANALYST, MANAGER, ADMIN |
| `/purchase-history` | Purchase History | MANAGER, ADMIN |
| `/user-activity` | User Activity | ADMIN |
| `/login-history` | Login History | ADMIN |
| `/user-management` | User Management | ADMIN |

---

## 🔐 Role-Based Access Control

### User Roles

| Role | Description | Accessible Modules |
|------|-------------|-------------------|
| **ADMIN** | System Administrator | All modules (full access) |
| **CASHIER** | Point of Sale Operator | Billing only |
| **STOCK_MONITOR** | Inventory Monitor | Inventory monitoring only |
| **STOCK_KEEPER** | Medicine Manager | Medicine & batch management |
| **CUSTOMER_SUPPORT** | Returns Handler | Returns processing only |
| **ANALYST** | Data Analyst | Reports only |
| **MANAGER** | Store Manager | Reports + Purchase History |

### Default Users

All default users have password: `password123`

| Username | Role | Email |
|----------|------|-------|
| `admin` | ADMIN | admin@medicalstore.com |
| `cashier` | CASHIER | cashier@medicalstore.com |
| `stockmonitor` | STOCK_MONITOR | stockmonitor@medicalstore.com |
| `stockkeeper` | STOCK_KEEPER | stockkeeper@medicalstore.com |
| `customersupport` | CUSTOMER_SUPPORT | customersupport@medicalstore.com |
| `analyst` | ANALYST | analyst@medicalstore.com |
| `manager` | MANAGER | manager@medicalstore.com |

### Security Features

1. **JWT Authentication**: Token-based authentication with 24-hour expiration
2. **Password Encryption**: BCrypt hashing for all passwords (10 rounds)
3. **Role-Based Authorization**: Route and API-level access control via @PreAuthorize
4. **Audit Logging**: All critical operations are logged with IP addresses
5. **Token Interceptor**: Automatic token injection in HTTP requests
6. **Route Guards**: Frontend route protection based on roles (AuthGuard + RoleGuard)
7. **CORS Configuration**: Configurable CORS settings for cross-origin requests
8. **Security Headers**: Spring Security default security headers enabled

---

## 🚀 Setup Instructions

### Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: 18.x or higher
- **PostgreSQL**: 12.x or higher
- **Maven**: 3.6+ (optional, wrapper included)
- **Angular CLI**: 17.x (install via `npm install -g @angular/cli@17`)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd MediPOS
   ```

2. **Configure Database**
   - Create PostgreSQL database:
     ```sql
     CREATE DATABASE medical_store_pos;
     ```
   - Update `src/main/resources/application.yml`:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://localhost:5432/medical_store_pos
         username: your_username
         password: your_password
     ```

3. **Initialize Database Schema**
   ```bash
   # Run the SQL scripts in order:
   psql -U postgres -d medical_store_pos -f DBQ/create_all_tables.sql
   psql -U postgres -d medical_store_pos -f DBQ/create_all_users.sql
   ```

4. **Build and Run Backend**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   
   Backend will start on `http://localhost:8080`

5. **Verify Backend**
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Health check: `http://localhost:8080/api/auth/login`

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Configure API Endpoint**
   - Update `src/environments/environment.ts`:
     ```typescript
     export const environment = {
       production: false,
       apiUrl: 'http://localhost:8080/api'
     };
     ```

3. **Run Development Server**
   ```bash
   npm start
   # or
   ng serve
   ```
   
   Frontend will start on `http://localhost:4200`

4. **Build for Production**
   ```bash
   npm run build
   ```
   
   Output will be in `dist/medical-store-pos/`

### Environment Variables

#### Backend (`application.yml` or environment variables)

```yaml
# Database
DATABASE_URL: jdbc:postgresql://localhost:5432/medical_store_pos
DATABASE_USERNAME: postgres
DATABASE_PASSWORD: postgres

# JWT
JWT_SECRET: MedicalStorePOSSecretKeyForJWTTokenGeneration2024Production
JWT_EXPIRATION: 86400000  # 24 hours in milliseconds

# Server
PORT: 8080

# Hibernate
DDL_AUTO: validate  # or 'none' for production
SHOW_SQL: false
```

#### Frontend (`environment.ts`)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## 📦 Deployment

### Backend Deployment

#### Option 1: Docker
```bash
docker build -t medical-store-pos-backend .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/db \
  -e DATABASE_USERNAME=user \
  -e DATABASE_PASSWORD=pass \
  -e JWT_SECRET=your-secret-key \
  medical-store-pos-backend
```

#### Option 2: JAR File
```bash
mvn clean package
java -jar target/pos-backend-1.0.0.jar
```

#### Option 3: Railway
- Use `railway.json` configuration file
- Set environment variables in Railway dashboard
- Deploy from Git repository
- Railway will automatically detect Java and build

#### Option 4: Render
- Use `render.yaml` configuration file
- Set environment variables in Render dashboard
- Deploy from Git repository

#### Option 5: Heroku
- Use `Procfile` for process definition
- Set environment variables in Heroku dashboard
- Deploy using Git or Heroku CLI

### Frontend Deployment

#### Option 1: Static Hosting (Vercel/Netlify)
```bash
cd frontend
npm run build
# Deploy dist/medical-store-pos/ folder to hosting platform
```

#### Option 2: Vercel
- Use `vercel.json` configuration file
- Connect Git repository
- Vercel will automatically build and deploy

#### Option 3: Docker
```bash
cd frontend
# Create Dockerfile for Angular app
docker build -t medical-store-pos-frontend .
docker run -p 80:80 medical-store-pos-frontend
```

### Production Checklist

- [ ] Set `DDL_AUTO=validate` or `none` in production
- [ ] Use strong `JWT_SECRET` (minimum 256 bits, random)
- [ ] Configure HTTPS/TLS
- [ ] Set up database backups
- [ ] Configure CORS for production domain
- [ ] Enable production logging
- [ ] Set up monitoring and alerts
- [ ] Review and harden security settings
- [ ] Update frontend `environment.prod.ts` with production API URL
- [ ] Test all critical workflows in production environment

---

## 💻 Development

### Project Structure

```
MediPOS/
├── DBQ/                          # Database scripts
│   ├── create_all_tables.sql     # Schema creation
│   ├── create_all_users.sql      # Default users
│   └── ...                       # Migration/fix scripts
├── frontend/                     # Angular frontend
│   ├── src/
│   │   ├── app/                  # Application code
│   │   ├── environments/        # Environment configs
│   │   └── styles/               # Global styles
│   ├── angular.json              # Angular config
│   └── package.json              # Dependencies
├── src/                          # Java backend
│   └── main/
│       ├── java/
│       │   └── com/medicalstore/pos/
│       │       ├── config/       # Configuration classes
│       │       ├── controller/   # REST controllers
│       │       ├── dto/          # Data Transfer Objects
│       │       ├── entity/       # JPA entities
│       │       ├── exception/    # Exception handlers
│       │       ├── repository/   # JPA repositories
│       │       ├── security/     # Security config
│       │       └── service/      # Business logic
│       └── resources/
│           └── application.yml   # Application config
├── pom.xml                       # Maven config
├── Dockerfile                    # Docker config
├── Procfile                      # Heroku config
├── railway.json                  # Railway config
├── render.yaml                   # Render config
└── vercel.json                   # Vercel config
```

### Building the Project

#### Backend
```bash
# Clean and build
mvn clean install

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Build JAR
mvn clean package
```

#### Frontend
```bash
cd frontend

# Install dependencies
npm install

# Development server
npm start

# Build for production
npm run build

# Run tests
npm test

# Lint code
npm run lint
```

### Database Migrations

Database schema is managed via SQL scripts in the `DBQ/` directory:

1. **Initial Setup**: Run `create_all_tables.sql` and `create_all_users.sql`
2. **Migrations**: Additional scripts for schema updates
3. **Fixes**: Scripts for data fixes and constraint updates

### Code Style

- **Backend**: Follow Java naming conventions, use Lombok for boilerplate reduction
- **Frontend**: Follow Angular style guide, use TypeScript strict mode
- **Database**: Use snake_case for table and column names

### Testing

- **Backend**: JUnit tests with Spring Boot Test
- **Frontend**: Angular testing utilities (Jasmine/Karma)
- **Integration**: Test API endpoints via Swagger UI or Postman

---

## 📊 Database Indexes Summary

### Performance Indexes

| Table | Index Name | Columns | Purpose |
|-------|-----------|---------|---------|
| users | idx_username | username | Fast login lookup |
| users | idx_email | email | Email uniqueness check |
| medicines | idx_medicine_name | name | Search by name |
| medicines | idx_medicine_hsn | hsn_code | HSN code lookup |
| medicines | idx_medicine_barcode | barcode | Barcode scanning |
| medicines | idx_medicine_status | status | Status filtering |
| batches | idx_batch_medicine | medicine_id | Medicine batch lookup |
| batches | idx_batch_expiry | expiry_date | Expiry date queries |
| batches | idx_batch_number | batch_number | Batch number lookup |
| batches | idx_batch_medicine_expiry | medicine_id, expiry_date | Composite query optimization |
| bills | idx_bill_number | bill_number | Bill lookup |
| bills | idx_bill_date | bill_date | Date range queries |
| bill_items | idx_bill_item_bill | bill_id | Bill items retrieval |
| payments | idx_payment_bill | bill_id | Payment lookup |
| audit_logs | idx_audit_user | user_id | User activity queries |
| audit_logs | idx_audit_date | timestamp | Time-based queries |

---

## 🔍 Key Features Implementation

### 1. Optimistic Locking
- Prevents concurrent update conflicts
- Uses `@Version` annotation on `medicines` and `batches` entities
- Throws `OptimisticLockException` on conflict
- Frontend handles conflicts gracefully

### 2. Batch Expiry Management
- Automatic expiry detection via `Batch.isExpired()` method
- Low stock alerts via threshold queries
- FEFO (First Expired First Out) support in batch selection
- Expiry date validation on batch creation

### 3. GST Calculation
- Per-item GST calculation based on `gst_percentage`
- Total GST aggregation across all items
- GST reports for tax compliance
- HSN code tracking for each medicine

### 4. PDF Bill Generation
- Automatic PDF generation using iText 7
- Includes all bill details, items, and payment info
- Downloadable bill PDFs via `/api/cashier/bills/{id}/pdf`
- Professional bill formatting

### 5. Audit Trail
- Comprehensive logging of:
  - User actions (CREATE, UPDATE, DELETE)
  - Entity changes (before/after values as JSON)
  - IP addresses
  - Timestamps
- Immutable audit log records
- Queryable by user, action, or date range

### 6. Barcode Support
- Product-level barcodes (GTIN/EAN) in medicines table
- Optional serial number tracking via stock_barcodes table
- Frontend barcode scanning using ZXing library
- Barcode search functionality

### 7. Returns Processing
- Return items linked to original bill
- Automatic stock restoration on return
- Refund amount calculation
- Return type tracking (FULL, PARTIAL)

### 8. Reporting
- Sales reports with date range filtering
- GST reports for tax compliance
- Cashier performance reports
- Stock reports with expiry and low stock alerts

---

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify PostgreSQL is running
   - Check database credentials in `application.yml`
   - Ensure database exists

2. **JWT Token Expired**
   - Token expires after 24 hours
   - Re-login to get new token
   - Check `JWT_EXPIRATION` setting

3. **CORS Errors**
   - Configure CORS in `SecurityConfig`
   - Ensure frontend URL is whitelisted
   - Check browser console for specific errors

4. **Optimistic Locking Exception**
   - Occurs when concurrent updates happen
   - Refresh data and retry operation
   - Consider implementing retry logic

5. **Build Errors**
   - Clean and rebuild: `mvn clean install`
   - Check Java version (must be 17+)
   - Verify all dependencies are downloaded

---



---

## 📞 Support

For issues and questions, please contact: [jerophinstanley47@gmail.com]

---

**Last Updated**: 2026
