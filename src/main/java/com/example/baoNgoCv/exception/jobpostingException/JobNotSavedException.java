package com.example.baoNgoCv.exception.jobpostingException;

public class JobNotSavedException extends RuntimeException {
    public JobNotSavedException(String message) {
        super(message);
    }

    public JobNotSavedException(Long jobId) {
        super("Job with ID " + jobId + " was not saved by current user");
    }
}
