package com.example.baoNgoCv.exception.jobpostingException;

public class JobNotFoundExceptionJson extends RuntimeException {

    public JobNotFoundExceptionJson(String message) {
        super(message);
    }

    public JobNotFoundExceptionJson(String message, Throwable cause) {
        super(message, cause);
    }

    public JobNotFoundExceptionJson(Long jobId) {
        super("Job posting with ID " + jobId + " not found");
    }
}
