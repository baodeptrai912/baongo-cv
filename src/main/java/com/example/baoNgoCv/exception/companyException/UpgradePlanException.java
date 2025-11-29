package com.example.baoNgoCv.exception.companyException;

public class UpgradePlanException extends RuntimeException {

    public UpgradePlanException(String message) {
        super(message);
    }

    public UpgradePlanException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpgradePlanException(Throwable cause) {
        super(cause);
    }
}
