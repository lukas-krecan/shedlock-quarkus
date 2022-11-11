package net.javacrumbs.quarkus;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

class Utils {
    private static final Pattern START_WITH_DIGITS = Pattern.compile("^[-+]?\\d+.*");
    static Duration parseDuration(String value) {
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            if (START_WITH_DIGITS.matcher(value).matches()) {
                return Duration.parse("PT" + value);
            }

            return Duration.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
