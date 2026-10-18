# Decisions

Architecture decision records for the Policy Management Platform. Each entry states the context, the decision, and the consequence. Status is "Accepted" unless noted.

## D1. Clean Architecture with five packages
**Context.** CLAUDE.md requires `api`, `application`, `domain`, `infrastructure`, `config`, with the domain free of Spring and the application free of infrastructure.
**Decision.** Follow that layout. Dependencies point inward; infrastructure implements interfaces owned by `application`.
**Consequence.** Business rules and use cases are testable without Spring. More classes and mapping than a single-layer service.

## D2. Contract first, OpenAPI 3.0.3, maintained by hand
**Context.** `openapi/policy-api.yaml` is the source of truth.
**Decision.** Write the contract before the code and keep it by hand. Use 3.0.3 for broad tooling compatibility.
**Consequence.** Nothing generates code from it or verifies the running API against it, so drift is possible. The springdoc UI exists but is generated from code and is not authoritative. A contract test or generator would close this gap.

## D3. `PATCH /flag` returns the updated policy
**Decision.** Return 200 with the full `Policy` instead of 204.
**Consequence.** Clients need no follow-up read. Slightly larger response.

## D4. Premium is summarised per line of business and currency
**Context.** Seed data deliberately mixes currencies; adding amounts across currencies is meaningless.
**Decision.** `premiumByLineOfBusiness` is a list of `{lineOfBusiness, currency, totalPremium}`.
**Consequence.** Larger payload and the client decides how to present it. Currency conversion was out of scope.

## D5. Use cases are plain classes wired in `UseCaseConfig`
**Decision.** No Spring annotations in `application`; beans are declared in `config`.
**Consequence.** The application layer stays framework-free. Each new use case needs a bean method. `@Service` would be simpler but breaks the rule.

## D6. Records and hand-written mappers instead of Lombok and MapStruct
**Context.** CLAUDE.md lists Lombok and MapStruct, but the project's pom did not include them.
**Decision.** Use Java records for DTOs and models, and explicit mapper classes. Did not alter the pom to add annotation processors.
**Consequence.** More boilerplate, no annotation-processor setup or build risk. This deviates from the stack in CLAUDE.md; adding the dependencies later is straightforward.

## D7. Separate query, command and summary ports
**Decision.** `PolicyQueryPort`, `PolicyCommandPort`, `PolicySummaryPort`, plus `SummaryCachePort`, all implemented by one `PolicyPersistenceAdapter`.
**Consequence.** Read models can change independently of aggregate persistence. The initially planned domain-level `PolicyRepository` was dropped because it overlapped with these ports.

## D8. Caching lives in infrastructure; eviction goes through a port
**Decision.** `@Cacheable` is on `PolicyPersistenceAdapter.summarize`. Eviction is done by `SummaryCacheAdapter` behind `SummaryCachePort`. Cache key is `<date>:<expiringSoonDays>`.
**Consequence.** `application` has no caching dependency, and the key rolls over daily, so "expiring soon" cannot go stale across midnight. The cache is in-memory and per instance, so a multi-node deployment needs a shared cache. Eviction only happens when a flag actually changed.

## D9. Sort fields are a whitelist enum
**Decision.** `PolicySortField` lists sortable fields; the API pattern and entity attributes share the same names. `id` is added as a tie-breaker.
**Consequence.** No sorting by arbitrary properties and stable pagination. A new sortable field needs an enum entry and a pattern update.

## D10. Offset pagination, size capped at 100
**Decision.** Standard page/size paging.
**Consequence.** Simple and supports page jumps; deep pages get slower on very large tables. Fine for the seed volume.

## D11. Free-text search uses escaped, case-insensitive `LIKE`
**Decision.** `lower(column) like %term%` with `%`, `_` and `\` escaped, across policy number, holder name and underwriter.
**Consequence.** A leading wildcard cannot use an index, so it scans. Acceptable at current scale. Wildcard characters typed by users are treated literally (covered by a test).

## D12. Flyway owns the schema; Hibernate only validates
**Decision.** `ddl-auto=validate`. `CHECK` constraints enforce enum values, non-negative premium and `expiry_date >= effective_date`. Indexes on `policy_number`, `status`, `region`, `effective_date`.
**Consequence.** Entity and schema drift fails at startup. Adding an enum value needs a migration. `currency` is `CHAR(3)` and the entity declares `SqlTypes.CHAR`; enum columns are forced to `VARCHAR` so validation matches.

## D13. Validation: Jakarta constraints on request records
**Decision.** Query parameters are bound to `PolicySearchRequest` with constraints; the date range uses an `@AssertTrue` method rather than a custom annotation. Defaults are applied in the record constructor.
**Consequence.** Less code. The date-range error is reported on the field name `dateRangeValid`.

## D14. Errors extend `ResponseEntityExceptionHandler`
**Decision.** `GlobalExceptionHandler` builds RFC 7807 problems and adds `timestamp` and `requestId` in `handleExceptionInternal`. Validation failures add an `errors` list. Unexpected errors return a generic message and are logged server-side.
**Consequence.** Framework errors (405, 415, malformed JSON, unknown route) get the same format for free. Rejected values are not echoed back.

## D15. Request id handling
**Decision.** `RequestLoggingFilter` runs first, accepts a client `X-Request-Id` only if it matches `^[A-Za-z0-9-]{1,64}$`, otherwise generates one. It sets the MDC and response header, and logs method, path, status and duration. The query string is not logged.
**Consequence.** Prevents log injection through the header and keeps search terms out of logs.

## D16. Structured logging with Spring Boot's ECS format
**Decision.** `logging.structured.format.console=ecs`.
**Consequence.** JSON logs with MDC fields and no extra dependency. Harder to read in a terminal during development.

## D17. Injected `Clock`
**Decision.** A `Clock` bean (system default zone) is passed to use cases; the domain takes `now` as an argument.
**Consequence.** Deterministic tests. The system default zone means "today" and timestamps follow the server's zone; APAC deployments should pin a zone deliberately.

## D18. Seed data is generated and date-anchored
**Decision.** 250 policies were produced by a script with a fixed random seed. Statuses are consistent with dates relative to 2026-10-18.
**Consequence.** Reproducible and realistic, but static: over time `ACTIVE` seed rows will pass their expiry date. Premiums in non-decimal currencies (JPY, KRW, IDR, VND) are whole numbers stored with two decimals.

## D19. Test strategy and isolation
**Decision.** Unit tests for domain, use cases, mappers, validation and filter; `@WebMvcTest` for HTTP; `@DataJpaTest` and `@SpringBootTest` run the real migrations on in-memory H2 under a `test` profile. Mutating integration tests are `@Transactional` and roll back.
**Consequence.** Tests never touch `./data/policydb`. A few assertions depend on seed data properties (at least 250 rows, every status present).

## D20. Concurrent flag updates are last-write-wins
**Decision.** No optimistic locking; flag is read then saved in two transactions.
**Consequence.** Simple, and acceptable for a boolean flag, but two simultaneous different updates cannot be detected. Adding `@Version` is the fix if it matters.

## D21. Coverage gate at 80% for lines and branches, run at `verify`
**Context.** CLAUDE.md sets an 80%+ coverage goal.
**Decision.** `jacoco:check` is bound to the `verify` phase with a bundle-level rule: line and branch covered ratio of at least `${jacoco.minimum.coverage}` (default 0.80, overridable on the command line).
**Consequence.** `mvnw verify` fails when coverage drops, which suits CI. `mvnw test` does not enforce it, so a developer running only tests gets the report but no failure. The rule applies to the whole project, not per class, so a single poorly tested class can hide behind a high average. Verified both ways: passes at 0.80, fails when the minimum is set to 1.00.

## Open items
- Coverage is measured (JaCoCo, 99.4% lines, 87.9% branches) and gated at 80% by `mvnw verify` (see D21).
- Load testing against P95 < 300 ms at 50 concurrent users has not been done.
- No authentication or authorization.
- H2 console is enabled by default; disable outside local development.
