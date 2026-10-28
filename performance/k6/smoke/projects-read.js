import http from "k6/http";
import { check } from "k6";
import { authorizationHeaders, requireSafeLocalTarget } from "../lib/config.js";

export const options = { vus: 1, iterations: 10, thresholds: { http_req_failed: ["rate<0.01"] } };
const baseUrl = requireSafeLocalTarget();
export default function () {
  const response = http.get(`${baseUrl}/api/projects`, { headers: authorizationHeaders() });
  check(response, { "projects endpoint responds successfully": (r) => r.status >= 200 && r.status < 300 });
}
