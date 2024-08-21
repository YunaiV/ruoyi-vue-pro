package com.somle.esb.model;

public enum Domain {
    ECCANG("eccang"),
    AMAZONSP("amazonsp"),
    AMAZONAD("amazonad"),
    KINGDEE("kingdee"),
    AI("ai"),
    ERP("erp"),
    MATOMO("matomo"),
    DINGTALK("dingtalk");

    private final String value;

    // Constructor to initialize the enum constant with a string value
    Domain(String value) {
        this.value = value;
    }

    // Overloaded constructor for enum constants without a specific string value
    Domain() {
        this.value = this.name().toLowerCase(); // Default to the enum name in lowercase
    }

    // Method to get the string value
    public String getValue() {
        return value;
    }

    // Static method to convert a string to the corresponding enum constant
    public static Domain fromString(String value) {
        for (Domain domain : Domain.values()) {
            if (domain.value.equalsIgnoreCase(value)) {
                return domain;
            }
        }
        throw new IllegalArgumentException("Unknown domain: " + value);
    }
}
