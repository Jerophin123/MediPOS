# Medical Store POS System

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

---

## 🎯 Overview

A production-grade Point of Sale (POS) system designed specifically for medical stores and pharmacies. The system provides comprehensive functionality for billing, inventory management, medicine tracking, returns processing, and detailed reporting with GST compliance.

### Key Features
- **Billing & POS**: Real-time bill generation with GST calculation
- **Inventory Management**: Batch tracking with expiry date monitoring
- **Medicine Management**: Complete medicine catalog with barcode support
- **Returns Processing**: Customer return and refund management
- **Reporting**: Sales reports, GST reports, and cashier performance analytics
- **Audit Logging**: Complete audit trail of all system activities
- **User Management**: Role-based access control with multiple user roles
- **PDF Generation**: Automatic bill PDF generation

---

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with JWT
- **API Documentation**: Swagger/OpenAPI 3
- **PDF Generation**: iText 7
- **Build Tool**: Maven

### Frontend
- **Framework**: Angular 17
- **Language**: TypeScript 5.2
- **UI Library**: Bootstrap 5.3
- **Barcode Scanner**: ZXing Library
- **Build Tool**: Angular CLI

### Database
- **RDBMS**: PostgreSQL
- **Connection Pool**: HikariCP

---

## 🏗 System Architecture

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                            │
│                    (Angular 17 Frontend)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Billing  │  │Inventory │  │Medicines │  │ Reports  │  ...  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP/REST API
                            │ JWT Authentication
┌───────────────────────────▼─────────────────────────────────────┐
│                      API GATEWAY LAYER                          │
│              (Spring Security + JWT Filter)                     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                      APPLICATION LAYER                          │
│                    (Spring Boot REST API)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Controllers  │  │   Services   │  │    DTOs      │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Security   │  │ Audit Log    │  │   PDF Gen    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                      PERSISTENCE LAYER                          │
│              (Spring Data JPA / Hibernate)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Repositories │  │   Entities   │  │  Optimistic  │         │
│  │              │  │              │  │   Locking    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└───────────────────────────┬─────────────────────────────────────┘
                            │ JDBC
┌───────────────────────────▼─────────────────────────────────────┐
│                      DATABASE LAYER                             │
│                      (PostgreSQL)                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Tables     │  │   Indexes    │  │  Foreign     │         │
│  │              │  │              │  │   Keys       │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
```

### Request Flow

1. **Client Request** → Angular frontend makes HTTP request
2. **JWT Authentication** → Spring Security validates JWT token
3. **Role Authorization** → RoleGuard checks user permissions
4. **Controller** → REST controller receives request
5. **Service Layer** → Business logic processing
6. **Repository** → Data access via JPA
7. **Database** → PostgreSQL executes query
8. **Response** → JSON response sent back to client

---

## 🗄 Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          USERS                                  │
│  ┌─────────────┬──────────────────────────────────────────┐    │
│  │ id (PK)     │ BIGSERIAL                                │    │
│  │ username    │ VARCHAR(50) UNIQUE                       │    │
│  │ password    │ VARCHAR(255)                              │    │
│  │ email       │ VARCHAR(100) UNIQUE                       │    │
│  │ full_name   │ VARCHAR(100)                              │    │
│  │ role        │ VARCHAR(20) [ADMIN, CASHIER, etc.]       │    │
│  │ active      │ BOOLEAN                                    │    │
│  │ created_at  │ TIMESTAMP                                  │    │
│  │ updated_at  │ TIMESTAMP                                  │    │
│  └─────────────┴──────────────────────────────────────────┘    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            │ 1:N
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                        MEDICINES                                │
│  ┌─────────────┬──────────────────────────────────────────┐    │
│  │ id (PK)     │ BIGSERIAL                                │    │
│  │ name        │ VARCHAR(200)                              │    │
│  │ manufacturer│ VARCHAR(200)                              │    │
│  │ category    │ VARCHAR(100)                              │    │
│  │ barcode     │ VARCHAR(50) [GTIN/EAN]                   │    │
│  │ hsn_code    │ VARCHAR(20) UNIQUE                       │    │
│  │ gst_percent │ NUMERIC(5,2)                               │    │
│  │ presc_req   │ BOOLEAN                                    │    │
│  │ status      │ VARCHAR(20) [ACTIVE, DISCONTINUED]       │    │
│  │ version     │ BIGINT [Optimistic Locking]               │    │
│  │ created_at  │ TIMESTAMP                                  │    │
│  │ updated_at  │ TIMESTAMP                                  │    │
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
                            │  │ return_type     │  │
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
4. **Audit Trail**: All critical operations are logged in `audit_logs` with before/after values
5. **Indexing Strategy**: Comprehensive indexes on foreign keys, search fields, and date columns for performance

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

### User Management Endpoints (`/admin/users`)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/admin/users` | Get all users | ADMIN |
| GET | `/api/admin/users/{id}` | Get user by ID | ADMIN |
| POST | `/api/admin/users` | Create user | ADMIN |
| PUT | `/api/admin/users/{id}` | Update user | ADMIN |
| PATCH | `/api/admin/users/{id}/status` | Update user status | ADMIN |

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
│   │   │   └── token.interceptor.ts # JWT token interceptor
│   │   │
│   │   ├── core/                    # Core shared components
│   │   │   ├── components/          # Reusable components
│   │   │   ├── constants/           # API endpoints
│   │   │   ├── models/              # TypeScript models
│   │   │   ├── pipes/               # Custom pipes
│   │   │   └── services/            # Core services
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
2. **Password Encryption**: BCrypt hashing for all passwords
3. **Role-Based Authorization**: Route and API-level access control
4. **Audit Logging**: All critical operations are logged
5. **Token Interceptor**: Automatic token injection in HTTP requests
6. **Route Guards**: Frontend route protection based on roles

---

## 🚀 Setup Instructions

### Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: 18.x or higher
- **PostgreSQL**: 12.x or higher
- **Maven**: 3.6+ (optional, wrapper included)
- **Angular CLI**: 17.x

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd POS
   ```

2. **Configure Database**
   - Create PostgreSQL database:
     ```sql
     CREATE DATABASE medical_store_pos;
     ```
   - Update `Backend_DeployReady/src/main/resources/application.yml`:
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
DDL_AUTO: update  # or 'none' for production
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
cd Backend_DeployReady
docker build -t medical-store-pos-backend .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/db \
  -e DATABASE_USERNAME=user \
  -e DATABASE_PASSWORD=pass \
  medical-store-pos-backend
```

#### Option 2: JAR File
```bash
cd Backend_DeployReady
mvn clean package
java -jar target/pos-backend-1.0.0.jar
```

#### Option 3: Railway/Render
- Use `railway.json` or `render.yaml` configuration files
- Set environment variables in platform dashboard
- Deploy from Git repository

### Frontend Deployment

#### Option 1: Static Hosting (Vercel/Netlify)
```bash
cd frontend_DeployReady
npm run build
# Deploy dist/ folder to hosting platform
```

#### Option 2: Docker
```bash
cd frontend_DeployReady
docker build -t medical-store-pos-frontend .
docker run -p 80:80 medical-store-pos-frontend
```

### Production Checklist

- [ ] Set `DDL_AUTO=none` in production
- [ ] Use strong `JWT_SECRET` (minimum 256 bits)
- [ ] Configure HTTPS/TLS
- [ ] Set up database backups
- [ ] Configure CORS for production domain
- [ ] Enable production logging
- [ ] Set up monitoring and alerts
- [ ] Review and harden security settings

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
| batches | idx_batch_medicine | medicine_id | Medicine batch lookup |
| batches | idx_batch_expiry | expiry_date | Expiry date queries |
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
- Uses `@Version` annotation on `medicines` and `batches`
- Throws `OptimisticLockException` on conflict

### 2. Batch Expiry Management
- Automatic expiry detection via `Batch.isExpired()`
- Low stock alerts via threshold queries
- FEFO (First Expired First Out) support

### 3. GST Calculation
- Per-item GST calculation
- Total GST aggregation
- GST reports for tax compliance

### 4. PDF Bill Generation
- Automatic PDF generation using iText 7
- Includes all bill details, items, and payment info
- Downloadable bill PDFs

### 5. Audit Trail
- Comprehensive logging of:
  - User actions
  - Entity changes (before/after values)
  - IP addresses
  - Timestamps
- Immutable audit log records

---

## 📝 License

[Specify your license here]

---

## 👥 Contributors

[Add contributor information]

---

## 📞 Support

For issues and questions, please contact: [jerophinstanley47@gmail.com]

---

**Last Updated**: 2026

