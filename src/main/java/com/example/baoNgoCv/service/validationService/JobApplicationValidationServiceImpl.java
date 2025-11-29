package com.example.baoNgoCv.service.validationService;

import com.example.baoNgoCv.event.jobposting.JobPostingExpiredEvent;
import com.example.baoNgoCv.exception.application.ApplicationLimitReachedException;
import com.example.baoNgoCv.exception.application.DuplicateApplicationException;
import com.example.baoNgoCv.exception.jobseekerException.JobExpiredException;
import com.example.baoNgoCv.exception.jobseekerException.ProfileIncompleteException;
import com.example.baoNgoCv.jpa.repository.ApplicantRepository;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.ExpireReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationValidationServiceImpl implements JobApplicationValidationService {

    private final ApplicantRepository applicantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public CompletableFuture<Void> validateJobExpiry(JobPosting jobPosting) {
        return CompletableFuture.runAsync(() -> {
            log.debug("[VALIDATION] [EXPIRY_CHECK] Checking job expiration...");

            if (jobPosting.isExpired()) {
                log.warn("[VALIDATION] [EXPIRY_CHECK] ⚠️ Job is expired");

                if (jobPosting.needsStatusUpdate()) {
                    log.debug("[VALIDATION] [EXPIRY_CHECK] Updating status to EXPIRED");
                    jobPosting.expire();

                    log.debug("[VALIDATION] [EXPIRY_CHECK] 📡 Publishing JobPostingExpiredEvent");
                    applicationEventPublisher.publishEvent(
                            new JobPostingExpiredEvent(
                                    jobPosting.getId(),
                                    jobPosting.getTitle(),
                                    jobPosting.getCompany().getId(),
                                    jobPosting.getCompany().getContactEmail(),
                                    jobPosting.getCompany().getName(),
                                    ExpireReason.DEADLINE_PASSED
                            )
                    );
                }
                throw new RuntimeException("JobExpired");
            }

            log.debug("[VALIDATION] [EXPIRY_CHECK] ✅ Job expiration check passed");
        });
    }

    @Override
    public CompletableFuture<Void> validateProfileCompletion(User user) {
        return CompletableFuture.runAsync(() -> {
            log.debug("[VALIDATION] [PROFILE_CHECK] Checking profile completeness...");

            if (!user.isProfileComplete()) {
                log.error("[VALIDATION] [PROFILE_CHECK] ❌ Profile incomplete");
                throw new RuntimeException("ProfileIncomplete");
            }

            log.debug("[VALIDATION] [PROFILE_CHECK] ✅ Profile check passed");
        });
    }

    @Override
    public CompletableFuture<Void> validateNoDuplicateApplication(User user, JobPosting jobPosting) {
        return CompletableFuture.runAsync(() -> {
            log.debug("[VALIDATION] [DUPLICATE_CHECK] Checking for duplicate application...");

            var existing = applicantRepository.findExistingApplication(
                    user.getId(),
                    jobPosting.getId()
            );

            if (existing.isPresent()) {
                log.error("[VALIDATION] [DUPLICATE_CHECK] ❌ Duplicate found - ID: {}",
                        existing.get().getId());
                throw new RuntimeException("DuplicateApplication:" + existing.get().getId());
            }

            log.debug("[VALIDATION] [DUPLICATE_CHECK] ✅ No duplicate found");
        });
    }



    @Override
    public void executeAllValidations(JobPosting jobPosting, User user) {
        log.debug("[VALIDATION] 🔄 Starting parallel validations...");

        CompletableFuture<Void> expiryCheck = validateJobExpiry(jobPosting);
        CompletableFuture<Void> profileCheck = validateProfileCompletion(user);
        CompletableFuture<Void> duplicateCheck = validateNoDuplicateApplication(user, jobPosting);


        log.debug("[VALIDATION] ⏳ Waiting for all validations to complete...");
        try {
            CompletableFuture.allOf(expiryCheck, profileCheck, duplicateCheck).join();
            log.info("[VALIDATION] ✅ All validations passed");
        } catch (CompletionException e) {
            log.error("[VALIDATION] ❌ Validation failed: {}", e.getCause().getMessage());
            String message = e.getCause().getMessage();

            if (message.equals("JobExpired")) {
                throw new JobExpiredException();
            } else if (message.equals("ProfileIncomplete")) {
                throw new ProfileIncompleteException();
            } else if (message.startsWith("DuplicateApplication:")) {
                String applicantId = message.split(":")[1];
                throw new DuplicateApplicationException("You have already applied for this job.");
            } else if (message.equals("ApplicationLimitReached")) {
                throw new ApplicationLimitReachedException();
            }
            throw e;
        }
    }
}