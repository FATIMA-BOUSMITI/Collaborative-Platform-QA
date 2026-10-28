# Bug and Integration Findings

> Historical document: some implementation and execution statements describe an earlier state. See the [published evidence summary](docs/results/README.md) and the root README for the current publication scope. Source-inspection findings are not automatically current runtime defects.

This document separates source-inspection findings from runtime-confirmed defects. No new browser reproduction was performed for these findings; they are not claimed as runtime confirmed.

## BR-01 — Role assignment request is routed to the wrong service

- **Status:** Source-confirmed integration mismatch; runtime reproduction pending.
- **Severity:** High for administrator role-management workflow.
- **Area:** Roles / user assignment.
- **Reproduction:** Sign in as an administrator, open `/roles`, choose a user, select a different role, and save.
- **Expected:** The selected role is assigned and remains assigned after reloading the role page.
- **Observed in source:** Frontend posts to `/api/users/assign-roles`. Gateway routes `/api/users/**` to User-service. The assignment controller is exposed by Auth-service at `/api/auth/users/assign-roles`.
- **Evidence:** `src/features/roles/Pages/RolesPage.tsx`, `src/api/roleApi.ts`, `api-gatway/src/main/resources/application.yml`, `auth-service/.../UserController.java`.
- **Impact:** Role assignment is not expected to reach its controller through the configured gateway.
- **Screenshot:** Not available; no browser reproduction performed.

## BR-02 — Permission catalogue has no gateway route

- **Status:** Source-confirmed integration mismatch; runtime reproduction pending.
- **Severity:** High for permission-matrix loading.
- **Area:** Permissions.
- **Reproduction:** Sign in as an administrator and open `/permissions`.
- **Expected:** The role/permission matrix loads the permission catalogue and current role assignments.
- **Observed in source:** Frontend requests `/api/permissions`; Auth-service has a matching controller, but the gateway configuration has no `/api/permissions/**` route.
- **Evidence:** `src/features/permissions/pages/PermissionsPage.tsx`, `src/api/permissionApi.ts`, `api-gatway/src/main/resources/application.yml`, `auth-service/.../PermissionController.java`.
- **Impact:** The permission catalog request cannot be routed by the configured gateway.
- **Screenshot:** Not available; no browser reproduction performed.

## BR-03 — Project creation in Kanban is not persisted

- **Status:** Source-confirmed missing frontend integration; runtime persistence failure not separately reproduced.
- **Severity:** High for project creation workflow.
- **Area:** Projects.
- **Reproduction:** Open `/kanban`, choose “Nouveau projet”, complete the fields, submit, then reload.
- **Expected:** Project is stored by Project Management Service and remains visible after reload.
- **Observed in source:** `CreateProjectModal` constructs a random client-side id and invokes a state callback. The page reads projects with `GET /api/projects` but does not call the backend create endpoint.
- **Evidence:** `src/features/manager/Kanban/pages/KanbanPage.tsx`, `src/features/manager/Kanban/components/CreateProjectModal.tsx`, `src/api/projectApi.ts`, `project-management-service/.../ProjectController.java`.
- **Impact:** A newly created modal project is only in in-memory React state.
- **Screenshot:** Not available; no browser reproduction performed.

## BR-04 — Kanban task operations are local and task routes are not gateway-routed

- **Status:** Source-confirmed missing integration; runtime persistence failure not separately reproduced.
- **Severity:** High for task workflow.
- **Area:** Kanban tasks.
- **Reproduction:** Open `/kanban`, create or edit a task or change its status, then reload.
- **Expected:** Task changes are stored by Project Management Service and remain after reload.
- **Observed in source:** Task create/edit/delete/move update local React state. Backend task endpoints exist under `/api/tasks/**` and `/api/projects/{id}/tasks`, while the gateway declares project routes but no `/api/tasks/**` route. The page does not call task APIs.
- **Evidence:** `src/features/manager/Kanban/pages/KanbanPage.tsx`, `project-management-service/.../TaskController.java`, `api-gatway/src/main/resources/application.yml`.
- **Impact:** Task mutations cannot currently be verified as persisted through the visible UI.
- **Screenshot:** Not available; no browser reproduction performed.

## BR-05 — Dashboard statistics endpoint does not match the routed service

- **Status:** Source-confirmed endpoint mismatch; runtime reproduction pending.
- **Severity:** Medium; dashboard statistics may fail to load.
- **Area:** Admin dashboard.
- **Reproduction:** Sign in as administrator and load `/dashboard`.
- **Expected:** Dashboard statistics load from the users API.
- **Observed in source:** Frontend requests `/api/users/stats`; gateway routes that path to User-service, whose inspected controller exposes list/profile/departments but no `/stats` handler. Auth-service has user statistics under its own `/api/auth/users/stats` route.
- **Evidence:** `src/features/dashboard` API usage, `src/api/userApi.ts`, `api-gatway/src/main/resources/application.yml`, User-service `UserProfileController.java`, Auth-service `UserController.java`.
- **Impact:** Dashboard stats may return a not-found response unless an uninspected gateway/service mapping handles it.
- **Screenshot:** Not available; no browser reproduction performed.

## Runtime verification status

No backend-dependent case was run as part of the current implementation. Set up an isolated QA environment and capture Allure screenshots/logs before changing these statuses to runtime reproduced or resolved. No application code was modified by the QA test-suite work.


## QA-AUTH-20261020 — Application login times out before user-list test

Severity: High. Status: Open; reproduced during repair verification. Run UserListTest against localhost:5173 with the configured QA administrator. Expected: login reaches /dashboard and the users table loads. Actual: the frontend displays `timeout of 30000ms exceeded`; the test fails in setup, before checking headers. Source logs also contain earlier authenticated /api/users 401s, but their exact common cause has not been established. Unauthenticated availability checks respond promptly, so service reachability alone does not resolve this issue. No credential values are included.
