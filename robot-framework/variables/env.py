"""Shared Robot variables loaded from the ignored project-root .env file.

Process environment variables have priority, which keeps CI secrets out of
the local file. API credentials reuse QA_VALID_* unless dedicated QA_API_*
variables are explicitly supplied.
"""

from os import environ
from pathlib import Path


def _read_env_file() -> dict[str, str]:
    path = Path(__file__).resolve().parents[2] / ".env"
    values: dict[str, str] = {}
    if not path.is_file():
        return values
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip().strip("\"'")
    return values


def _value(name: str, values: dict[str, str]) -> str:
    return environ.get(name) or values.get(name, "")


def get_variables() -> dict[str, str]:
    values = _read_env_file()
    variables = {name: _value(name, values) for name in (
        "QA_API_BASE_URL", "QA_VALID_EMAIL", "QA_VALID_PASSWORD",
        "QA_INVALID_EMAIL", "QA_INVALID_PASSWORD", "QA_TEST_DATA_ISOLATED",
    )}
    variables["QA_API_EMAIL"] = _value("QA_API_EMAIL", values) or variables["QA_VALID_EMAIL"]
    variables["QA_API_PASSWORD"] = _value("QA_API_PASSWORD", values) or variables["QA_VALID_PASSWORD"]
    return variables
