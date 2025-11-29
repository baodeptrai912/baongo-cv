package com.example.baoNgoCv.exception.securityException;

public class PasswordResetSessionExpiredException extends RuntimeException {
    public PasswordResetSessionExpiredException(String message) {
        super(message);
    }
}
