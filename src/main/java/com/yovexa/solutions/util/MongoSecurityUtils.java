package com.yovexa.solutions.util;

import java.util.regex.Pattern;

/**
 * Security utilities for database queries and input sanitization.
 * Prevents NoSQL / MongoDB regex injection and ReDoS attacks.
 */
public final class MongoSecurityUtils {

    private MongoSecurityUtils() {}

    /**
     * Escapes regex metacharacters in user query string to treat it strictly as a literal substring,
     * preventing ReDoS and unauthorized pattern matching in MongoDB.
     *
     * @param input Raw search keyword or category filter from user
     * @return Escaped literal pattern safe for MongoDB regex evaluation
     */
    public static String escapeRegex(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return Pattern.quote(input.trim());
    }

    /**
     * Sanitizes plain text input by stripping control characters and HTML tags
     * to prevent stored XSS and log forging.
     */
    public static String sanitizePlainText(String input) {
        if (input == null) {
            return null;
        }
        // Remove HTML tags and control characters except basic whitespace
        return input.replaceAll("<[^>]*>", "")
                    .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                    .trim();
    }

    /**
     * Sanitizes header/subject text by removing carriage returns and line feeds
     * to prevent SMTP header injection attacks.
     */
    public static String sanitizeHeader(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[\\r\\n]", " ").trim();
    }
}
