package com.digitalharbor.duckstore.entity.enums;

import com.digitalharbor.duckstore.common.Labeled;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Size implements Labeled {
    XLARGE("XLarge"), LARGE("Large"), MEDIUM("Medium"), SMALL("Small"), XSMALL("XSmall");

    private final String label;

    Size(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static Size from(String value) {
        return Labeled.parse(Size.class, value, "size");
    }
}
