const { test } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");
const source = fs.readFileSync(require("node:path").join(__dirname, "config.js"), "utf8").replace(/export function/g, "function");
function target(env) {
  return vm.runInNewContext(source + "\nrequireSafeLocalTarget();", { __ENV: env, URL: undefined });
}
test("works without global URL and supports loopback hosts", () => {
  for (const host of ["localhost", "LOCALHOST", "127.0.0.1", "[::1]"]) {
    const url = `http://${host}:9000`;
    assert.equal(target({ K6_BASE_URL: url + "/", K6_ALLOW_LOCAL: "true" }), url);
  }
});
test("requires the appropriate explicit target opt-in", () => {
  assert.throws(() => target({ K6_BASE_URL: "http://localhost:9000", K6_ALLOW_TEST_ENVIRONMENT: "true" }));
  assert.throws(() => target({ K6_BASE_URL: "https://qa.example.test", K6_ALLOW_LOCAL: "true" }));
  assert.equal(target({ K6_BASE_URL: "http://host.docker.internal:9000", K6_ALLOW_TEST_ENVIRONMENT: "true" }), "http://host.docker.internal:9000");
});
test("rejects malformed origins, credentials and path suffixes", () => {
  for (const url of ["ftp://localhost", "http://user:pass@localhost", "http://localhost/api", "http://localhost?host=qa", "http://localhost#qa", "http://localhost:0", "http://localhost:65536", "http://localhost\n"]) {
    assert.throws(() => target({ K6_BASE_URL: url, K6_ALLOW_LOCAL: "true", K6_ALLOW_TEST_ENVIRONMENT: "true" }), url);
  }
});