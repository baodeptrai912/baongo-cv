package com.example.baoNgoCv.event;

import com.example.baoNgoCv.event.applicant.ApplicantStatusChangedEvent;
import com.example.baoNgoCv.event.applicant.ApplicantWithdrewEvent;
import com.example.baoNgoCv.event.company.CompanyAccountDeletedEvent;
import com.example.baoNgoCv.event.company.CreatedCompanyAccountEvent;
import com.example.baoNgoCv.event.applicant.ApplicationSubmittedEvent;
import com.example.baoNgoCv.event.company.SubscriptionDowngradedEvent;
import com.example.baoNgoCv.event.company.UpgradePlanSuccessEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingCreatedEvent;
import com.example.baoNgoCv.event.jobposting.*;
import com.example.baoNgoCv.event.user.UserAccountDeletedEvent;
import com.example.baoNgoCv.event.user.UserRegisteredEvent;
import com.example.baoNgoCv.jpa.repository.ApplicantRepository;
import com.example.baoNgoCv.jpa.repository.UserRepository;
import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.User;

import com.example.baoNgoCv.service.domainService.JobPostingService;
import com.example.baoNgoCv.service.domainService.UserService;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.service.utilityService.FileService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailHandler {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleCompanyAccountCreation(CreatedCompanyAccountEvent event) {
        log.info("📧 [EMAIL_HANDLER] New company account created event received. Sending welcome email to: {}", event.contactEmail());
        try {
            emailService.sendCompanyWelcomeEmail(
                    event.contactEmail(),
                    event.companyName()
            );
            log.info("✅ Successfully sent welcome email to new company '{}'.", event.companyName());
        } catch (MessagingException e) {
            log.error("❌ Failed to send welcome email to new company {}. Error: {}", event.contactEmail(), e.getMessage());
        }
    }

    private final UserService userService;
    private final EmailService emailService;
    private final JobPostingService jobPostingService;
    private final ApplicantRepository applicantRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUpgradePlanSuccess(UpgradePlanSuccessEvent event) {
        try {
            emailService.sendUpgradeSuccessEmail(
                    event.companyEmail(),
                    event.companyName(),
                    event.newTier()
            );
        } catch (MessagingException e) {
            log.error("Failed to send upgrade success email to {}: {}", event.companyEmail(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleSubscriptionDowngraded(SubscriptionDowngradedEvent event) {
        try {
            emailService.sendSubscriptionDowngradedEmail(
                    event.getCompanyEmail(),
                    event.getCompanyName(),
                    event.getOldTier()
            );
        } catch (MessagingException e) {
            log.error("Failed to send subscription downgraded email to {}: {}", event.getCompanyEmail(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) throws MessagingException {
        if (!event.emailNotificationEnabled()) return;

        // Handler chỉ chuyển ID, không build URL
        emailService.sendNewApplicationNotification(
                event.employerEmail(),
                event.employerName(),
                event.jobTitle(),
                event.candidateFullName(),
                event.candidateAvatarUrl(),
                event.applicantId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserRegistration(UserRegisteredEvent event) {
        try {
            String userEmail = event.getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                return;
            }
            String fullName = event.getUsername();
            if (fullName == null || fullName.isBlank()) {
                fullName = event.getUsername();
            }
            emailService.sendWelcomeEmail(userEmail, fullName);
        } catch (MessagingException e) {
            // Log and ignore
        } catch (Exception e) {
            // Log and ignore
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserAccountDeleted(UserAccountDeletedEvent event) {
        try {
            String userEmail = event.email();
            if (userEmail == null || userEmail.isEmpty()) {
                return;
            }
            if (emailService == null) {
                return;
            }
            emailService.sendAccountDeletionConfirmationEmail(userEmail);
        } catch (org.springframework.messaging.MessagingException e) {
            // Log and ignore
        } catch (Exception e) {
            // Log and ignore
        }
    }


    @EventListener
    @Async
    public void handleJobPostingExpired(JobPostingExpiredEvent event) {
        try {
            emailService.sendJobExpiredNotification(
                    event.employerEmail(),
                    event.jobTitle()
            );
        } catch (Exception e) {
            // Log and ignore
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleJobPostingCreated(JobPostingCreatedEvent event) {
        log.info("📧 [EMAIL_HANDLER] Job posting created event received. Sending confirmation email to: {}", event.companyEmail());
        try {
            emailService.sendJobPostingConfirmationEmail(
                    event.companyEmail(),
                    event.companyName(),
                    event.jobTitle()
            );
            log.info("✅ Successfully sent confirmation email to company.");
        } catch (Exception e) {
            log.error("❌ Failed to send confirmation email to company {}. Error: {}",
                    event.companyEmail(), e.getMessage());
        }
        notifyFollowersOfNewJob(event);
        log.info("✅ [EMAIL_HANDLER] Successfully sent job posting confirmation email to: {}", event.companyEmail());
    }

    private void notifyFollowersOfNewJob(JobPostingCreatedEvent event) {
        if (event.followerUsernames() != null && !event.followerUsernames().isEmpty()) {
            log.info("📧 [EMAIL_HANDLER] Found {} followers to notify via email. Fetching user data in bulk.", event.followerUsernames().size());
            userService.findAllByUsernameIn(event.followerUsernames()).forEach(user -> {
                try {
                    if (user.getContactInfo() != null && user.getContactInfo().getEmail() != null) {
                        emailService.sendNewJobFromFollowedCompanyEmail(
                                user.getContactInfo().getEmail(),
                                event.companyName(),
                                event.jobTitle(),
                                user.getPersonalInfo().getFullName()
                        );
                    } else {
                        log.warn("⚠️ [EMAIL_HANDLER] Skipping notification for user {} due to missing email.", user.getUsername());
                    }
                } catch (Exception e) {
                    log.error("❌ [EMAIL_HANDLER] Failed to send new job notification email to follower: {}. Error: {}", user.getUsername(), e.getMessage());
                }
            });
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleJobPostingDeleted(JobPostingDeletedEvent event) {
        log.info("📧 [EMAIL_HANDLER] Job posting deleted event received for job '{}' at company '{}'. Notifying {} applicants.",
                event.jobTitle(), event.companyName(), event.applicants().size());

        if (event.applicants() == null || event.applicants().isEmpty()) {
            log.info("No applicants to notify for deleted job '{}'.", event.jobTitle());
            return;
        }

        event.applicants().forEach(applicant -> {
            try {
                if (applicant.userEmail() != null && !applicant.userEmail().isEmpty()) {
                    emailService.sendJobPostingDeletedNotification(
                            applicant.userEmail(),
                            applicant.userFullName(),
                            event.jobTitle(),
                            event.companyName()
                    );
                    log.info("✅ Successfully sent job deletion notification to applicant: {}", applicant.username());
                } else {
                    log.warn("⚠️ [EMAIL_HANDLER] Skipping notification for applicant {} due to missing email.", applicant.username());
                }
            } catch (Exception e) {
                log.error("❌ [EMAIL_HANDLER] Failed to send job deletion notification email to applicant: {}. Error: {}",
                        applicant.username(), e.getMessage());
            }
        });

        log.info("✅ [EMAIL_HANDLER] Finished processing job deletion notifications for job '{}'.", event.jobTitle());
    }

    @EventListener
    public void handleJobPostingExpiredEvent(JobPostingExpiredEvent event) {
        try {
            emailService.sendJobExpiredNotification(
                    event.employerEmail(),
                    event.jobTitle()
            );
        } catch (MessagingException e) {
            // Xử lý lỗi gửi email
            log.error("Failed to send job expired email for job {} to {}", event.jobTitle(), event.employerEmail(), e);
        }
    }

    @EventListener
    @Async
    public void handleJobPostingReminder(JobPostingReminderEvent event) {
        log.info("Handling job posting reminder event for job: {}", event.jobTitle());
        event.savedUserIds().forEach(userId -> {
            userRepository.findById(userId).ifPresent(user -> {
                try {
                    if (user.getContactInfo() != null && user.getContactInfo().getEmail() != null) {
                        emailService.sendJobPostingReminderEmail(
                                user.getContactInfo().getEmail(),
                                user.getPersonalInfo().getFullName(),
                                event.jobTitle()
                        );
                        log.info("Sent job posting reminder email to user: {}", userId);
                    }
                } catch (Exception e) {
                    log.error("Failed to send job posting reminder email to user: {}. Error: {}", userId, e.getMessage());
                }
            });
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleCompanyAccountDeletion(CompanyAccountDeletedEvent event) {
        log.info("📧 [EMAIL_HANDLER] Company account deleted event received for '{}'", event.companyName());

        // 1. Gửi email xác nhận cho chính công ty đã bị xóa
        try {
            emailService.sendAccountDeletionConfirmationEmail(event.companyEmail());
            log.info("✅ Sent account deletion confirmation email to {}", event.companyEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send deletion confirmation email to {}. Error: {}", event.companyEmail(), e.getMessage());
        }

        // 2. Dọn dẹp logo
        if (event.companyLogoPath() != null && !event.companyLogoPath().contains("default")) {
            fileService.deleteFile(event.companyLogoPath());
        }

        // 3. Gửi email thông báo cho người dùng liên quan (followers và applicants)
        Set<Long> userIdsToNotify = new HashSet<>();
        userIdsToNotify.addAll(event.followerUserIds());
        userIdsToNotify.addAll(event.applicantUserIds());

        if (!userIdsToNotify.isEmpty()) {
            List<User> users = userRepository.findAllById(userIdsToNotify);
            users.forEach(user -> {
                 emailService.sendCompanyDeletedNotificationToUser(user, event.companyName());
            });
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleApplicantStatusChange(ApplicantStatusChangedEvent event) {
        log.info("📧 [EMAIL_HANDLER] Applicant status changed to {} for ApplicantID: {}. Preparing email...",
                event.newStatus(), event.applicantId());

        try {
            switch (event.newStatus()) {
                case SHORTLISTED -> {
                    // Gửi email chúc mừng vào danh sách rút gọn
                    String trackingLink = "/jobseeker/my-application/" + event.applicantId();

                    emailService.sendShortlistNotificationEmail(
                            event.userEmail(),
                            event.userFullName(),
                            event.companyName(),
                            event.jobTitle(),
                            trackingLink
                    );
                    log.info("✅ Sent SHORTLIST email to {}", event.userEmail());
                }

                case REJECTED -> {
                    // Gửi email từ chối lịch sự
                    emailService.sendRejectionEmail(
                            event.userEmail(),
                            event.userFullName(),
                            event.companyName(),
                            event.jobTitle()
                    );
                    log.info("✅ Sent REJECT email to {}", event.userEmail());
                }

                default -> log.debug("ℹ️ No email action defined for status: {}", event.newStatus());
            }
        } catch (Exception e) {
            log.error("❌ [EMAIL_HANDLER] Error handling status change event for {}: {}", event.userEmail(), e.getMessage());
        }
    }

    /**
     * [NEW] Lắng nghe sự kiện khi một ứng viên rút đơn ứng tuyển.
     * Nhiệm vụ của handler này là gọi EmailService để gửi email thông báo cho Company.
     *
     * @param event Sự kiện chứa thông tin về đơn đã rút.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleApplicantWithdrawal(ApplicantWithdrewEvent event) {
        log.info("📧 [EMAIL_HANDLER] Applicant withdrew event received for Applicant ID: {}. Preparing email for company...", event.applicant().getId());

        try {
            Applicant applicant = event.applicant();
            JobPosting jobPosting = applicant.getJobPosting();
            Company company = jobPosting.getCompany();

            if (company.getContactEmail() == null) {
                log.warn("⚠️ [EMAIL_HANDLER] Skipping withdrawal email notification. Company ID {} has no contact email.", company.getId());
                return;
            }

            // [FIX] Không cần tạo link ở đây nữa, chỉ cần truyền ID
            emailService.sendApplicantWithdrawalNotification(
                    company.getContactEmail(),
                    company.getName(),
                    applicant.getUser().getPersonalInfo().getFullName(),
                    jobPosting.getTitle(),
                    jobPosting.getId() // Truyền ID để service tự xây dựng link
            );
        } catch (Exception e) {
            log.error("❌ [EMAIL_HANDLER] Failed to send applicant withdrawal email. Applicant ID: {}. Error: {}", event.applicant().getId(), e.getMessage(), e);
        }
    }

    /**
     * [NEW] Lắng nghe sự kiện khi một tin tuyển dụng sắp hết hạn.
     * Gửi email nhắc nhở cho công ty.
     *
     * @param event Sự kiện chứa thông tin về công việc sắp hết hạn.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleJobPostingExpiringSoon(JobPostingExpiringSoonEvent event) {
        log.info("📧 [EMAIL_HANDLER] Job expiring soon event received for job '{}'. Sending reminder email to: {}", event.jobTitle(), event.companyEmail());
        try {
            // Giả sử EmailService có một phương thức để gửi loại email này
            emailService.sendJobExpiringSoonReminderEmail(
                    event.companyEmail(),
                    event.companyName(),
                    event.jobTitle(),
                    event.jobId() // Truyền ID công việc để có thể tạo link trong email
            );
            log.info("✅ Successfully sent job expiring soon reminder to '{}'.", event.companyEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send job expiring soon reminder to {}. Error: {}", event.companyEmail(), e.getMessage());
        }
    }
}
