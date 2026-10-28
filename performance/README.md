# API performance baseline

> Historical document: some implementation and execution statements describe an earlier state. See the [published evidence summary](../docs/results/README.md) and the root README for the current publication scope. Source-inspection findings are not automatically current runtime defects.

This is a small, read-only k6 baseline for the project's observed `GET /api/projects` API route. The frontend API client uses port `9000`, and the API gateway routes `/api/projects/**` to the project-management service. No endpoint or mutation was added for this script.

## Scope and safety

- One virtual user executes ten sequential GET requests, pausing one second between requests.
- k6 reports request duration (including p90/p95), request rate, and HTTP failures. The only threshold is functional: all responses must be 2xx. No pass/fail latency SLA is configured because the application has no documented performance target.
- The script rejects non-loopback hosts and requires `QA_PERF_ALLOW_LOCAL_API=true` before it sends requests.
- The endpoint is read-only at the HTTP-method level. It can still query the service's configured database, so run only when the local service is pointed at a dedicated test database and the application owner has approved the load.
- The script does not print response bodies or the optional bearer token.

This is a smoke-sized baseline, not a capacity or stress test. Ten samples are not sufficient to establish a production SLA or peak-load capacity.

## Prerequisites

- k6 installed locally.
- The API gateway and project-management service running at the configured loopback address.
- A dedicated, non-production test database configured for those services.
- If the API requires authentication, a short-lived test token supplied through `QA_PERF_BEARER_TOKEN`.

The current source audit found remote PostgreSQL datasource configuration and a port collision on `8082` between User-service and project-management-service. Do not launch these services for this test until their datasource is confirmed to be isolated and the port setup is resolved.

## Run

From the `Collaborative-Platform-QA` directory in PowerShell:

```powershell
$env:QA_PERF_ALLOW_LOCAL_API = "true"
$env:QA_PERF_BASE_URL = "http://localhost:9000/api"
# Optional only if the local API requires authentication:
$env:QA_PERF_BEARER_TOKEN = "<short-lived test token>"
k6 run --summary-export performance/results/projects-read-baseline.json performance/k6/projects-read-baseline.js
```

Do not put a real token in a tracked file or pass one in a command that will be saved in shell history. Set it privately in the current process environment. Unset sensitive variables after the run:

```powershell
Remove-Item Env:QA_PERF_BEARER_TOKEN -ErrorAction SilentlyContinue
Remove-Item Env:QA_PERF_ALLOW_LOCAL_API -ErrorAction SilentlyContinue
```

`QA_PERF_BASE_URL` defaults to `http://localhost:9000/api`; only `localhost`, `127.0.0.1`, and `::1` are accepted. Results are written under the ignored `performance/results/` directory.

## Status

The k6 script is authored but has not been executed: k6 is not installed and the gateway/service ports were not listening at the last local check. No performance measurements are claimed.
