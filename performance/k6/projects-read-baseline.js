import http from "k6/http";
import { check, sleep } from "k6";

const configuredBaseUrl = __ENV.QA_PERF_BASE_URL || "http://localhost:9000/api";
const apiBaseUrl = new URL(configuredBaseUrl);
const allowedHosts = new Set(["localhost", "127.0.0.1", "::1"]);

if (__ENV.QA_PERF_ALLOW_LOCAL_API !== "true") {
  throw new Error("Set QA_PERF_ALLOW_LOCAL_API=true to explicitly allow local API traffic.");
}

if (!allowedHosts.has(apiBaseUrl.hostname)
    || apiBaseUrl.protocol !== "http:"
    || apiBaseUrl.username
    || apiBaseUrl.password
    || apiBaseUrl.pathname.replace(/\/+$/, "") !== "/api") {
  throw new Error(
    "QA_PERF_BASE_URL must be a loopback HTTP URL ending in /api, without credentials."
  );
}

const projectsUrl = `${apiBaseUrl.origin}/api/projects`;
const headers = __ENV.QA_PERF_BEARER_TOKEN
  ? { Authorization: `Bearer ${__ENV.QA_PERF_BEARER_TOKEN}` }
  : {};

export const options = {
  thresholds: {
    checks: ["rate==1"],
  },
  scenarios: {
    readOnlyBaseline: {
      executor: "shared-iterations",
      vus: 1,
      iterations: 10,
      maxDuration: "30s",
    },
  },
};

export default function () {
  const response = http.get(projectsUrl, {
    headers,
    tags: { endpoint: "GET /api/projects" },
  });

  check(response, {
    "GET /api/projects returns a 2xx response": (res) =>
      res.status >= 200 && res.status < 300,
  });

  sleep(1);
}
