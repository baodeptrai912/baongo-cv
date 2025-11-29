package com.example.baoNgoCv.exception.companyException;

import lombok.Getter;
import java.util.Map;

@Getter
public class InvalidPasswordChangeException extends RuntimeException {
    private final Map<String, String> errors;

    public InvalidPasswordChangeException(Map<String, String> errors) {
        super("Password change validation failed.");
        this.errors = errors;
    }

}
