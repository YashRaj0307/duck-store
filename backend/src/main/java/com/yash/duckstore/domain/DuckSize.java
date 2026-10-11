package com.yash.duckstore.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum DuckSize {
    XLARGE("XLarge"),
    LARGE("Large"),
    MEDIUM("Medium"),
    SMALL("Small"),
    XSMALL("XSmall");

    private final String displayName;

    DuckSize(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String displayName() {
        return displayName;
    }

    @JsonCreator
    public static DuckSize fromJson(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(size -> size.name().equalsIgnoreCase(value.trim())
                        || size.displayName.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported size '" + value + "'. Allowed values: XLarge, Large, Medium, Small, XSmall"));
    }
}
