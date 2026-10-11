package com.digitalharbor.duckstore.entity.enums;

import com.digitalharbor.duckstore.common.Labeled;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Color implements Labeled {
    RED("Red"), GREEN("Green"), YELLOW("Yellow"), BLACK("Black");

    private final String label;

    Color(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static Color from(String value) {
        return Labeled.parse(Color.class, value, "color");
    }
}
