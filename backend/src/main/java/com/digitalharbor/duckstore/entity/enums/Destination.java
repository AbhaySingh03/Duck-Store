package com.digitalharbor.duckstore.entity.enums;

import java.util.Locale;
import java.util.Set;


public enum Destination {
    USA(18, "usa", "us", "u.s.", "u.s.a.", "united states", "united states of america"),
    BOLIVIA(13, "bolivia"),
    INDIA(19, "india"),
    OTHER(15);

    private final int surchargePercent;
    private final Set<String> names;

    Destination(int surchargePercent, String... names) {
        this.surchargePercent = surchargePercent;
        this.names = Set.of(names);
    }

    public int surchargePercent() {
        return surchargePercent;
    }


    public static Destination from(String country) {
        String normalized = country == null ? "" : country.trim().toLowerCase(Locale.ROOT);
        for (Destination d : values()) {
            if (d.names.contains(normalized)) {
                return d;
            }
        }
        return OTHER;
    }
}
