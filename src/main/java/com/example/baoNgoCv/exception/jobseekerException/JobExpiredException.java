package com.example.baoNgoCv.exception.jobseekerException;

public class JobExpiredException extends RuntimeException {
    public JobExpiredException( ) {
        super("This job posting has expired. You can no longer apply.");
    }
}
