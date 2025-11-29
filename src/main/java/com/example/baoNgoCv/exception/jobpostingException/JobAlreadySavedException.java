package com.example.baoNgoCv.exception.jobpostingException;

public class JobAlreadySavedException extends RuntimeException {
    public JobAlreadySavedException(String message) {
        super(message);
    }

    public JobAlreadySavedException(Long jobId) {
        super("Job with ID " + jobId + " has already been saved");
    }
}
