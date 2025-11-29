package com.example.baoNgoCv.event;

import com.example.baoNgoCv.event.applicant.ApplicantStatusChangedEvent;
import com.example.baoNgoCv.event.applicant.ApplicationSubmittedEvent;
import com.example.baoNgoCv.event.applicant.ApplicantWithdrewEvent;
import com.example.baoNgoCv.event.company.CreatedCompanyAccountEvent;
import com.example.baoNgoCv.event.company.SubscriptionDowngradedEvent;
import com.example.baoNgoCv.event.company.UpgradePlanSuccessEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingCreatedEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingExpiredEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingReminderEvent;
import com.example.baoNgoCv.event.user.UserRegisteredEvent;
import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.repository.ApplicantRepository;
import com.example.baoNgoCv.jpa.repository.UserRepository;
import com.example.baoNgoCv.service.domainService.JobPostingService;
import com.example.baoNgoCv.service.utilityService.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationHandler {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApplicantStatusChange(ApplicantStatusChangedEvent event) {

        // 1. Ủy quyền hoàn toàn cho Service xử lý
        log.info("🔔 [NOTI_HANDLER] Delegating notification creation for ApplicantID: {}", event.applicantId());
        notificationService.createAndSendStatusNotification(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleCompanyAccountCreation(CreatedCompanyAccountEvent event) {
        log.info("🔔 [NOTIFICATION_HANDLER] New company account created event received. Creating welcome notification for Company ID: {}", event.companyId());
        try {
            notificationService.createCompanyWelcomeNotification(event.companyId());
            log.info("✅ Successfully created welcome notification for new company '{}'.", event.companyName());
        } catch (Exception e) {
            log.error("❌ Failed to create welcome notification for new company {}. Error: {}", event.companyName(), e.getMessage(), e);
        }
    }

    private final NotificationService notificationService;
    private final ApplicantRepository applicantRepository;
    private final JobPostingService jobPostingService;
    private final UserRepository userRepository;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        // Chỉ tập trung logic WebSocket/Notification
        log.info("🔔 [WEBSOCKET] Pushing notification to company: {}", event.companyUsername());

        try {
            // Luôn gửi, không cần check setting email
            notificationService.notifyEmployerOfNewApplication(
                    event.companyUsername(),
                    event.candidateFullName(),
                    event.candidateAvatarUrl(),
                    event.jobTitle(),
                    event.applicantId()
            );
        } catch (Exception e) {
            log.error("❌ Failed to push websocket notification: {}", e.getMessage());
        }
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserRegistration(UserRegisteredEvent event) {
        try {
            notificationService.createWelcomeNotification(event.getUserId());
        } catch (Exception e) {
            log.error("❌ [WELCOME_NOTIFICATION] Failed to create welcome notification for UserID: {}. Error: {}", event.getUserId(), e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleJobPostingExpired(JobPostingExpiredEvent event) {
        log.info("🔔 [NOTIFICATION_HANDLER] Job expired event received - JobID: {}, CompanyID: {}", event.jobPostingId(), event.companyId());
        try {
            notificationService.notifyEmployerOfJobExpiry(event.companyId(), event.jobTitle());
            if (event.jobPostingId() != null) {
                notificationService.notifySaversOfJobExpiry(event.jobPostingId(), event.jobTitle());
            }
        } catch (Exception e) {
            log.error("❌ [JOB_EXPIRED_NOTIFICATION] Failed to create notification for expired job {}. Error: {}", event.jobPostingId(), e.getMessage(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleJobPostingCreated(JobPostingCreatedEvent event) {
        if (event.followerUsernames() == null || event.followerUsernames().isEmpty()) {
            return;
        }
        notificationService.notifyFollowersOfNewJob(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUpgradePlanSuccess(UpgradePlanSuccessEvent event) {
        log.info("🔔 [NOTIFICATION_HANDLER] Event received - CompanyID: {}, New Tier: {}", event.companyId(), event.newTier());
        try {
            notificationService.notifyCompanyOfUpgradePlanSuccess(
                    event.companyId(),
                    event.companyName(),
                    event.newTier()
            );
        } catch (Exception e) {
            log.error("❌ [UPGRADE_PLAN_NOTIFICATION] Failed to create notification for CompanyID: {}. Error: {}", event.companyId(), e.getMessage(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleSubscriptionDowngraded(SubscriptionDowngradedEvent event) {
        log.info("🔔 [NOTIFICATION_HANDLER] Subscription downgraded event received - CompanyID: {}", event.getCompanyId());
        try {
            notificationService.notifyCompanyOfDowngrade(
                    event.getCompanyId(),
                    event.getCompanyName(),
                    event.getOldTier()
            );
        } catch (Exception e) {
            log.error("❌ [DOWNGRADE_NOTIFICATION] Failed to create notification for CompanyID: {}. Error: {}", event.getCompanyId(), e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleJobPostingReminder(JobPostingReminderEvent event) {
        log.info("Handling job posting reminder event for job: {}", event.jobTitle());
        String message = String.format("The job posting '%s' you saved is expiring soon.", event.jobTitle());
        String link = "/jobseeker/job-detail/" + event.jobPostingId();
        event.savedUserIds().forEach(userId -> {
            try {
                notificationService.createAndSendNotification(userId, message, link);
                log.info("Sent job posting reminder notification to user: {}", userId);
            } catch (Exception e) {
                log.error("Failed to send job posting reminder notification to user: {}. Error: {}", userId, e.getMessage());
            }
        });
    }

    /**
     * [NEW] Lắng nghe sự kiện khi một ứng viên rút đơn ứng tuyển.
     * Nhiệm vụ của handler này là gọi NotificationService để thông báo cho Company.
     *
     * @param event Sự kiện chứa thông tin về đơn đã rút.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleApplicantWithdrawal(ApplicantWithdrewEvent event) {
        log.info("🔔 [NOTIFICATION_HANDLER] Applicant withdrew event received for Applicant ID: {}", event.applicant().getId());
        try {
            notificationService.handleApplicantWithdrawal(event);
        } catch (Exception e) {
            log.error("❌ [APPLICANT_WITHDRAWAL] Failed to process notification for applicant withdrawal. Applicant ID: {}. Error: {}", event.applicant().getId(), e.getMessage(), e);
        }
    }
}
