package com.yash.duckstore.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ShippingMode {
    LAND("Land"),
    AIR("Air"),
    SEA("Sea");

    private final String displayName;

    ShippingMode(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String displayName() {
        return displayName;
    }

    @JsonCreator
    public static ShippingMode fromJson(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(mode -> mode.name().equalsIgnoreCase(value.trim())
                        || mode.displayName.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported shipping mode '" + value + "'. Allowed values: Land, Air, Sea"));
    }
}
