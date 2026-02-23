package com.valid8.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Utility methods for parsing date/time strings from CSV files.
 * Tries multiple common formats so the parser is flexible.
 */
public class TimeUtils {

    // Formats tried in order
    private static final List<DateTimeFormatter> FORMATTERS = List.of(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    /**
     * Parse a datetime string using any of the known formats.
     *
     * @param raw The raw string from CSV
     * @return LocalDateTime, or null if blank or unparseable
     */
    public static LocalDateTime parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        for (DateTimeFormatter fmt : FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        // If nothing worked, log and return null
        System.err.println("[TimeUtils] Could not parse datetime: '" + trimmed + "'");
        return null;
    }

    private TimeUtils() {}
}
