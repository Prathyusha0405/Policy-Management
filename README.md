# CHUBB APAC Policy Management Platform

Backend service for searching and reviewing insurance policies across APAC. Built contract-first with Clean Architecture on Java 21 and Spring Boot 4.

## Features

- Search policies with pagination, sorting, filters and free-text search
- Fetch a single policy
- Flag or unflag a policy for review
- Portfolio summary (counts by status, premium by line of business and currency, expiring soon), cached
- RFC 7807 problem responses carrying `timestamp` and `requestId`
- Structured JSON logging with a request id on every line

## Tech stack

Java 21, Spring Boot 4.1, Spring Data JPA (Specifications), H2 (file database), Flyway, Spring Cache, Jakarta Validation, springdoc, JUnit 5, Mockito.

## Getting started

Requirements: JDK 21 or newer. Maven is provided by the wrapper.

```powershell
# run the app (http://localhost:8080)
.\mvnw.cmd spring-boot:run

# run all tests
.\mvnw.cmd test
```

On macOS/Linux use `./mvnw`.

On first start Flyway creates the schema (`V1__schema.sql`) and loads 250 seed policies (`V2__seed_data.sql`) into `./data/policydb`. Delete the `data/` folder to reset the database.

| URL | Purpose |
|---|---|
| `http://localhost:8080/api/v1/policies` | API |
| `http://localhost:8080/h2-console` | H2 console (JDBC URL `jdbc:h2:file:./data/policydb`, user `sa`, empty password). Local development only. |
| `http://localhost:8080/swagger-ui.html` | springdoc UI generated from the running code |

## API

The contract is [`openapi/policy-api.yaml`](openapi/policy-api.yaml). It is the source of truth: update it before changing an endpoint.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/policies` | Search policies |
| GET | `/api/v1/policies/{id}` | Get one policy |
| PATCH | `/api/v1/policies/flag` | Set `flaggedForReview` and return the updated policy |
| GET | `/api/v1/policies/summary` | Portfolio summary (cached) |

### Search parameters

| Parameter | Notes |
|---|---|
| `page` | Zero-based, default 0 |
| `size` | 1 to 100, default 20 |
| `sort` | `field[,asc\|desc]`, default `effectiveDate,desc`. Fields: `policyNumber`, `policyholderName`, `lineOfBusiness`, `status`, `premiumAmount`, `effectiveDate`, `expiryDate`, `region`, `underwriter`, `createdAt`, `updatedAt` |
| `search` | Case-insensitive partial match over `policyNumber`, `policyholderName`, `underwriter` (max 100 chars) |
| `status` | `ACTIVE`, `PENDING`, `EXPIRED`, `CANCELLED`, `SUSPENDED` |
| `lineOfBusiness` | `PROPERTY`, `MOTOR`, `HEALTH`, `LIFE`, `TRAVEL`, `MARINE`, `LIABILITY`, `ACCIDENT` |
| `region` | Exact match, case-insensitive (for example `Singapore`) |
| `effectiveDateFrom`, `effectiveDateTo` | ISO dates, inclusive; `from` must not be after `to` |

### Examples

```powershell
curl "http://localhost:8080/api/v1/policies?status=ACTIVE&region=Singapore&sort=premiumAmount,desc&size=5"
curl "http://localhost:8080/api/v1/policies?search=tan"
curl "http://localhost:8080/api/v1/policies/summary"
curl -X PATCH http://localhost:8080/api/v1/policies/flag -H "Content-Type: application/json" `
     -d '{"policyId":"<uuid>","flaggedForReview":true}'
```

### Errors

All errors are `application/problem+json`:

```json
{
  "status": 400,
  "title": "Bad Request",
  "detail": "Validation failed",
  "instance": "/api/v1/policies",
  "timestamp": "2026-10-18T15:39:08.145Z",
  "requestId": "852be22b-f0fb-4369-902c-f8b3c62ef563",
  "errors": [{ "field": "size", "message": "must be less than or equal to 100" }]
}
```

Send `X-Request-Id` (letters, digits and hyphens, up to 64 characters) to supply your own correlation id. Otherwise one is generated. It is returned in the `X-Request-Id` response header and included in every log line.

## Configuration

`src/main/resources/application.properties`:

| Property | Default | Meaning |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/policydb` | Database location |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Flyway owns the schema; Hibernate only checks it |
| `policy.summary.expiring-soon-days` | `30` | Window for the "expiring soon" count |
| `logging.structured.format.console` | `ecs` | JSON logs |

## Testing

89 tests: unit tests for the domain, use cases, mappers, validation and filter; `@WebMvcTest` controller tests; a `@DataJpaTest` suite that runs the real Flyway migrations; and `@SpringBootTest` end-to-end tests. Integration tests use the `test` profile with an in-memory database and never touch `./data`.

```powershell
.\mvnw.cmd test     # runs the tests and writes the JaCoCo report
.\mvnw.cmd verify   # same, plus fails the build if coverage is below 80%
```

| Report | Location |
|---|---|
| Test results (XML/text) | `target/surefire-reports/` |
| Coverage (HTML) | `target/site/jacoco/index.html` |
| Coverage (CSV) | `target/site/jacoco/jacoco.csv` |

### Latest results

Run on 2026-10-18 with `mvnw verify` (build passed, 80% coverage gate satisfied): **89 tests, 0 failures, 0 errors, 0 skipped.**

| Test class | Tests |
|---|---|
| `api.controller.PolicyControllerTest` | 20 |
| `api.dto.PolicySearchRequestValidationTest` | 14 |
| `api.filter.RequestLoggingFilterTest` | 5 |
| `api.mapper.PolicyApiMapperTest` | 7 |
| `application.usecase.*` (4 classes) | 7 |
| `domain.PolicyTest` | 4 |
| `infrastructure.persistence.PolicyEntityMapperTest` | 2 |
| `infrastructure.persistence.PolicyPersistenceAdapterIntegrationTest` | 19 |
| `integration.PolicyApiIntegrationTest` | 10 |
| `PolicyManagementPlatformApplicationTests` | 1 |

### Coverage (JaCoCo)

| Metric | Coverage |
|---|---|
| Lines | 99.4% (358 of 360) |
| Instructions | 99.2% |
| Branches | 87.9% |

This is above the 80% goal, and the build enforces it: `mvnw verify` fails if line or branch coverage of the whole project drops below 80%. The threshold is the `jacoco.minimum.coverage` property in `pom.xml` (default `0.80`) and can be overridden for a check, for example `.\mvnw.cmd verify "-Djacoco.minimum.coverage=0.90"`. `mvnw test` alone runs the tests and writes the report but does not apply the gate.

The only uncovered lines are in the `main` method of `PolicyManagementPlatformApplication`, which tests do not call. The weakest branch coverage is in `GlobalExceptionHandler` (58%, the fallback request-id path), `SummaryCacheAdapter` (50%, the missing-cache guard) and `PolicySpecifications` (94%).

Coverage numbers are a snapshot; rerun `mvnw test` to refresh them. They measure which code the tests execute, not whether the assertions are sufficient.

If the console shows `Unsupported class file major version` errors from the JaCoCo agent while instrumenting `java/...` classes, the JDK in use is newer than this JaCoCo version supports. Only JDK classes are affected and the results above are unaffected; running the build on JDK 21 removes the noise.

Not yet done: load testing against the P95 < 300 ms / 50 concurrent users target.

## Project layout

```
openapi/policy-api.yaml          API contract
src/main/java/com/hcl/chubb/policy
  api/                           controller, DTOs, mapper, exception handler, request filter
  application/                   use cases, ports, commands, queries, results
  domain/                        Policy, enums, domain exceptions
  infrastructure/                JPA entity, repository, specifications, adapters, cache
  config/                        cache, use-case wiring, properties
src/main/resources/db/migration  V1__schema.sql, V2__seed_data.sql
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for design details.

## Notes

- The pom does not include Lombok or MapStruct. Records and hand-written mappers are used instead.
- The summary cache is in-memory and per instance. A multi-node deployment would need a shared cache.
- Seed statuses are consistent with dates as of 2026-10-18; over time more `ACTIVE` seed policies will be past their expiry date.
