# CHUBB APAC Policy Management Platform

## Objective

Build a production-quality backend service for managing insurance policies.

Focus on:

- Contract First Design
- Clean Architecture
- SOLID Principles
- Enterprise Coding Standards
- Testability
- Maintainability
- Scalability

The backend must demonstrate production-quality engineering practices.

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 4
- Spring Data JPA
- H2 Database
- Flyway
- OpenAPI 3
- Lombok
- MapStruct
- Spring Cache
- JUnit 5
- Mockito

---

# Architecture

Follow Clean Architecture.

Package Structure:

com.hcl.chubb.policy

├── api
├── application
├── domain
├── infrastructure
├── config

Rules:

- Domain must not depend on Spring
- Application must not depend on Infrastructure
- Controllers must remain thin
- Business logic belongs in Use Cases

Dependency Direction:

API
↓
Application
↓
Domain

Infrastructure → Application
Infrastructure → Domain

---

# OpenAPI First

Maintain:

openapi/policy-api.yaml

OpenAPI is the source of truth.

Never implement endpoints without updating the OpenAPI contract.

---

# Endpoints

GET /api/v1/policies

GET /api/v1/policies/{id}

PATCH /api/v1/policies/flag

GET /api/v1/policies/summary

---

# Policy Search Requirements

Implement:

- Pagination
- Sorting
- Filtering
- Free Text Search

Filters:

- status
- lineOfBusiness
- region
- effectiveDateFrom
- effectiveDateTo

Search across:

- policyNumber
- policyholderName
- underwriter

---

# Database

Use H2 Database.

Flyway Migrations:

V1__schema.sql

V2__seed_data.sql

Requirements:

- Minimum 250 policies
- APAC regions
- Realistic names
- Mixed statuses
- Mixed currencies
- Realistic premium amounts

H2 URL:

jdbc:h2:file:./data/policydb

---

# Domain Model

Policy

Fields:

- UUID id
- String policyNumber
- String policyholderName
- LineOfBusiness lineOfBusiness
- PolicyStatus status
- BigDecimal premiumAmount
- String currency
- LocalDate effectiveDate
- LocalDate expiryDate
- String region
- String underwriter
- Boolean flaggedForReview
- LocalDateTime createdAt
- LocalDateTime updatedAt

---

# Persistence

Use:

- Spring Data JPA
- JPA Specifications

Indexes:

- policy_number
- status
- region
- effective_date

Avoid native SQL unless necessary.

---

# Error Handling

Use RFC7807 Problem Details.

Create:

GlobalExceptionHandler

Response should contain:

- timestamp
- requestId
- status
- title
- detail

---

# Validation

Use Jakarta Validation.

Validate:

- Query parameters
- Path variables
- Request payloads

---

# Logging

Use structured logging.

Log:

- Request Id
- Endpoint
- Execution time
- Errors

Never log sensitive information.

---

# Summary API

Return:

- Total policies
- Count by status
- Premium by line of business
- Expiring soon count

---

# Caching

Use Spring Cache.

Cache:

GET /api/v1/policies/summary

Evict cache when policies are flagged.

---

# Performance

Target:

P95 < 300ms

Support:

50 concurrent users

Optimize:

- Pagination
- Indexes
- Specifications
- Summary caching

---

# Testing

Unit Tests:

- Use Cases
- Services
- Domain Logic

Integration Tests:

- Repository Tests
- Controller Tests

Coverage Goal:

80%+

Use:

- JUnit5
- Mockito

---

# Documentation

Maintain:

README.md

ARCHITECTURE.md

DECISIONS.md

AI_WORKING_JOURNAL.md

---

# AI Working Journal

Track:

- Prompt
- Accepted Output
- Rejected Output
- Manual Changes
- Reasoning

---

# Git Commit Format

feat:
fix:
test:
docs:
refactor:
chore:

Example:

feat: implement policy summary endpoint