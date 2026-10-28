package com.qa.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class EnvFile {
    private static final Map<String, String> VALUES = load();

    private EnvFile() {
    }

    static String get(String key) {
        return VALUES.get(key);
    }

    private static Map<String, String> load() {
        Path file = Path.of(System.getProperty("user.dir"), ".env");
        if (!Files.isRegularFile(file)) {
            return Collections.emptyMap();
        }
        Map<String, String> values = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(file)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator < 1) {
                    continue;
                }
                String key = trimmed.substring(0, separator).trim();
                String value = unquote(trimmed.substring(separator + 1).trim());
                values.put(key, value);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read local QA configuration: " + file, exception);
        }
        return Collections.unmodifiableMap(values);
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
