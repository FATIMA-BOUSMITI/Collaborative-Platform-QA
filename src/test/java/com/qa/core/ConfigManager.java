package com.qa.core;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import org.junit.jupiter.api.Assumptions;

public final class ConfigManager {

    private static final String DEFAULT_BASE_URL = "http://localhost:5173";
    private static final long DEFAULT_WAIT_SECONDS = 30;

    private ConfigManager() {
    }

    public static URI baseUri() {
        String configuredUrl = setting("qa.baseUrl", "QA_BASE_URL")
                .orElse(DEFAULT_BASE_URL);
        URI uri = URI.create(configuredUrl);
        if (!("http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new IllegalArgumentException(
                    "QA_BASE_URL must be an absolute HTTP or HTTPS URL.");
        }
        return uri;
    }

    public static Browser browser() {
        String value = setting("qa.browser", "QA_BROWSER")
                .orElse("chrome")
                .toLowerCase(Locale.ROOT);
        try {
            return Browser.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported QA_BROWSER '" + value + "'. Use chrome or firefox.",
                    exception);
        }
    }

    public static boolean headless() {
        return setting("qa.headless", "QA_HEADLESS")
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    public static Duration explicitWait() {
        String raw = setting("qa.waitSeconds", "QA_WAIT_SECONDS")
                .orElse(Long.toString(DEFAULT_WAIT_SECONDS));
        try {
            long seconds = Long.parseLong(raw);
            if (seconds < 1 || seconds > 120) {
                throw new IllegalArgumentException(
                        "QA_WAIT_SECONDS must be between 1 and 120.");
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "QA_WAIT_SECONDS must be a whole number of seconds.",
                    exception);
        }
    }

    public static Credentials validCredentials() {
        return new Credentials(
                requiredSetting("QA_VALID_EMAIL"),
                requiredSetting("QA_VALID_PASSWORD"));
    }

    public static Credentials adminCredentials() {
        return new Credentials(
                requiredSetting("QA_ADMIN_EMAIL"),
                requiredSetting("QA_ADMIN_PASSWORD"));
    }

    public static Credentials nonAdminCredentials() {
        return new Credentials(
                requiredSetting("QA_NON_ADMIN_EMAIL"),
                requiredSetting("QA_NON_ADMIN_PASSWORD"));
    }

    public static Credentials invalidCredentials() {
        return new Credentials(
                requiredSetting("QA_INVALID_EMAIL"),
                requiredSetting("QA_INVALID_PASSWORD"));
    }

    public static void requireIsolatedQaData() {
        String isolatedData = setting("qa.test.data.isolated", "QA_TEST_DATA_ISOLATED")
                .orElse("");
        if (!"true".equalsIgnoreCase(isolatedData)) {
            throw new IllegalStateException(
                    "User-management tests can create, update, or delete records. "
                            + "Set QA_TEST_DATA_ISOLATED=true only when the backend uses a dedicated QA database.");
        }
    }

    public static boolean isIsolatedQaData() {
        return "true".equalsIgnoreCase(setting("qa.test.data.isolated", "QA_TEST_DATA_ISOLATED")
                .orElse(""));
    }

    public static void assumeIsolatedQaData() {
        Assumptions.assumeTrue(isIsolatedQaData(),
                "Skipped: this test writes QA data and requires QA_TEST_DATA_ISOLATED=true.");
    }

    public static String validLandingPath() {
        String path = requiredSetting("QA_VALID_LANDING_PATH");
        if (!path.matches("/(dashboard|director|manager|employee)")) {
            throw new IllegalArgumentException(
                    "QA_VALID_LANDING_PATH must match a landing route declared by the application.");
        }
        return path;
    }

    private static String requiredSetting(String name) {
        return setting("qa." + toPropertyName(name), name)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalStateException(
                        "Required test setting is missing: " + name
                                + ". Configure it in the process environment; do not store credentials in source."));
    }

    private static java.util.Optional<String> setting(
            String systemProperty,
            String environmentVariable) {
        String property = System.getProperty(systemProperty);
        if (property != null && !property.isBlank()) {
            return java.util.Optional.of(property.trim());
        }
        String environment = System.getenv(environmentVariable);
        if (environment != null && !environment.isBlank()) {
            return java.util.Optional.of(environment.trim());
        }
        String localValue = EnvFile.get(environmentVariable);
        if (localValue != null && !localValue.isBlank()) {
            return java.util.Optional.of(localValue.trim());
        }
        return java.util.Optional.empty();
    }

    private static String toPropertyName(String environmentVariable) {
        return environmentVariable.toLowerCase(Locale.ROOT)
                .replace('_', '.');
    }

    public enum Browser {
        CHROME,
        FIREFOX
    }

    public record Credentials(String email, String password) {
    }
}
