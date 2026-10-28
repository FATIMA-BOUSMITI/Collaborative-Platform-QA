# Selenium UI and E2E Test Plan

## Purpose

Provide maintainable browser coverage for behavior that is implemented in the React frontend and, where connected, verify persistence through the real Spring Boot services. A test that only observes React state is classified as UI coverage, not as a backend E2E.

## Scope

### Included

- Authentication form behavior, login/logout, and anonymous access to the protected Kanban route.
- User list, search, required fields, email validation, creation, editing, and deletion.
- Read-only role page presentation and search.
- Forgot-password and reset-password client-side validation.
- Kanban/project interface rendering and modal open/close behavior.
- Backend persistence assertions for user create/edit/delete, when run against an isolated QA environment.

### Deferred / blocked

- Role assignment: current frontend route and gateway/controller routes do not match.
- Permission matrix: gateway does not route `/api/permissions/**`.
- Project creation E2E: the current modal updates React state rather than calling project creation API.
- Task create/update/status E2E: current Kanban page keeps tasks in React state; the gateway also lacks a `/api/tasks/**` route.
- Department update/delete and team CRUD persistence: current UI handlers only update local state.
- Password reset through an email link: requires a QA mailbox or a controlled way to obtain a real reset token. No token is fabricated by the suite.
- Session-expiration behavior: a client-side expiry/refresh workflow was not confirmed in the inspected frontend.
- Responsive layout and accessibility audits: deferred until explicit viewport and accessibility acceptance criteria are defined.

## Test environment

- Java 17, Maven 3.x, Selenium WebDriver 4, JUnit 5, AssertJ, Allure.
- Local frontend default: `http://localhost:5173`.
- API gateway: `http://localhost:9000`; services must be reachable behind the gateway.
- Chrome or Firefox; headless mode is configurable.
- User mutation tests require a **dedicated, isolated QA database**, an administrator QA account, and `QA_TEST_DATA_ISOLATED=true`.
- Authentication UI tests that call backend services require dedicated QA accounts. No credentials are stored in this repository.
- Test data is uniquely named; cleanup targets only records created by the current test. Do not use production or shared data.

## Entry criteria

1. Frontend and required gateway/services are running and reachable.
2. Maven runs with JDK 17 and a supported browser installed.
3. Required QA credentials are supplied as process environment variables.
4. For user mutation tests, the datastore is confirmed isolated and the explicit isolation opt-in is enabled.
5. Department and role records required by user creation are present.
6. Email delivery or another approved QA-only token retrieval mechanism exists before reset-link E2E testing.

## Exit criteria

- The selected tests complete without framework errors in the target browser.
- All failures are classified as application defects, environment/configuration failures, or automation defects with evidence.
- Persistence claims are supported by reloading the UI and reading the resource back from the real backend.
- No blocked or frontend-only scenario is counted as a passing persistence E2E.
- Allure results include failure screenshots when a browser test fails.

## Coverage and execution policy

The current suite contains five preserved authentication/navigation tests and twenty added tests. The twenty-five test methods are source coverage, not a statement that all scenarios have been executed or passed. Backend-dependent cases require the entry criteria above. See [TEST_CASES.md](./TEST_CASES.md) for case-level classification and [TEST_EXECUTION.md](./TEST_EXECUTION.md) for commands.

## Known integration risks

- Role assignment UI posts `/api/users/assign-roles`; the gateway routes `/api/users/**` to User-service, while the assignment controller is under Auth-service `/api/auth/users/assign-roles`.
- The permission catalog endpoint has no gateway route for `/api/permissions/**`.
- Kanban's visible task CRUD is local state; `/api/tasks/**` has no gateway route.
- Project listing is connected to `GET /api/projects`, but project creation from the modal is local state.
- Dashboard requests `/api/users/stats`; the inspected User-service controller does not expose that route.

These are source-inspection findings and have not been presented as runtime-reproduced defects.
