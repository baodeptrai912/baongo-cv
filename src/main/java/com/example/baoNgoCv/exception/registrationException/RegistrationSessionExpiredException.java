package com.example.baoNgoCv.exception.registrationException;

public class RegistrationSessionExpiredException extends RuntimeException {
    public RegistrationSessionExpiredException(String message) {
        super(message);
    }
}
