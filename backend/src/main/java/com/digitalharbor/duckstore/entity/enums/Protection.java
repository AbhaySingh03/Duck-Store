package com.digitalharbor.duckstore.entity.enums;

import com.digitalharbor.duckstore.common.Labeled;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Protection implements Labeled {
    POLYSTYRENE_BALLS("Polystyrene balls"),
    BUBBLE_WRAP_BAGS("Bubble-wrap bags"),
    MOISTURE_ABSORBING_BEADS("Moisture-absorbing beads");

    private final String label;

    Protection(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }
}
