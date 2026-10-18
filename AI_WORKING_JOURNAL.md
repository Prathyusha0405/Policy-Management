# AI Working Journal

Record of the AI-assisted work on this project, in the format CLAUDE.md asks for: prompt, accepted output, rejected output, manual changes, reasoning. Written from the session history; the "Manual changes" entries only list what is known from the session, so the project owner should complete them.

Tool: Claude Code (Sonnet 5.5). Each step below was reviewed and approved or run by the project owner. Planning steps went through plan mode and were approved before files were written.

---

## 1. Backend folder structure
**Prompt.** Read CLAUDE.md. Create the backend folder structure using Clean Architecture; only package structure, tree and class responsibilities, no implementation code.
**Accepted.** Tree and per-class responsibilities under `com.hcl.chubb.policy` (`api`, `application`, `domain`, `infrastructure`, `config`), then empty package folders with `.gitkeep` files.
**Rejected.** Nothing rejected by the owner. Two points were flagged as open (overlap between a domain repository and application ports; Spring annotations on use cases) and later resolved in code (see D5, D7 in DECISIONS.md).
**Manual changes.** None recorded.
**Reasoning.** CLAUDE.md fixes the five packages and the dependency direction; the tree is the smallest structure that satisfies them.

## 2. OpenAPI contract
**Prompt.** Create the OpenAPI contract `policy-api.yaml`.
**Accepted.** `openapi/policy-api.yaml`, OpenAPI 3.0.3: four endpoints, filters and paging, schemas, RFC 7807 responses, examples. Premium grouped per currency.
**Rejected.** None. The contract was not machine-validated: `npx` was unavailable in the environment, so no linter ran.
**Manual changes.** None recorded.
**Reasoning.** Contract first per CLAUDE.md. Enum values (statuses, lines of business) were proposed by the AI and then reused by the schema and seed data.

## 3. Database schema
**Prompt.** Generate `V1__schema.sql` using H2.
**Accepted.** `policy` table, unique `policy_number`, `CHECK` constraints, the four indexes CLAUDE.md lists.
**Rejected.** None.
**Manual changes.** None recorded.
**Reasoning.** Constraints keep bad data out at the database level. Noted that the unique constraint already indexes `policy_number`, so that index is redundant but kept because CLAUDE.md names it.

## 4. Seed data
**Prompt.** Generate `V2__seed_data.sql` with 250 realistic policies.
**Accepted.** 250 rows across 14 APAC regions with local currencies, realistic names and premiums, all statuses and lines of business, 13 flagged, 22 expiring in the next 30 days.
**Rejected.** None. A first approach of hand-writing rows was not used; a seeded PowerShell generator produced them so the output is reproducible and checkable.
**Manual changes.** None recorded.
**Reasoning.** Hand-typing 250 realistic rows is error-prone. Statuses follow dates relative to 2026-10-18. Row count and uniqueness were checked; a region-distribution check used the wrong column and was not repeated, so regional spread is unverified.

## 5. application.properties
**Prompt.** Generate `application.properties` for H2, Flyway and JPA.
**Accepted.** File-based H2 URL from CLAUDE.md, Flyway on `classpath:db/migration`, `ddl-auto=validate`, `open-in-view=false`, H2 console on.
**Rejected.** None.
**Manual changes.** None recorded.
**Reasoning.** Flyway owns the schema; Hibernate only validates. The AI warned that `validate` would fail if the entity disagreed with `CHAR(3)` and `UUID` columns; that was handled in the entity (D12).

## 6. Domain, application, infrastructure, API layers and GlobalExceptionHandler
**Prompt.** Generate the domain, application, infrastructure and API layers and GlobalExceptionHandler.
**Accepted.** All four layers, handler, request logging filter, cache config, use-case wiring, plus `policy.summary.expiring-soon-days` and ECS logging properties and a `data/` entry in `.gitignore`. The app was started and the endpoints and error cases were called by hand.
**Rejected (by the AI during the work).**
- The planned domain `PolicyRepository`, `PolicyExpiryPolicy` and a custom `ValidDateRange` annotation were not built; ports and an `@AssertTrue` check replaced them.
- MapStruct and Lombok were not used because the pom did not contain them; hand-written mappers and records were used.
- First compile failed on an override of `handleBindException`, which does not exist in this Spring version; the override was removed. The behaviour was then confirmed by calling the running app.
**Manual changes.** None recorded.
**Reasoning.** See D1 to D17 in DECISIONS.md. A leftover running Java process held the H2 file lock after the manual run and had to be stopped before tests could use the database.
**Not verified.** Cache eviction was not observable by hand because flagging does not change summary numbers; it is covered later by an integration test.

## 7. Tests
**Prompt.** Write unit and integration tests.
**Accepted.** 89 tests: unit, `@WebMvcTest`, `@DataJpaTest` on real migrations, `@SpringBootTest` end to end with a `test` profile and in-memory H2.
**Rejected.** None. All 89 passed on the first full run.
**Manual changes.** None recorded.
**Reasoning.** Boot 4 moved test annotations to new packages (for example `org.springframework.boot.webmvc.test.autoconfigure`); the AI checked the jars before writing imports. Mutating integration tests are transactional so they roll back.

## 8. Run all tests and share the report
**Prompt.** Run all tests and share the report.
**Accepted.** 89 run, 0 failures, 0 errors, 0 skipped, per-class table.
**Rejected.** The first attempt, `mvnw clean test`, failed in the clean step (a file in `target/` could not be deleted, likely OneDrive sync or an IDE lock). The report printed from that attempt was stale, which the AI noticed and reported. A rerun without `clean`, after deleting old reports, produced the real result.
**Manual changes.** None recorded.
**Reasoning.** Do not present old surefire files as the result of a failed build.

## 9. README.md and ARCHITECTURE.md
**Prompt.** Generate README.md and ARCHITECTURE.md.
**Accepted.** Both documents, including a limitations section (no coverage figure, no load test, no auth, per-instance cache).
**Rejected.** None.
**Manual changes.** None recorded. The run commands in the README were not executed.
**Reasoning.** Documentation states what is unverified rather than implying the performance and coverage targets were met.

## 10. DECISIONS.md and AI_WORKING_JOURNAL.md
**Prompt.** Generate DECISIONS.md and AI_WORKING_JOURNAL.md.
**Accepted.** These two files.
**Rejected.** None.
**Manual changes.** None recorded.
**Reasoning.** Decisions were reconstructed from the code and session, not from contemporaneous notes.

## 11. JaCoCo and coverage in the README
**Prompt.** Add JaCoCo and update README with coverage.
**Accepted.** `jacoco-maven-plugin` 0.8.14 in the pom (agent plus a report at the `test` phase). README Testing section now has report locations, per-class test counts and coverage: lines 99.4%, instructions 99.2%, branches 87.9%. `ARCHITECTURE.md` and `DECISIONS.md` were updated to match.
**Rejected.** None. No coverage gate (`jacoco:check`) was added because it was not requested.
**Manual changes.** None recorded.
**Reasoning.** The numbers were taken from the generated `jacoco.csv`, not estimated. The build is green, but the JaCoCo agent logs `Unsupported class file major version 71` while instrumenting JDK classes, because the machine's JDK (27) is newer than the plugin supports; application classes are instrumented correctly. Only uncovered lines are in the `main` method.

## 12. Coverage gate
**Prompt.** Add jacoco:check rule to enforce 80% coverage.
**Accepted.** A `check` execution on the `verify` phase requiring at least 80% line and branch coverage for the whole project, with the minimum held in a `jacoco.minimum.coverage` property. README, ARCHITECTURE.md and DECISIONS.md (D21) were updated.
**Rejected.** None. Binding the check to `test` was not chosen; `verify` is the conventional phase and keeps `mvnw test` fast for local runs.
**Manual changes.** None recorded.
**Reasoning.** The gate was proven in both directions: `mvnw verify` passed at 0.80, and with `-Djacoco.minimum.coverage=1.00` it failed reporting lines 0.99 and branches 0.87. My first attempt to summarise that output with a broad text filter matched stack-trace noise, so the pass/fail result above rests on the exit codes and the two violation messages.

---