package com.example.baoNgoCv.exception.securityException;

public class RateLimitExceededException extends RuntimeException {
    private final long remainingSeconds;

    public RateLimitExceededException(String message, long remainingSeconds) {
        super(message);
        this.remainingSeconds = remainingSeconds;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
