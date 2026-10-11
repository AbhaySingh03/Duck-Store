package com.digitalharbor.duckstore.entity.enums;

import com.digitalharbor.duckstore.common.Labeled;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PackageType implements Labeled {
    WOOD("Wood"), CARDBOARD("Cardboard"), PLASTIC("Plastic");

    private final String label;

    PackageType(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }
}
