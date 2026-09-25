package com.petstore.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

public final class Config {

    private static final String DEFAULTS_FILE = "config/config.properties";
    private static final String LOCAL_OVERRIDES_FILE = "config/config.local.properties";

    private static Properties fileProperties;

    private Config() {
    }

    public static String baseUrl() {
        String value = required("petstore.base.url");
        try {
            URI uri = URI.create(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new ConfigurationException("'petstore.base.url' must be an absolute URL, got '" + value + "'");
            }
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Invalid 'petstore.base.url': '" + value + "'", e);
        }
        return value;
    }

    public static String basePath() {
        return required("petstore.base.path");
    }

    public static String apiKey() {
        return required("petstore.api.key");
    }

    public static Duration consistencyTimeout() {
        return Duration.ofSeconds(positiveLong("consistency.timeout.seconds"));
    }

    public static Duration consistencyPollInterval() {
        return Duration.ofMillis(positiveLong("consistency.poll.interval.millis"));
    }

    private static long positiveLong(String key) {
        String value = required(key);
        long number;
        try {
            number = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("'" + key + "' must be a whole number, got '" + value + "'", e);
        }
        if (number <= 0) {
            throw new ConfigurationException("'" + key + "' must be positive, got " + value);
        }
        return number;
    }

    private static String required(String key) {
        return optional(key).orElseThrow(() -> new ConfigurationException(
                "Missing required configuration '" + key + "'. Set it in " + DEFAULTS_FILE
                        + ", pass -D" + key + "=<value>, or export " + toEnvName(key) + "."));
    }

    private static Optional<String> optional(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(toEnvName(key));
        }
        if (value == null) {
            value = fileProperties().getProperty(key);
        }
        return Optional.ofNullable(value).map(String::trim).filter(v -> !v.isEmpty());
    }

    private static String toEnvName(String key) {
        return key.replace('.', '_').toUpperCase(Locale.ROOT);
    }

    private static synchronized Properties fileProperties() {
        if (fileProperties == null) {
            Properties properties = new Properties();
            loadInto(properties, DEFAULTS_FILE, true);
            loadInto(properties, LOCAL_OVERRIDES_FILE, false);
            fileProperties = properties;
        }
        return fileProperties;
    }

    private static void loadInto(Properties target, String resource, boolean mandatory) {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                if (mandatory) {
                    throw new ConfigurationException("Configuration file not found on the classpath: " + resource);
                }
                return;
            }
            target.load(in);
        } catch (IOException e) {
            throw new ConfigurationException("Could not read configuration file " + resource, e);
        }
    }
}
