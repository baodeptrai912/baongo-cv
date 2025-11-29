package com.example.baoNgoCv.exception.jobseekerException;

public class JobExperienceNotFoundException extends RuntimeException {
    public JobExperienceNotFoundException() {
        super("Your experience is not found at this moment");
    }
}
