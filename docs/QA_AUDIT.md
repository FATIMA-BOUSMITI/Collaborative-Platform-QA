# Collaborative Platform QA Audit

> Historical document: some implementation and execution statements describe an earlier state. See the [published evidence summary](results/README.md) and the root README for the current publication scope. Source-inspection findings are not automatically current runtime defects.

Audit scope: source currently present in `../Ger/platfrom-collaborative`,
`../Ger/platfrom-front/Platform-collaboration-ocp-frontend`,
this Selenium project, and the separate
`../qa/QA-Automation-Portfolio` directory. This is a source inspection, not a
runtime/database-isolation certification. No credentials, tokens, database
URLs, or secret values are recorded here.

## 1. Architecture observed

| Component | Observed implementation | Configuration/dependency |
|---|---|---|
| Frontend | React 19, TypeScript, Vite, React Router, Zustand, Axios | `Ger/platfrom-front/Platform-collaboration-ocp-frontend/package.json`; Axios uses the gateway base URL `http://localhost:9000/api` in `src/api/axiosClient.ts`. |
| Gateway | Spring Cloud Gateway, port 9000 | `Ger/platfrom-collaborative/api-gatway`; explicit local routes in `src/main/resources/application.yml`. |
| Authentication | `auth-service`, Spring Boot 3.5.3, Java 17, Spring MVC, Spring Security, JPA, PostgreSQL, JWT | `auth-service/pom.xml`, `src/main/resources/application.properties`. |
| User/organisation | `User-service` artifact is `organisation-service`, Spring Boot 3.5.3, Java 17, Spring MVC, JPA, PostgreSQL, OpenFeign and a Spring `RestClient` auth client | `User-service/pom.xml`; `AuthClient` calls auth-service for credential, role, and deletion operations. |
| Projects/tasks/calendar | `project-management-service`, Spring Boot 3.5.3, Java 17, Spring MVC, JPA, PostgreSQL | `project-management-service/pom.xml`; no tests were found in this module. |
| Communication | `communication-service`, Spring Boot 3.5.3, Java 17, Spring MVC, JPA, PostgreSQL | `communication-service/pom.xml`; no tests were found in this module. |
| Browser QA | Standalone Maven project, Java 17, Selenium 4.51.0, JUnit 5.14.4, AssertJ and Allure JUnit | This project's `pom.xml`; preserves Selenium and Allure reporting. |
| Additional API QA | Separate Robot Framework portfolio at `../qa/QA-Automation-Portfolio` | Existing user-creation `.robot` suites and generated Robot HTML/XML reports; not part of the Selenium Maven build. |

The backend modules are separate Maven projects rather than a Maven reactor.
The gateway POM and the service POMs use different Spring Boot patch versions.
The QA directory does not have an independent Git root in the current layout;
Git identifies its parent Desktop directory as its root. Backend and frontend
have separate Git roots. Do not use a root-level Git operation to clean or
reset this workspace.

## 2. Endpoint inventory verified in controllers

Gateway routes are explicitly configured for `/api/projects/**` to
project-management-service, `/api/conversations/**` to communication-service,
`/api/auth/**` and `/api/roles/**` to auth-service, and `/api/users/**` to
organisation-service. Phase 1 adds explicit routes for `/api/permissions/**`
to auth-service and `/api/tasks/**`, `/api/subtasks/**`, and
`/api/calendar/**` to project-management-service. Nested project task/calendar
paths already match `/api/projects/**`.

### Authentication and identity (`auth-service`)

Controller base: `/api/auth` (`AuthenticationController`).

| Method | Path | Request/response observed |
|---|---|---|
| POST | `/api/auth/login` | `LoginRequest { email, password }`; 200 `AuthResponse { accessToken, refreshToken, tokenType }`. |
| POST | `/api/auth/logout` | `LogoutRequest`; 200 `"Logout successful"`. |
| POST | `/api/auth/refresh-token` | `RefreshTokenRequest`; 200 `AuthResponse`. |
| POST | `/api/auth/forgot-password` | `ForgotPasswordRequest`; 200 generic response to avoid revealing account existence. |
| POST | `/api/auth/reset-password` | `ResetPasswordRequest`; 200 text. |
| POST | `/api/auth/change-password/{credentialId}` | `ChangePasswordRequest`; 200 text. |

### Authentication users (`auth-service`)

Controller base: `/api/auth/users` (`UserController`).

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/users` | Create credential; `CreateUserRequest { email, userId, roleId }`; returns 201. Email validation uses `@NotBlank` and `@Email`. |
| GET | `/api/auth/users`, `/{id}`, `/user/{userId}` | List/look up credentials. `id` is the credential entity id; `userId` is its associated profile id. |
| GET | `/api/auth/users/role-assignments`, `/{userId}/roles`, `/stats`, `/stats/failed-attempts/last-24h` | Role assignments and statistics. |
| PUT | `/api/auth/users/{id}` | Update credential using a credential lookup by associated `userId` in the service; DTO is `UpdateUserRequest`. |
| POST | `/api/auth/users/assign-roles` | Assign a role. `AssignRoleRequest` contains singular UUID `roleId` and UUID `userId`. Frontend now sends the matching singular field; backend now resolves `userId` as the linked profile id. |
| DELETE | `/api/auth/users/{id}`, `/user/{userId}` | Delete by credential id or associated profile id; service rejects administrator deletion and removes sessions, refresh tokens, reset tokens, and login history first. |

### Roles and permissions (`auth-service`)

| Method | Path | Purpose/request |
|---|---|---|
| GET, POST | `/api/roles` | List roles; create role with `CreateRoleRequest { name, description }` (201). |
| GET | `/api/roles/{id}`, `/api/roles/stats` | Role detail and user counts. |
| GET, PUT | `/api/roles/{id}/permissions` | Read permissions; update with `{ permissionIds: UUID[] }`. |
| GET, POST | `/api/permissions`, `/api/permissions/{id}` | List/detail/create permission. |

`RoleController` does not expose role update or delete. `PermissionController`
does not expose update or delete.

### User profiles and departments (`organisation-service`)

Base paths are `/api/users` and `/api/users/departements`.

| Method | Path | Purpose/request |
|---|---|---|
| GET, POST | `/api/users` | List profiles; create profile using `UserCreateRequestDTO` fields including `firstName`, `lastName`, `email`, `departement` (UUID), and `roleId` (UUID); create returns 201. The service separately creates the auth credential through `AuthClient`. |
| GET | `/api/users/{id}`, `/api/users/auth/{authUserId}` | Look up by profile id or linked auth id. |
| PUT, DELETE | `/api/users/{id}` | Update or delete profile. Deletion calls auth-service and removes team-member links before deleting the profile. |
| GET, GET by id, POST, PUT, DELETE | `/api/users/departements`, `/{id}` | Department list/detail/create/update/delete. Create returns 201; delete returns 204. |

### Projects, tasks and calendar (`project-management-service`)

`ProjectController` exposes `/api/projects`: GET list/detail/by department,
POST create (201), PUT update, PATCH `/{id}/status`, POST `/{id}/archive`
(204), and DELETE `/{id}` (204).

`TaskController` is rooted at `/api` and exposes POST/GET
`/api/projects/{projectId}/tasks`, GET `/api/tasks/{id}`,
GET `/api/tasks/assignee/{userId}`, PUT `/api/tasks/{id}`,
PATCH `/api/tasks/{id}/status`, POST/GET task subtasks and comments, and
PATCH `/api/subtasks/{id}/toggle`. Project-scoped task paths are within the
gateway's `/api/projects/**` route; the separate `/api/tasks/**` paths have no
explicit gateway route in the inspected YAML.

`CalendarController` exposes POST/GET `/api/projects/{projectId}/calendar` and
DELETE `/api/calendar/{eventId}`. The DELETE path is not covered by an explicit
gateway route in the inspected YAML.

### Conversations and messages (`communication-service`)

`ConversationController` exposes POST `/api/conversations`, GET
`/api/conversations/{id}`, GET `/api/conversations/user/{userId}`, and POST
`/api/conversations/{conversationId}/members/{userId}` (204).
`MessageController` exposes POST/GET
`/api/conversations/{conversationId}/messages`; message creation returns 201.
These are covered by the gateway's `/api/conversations/**` route. No frontend
API module for conversations/messages was found in `src/api`.

## 3. Authentication, authorization and data flow

- The frontend stores `accessToken`, `refreshToken`, and `role` in browser
  local storage (`features/auth/AuthStore.ts`). It decodes role/user claims
  client-side, and `axiosClient` attaches the stored access token to requests.
- Frontend route guards check authentication and role in the browser
  (`routes/ProtectedRoute.tsx`, `routes/RoleRoute.tsx`).
- `auth-service` `SecurityConfig` currently permits `/api/auth/**`,
  `/api/roles/**`, `/api/users/**`, `/api/permissions/**`,
  `/api/projects/**`, and `/api/conversations/**` without authentication.
  The inspected config did not show endpoint-level role authorization or a
  JWT request filter. UI route protection is not a replacement for API
  authorization.
- `organisation-service` creates an auth credential over a `RestClient`, with
  a profile id assigned as credential `userId`; these are distinct from the
  credential's own database id. It also requests grouped role assignments to
  build the user list. Create/delete therefore span two service databases
  without a demonstrated distributed transaction or compensation strategy.
- No database runtime identity was queried or otherwise verified. The source
  configuration has database/JWT/mail properties, but no isolated QA profile
  or test database configuration was found by the file inventory. A boolean
  environment flag alone is not evidence of isolation.

## 4. Frontend persistence and coverage

| Feature | Observed UI/API behavior | QA implication |
|---|---|---|
| Authentication | Login/logout and password recovery APIs exist; routes enforce client-side session state. | Existing Selenium covers login validation, logout, valid/invalid login and unauthenticated route protection. API authorization still needs backend tests. |
| Users | Profile list/create/update/delete uses `/api/users`; creation triggers auth-service credential creation. | Selenium has existing user CRUD coverage and strict QA-email cleanup guard. Destructive execution requires runtime proof of isolated databases for both services. |
| Roles | UI lists users and changes a user's role; no create/delete role form. `roleApi.ts` POSTs `{ userId, roleIds: [roleId] }` to `/users/assign-roles`, while backend DTO expects singular UUID `roleId`. | Assignment cannot be considered verified until the request shape and user identifier semantics are reconciled. |
| Permissions | UI reads/writes role permissions via `/roles/{id}/permissions` and `/permissions`; changes require persistence and restoration. | Frontend calls `/api/permissions` through the gateway, but the gateway route for `/api/permissions/**` is absent. |
| Departments | UI uses backend GET/POST/PUT/DELETE endpoints. | A CRUD E2E test can verify persistence, but creation/deletion must be confined to verified QA storage and cleanup must run even on assertion failure. |
| Teams | Existing cards/modal operations operate on component state; no teams REST controller or frontend teams API module was found. Backend has a team-member repository used for user cleanup, not an identified teams CRUD API. | UI-only tests may validate current-page behavior, not backend persistence. |
| Kanban tasks | React Kanban task data is seeded and mutated in component state; these task actions do not call the task REST endpoints. | Current UI task CRUD/drag-drop tests are in-memory only; API task tests need independently verified project/task request DTOs and gateway routing. |
| Projects | Frontend fetches projects with GET `/api/projects`; the UI create modal mutates local page state. Backend exposes full project CRUD. | Distinguish backend API persistence from frontend-only project creation. |
| Communication | Backend conversation/message endpoints are present; no corresponding frontend API client was found. | API tests can be based on verified DTOs, but end-to-end UI coverage depends on implementing/locating a real frontend flow. |

Existing Selenium tests include authentication, protected routes, roles listing,
Kanban UI smoke, and user CRUD. A test source inventory also includes unit
tests in auth-service and one in User-service. No backend tests were found in
project-management-service, communication-service, or gateway during this
source inventory.

## 5. Existing tooling and risks

- Selenium Maven currently includes JUnit Jupiter, Selenium, AssertJ and
  Allure JUnit. It does not include Cucumber.
- Backend service POMs include Spring Boot Test (JUnit/Mockito support through
  the starter), but Testcontainers, JaCoCo and Sonar Maven configuration were
  not found in the inspected POMs.
- Robot Framework suites exist in the separate portfolio directory. Phase 1
  centralizes its auth/HTTP helpers, removes fixed user/UUID values from the
  credential-create suites, makes the duplicate case create its own record,
  and adds exact-record cleanup. A fail-closed attestation precondition is
  added, but it is not runtime proof by itself; no such attestation is
  currently available.
- No Postman collection/environment JSON, Newman setup, k6 scripts,
  Dockerfile/compose definition, or GitHub Actions workflow was found in the
  searched project trees.
- The services have separate database configuration properties. Application
  datasource target values were intentionally not printed or recorded; a
  human/runtime check must prove each destructive test target is QA-only.
- The auth user-creation service sets a fixed default password for newly
  created credentials and disables/locks them. This requires explicit review
  before expanding account lifecycle or login scenarios; it is not assumed to
  represent a supported invitation/password setup.
- Global error handling and exact error payloads need further per-service
  inspection before writing assertions for all negative API cases.

## 6. Initial API-to-scenario-to-framework traceability

| API/feature | Existing coverage | Candidate additional coverage | Safe precondition |
|---|---|---|---|
| `/api/auth/login`, logout, refresh | Selenium login/logout; auth-service unit/controller tests | Robot/Postman positive/negative auth; Cucumber representative login/logout | Use dedicated non-destructive QA credentials; no secret in source. |
| `/api/auth/users` create/update/delete and `/api/users` profiles | Selenium user CRUD; Robot create validation cases | Robot/Postman API lifecycle, Cucumber create/retrieve, backend integration tests | Prove isolated DB for **auth and organisation** services; unique test data and reliable cleanup. |
| `/api/roles` list/create and role assignment | Selenium roles list/search; frontend role assignment | API role create only when cleanup route or DB rollback is available; assignment after fixing DTO/id contract | Isolated QA identity; don't create persistent orphan roles. |
| `/api/roles/{id}/permissions`, `/api/permissions` | Frontend matrix | API positive/negative and restoration tests; backend unit/integration tests | Gateway route fix/check; save and restore exact prior permission set. |
| `/api/users/departements` | Frontend departments CRUD | Robot/Postman department lifecycle and Selenium persistence check | Dedicated organisation QA DB; cleanup via verified DELETE. |
| `/api/projects/**`, `/api/projects/{id}/tasks` | Frontend project list; Selenium in-memory Kanban | API task/project lifecycle; k6 read-only smoke first | Project/task gateway and DTO checks; isolated DB for mutations. |
| `/api/tasks/**`, `/api/calendar/**` | No identified frontend client | API tests only after gateway routing and request contract checks | Confirm routes are reachable through gateway; isolated data for mutations. |
| `/api/conversations/**` | No identified frontend client | API contract tests and limited BDD scenarios | Test participants/cleanup semantics and database target first. |
| UI-only teams and Kanban interactions | Selenium team/Kanban UI tests | Keep as UI behavior tests unless backend persistence is added | Do not assert persistence that the UI does not implement. |

## 7. Phase 0 conclusion and implementation plan

Phase 0 identifies a multi-repository system with five backend Maven services,
one React frontend, a standalone Selenium Maven suite, and a separate Robot
portfolio. It also identifies concrete routing, contract, and security
prerequisites. No new test code should be generated against the mismatched
contracts as-is.

Recommended incremental order:

1. **Resolve integration blockers:** add/verify gateway routing for permissions
   and non-project `/api/tasks/**`/calendar paths if clients need them; reconcile
   role assignment DTO cardinality and whether the supplied `userId` denotes a
   profile id or credential id; define server-side authorization rather than
   relying on frontend routes.
2. **Establish QA isolation:** create a documented test profile/DB arrangement
   for every service participating in mutations; add startup assertions or
   health metadata that lets tests verify database identity without logging
   credentials. Do not rely solely on `QA_TEST_DATA_ISOLATED=true`.
3. **Harden existing Robot suite:** parameterize gateway/role/test-user data,
   remove fixed real-account duplicate data, use unique IDs/emails, and add
   cleanup for every successful create before expanding scenarios.
4. **Add backend unit tests:** extend auth and organisation business-rule tests;
   establish JUnit/Mockito coverage in gateway, project-management and
   communication modules based on service logic.
5. **Add reproducible integration tests:** Spring Boot Test + Testcontainers
   PostgreSQL, separated from unit tests where useful; cover constraints,
   transactional behavior, security, and auth/organisation interservice failure
   paths. Verify Docker availability before claiming these tests run.
6. **Add API frameworks in layers:** Robot for reusable API flows; Postman
   collections/Newman only for stable, shared API contracts; a small Cucumber
   suite for representative business journeys rather than duplicating every
   Robot case.
7. **Add quality/reporting:** JaCoCo per backend module, Sonar configuration
   with XML coverage import, and retain native Robot/Newman/k6/JUnit reports
   alongside Allure where supported.
8. **Add k6 conservatively:** start with read-only smoke/load scripts against
   explicitly dedicated QA services; stress tests remain opt-in and never run
   on shared or production services.
9. **Add CI incrementally:** unit → Testcontainers integration → build/API
   validation → optional isolated E2E → optional performance smoke, with
   secrets from CI secret storage and reports archived even on failure.
10. **Validate each phase:** compile and run the narrowest relevant tests after
    each phase; report `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN` separately.

No runtime database isolation, backend role authorization, Postman/Newman
execution, Robot execution, Cucumber execution, Testcontainers execution,
Sonar Quality Gate, or k6 baseline is certified by this source audit.

## 8. Phase 1 progress (2026-10-19)

### Implemented

- Gateway routing added for existing auth permission endpoints and existing
  standalone task/subtask/calendar endpoint paths. Existing port changes in
  the gateway working tree were preserved.
- Frontend `roleApi.ts` now sends singular `roleId`, matching
  `AssignRoleRequest`.
- Backend role assignment now looks up `UserCredential` by `userId`, which is
  the profile identifier sent by `RolesPage` (`mapUserToUserRole` maps the
  profile's `id`). Credential primary key `id` remains a separate value.
- `AssignRoleRequest` uses `@NotNull` for UUID fields, avoiding use of a
  `CharSequence`/collection-only `@NotEmpty` constraint on UUID values.
- `CreateUserRequest` now validates required `userId` and `roleId` before
  entering service logic; its email validation message typo is corrected.
- Robot common resource centralizes environment variables, admin login,
  non-admin role preflight, response checks, a fail-closed QA-database
  attestation precondition, and verify-before-delete cleanup. The positive and
  duplicate credential tests use generated UUIDs and `qa.robot.*@example.invalid`
  addresses; duplicate testing no longer depends on a fixed existing account.
- Added Robot dependency manifest and execution/safety instructions in the
  separate QA portfolio.
- Auth-service now issues access tokens with a signed role claim and validates
  access-token type/signature/expiry in a stateless request filter. Login,
  refresh, forgot-password and reset-password remain public; user/role/
  permission administration requires an administrator role. Logout and
  password changes are authenticated and restricted to the token owner.
- User-service validates the same signed access tokens, restricts profile and
  department management to administrators, and permits an authenticated user
  to read only the profile matching the token's profile identifier (admins may
  read other profiles). Its synchronous AuthClient forwards the inbound bearer
  token so these internal calls retain the caller's authorization context.
- Replaced the JWT signing key literal in User-service configuration with the
  required `JWT_SECRET` environment property. Auth-service, User-service and
  communication-service must receive the same strong key; this has not yet
  been verified in the running processes.
- Added MockMvc authorization tests for login access, anonymous/employee/admin
  access to auth and profile operations, and self-versus-other profile/role
  reads.
- Communication-service now validates the same stateless access tokens.
  Conversation listing is restricted to the caller's own user id, conversation
  and message reads require existing membership, message sender and
  conversation creator ids must match the token, and only conversation
  administrators may add members. Denials and missing conversations return
  structured 403/404 responses.
- Added communication-service tests for unauthenticated access, self-only
  conversation listing, creator/sender spoofing, member-only conversation
  access, and conversation-admin member management.

### Validation and blockers

- Frontend `npm run build`: PASS.
- Auth service `mvn -Dtest=UserServiceTest test`: PASS (6 tests).
- Robot Framework `--dryrun`: PASS, 18 tests parsed across the shared user
  credential suites. This is keyword/syntax validation only; no API tests or
  hooks that mutate server data were executed.
- Read-only inspection found four distinct datasource identity fingerprints
  in source configuration, but no QA/test/sandbox marker in those datasource
  settings. Runtime overrides and the actual active database selected by each
  running service are unknown. Distinct databases do not prove isolation.
- `QA_TEST_DATA_ISOLATED` and `QA_DATABASE_ISOLATION_ATTESTATION` are absent
  from the current process environment. No destructive API or E2E test was
  run. The attestation gate prevents accidental suite execution but must be
  backed by an actual read-only check of the active service connection.
- Auth-service `SecurityAuthorizationTest`: PASS (6 tests); combined with
  `UserServiceTest` and `CreateUserRequestValidationTest`: PASS (14 tests,
  `BUILD SUCCESS`).
- User-service `SecurityAuthorizationTest`: PASS (5 tests). The combined run
  with `UserProfileServiceImplTest`: PASS (9 tests, `BUILD SUCCESS`).
- Communication-service targeted tests: PASS (7 tests,
  `BUILD SUCCESS`).
- Auth-service, User-service and communication-service protections are not
  yet runtime-certified.
  The `JWT_SECRET` value and signing algorithm agreement across all three
  protected services must be checked
  safely in deployment configuration, and existing running instances must be
  restarted before UI/API validation. Never print the secret during that
  verification.
- Access tokens issued before the signed role claim was added cannot satisfy
  the new protected routes. Existing sessions must obtain a replacement token
  through the refresh endpoint or authenticate again after rollout.
- Project-management-service still has no backend JWT security filter.
  Project records expose `responsibleId` and `departmentId`; tasks expose
  `assigneeId`, but there is no project-member relation or department-role
  lookup in the service. The exact read/write/status rules for ADMIN,
  DIRECTOR, MANAGER and EMPLOYEE must be confirmed before enforcing
  ownership-based access. Direct calls to that service remain an unresolved
  security gap.
- Communication-service authorization is covered by tests but not
  runtime-certified. Its active `JWT_SECRET` configuration must match
  auth-service, and deployed instances must be restarted before verification.

## 9. Local-only authentication bypass

- The `local` Spring profile now disables JWT filters and authorization
  enforcement in auth-service, User-service, communication-service, and
  project-management-service. It is deliberately opt-in:
  set `SPRING_PROFILES_ACTIVE=local` for each local service process.
- With this profile active, endpoints that normally depend on a verified
  caller identity accept caller-supplied identifiers. This is for local
  development only and must never be enabled in shared QA, staging, or
  production.
- Without the `local` profile, the authenticated/admin/ownership rules remain
  enabled. Local-profile behavior has not yet been runtime-verified against
  running services; current verification is compilation-only and skips test
  execution to avoid opening configured Neon datasource connections.
- No destructive API/E2E tests were run: active datasource identities remain
  unverified against dedicated QA databases. Existing 12 Selenium tests were
  not rerun in this phase.
- Postman/Newman, Cucumber, Testcontainers, JaCoCo/Sonar, k6 and CI remain
  unimplemented.
