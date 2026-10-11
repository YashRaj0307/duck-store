package com.yash.duckstore.domain;

public enum PackageType {
    WOOD("Wood"),
    CARDBOARD("Cardboard"),
    PLASTIC("Plastic");

    private final String displayName;

    PackageType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
