package com.yash.duckstore.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum DuckColor {
    RED("Red"),
    GREEN("Green"),
    YELLOW("Yellow"),
    BLACK("Black");

    private final String displayName;

    DuckColor(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String displayName() {
        return displayName;
    }

    @JsonCreator
    public static DuckColor fromJson(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(color -> color.name().equalsIgnoreCase(value.trim())
                        || color.displayName.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported color '" + value + "'. Allowed values: Red, Green, Yellow, Black"));
    }
}
