package com.digitalharbor.duckstore.entity.enums;

import com.digitalharbor.duckstore.common.Labeled;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ShippingMode implements Labeled {
    LAND("Land"), AIR("Air"), SEA("Sea");

    private final String label;

    ShippingMode(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static ShippingMode from(String value) {
        return Labeled.parse(ShippingMode.class, value, "shippingMode");
    }
}
