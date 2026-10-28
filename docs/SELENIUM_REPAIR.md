# Selenium repair report — 20 October 2026

> Historical document: some implementation and execution statements describe an earlier state. See the [published evidence summary](results/README.md) and the root README for the current publication scope. Source-inspection findings are not automatically current runtime defects.

## Scope

Repair the existing Java/Selenium tests against the React source currently present in the ERP. No tests were disabled to turn failures into successes, no credentials were added to source, and no ERP business records were created/deleted during these verification runs. Git was left unchanged following the declined request to initialize an independent repository.

## Corrections

| Area | Correction |
|---|---|
| User forms | Locate the current native dialog by its accessible title; use the current save/close buttons and field labels. |
| Required fields / malformed email | Assert actual HTML constraint validation and native validation messages instead of obsolete frontend error strings. Duplicate-email checks still require a real server rejection. |
| User table headers | Read semantic header text, independent of CSS uppercase transformation. The expected six column names remain unchanged. |
| Pagination | Search the generated test email before presence, persistence and cleanup checks so a record on another page is not treated as absent. |
| Delete confirmations | Accept the application's real confirmation dialog; preserve HTTP 204 and reload/persistence checks. |
| Departments / teams | Use current page headings, cards, labeled forms and confirmation dialogs; select real available fixture options, not hardcoded department names. |
| Role assignment | Use the current inline select and confirmation; preserve reload verification and allow mutation only of generated QA users. |
| Permissions | Use a uniquely named temporary QA role instead of modifying protected system-role permissions; restore and remove that fixture. Observe actual rendered checkboxes and success/error feedback instead of nonexistent state attributes. |
| Kanban | Wait for projects/columns to load, use actual status markers, current task cards, and Selenium pointer Actions for dnd-kit. Verify persistence after reload. Test archiving, which is what the UI implements, rather than claim a hard deletion. |
| Login diagnostics | Stop on an actual application error; no automatic credential replay to conceal a failed login. |
| Screenshots | Capture setup failures as well as test-body failures before browser teardown. |

## Actual execution results

Results below are per command, not an aggregate of old XML files left in target/surefire-reports.

| Execution | Result | Scope |
|---|---|---|
| mvn -DskipTests test-compile | PASS | Compilation of all current Java test sources. |
| EmptyLoginValidationTest, headless, localhost:5173 | 1 test; 0 failures; 0 errors; 0 skipped | Actual frontend empty-form validation, no authentication API request. Completed 17:35:29 UTC. |
| PageObjectRegressionTest, cached ChromeDriver, headless | 4 tests; 0 failures; 0 errors; 0 skipped | Self-contained browser HTML fixtures: semantic headers, required input, malformed email, and login-error propagation without retry. These do NOT prove ERP CRUD or backend integration. Completed 17:47:07 UTC. |
| UserListTest, cached ChromeDriver, localhost:5173 | 1 test; 0 assertion failures; 1 setup error; 0 skipped | Actual application login displayed timeout of 30000ms exceeded before reaching the user table. Completed 18:15:12 UTC. |

The first fixture-only run was interrupted because Selenium Manager repeatedly waited on unavailable driver metadata; all four cases were then rerun using the already cached matching driver. No successful result is claimed for the interrupted run.

Availability probes after the login failure: frontend returned 200; gateway and all four backend services returned 401 promptly to unauthenticated GETs. Those 401 responses are expected without a JWT. They prove reachability, not working authenticated requests, database connectivity, or resolution of the authenticated 401s in the supplied logs.

## Remaining application/environment blocker

The full suite is NOT confirmed green. Login currently times out in the actual frontend before UserListTest reaches its header assertion. The supplied earlier logs also include 401 responses on authenticated /api/users requests. These must remain visible failures, not be hidden by longer waits, skipped assertions or repeated sign-ins. Backend logs and the failing login/profile request need investigation to identify whether the delay is in authentication, profile lookup, session validation or database access.

CRUD, permission mutations and task creation/archiving were not executed in this repair verification because isolation of the configured ERP database has not been confirmed. They still require the dedicated QA database and relevant fixtures (administrator, writable manager project, departments and permissions).

## Reproduction

Use the existing Maven project root. For the current machine, ChromeDriver 154 is already cached; either let Selenium Manager resolve a driver normally or provide its actual path through -Dwebdriver.chrome.driver. Never copy a machine-specific driver path into shared source.

Safe browser-fixture command:

    mvn -Dtest=PageObjectRegressionTest -Dqa.headless=true test

Actual read-only user-list command (requires configured QA credentials and a responsive ERP):

    mvn -Dtest=UserListTest -Dqa.headless=true test

Only after verifying a dedicated QA database, use QA_TEST_DATA_ISOLATED=true for the write scenarios. This flag is an operator attestation, not an automatic check of database isolation. Keep .env and generated reports/screenshots out of Git. See target/surefire-reports and the Allure output directory configured in src/test/resources/allure.properties for raw evidence.
