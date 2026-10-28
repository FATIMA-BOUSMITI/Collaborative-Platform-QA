# Selenium Test Cases

Status terminology:

- **Automated**: a Selenium test method exists.
- **Not executed**: no execution result is claimed in this document.
- **UI-only**: assertions cover browser-rendered behavior only; they do not claim backend persistence.
- **Blocked**: source-level integration gaps or unavailable QA infrastructure prevent a valid E2E assertion.

| ID | Class / method | Preconditions | Steps and expected result | Classification |
|---|---|---|---|---|
| AUTH-01 | `ValidLoginTest#validCredentialsRedirectToConfiguredRoleLandingPage` | Valid dedicated account and expected landing path | Sign in; arrive on configured role landing route | Backend E2E; not executed |
| AUTH-02 | `InvalidLoginTest#invalidCredentialsDisplayAnErrorAndDoNotCreateAnAccessToken` | Invalid QA credentials | Submit invalid credentials; see error, remain on login, no access token | Backend E2E; not executed |
| AUTH-03 | `EmptyLoginValidationTest` | Frontend reachable | Submit empty login form; browser/app validation prevents authentication | UI; run in Chrome on 2026-10-18, passed |
| AUTH-04 | `LogoutTest#logoutReturnsToLoginAndClearsStoredTokens` | Valid dedicated account | Sign in, log out; return to login and clear access/refresh tokens | Backend E2E; not executed |
| AUTH-05 | `ProtectedRouteTest#unauthenticatedUserIsRedirectedFromKanbanToLogin` | Frontend reachable | Clear browser session, open `/kanban`; redirect to login | UI; run in Chrome on 2026-10-18, passed |
| AUTH-06 | `ForgotPasswordValidationTest#emptyEmailShowsRequiredFieldMessage` | Frontend reachable | Submit the forgot-password form empty; required-field message appears | UI; run in Chrome on 2026-10-18, passed |
| AUTH-07 | `ResetPasswordValidationTest#missingTokenIsReportedWithoutShowingForm` | Frontend reachable | Open reset page without token; invalid-link message appears and form is hidden | UI; run in Chrome on 2026-10-18, passed |
| AUTH-08 | `ResetPasswordValidationTest#mismatchedPasswordsAreRejectedBeforeRequest` | Frontend reachable | Open reset page with a nonfunctional test token, enter mismatched passwords; client rejects before request | UI; run in Chrome on 2026-10-18, passed |
| USER-01 | `UserListTest#userTableDisplaysItsExpectedColumns` | Admin QA account, services and user list available | Open users page; verify observed table headers | UI with backend data; not executed |
| USER-02 | `UserSearchTest#searchFindsCreatedUserByEmail` | Isolated QA datastore, admin, department and role records | Create unique user through UI; search by email; verify exact result | Persistent E2E with cleanup; not executed |
| USER-03 | `UserCreationTest#shouldCreateUserSuccessfully` | Same as USER-02 | Create unique user with a selected department and role; reload page; verify name, department, and role remain assigned | Persistent E2E with cleanup; not executed |
| USER-04 | `UserCreationTest#shouldRejectDuplicateEmail` | Same as USER-02 | Create user, submit same email again; expect rejection and only one row | Persistent E2E with cleanup; not executed |
| USER-05 | `UserRequiredFieldsTest#firstNameIsRequired` | Admin and user form data dependencies available | Omit first name; expect the observed validation message | UI; not executed |
| USER-06 | `UserRequiredFieldsTest#lastNameIsRequired` | Same as USER-05 | Omit last name; expect the observed validation message | UI; not executed |
| USER-07 | `UserRequiredFieldsTest#emailIsRequired` | Same as USER-05 | Omit email; expect the observed validation message | UI; not executed |
| USER-08 | `UserRequiredFieldsTest#departmentIsRequired` | Same as USER-05 | Leave department unselected; expect the observed validation message | UI; not executed |
| USER-09 | `UserRequiredFieldsTest#roleIsRequired` | Same as USER-05 | Leave role unselected; expect the observed validation message | UI; not executed |
| USER-10 | `UserEmailValidationTest#malformedEmailIsRejected` | Isolated QA datastore, admin, department and role records | Submit malformed email; expect the form to remain open with an error and no user row | Backend/UI defect-detection test; not executed |
| USER-11 | `UserEditTest#editedUserIsPersistedAfterRefresh` | Same as USER-02 | Create test user, edit its name, reload; verify updated value | Persistent E2E with cleanup; not executed |
| USER-12 | `UserDeletionTest#deletedUserRemainsAbsentAfterRefresh` | Same as USER-02 | Create test user, delete it, reload; verify it remains absent | Persistent E2E with cleanup; not executed |
| ROLE-01 | `RoleListTest#roleTableDisplaysExpectedColumns` | Admin QA account and role/user services | Open role page; verify observed table headers | UI with backend data; not executed |
| ROLE-02 | `RoleListTest#roleSearchFiltersUsersByEmail` | Same as ROLE-01; admin appears in returned user list | Search by admin email; verify the filtered user row | UI with backend data; not executed |
| ROLE-03 | Not implemented | Isolated QA account and corrected API routing required | Assign a role, reload/re-read assignment, then verify applicable access as that account | Blocked: route mismatch; not counted as automated or passed |
| PERM-01 | Not implemented | Gateway route for `/api/permissions/**` required | Load role-permission matrix and verify saved permission state | Blocked: API catalog endpoint is not routed |
| DEPT-01 | Not implemented | Safe cleanup path required | Create department and verify after reload | Deferred: UI creation calls API but no reliable UI cleanup path exists |
| DEPT-02 | Not implemented | Backend wiring required | Edit/delete department and verify persistence | Blocked: current edit/delete handlers are local state only |
| TEAM-01 | Not implemented | Backend wiring required | Create, edit, delete, or assign a team member and verify persistence | Blocked: current teams page uses sample/local state |
| PROJ-01 | `KanbanUiTest#projectFormCanBeOpenedAndClosed` | Authenticated account and `/kanban` accessible | Open and close project form; verify modal title | UI-only; explicitly not project-creation E2E; not executed |
| PROJ-02 | Not implemented | Project create endpoint wired to modal and gateway reachable | Create project, reload projects view, verify persisted project | Blocked: modal updates local state only |
| KAN-01 | `KanbanUiTest#boardDisplaysImplementedColumnsAndSeedTasks` | Authenticated account and `/kanban` accessible | Verify three columns and source-defined sample tasks render | UI-only; not executed |
| KAN-02 | `KanbanUiTest#taskFormOpensWithSelectedColumnStatus` | Same as KAN-01 | Open add-task form in first column; verify status; close it | UI-only; not task persistence; not executed |
| KAN-03 | Not implemented | UI API integration and gateway `/api/tasks/**` route required | Create task, move status, reload, verify persisted status | Blocked: task UI is local state and task routes are not exposed by gateway |
| MSG-01 | Not implemented | Dedicated users/conversation data and isolated cleanup strategy required | Create channel/message through UI and verify after reload | Deferred until safe fixture and cleanup strategy are confirmed |

No scenario marked blocked or UI-only is counted as a successful backend E2E.
