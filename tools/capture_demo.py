"""Capture actual UI states with real authentication and no mocked API data."""
from pathlib import Path
from datetime import datetime, timezone
import argparse
import json
import os
import re
import time
import urllib.request

from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout

parser = argparse.ArgumentParser()
parser.add_argument("--project", type=Path, required=True)
parser.add_argument("--base-url", default="http://127.0.0.1:5173")
parser.add_argument("--skip-admin", action="store_true")
parser.add_argument("--member-only", action="store_true")
args = parser.parse_args()
root = args.project
private = root / "reports/demo-runtime"
private.mkdir(parents=True, exist_ok=True)
output = root / "docs/demo/screenshots"
output.mkdir(parents=True, exist_ok=True)
settings = dict(os.environ)
for line in (root / ".env").read_text(encoding="utf-8-sig").splitlines():
    if "=" in line and not line.lstrip().startswith("#"):
        key, value = line.split("=", 1)
        settings.setdefault(key.strip(), value.strip().strip("\"'"))
manifest = {"captured_at_utc": datetime.now(timezone.utc).isoformat(), "base_url": args.base_url, "mode": "Real frontend and compiled native backend; no API response mocking", "screens": [], "blocked_writes": 0, "api_statuses": {}, "limitations": []}
if args.skip_admin and (private / "capture-manifest.json").exists():
    previous = json.loads((private / "capture-manifest.json").read_text(encoding="utf-8"))
    manifest["api_statuses"] = {path: statuses for path, statuses in previous["api_statuses"].items() if path.startswith("/api/")}
    if args.member_only:
        manifest["screens"] = previous["screens"]
        if (output / "10-kanban.png").exists() and not any(s["file"].endswith("10-kanban.png") for s in manifest["screens"]):
            manifest["screens"].append({"file": "screenshots/10-kanban.png", "title": "Projets et Kanban", "route": "/kanban", "scope": "First successful real manager login; GET /api/projects returned 500", "captured_at_utc": datetime.fromtimestamp((output / "10-kanban.png").stat().st_mtime, timezone.utc).isoformat()})

# Wait for the actual local frontend. No background service is represented as ready merely by its PID.
deadline = time.monotonic() + 120
while True:
    try:
        with urllib.request.urlopen(args.base_url, timeout=3) as response:
            if response.status == 200:
                break
    except Exception:
        if time.monotonic() > deadline:
            raise SystemExit("Frontend unavailable; no screenshots generated.")
        time.sleep(2)

with sync_playwright() as playwright:
    browser = playwright.chromium.launch(channel="chrome", headless=True)
    context = browser.new_context(viewport={"width": 1440, "height": 960}, device_scale_factor=1)

    def guard(route):
        request = route.request
        if "/api/" in request.url and request.method not in ("GET", "HEAD", "OPTIONS"):
            if not re.search(r"/api/auth/(login|refresh-token|logout)(?:\?|$)", request.url):
                manifest["blocked_writes"] += 1
                route.abort()
                return
        route.continue_()

    context.route("**/*", guard)
    page = context.new_page()
    page.set_default_timeout(20000)

    def observed(response):
        if "/api/" in response.url:
            from urllib.parse import urlsplit
            path = urlsplit(response.url).path
            if not path.startswith("/api/"):
                return
            # Resource identifiers, response bodies and tokens are never exported.
            path = re.sub(r"[0-9a-fA-F]{8}-[0-9a-fA-F-]{27,}", ":id", path)
            path = re.sub(r"/\d+(?=/|$)", "/:id", path)
            manifest["api_statuses"].setdefault(path, [])
            if response.status not in manifest["api_statuses"][path]:
                manifest["api_statuses"][path].append(response.status)

    page.on("response", observed)

    def capture(slug, title, scope, expected=None):
        if expected:
            page.locator(expected).first.wait_for(state="visible")
        page.wait_for_timeout(1400)
        try:
            page.evaluate("document.fonts.ready")
        except Exception:
            pass
        # Masks preserve the original page layout without changing business data.
        page.locator("input[type=email], input[type=password]").evaluate_all("inputs => inputs.forEach(input => input.toggleAttribute('data-demo-sensitive', Boolean(input.value)))")
        masks = [page.locator("tbody"), page.locator("input[data-demo-sensitive]"), page.locator(".user-info, .user-details, .user-name, .user-email, .profile-info, .profile-name, .profile-email, .header-user, .topbar-user, .admin-user, .account-info, .admin-identity, .reference-activity")]
        masks.append(page.locator("*:not(:has(*))").filter(has_text=re.compile(r"[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}", re.I)))
        filename = slug + ".png"
        page.screenshot(path=str(output / filename), full_page=True, mask=masks, mask_color="#25344c", animations="disabled")
        manifest["screens"] = [screen for screen in manifest["screens"] if screen["file"] != "screenshots/" + filename]
        manifest["screens"].append({"file": "screenshots/" + filename, "title": title, "route": page.url.replace(args.base_url, ""), "scope": scope, "captured_at_utc": datetime.now(timezone.utc).isoformat()})
        print(json.dumps({"captured": slug}), flush=True)

    if not args.member_only:
        page.goto(args.base_url, wait_until="domcontentloaded")
        capture("01-login", "Authentification", "Interface réelle ; aucune connexion encore effectuée", "form.login-form")
        page.locator("form.login-form button[type=submit]").click()
        capture("02-login-validation", "Validation des champs obligatoires", "Soumission vide côté frontend ; aucun identifiant envoyé")
        page.goto(args.base_url + "/forgot-password", wait_until="domcontentloaded")
        capture("03-forgot-password", "Mot de passe oublié", "Formulaire affiché sans soumission ni envoi de mail", "h1")
        page.goto(args.base_url + "/reset-password", wait_until="domcontentloaded")
        capture("04-reset-password", "Lien de réinitialisation absent", "État réel sans token ; aucun mot de passe modifié", "form, h1, h2")

    for role, email_key, password_key, routes in [
        ("admin", "QA_ADMIN_EMAIL", "QA_ADMIN_PASSWORD", [("/dashboard", "05-admin-dashboard", "Tableau de bord administrateur"), ("/users", "06-users", "Gestion des utilisateurs"), ("/departments", "07-departments", "Départements"), ("/roles", "08-roles", "Rôles"), ("/permissions", "09-permissions", "Matrice des permissions")]),
        ("member", "QA_VALID_EMAIL", "QA_VALID_PASSWORD", [("/manager", "05-manager-dashboard", "Tableau de bord manager"), ("/kanban", "10-kanban", "Projets et Kanban")]),
    ]:
        if role == "admin" and args.skip_admin:
            manifest["limitations"].append("Admin login returned HTTP 401 in the first capture attempt; authenticated admin screens are not included.")
            continue
        email, password = settings.get(email_key), settings.get(password_key)
        if not email or not password:
            manifest["limitations"].append(role + ": QA credentials unavailable")
            continue
        context.clear_cookies()
        page.goto(args.base_url, wait_until="domcontentloaded")
        page.evaluate("localStorage.clear();sessionStorage.clear()")
        page.reload(wait_until="domcontentloaded")
        try:
            page.locator("input[type=email]").fill(email)
            page.locator("input[type=password]").fill(password)
            page.locator("form.login-form button[type=submit]").click()
            page.wait_for_url(re.compile(r"/(dashboard|director|manager|employee)(?:$|[?#])"), timeout=65000)
        except PlaywrightTimeout:
            manifest["limitations"].append(role + ": real login did not reach an authenticated route within 65 seconds")
            print(json.dumps({"login": role, "status": "unavailable"}), flush=True)
            continue
        for route, slug, title in routes:
            try:
                page.goto(args.base_url + route, wait_until="domcontentloaded")
                page.locator("main").wait_for(state="visible", timeout=20000)
                if page.url.rstrip("/") == args.base_url:
                    manifest["limitations"].append(slug + ": redirected to login")
                    continue
                try:
                    page.wait_for_load_state("networkidle", timeout=10000)
                except PlaywrightTimeout:
                    pass
                capture(slug, title, "Consultation authentifiée réelle ; données personnelles et tableaux masqués. Les erreurs éventuelles restent visibles.")
                if route == "/kanban":
                    page.get_by_role("button", name=re.compile("Nouveau projet")).click()
                    capture("11-project-form", "Formulaire de nouveau projet", "Formulaire réel ouvert sans saisie ni soumission ; aucun projet créé", ".project-modal")
            except PlaywrightTimeout:
                manifest["limitations"].append(slug + ": page unavailable or loading timeout")
    # Ending the browser session does not change application business records.
    browser.close()

manifest["screens"].sort(key=lambda screen: screen["file"])
(private / "capture-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
print(json.dumps({"total_screens": len(manifest["screens"]), "limitations": manifest["limitations"]}, ensure_ascii=False), flush=True)
