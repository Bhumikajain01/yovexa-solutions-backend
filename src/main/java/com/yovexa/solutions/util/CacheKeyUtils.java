package com.yovexa.solutions.util;

import java.util.Locale;

/**
 * Standardized cache key generator for Redis.
 * Ensures consistent cache hits across varying frontend request patterns
 * (e.g. null, empty string "", uppercase "ALL", lowercase "all", hyphens vs underscores).
 */
public final class CacheKeyUtils {

    private CacheKeyUtils() {}

    /**
     * Normalizes a category filter so that null, empty strings, "all", "ALL", etc.
     * always resolve to "all", and names like "web-applications", "Web Applications",
     * "web_applications" all resolve to "WEB_APPLICATIONS".
     */
    public static String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty() || "ALL".equalsIgnoreCase(category.trim())) {
            return "all";
        }
        return category.trim().toUpperCase(Locale.ENGLISH).replaceAll("[\\s-]+", "_");
    }

    /**
     * Normalizes search keywords by trimming and lowercasing so that null, empty,
     * or whitespace-only queries always resolve to empty string "".
     */
    public static String normalizeSearch(String search) {
        if (search == null || search.trim().isEmpty()) {
            return "";
        }
        return search.trim().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Normalizes status filter (e.g. "DRAFT", "PUBLISHED", "ALL" -> "all").
     */
    public static String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status.trim())) {
            return "all";
        }
        return status.trim().toUpperCase(Locale.ENGLISH);
    }

    /**
     * Standardized cache key for public listing endpoints.
     * Example: "public-all-" or "public-WEB_APPLICATIONS-ecommerce"
     */
    public static String publicListKey(String category, String search) {
        return "public-" + normalizeCategory(category) + "-" + normalizeSearch(search);
    }

    /**
     * Standardized cache key for admin paginated listing endpoints.
     * Example: "admin--all-all-0-10"
     */
    public static String adminListKey(String search, String category, String status, int page, int size) {
        return "admin-" + normalizeSearch(search) + "-" + normalizeCategory(category) + "-"
                + normalizeStatus(status) + "-" + page + "-" + size;
    }

    /**
     * Normalizes slug so that lowercase/uppercase always hit the same key.
     */
    public static String slugKey(String slug) {
        return "slug-" + (slug != null ? slug.trim().toLowerCase(Locale.ENGLISH) : "");
    }
}
