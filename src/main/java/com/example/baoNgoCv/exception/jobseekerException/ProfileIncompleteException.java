package com.example.baoNgoCv.exception.jobseekerException;

public class ProfileIncompleteException extends RuntimeException {
    public ProfileIncompleteException() {
        super("Please update your profile before applying for a job.");
    }
}
