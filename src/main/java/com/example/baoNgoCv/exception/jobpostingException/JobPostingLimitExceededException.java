package com.example.baoNgoCv.exception.jobpostingException;

public class JobPostingLimitExceededException extends RuntimeException {
    public JobPostingLimitExceededException(String message) {
        super(message);
    }
}
