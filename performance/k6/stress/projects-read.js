import http from "k6/http";
import { check } from "k6";
import { authorizationHeaders, requireSafeLocalTarget } from "../lib/config.js";

export const options = { stages: [{ duration: "1m", target: 10 }, { duration: "2m", target: 20 }, { duration: "1m", target: 0 }], thresholds: { http_req_failed: ["rate<0.02"] } };
const baseUrl = requireSafeLocalTarget();
export default function () {
  const response = http.get(`${baseUrl}/api/projects`, { headers: authorizationHeaders() });
  check(response, { "projects list succeeds": (r) => r.status >= 200 && r.status < 300 });
}
