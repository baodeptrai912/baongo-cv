package com.example.baoNgoCv.exception.application;

public class ApplicationLimitReachedException extends RuntimeException {
    public ApplicationLimitReachedException() {
        super("The number of applicants for this job posting has reached its limit!");
    }
}
