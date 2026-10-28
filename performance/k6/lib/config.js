export function requireSafeLocalTarget() {
  const baseUrl = __ENV.K6_BASE_URL || "http://127.0.0.1:9000";
  // k6 does not provide the browser's global URL constructor.
  // Accept an HTTP(S) origin only: callers append /api themselves.
  const match = /^(https?):\/\/(\[[0-9a-f:]+\]|[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?)(?::([0-9]{1,5}))?\/?$/i.exec(baseUrl);
  if (!match || (match[3] && (Number(match[3]) < 1 || Number(match[3]) > 65535))) {
    throw new Error("K6_BASE_URL must be an HTTP(S) origin, without credentials, path, query or fragment.");
  }
  const hostname = match[2].toLowerCase();
  const localHosts = ["127.0.0.1", "localhost", "[::1]"];
  if (localHosts.includes(hostname) && __ENV.K6_ALLOW_LOCAL === "true") {
    return baseUrl.replace(/\/$/, "");
  }
  if (!localHosts.includes(hostname) && __ENV.K6_ALLOW_TEST_ENVIRONMENT === "true") {
    return baseUrl.replace(/\/$/, "");
  }
  throw new Error("Set K6_ALLOW_LOCAL=true for loopback, or K6_ALLOW_TEST_ENVIRONMENT=true for an approved non-production test target.");
}

export function authorizationHeaders() {
  return __ENV.K6_ACCESS_TOKEN ? { Authorization: `Bearer ${__ENV.K6_ACCESS_TOKEN}` } : {};
}