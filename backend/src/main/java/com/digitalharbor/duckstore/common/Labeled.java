package com.digitalharbor.duckstore.common;

import java.util.Arrays;


public interface Labeled {

    String label();

   
    static <E extends Enum<E> & Labeled> E parse(Class<E> type, String value, String field) {
        String wanted = value == null ? "" : value.trim();
        return Arrays.stream(type.getEnumConstants())
                .filter(e -> e.label().equalsIgnoreCase(wanted))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid " + field + " '" + value + "'. Allowed: "
                        + Arrays.stream(type.getEnumConstants()).map(Labeled::label).toList()));
    }
}
