package com.digitalharbor.duckstore.common;

import java.util.Arrays;

/** Enums that are shown to API clients with a friendly label (e.g. "XLarge") instead of their constant name. */
public interface Labeled {

    String label();

    /** Case-insensitive lookup by label; the error message lists the allowed values. */
    static <E extends Enum<E> & Labeled> E parse(Class<E> type, String value, String field) {
        String wanted = value == null ? "" : value.trim();
        return Arrays.stream(type.getEnumConstants())
                .filter(e -> e.label().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid " + field + " '" + value + "'. Allowed: "
                        + Arrays.stream(type.getEnumConstants()).map(Labeled::label).toList()));
    }
}
