# Policy Management Service – Architecture

## 1. Architecture Overview

The application follows Clean Architecture principles.

The primary objective is to keep business logic independent from frameworks, databases, and external infrastructure.

```text
                    ┌──────────────────────┐
                    │      API Layer       │
                    │ Controllers / DTOs   │
                    │ Exception Handling   │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Application Layer    │
                    │ Services / Use Cases │
                    │ Business Rules       │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    Domain Layer      │
                    │ Models / Enums       │
                    │ Repository Contracts │
                    └──────────▲───────────┘
                               │
                               │
                    ┌──────────┴───────────┐
                    │ Infrastructure Layer │
                    │ JPA / H2 / Flyway    │
                    │ Repository Adapters  │
                    └──────────────────────┘
```

## 2. Layer Responsibilities

### API Layer

Location:

```text
src/main/java/com/hcl/chubb/policy/api
```

Responsibilities:

* REST controllers
* Request and response DTOs
* Input validation
* HTTP-level exception handling

The API layer should not contain core business logic.

### Application Layer

Location:

```text
src/main/java/com/hcl/chubb/policy/application
```

Responsibilities:

* Application use cases
* Business validation
* Pagination and sorting rules
* Policy operations
* Mapping application results to API responses

### Domain Layer

Location:

```text
src/main/java/com/hcl/chubb/policy/domain
```

Responsibilities:

* Core policy model
* Policy status
* Line of business
* Repository interfaces

The domain layer should remain independent of database and framework implementation details.

### Infrastructure Layer

Location:

```text
src/main/java/com/hcl/chubb/policy/infrastructure
```

Responsibilities:

* JPA entities
* Spring Data repositories
* Repository implementations
* Database mapping
* Persistence specifications

## 3. Dependency Direction

Dependencies follow the inward direction:

```text
API
 ↓
Application
 ↓
Domain
 ↑
Infrastructure
```

Infrastructure implements interfaces defined by the domain/application boundary.

This keeps business logic independent from the database implementation.

## 4. Persistence Architecture

The persistence flow is:

```text
Controller
    ↓
PolicyService
    ↓
PolicyRepository
    ↓
PolicyRepositoryAdapter
    ↓
PolicyJpaRepository
    ↓
H2 Database
```

The domain `Policy` model is kept separate from the JPA `PolicyEntity`.

A mapper converts between the two representations.

## 5. Database Migration

Flyway manages database changes.

Migration files:

```text
src/main/resources/db/migration/
```

Current migrations include:

```text
V1__create_policy_table.sql
V2__seed_policy_data.sql
```

## 6. API Contract

The OpenAPI specification is maintained at:

```text
src/main/resources/openapi/policy-api.yaml
```

The specification defines:

* Endpoints
* HTTP methods
* Parameters
* Request bodies
* Response schemas
* Error responses
* Enum values

Swagger UI uses this contract for API documentation.

## 7. Error Handling

A centralized exception handler provides consistent error responses.

Example:

```text
Policy not found
        ↓
PolicyNotFoundException
        ↓
GlobalExceptionHandler
        ↓
HTTP 404 response
```

## 8. Performance Design

The application uses:

* Database-side filtering
* Database-side sorting
* Pagination
* Database indexes
* Aggregation queries for summary data

This avoids loading unnecessary records into application memory.

The target is p95 response time below 300 ms under 50 concurrent sessions.

## 9. Testing Architecture

Testing is performed at multiple levels:

```text
Unit Tests
    ↓
Service / Business Logic

Integration Tests
    ↓
Controller + Service + Database

Performance Tests
    ↓
REST APIs
```


