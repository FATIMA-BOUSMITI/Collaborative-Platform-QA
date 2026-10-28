# Test Execution Guide

> Historical document: some implementation and execution statements describe an earlier state. See the [published evidence summary](docs/results/README.md) and the root README for the current publication scope. Source-inspection findings are not automatically current runtime defects.

## Requirements

- JDK 17 and Maven 3.x.
- Local frontend at `http://localhost:5173` by default.
- Required gateway/services at their configured local ports.
- Chrome or Firefox available to Selenium Manager.
- Dedicated QA accounts and an isolated QA database for backend mutation tests.

## Configuration

### Selenium test credentials

Set environment variables in the same PowerShell process that runs Maven. Use dedicated QA-only credentials; never save them in tracked files.

```powershell
$env:QA_BASE_URL = "http://localhost:5173"
$env:QA_BROWSER = "chrome"
$env:QA_HEADLESS = "true"
$env:QA_WAIT_SECONDS = "30"
$env:QA_VALID_EMAIL = "<dedicated-qa-user-email>"
$env:QA_VALID_PASSWORD = "<dedicated-qa-user-password>"
$env:QA_VALID_LANDING_PATH = "/manager"
$env:QA_ADMIN_EMAIL = "<dedicated-qa-admin-email>"
$env:QA_ADMIN_PASSWORD = "<dedicated-qa-admin-password>"
$env:QA_TEST_DATA_ISOLATED = "true"
```

`QA_TEST_DATA_ISOLATED=true` is a safety acknowledgement, not a database check. Only set it after confirming that the configured backend datasource is a dedicated QA database.

For the invalid-login test, also provide `QA_INVALID_EMAIL` and `QA_INVALID_PASSWORD`. Do not use actual user credentials for the invalid case.

## Compile

```powershell
mvn test-compile
```

## Execute tests individually

```powershell
mvn "-Dtest=EmptyLoginValidationTest" test
mvn "-Dtest=ForgotPasswordValidationTest" test
mvn "-Dtest=ResetPasswordValidationTest" test
mvn "-Dtest=UserListTest" test
mvn "-Dtest=UserCreationTest" test
mvn "-Dtest=UserCreationTest#shouldCreateUserSuccessfully" test
mvn "-Dtest=UserSearchTest" test
mvn "-Dtest=UserRequiredFieldsTest" test
mvn "-Dtest=UserEmailValidationTest" test
mvn "-Dtest=UserEditTest" test
mvn "-Dtest=UserDeletionTest" test
mvn "-Dtest=RoleListTest" test
mvn "-Dtest=KanbanUiTest" test
```

`ProjectCreationTest` and `KanbanWorkflowTest` are intentionally not implemented as persistent E2E tests: the UI currently does not persist those operations. `KanbanUiTest` covers only observable UI behavior.

## Run suite and view Allure

```powershell
mvn test
allure serve target/allure-results
```

The failure screenshot extension attaches browser screenshots to Allure for test failures. Screenshots and reports are local generated outputs and should be checked before sharing.

## Result recording

Record the actual browser, command, executed/passed/failed/skipped counts, duration, and failure classification. Separate environment failures (for example, services unavailable) from application and automation defects. Do not infer a pass from successful compilation.

## Observed execution

- **Date:** 2026-10-18, latest run at 10:11 UTC.
- **Browser:** Chrome (the `ConfigManager` default).
- **Command:** `mvn '-Dtest=EmptyLoginValidationTest,ForgotPasswordValidationTest,ResetPasswordValidationTest,ProtectedRouteTest' test`.
- **Result:** 5 tests run, 5 passed, 0 failures, 0 errors, 0 skipped; Maven build succeeded in 1 minute 13 seconds.
- **Scope:** Frontend-only UI behavior. This run does not validate successful password reset, login against real credentials, user mutation, or backend persistence.
- **Service availability observed for this run:** Frontend `5173`, gateway `9000`, auth-service `8080`, User-service `8082`, and Communication `8089` were listening. Project Management `8083` was not listening.

- **Date:** 2026-10-18.
- **Browser:** Chrome (the `ConfigManager` default).
- **Command:** `mvn '-Dtest=ForgotPasswordValidationTest,ResetPasswordValidationTest,EmptyLoginValidationTest,ProtectedRouteTest' test`.
- **Result:** 5 tests run, 5 passed, 0 failures, 0 errors, 0 skipped; Maven build succeeded.
- **Scope:** Frontend-only UI behavior against the local Vite frontend. This result does not validate successful password reset, login against real credentials, user mutation, or backend persistence.
- **Service availability observed:** Frontend `5173`, gateway `9000`, auth-service `8080`, and User-service `8082` were listening. Project Management `8083` and Communication `8089` were not listening at check time.
- **Allure:** Results were generated for this run; no failure screenshot was expected because no test failed.


## Repair verification — 2026-10-20

See [the Selenium repair report](docs/SELENIUM_REPAIR.md). Actual results: empty-login UI 1 passed; local Page Object browser fixtures 4 passed; real UserListTest 1 setup error because the application login timed out after 30000ms. No full-suite success is claimed.
