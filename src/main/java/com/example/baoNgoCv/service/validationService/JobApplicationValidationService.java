package com.example.baoNgoCv.service.validationService;

import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.User;

import java.util.concurrent.CompletableFuture;

public interface JobApplicationValidationService {
    /**
     * Validate job posting expiry
     */
    CompletableFuture<Void> validateJobExpiry(JobPosting jobPosting);

    /**
     * Validate user profile completeness
     */
    CompletableFuture<Void> validateProfileCompletion(User user);

    /**
     * Validate no duplicate application
     */
    CompletableFuture<Void> validateNoDuplicateApplication(User user, JobPosting jobPosting);


    /**
     * Perform all validations in parallel
     */
    void executeAllValidations(JobPosting jobPosting, User user);
}
