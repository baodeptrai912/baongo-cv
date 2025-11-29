package com.example.baoNgoCv.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AlertFrequency {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY");

    private final String value;

    AlertFrequency(String value) {
        this.value = value;
    }

    @JsonValue  // Jackson sẽ serialize enum thành string value
    public String getValue() {
        return value;
    }

    // Jackson deserialization từ string
    public static AlertFrequency fromValue(String value) {
        for (AlertFrequency frequency : values()) {
            if (frequency.value.equals(value)) {
                return frequency;
            }
        }
        throw new IllegalArgumentException("Invalid frequency: " + value);
    }
}
