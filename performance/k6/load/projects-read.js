import http from "k6/http";
import { check } from "k6";
import { authorizationHeaders, requireSafeLocalTarget } from "../lib/config.js";

export const options = { stages: [{ duration: "30s", target: 5 }, { duration: "1m", target: 5 }, { duration: "30s", target: 0 }], thresholds: { http_req_failed: ["rate<0.01"], http_req_duration: ["p(95)<1000"] } };
const baseUrl = requireSafeLocalTarget();
export default function () {
  const response = http.get(`${baseUrl}/api/projects`, { headers: authorizationHeaders() });
  check(response, { "projects list succeeds": (r) => r.status >= 200 && r.status < 300 });
}
