# QA Traceability Matrix

| Requirement / feature | Automation asset | Level | Execution condition |
|---|---|---|---|
| Empty login validation | `EmptyLoginValidationTest` | Selenium UI | Frontend reachable |
| Protected routes | `ProtectedRouteTest` | Selenium UI | Frontend reachable |
| Valid login and role landing page | `ValidLoginTest` | Selenium UI | Dedicated QA credentials |
| Invalid login | `InvalidLoginTest` | Selenium UI | Dedicated invalid credentials configuration |
| Logout and local token cleanup | `LogoutTest` | Selenium UI | Dedicated QA credentials |
| JWT login response | `robot-framework/api/authentication.robot` | Robot API | Dedicated QA API account |
| Invalid API login | `robot-framework/api/authentication.robot` | Robot API | QA API target |
| Project-list smoke | `performance/k6/smoke/projects-read.js` | k6 | Explicit local or approved QA target |
| Project-list load | `performance/k6/load/projects-read.js` | k6 | Approved QA target only |
| Project-list stress | `performance/k6/stress/projects-read.js` | k6 | Approved QA target only; never production |

User, department, project, task, permission, and messaging mutations remain
blocked until the relevant services use demonstrably isolated QA data and their
API contracts are confirmed end-to-end.
