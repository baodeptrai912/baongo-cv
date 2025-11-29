package com.example.baoNgoCv.exception.securityException;

public class InvalidPasswordResetSessionException extends RuntimeException {
    public InvalidPasswordResetSessionException(String message) {
        super(message);
    }
}
