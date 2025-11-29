package com.example.baoNgoCv.exception.registrationException;

import lombok.Getter;

import java.util.Map;

@Getter
public class DuplicateRegistrationDataException extends RuntimeException {

    private final Map<String, String> errors;

    public DuplicateRegistrationDataException(String field, String message) {
        super(String.format("Duplicate data for field '%s': %s", field, message));
        this.errors = Map.of(field, message);
    }
}
