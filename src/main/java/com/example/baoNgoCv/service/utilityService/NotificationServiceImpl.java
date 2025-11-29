package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.event.applicant.ApplicantStatusChangedEvent;
import com.example.baoNgoCv.event.applicant.ApplicantWithdrewEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingCreatedEvent;
import com.example.baoNgoCv.jpa.projection.notification.NotificationCountProjection;
import com.example.baoNgoCv.jpa.projection.notification.NotificationProjection;
import com.example.baoNgoCv.model.dto.notification.GetAllNotificationResponse;
import com.example.baoNgoCv.model.entity.*;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.NotificationType;
import com.example.baoNgoCv.jpa.repository.*;
import com.example.baoNgoCv.service.domainService.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    @Value("${app.frontend.url}")
    private String frontendUrl;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserService userService;
    private final EmailService emailService;
    private static final String USER_NOTIFICATION_DESTINATION = "/queue/notifications";
    private final ApplicantRepository applicantRepository;

    public record NotificationPayload(
            String message, String title, String avatar, String sender,
            String href, NotificationType type, String timestamp, boolean isRead
    ) {
    }

    private void sendNotification(String username, NotificationPayload payload) {
        messagingTemplate.convertAndSendToUser(username, USER_NOTIFICATION_DESTINATION, payload);
    }

    @Transactional
    @Override
    public GetAllNotificationResponse getNotificationsForCurrentUser(
            Authentication authentication,
            String status,
            String type,
            Pageable pageable) {

        // 1. ✅ Lấy User hoặc Company trực tiếp từ Authentication
        Object principal = authentication.getPrincipal();
        User currentUser = null;
        Company currentCompany = null;

        if (principal instanceof User) {
            currentUser = (User) principal;
            log.debug("Loading notifications for User: {}", currentUser.getUsername());
        } else if (principal instanceof Company) {
            currentCompany = (Company) principal;
            log.debug("Loading notifications for Company: {}", currentCompany.getName());
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass().getName());
        }

        // 2. Chuyển đổi các tham số String thành kiểu dữ liệu phù hợp
        NotificationType typeFilter = (type == null || type.equalsIgnoreCase("all"))
                ? null
                : NotificationType.valueOf(type.toUpperCase());

        Boolean isReadFilter = (status == null || status.equalsIgnoreCase("all"))
                ? null
                : status.equalsIgnoreCase("read");

        // 3. Gọi đồng thời 2 câu query từ Repository
        // Query 1: Lấy trang dữ liệu thông báo đã được lọc và phân trang
        Page<NotificationProjection> notificationPageProjection = notificationRepository.findByFilters(
                currentUser,
                currentCompany,
                typeFilter,
                isReadFilter,
                pageable
        );

        // Query 2: Lấy số lượng thống kê (tất cả, đã đọc, chưa đọc)
        NotificationCountProjection countProjection = notificationRepository
                .countNotifications(currentUser, currentCompany);

        // 4. "Lắp ráp" DTO từ kết quả của 2 câu query

        // 4a. Tạo record NotificationCounts
        var countsDto = new GetAllNotificationResponse.NotificationCounts(
                countProjection.getAllCount(),
                countProjection.getUnreadCount(),
                countProjection.getReadCount()
        );

        // 4b. Chuyển đổi List<NotificationProjection> thành List<NotificationItem>
        List<GetAllNotificationResponse.NotificationItem> notificationItems =
                notificationPageProjection.getContent().stream()
                        .map(proj -> new GetAllNotificationResponse.NotificationItem(
                                proj.getId(),
                                proj.getTitle(),
                                proj.getMessage(),
                                proj.getAvatar(),
                                proj.getHref(),
                                proj.getType().name(),
                                proj.getIsRead(),
                                proj.getSenderName(),
                                proj.getCreatedAt()
                        ))
                        .toList();

        // 4c. Tạo record NotificationPage
        var notificationPageDto = new GetAllNotificationResponse.NotificationPage(
                notificationItems,
                notificationPageProjection.getTotalPages(),
                notificationPageProjection.getTotalElements(),
                notificationPageProjection.getNumber(),
                notificationPageProjection.isFirst(),
                notificationPageProjection.isLast()
        );

        // 5. Trả về DTO response cuối cùng
        return new GetAllNotificationResponse(
                true,
                notificationPageDto,
                countsDto
        );
    }



    @Transactional
    @Override
    public void createCompanyWelcomeNotification(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found for welcome notification: " + companyId));

        try {
            // Tạo thông báo ban đầu
            Notification notification = new Notification();
            notification.setRecipientCompany(company);
            notification.setType(NotificationType.SYSTEM_MAINTENANCE); // Hoặc một type phù hợp hơn
            notification.setTitle("Welcome to BaoNgoCv!");
            notification.setMessage("Your company profile has been successfully created. Let's start by posting your first job!");
            notification.setHref("/company/profile"); // href tạm thời
            notification.setRead(false);
            notification.setAvatar("/img/logo/logoShop.png"); // Avatar hệ thống

            Notification savedNotification = notificationRepository.save(notification);

            log.info("Saved welcome notification to DB for Company ID: {}", companyId);

            // Gửi qua WebSocket
            var payload = new NotificationPayload(
                    savedNotification.getMessage(),
                    savedNotification.getTitle(),
                    savedNotification.getAvatar(),
                    "BaoNgoCV System",
                    savedNotification.getHref(), // Sử dụng href đã cập nhật
                    savedNotification.getType(),
                    LocalDateTime.now().toString(),
                    false
            );
            sendNotification(company.getUsername(), payload);
            log.info("Sent WebSocket welcome notification to company '{}'", company.getUsername());

        } catch (Exception e) {
            log.error("Failed to create and send welcome notification for company ID: {}. Error: {}", companyId, e.getMessage(), e);
        }
    }

    @Override
    public void sendReviewNotificationToUser(String title, User user, String avatar, String sender, String href) {
        var payload = new NotificationPayload(
                title, title, avatar, sender, href,
                NotificationType.APPLICATION_REVIEWED,
                LocalDateTime.now().toString(), false
        );
        sendNotification(user.getUsername(), payload);
    }

    @Override
    public Page<Notification> findByUser(User user, Pageable pageable) {
        return notificationRepository.findByRecipientUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    public Page<Notification> findByUserAndStatus(User user, String status, Pageable pageable) {
        boolean isRead = "read".equals(status);
        return notificationRepository.findByRecipientUserAndIsReadOrderByCreatedAtDesc(user, isRead, pageable);
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientUserAndIsRead(user, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional
    public void markAllAsReadForCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User currentUser = (User) principal;
            log.info("Marking all notifications as read for User: {}", currentUser.getUsername());
            List<Notification> unreadNotifications = notificationRepository.findByRecipientUserAndIsRead(currentUser, false);
            unreadNotifications.forEach(notification -> notification.setRead(true));
            // saveAll is more efficient than saving one by one in a loop
            notificationRepository.saveAll(unreadNotifications);
        } else if (principal instanceof Company) {
            Company currentCompany = (Company) principal;
            log.info("Marking all notifications as read for Company: {}", currentCompany.getUsername());
            List<Notification> unreadNotifications = notificationRepository.findByRecipientCompanyAndIsRead(currentCompany, false);
            unreadNotifications.forEach(notification -> notification.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
        } else {
            throw new IllegalStateException("Unknown principal type for marking notifications as read: " + principal.getClass().getName());
        }
    }

    @Override
    @Transactional
    public void createAndSendStatusNotification(ApplicantStatusChangedEvent event) {
        try {
            // 1. Xác định nội dung thông báo dựa trên trạng thái
            String title;
            String message;
            NotificationType type;

            switch (event.newStatus()) {
                case SHORTLISTED -> {
                    title = "Application Shortlisted";
                    message = "Congratulations! Your application for " + event.jobTitle() + " has been shortlisted.";
                    type = NotificationType.APPLICATION_ACCEPTED;
                }
                case REJECTED -> {
                    title = "Application Update";
                    message = "Update regarding your application for " + event.jobTitle() + " at " + event.companyName() + ".";
                    type = NotificationType.APPLICATION_REJECTED;
                }
                default -> {
                    // Các trạng thái khác (như Interview) nếu không cần thông báo dạng này thì bỏ qua
                    return;
                }
            }

            // 2. Query lại Entity để đảm bảo dữ liệu và quan hệ JPA
            // Cần inject ApplicantRepository vào class này
            Applicant applicant = applicantRepository.findById(event.applicantId()).orElse(null);
            User recipient = userRepository.findById(event.userId()).orElse(null);

            if (applicant == null || recipient == null) {
                log.warn("⚠️ Cannot create notification: Applicant or User not found. AppID: {}, UserID: {}",
                        event.applicantId(), event.userId());
                return;
            }

            // 3. Tạo và Lưu Notification vào DB
            Notification notification = new Notification();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRecipientUser(recipient);
            notification.setSenderCompany(applicant.getJobPosting().getCompany());
            notification.setApplicant(applicant);
            notification.setAvatar(event.companyLogo());
            notification.setType(type);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);

            // Lưu lần 1 để lấy ID
            Notification savedNoti = notificationRepository.save(notification);

            // 4. Update Link Tracking (thêm notificationId vào URL)
            String href = "/jobseeker/my-application/" + event.applicantId() + "?notificationId=" + savedNoti.getId();
            savedNoti.setHref(href);
            notificationRepository.save(savedNoti); // Update lại href

            // 5. Gửi WebSocket (Real-time)
            var payload = new NotificationPayload(
                    message,
                    title,
                    event.companyLogo(),
                    event.companyName(), // Sender Name
                    href,
                    type,
                    LocalDateTime.now().toString(),
                    false
            );

            // Gửi tới username (email) lấy từ Event
            sendNotification(event.targetUsername(), payload);

            log.info("✅ Created DB Notification & Sent WebSocket for ApplicantID: {}", event.applicantId());

        } catch (Exception e) {
            log.error("❌ Failed to create status notification for ApplicantID: {}. Error: {}",
                    event.applicantId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleApplicantWithdrawal(ApplicantWithdrewEvent event) {
        try {
            Applicant applicant = event.applicant();
            Company company = applicant.getJobPosting().getCompany();
            User jobseeker = applicant.getUser();

            log.info("Handling ApplicantWithdrewEvent for applicant ID: {}. Notifying company ID: {}", applicant.getId(), company.getId());

            String message = String.format("Candidate '%s' has withdrawn their application for the position '%s'.",
                    jobseeker.getPersonalInfo().getFullName(),
                    applicant.getJobPosting().getTitle());

            // 1. Tạo và lưu Notification vào DB
            Notification dbNotification = new Notification();
            dbNotification.setRecipientCompany(company);
            dbNotification.setSenderUser(jobseeker); // Ghi nhận người gửi là jobseeker
            dbNotification.setApplicant(applicant);
            dbNotification.setType(NotificationType.APPLICATION_WITHDRAWN);
            dbNotification.setTitle("Application Withdrawn");
            dbNotification.setMessage(message);
            dbNotification.setRead(false);
            dbNotification.setAvatar(jobseeker.getProfilePicture()); // Lấy avatar của ứng viên
            
            // Lưu lần 1 để lấy ID
            Notification savedNoti = notificationRepository.save(dbNotification);
            
            // Tạo URL đầy đủ với ID thông báo để tracking
            String url = frontendUrl+"/company/job-view-applicants?jobId=" + applicant.getJobPosting().getId() + "&notificationId=" + savedNoti.getId();
            savedNoti.setHref(url);
            notificationRepository.save(savedNoti); // Cập nhật lại href

            // 2. Tạo payload để gửi qua WebSocket
            var payload = new NotificationPayload(
                    message, "Application Withdrawn", savedNoti.getAvatar(),
                    jobseeker.getPersonalInfo().getFullName(), url, NotificationType.APPLICATION_WITHDRAWN,
                    LocalDateTime.now().toString(), false
            );

            // 3. Gửi thông báo real-time
            sendNotification(company.getUsername(), payload);
            log.info("✅ Successfully sent WebSocket notification to company '{}' for applicant withdrawal.", company.getUsername());

        } catch (Exception e) {
            log.error("❌ Failed to process applicant withdrawal notification. Event data: {}. Error: {}", event, e.getMessage(), e);
        }
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public Map<String, Integer> getNotificationCounts(User user, Company company) {
        Map<String, Integer> counts = new HashMap<>();

        int total = getTotalCount(user, company);
        counts.put("all", total);

        counts.put("unread", getUnreadCount(user, company));
        counts.put("read", total - counts.get("unread"));

        for (NotificationType type : NotificationType.values()) {
            int count = getCountByType(user, company, type);
            counts.put(type.name().toLowerCase(), count);
        }

        return counts;
    }

    private int getTotalCount(User user, Company company) {
        return notificationRepository.countByRecipient(user, company);
    }

    private int getUnreadCount(User user, Company company) {
        return notificationRepository.countByRecipientAndIsRead(user, company, false);
    }

    private int getCountByType(User user, Company company, NotificationType type) {
        return notificationRepository.countByRecipientAndType(user, company, type);
    }

    @Override
    public Notification toggleReadStatus(Long notificationId, User user, Company company) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        boolean hasPermission = false;

        if (notification.getRecipientUser() != null) {
            hasPermission = notification.getRecipientUser().getId().equals(user.getId());
        } else if (notification.getRecipientCompany() != null) {
            if (company != null) {
                hasPermission = notification.getRecipientCompany().getId().equals(company.getId());
            }
        }

        if (!hasPermission) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setRead(!notification.isRead());
        return notificationRepository.save(notification);
    }

    @Override
    public Map<String, Integer> getAvailableTypeCounts(User user, Company company) {
        Long userId = (user != null && user.getId() != null) ? user.getId() : null;
        Long companyId = (company != null && company.getId() != null) ? company.getId() : null;

        if (userId == null && companyId == null) {
            return new HashMap<>();
        }

        List<Object[]> results = notificationRepository
                .findAvailableTypeCountsByUserOrCompany(userId, companyId);

        Map<String, Integer> typeCounts = new HashMap<>();
        for (Object[] result : results) {
            NotificationType type = (NotificationType) result[0];
            Long count = (Long) result[1];

            if (count > 0) {
                typeCounts.put(type.toString(), count.intValue());
            }
        }

        return typeCounts;
    }

    @Override
    public Notification createApplicationNotification(Company employer, User jobSeeker,
                                                      String jobTitle, Long applicantId) {
        Notification notification = Notification.createNewApplicationNotification(
                employer, jobSeeker, jobTitle, applicantId
        );
        return notificationRepository.save(notification);
    }


    @Override
    @Transactional
    public void notifyEmployerOfNewApplication(String companyUsername, String candidateName,
                                               String candidateAvatar, String jobTitle, Long applicantId) {
        try {
            // 1. Xử lý Gửi WebSocket (Ưu tiên chạy trước vì nhanh)
            var payload = new NotificationPayload(
                    String.format("New application from %s for %s", candidateName, jobTitle),
                    "New Job Application",
                    candidateAvatar,
                    candidateName,
                    "/company/job-application-detail/" + applicantId,
                    NotificationType.NEW_APPLICATION,
                    LocalDateTime.now().toString(),
                    false
            );
            sendNotification(companyUsername, payload);

            // 2. Xử lý Lưu Database (Cần Entity -> Phải query trong này)
            // Vì Interface nhận String, nên ta phải tự tìm Entity trong này để lưu DB.
            // Đây là sự đánh đổi: Handler bên ngoài sạch, Service bên trong chịu khó query bù.

            Company employer = companyRepository.findByUsername(companyUsername)
                    .orElseThrow(() -> new RuntimeException("Company not found: " + companyUsername));

            Notification dbNotification = new Notification();
            dbNotification.setRecipientCompany(employer);
            dbNotification.setType(NotificationType.NEW_APPLICATION);
            dbNotification.setTitle("New Application: " + jobTitle);
            dbNotification.setMessage(payload.message());
            dbNotification.setHref(payload.href());
            dbNotification.setRead(false);
            dbNotification.setAvatar(candidateAvatar);

            notificationRepository.save(dbNotification);

            log.info("Successfully notified employer {} about applicant {}", companyUsername, applicantId);

        } catch (Exception e) {
            log.error("Failed to notify employer {} for applicant ID: {}", companyUsername, applicantId, e);
        }
    }


    @Override
    @Transactional
    public void createWelcomeNotification(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            log.error("[WELCOME_NOTIFICATION] Cannot create welcome notification. User not found with ID: {}", userId);
            return;
        }

        try {
            Notification dbNotification = Notification.createWelcomeNotification(user);
            notificationRepository.save(dbNotification);
            log.info("[WELCOME_NOTIFICATION] Saved welcome notification to DB for UserID: {}", userId);
        } catch (Exception e) {
            log.error("[WELCOME_NOTIFICATION] Exception occurred while creating welcome notification for UserID: {}", userId, e);
        }
    }

    @Override
    @Transactional
    public void notifyEmployerOfJobExpiry(Long companyId, String jobTitle) {
        Company company = companyRepository.findById(companyId).orElse(null);

        if (company == null) {
            log.error("[JOB_EXPIRED_NOTIFICATION] Cannot send notification. Company not found with ID: {}", companyId);
            return;
        }

        try {
            Notification dbNotification = Notification.createJobExpiredNotification(company, jobTitle);
            dbNotification = notificationRepository.save(dbNotification);
            log.info("[JOB_EXPIRED_NOTIFICATION] Saved job expired notification to DB for CompanyID: {}", companyId);

            var payload = new NotificationPayload(
                    String.format("Your job posting '%s' has expired.", jobTitle),
                    "Job Posting Expired",
                    "/img/logo/logoShop.png",
                    "BaoNgoCV System",
                    dbNotification.getFullHref(),
                    NotificationType.JOB_EXPIRED,
                    LocalDateTime.now().toString(),
                    false
            );
            sendNotification(company.getUsername(), payload);
        } catch (Exception e) {
            log.error("[JOB_EXPIRED_NOTIFICATION] Failed to create and send notification for expired job. Company ID: {}. Error: {}", companyId, e);
        }
    }

    @Override
    @Transactional
    public void notifySaversOfJobExpiry(Long jobPostingId, String jobTitle) {
        jobPostingRepository.findById(jobPostingId).ifPresent(jobPosting -> {
            var applicantUserIds = jobPosting.getApplicants().stream()
                    .map(applicant -> applicant.getUser().getId())
                    .collect(java.util.stream.Collectors.toSet());

            var usersToNotify = jobPosting.getSavedJobs().stream()
                    .map(com.example.baoNgoCv.model.entity.JobSaved::getUser)
                    .filter(user -> !applicantUserIds.contains(user.getId()))
                    .toList();

            if (usersToNotify.isEmpty()) {
                log.info("[JOB_EXPIRED_SAVERS] No users to notify for expired job ID: {}", jobPostingId);
                return;
            }

            log.info("[JOB_EXPIRED_SAVERS] Found {} users to notify for expired job ID: {}", usersToNotify.size(), jobPostingId);

            usersToNotify.forEach(user -> {
                try {
                    Notification dbNotification = Notification.createJobExpiredForSaverNotification(user, jobTitle, jobPostingId);
                    dbNotification =     notificationRepository.save(dbNotification);

                    var payload = new NotificationPayload(
                            String.format("The job '%s' you saved has expired.", jobTitle),
                            "Saved Job Expired",
                            "/img/logo/logoShop.png",
                            "BaoNgoCV System",
                            dbNotification.getFullHref(),
                            NotificationType.JOB_EXPIRED,
                            LocalDateTime.now().toString(),
                            false
                    );
                    sendNotification(user.getUsername(), payload);
                } catch (Exception e) {
                    log.error("[JOB_EXPIRED_SAVERS] Failed to send notification to user ID: {} for job ID: {}. Error: {}",
                            user.getId(), jobPostingId, e.getMessage());
                }
            });
        });
    }

    @Override
    @PreAuthorize("@notificationSecurityServiceImpl.isOwner(#notificationId)")
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);

            log.info("Notification ID {} marked as read.", notificationId);
        }
    }

    @Override
    @Transactional
    public void notifyFollowersOfNewJob(JobPostingCreatedEvent event) {
        if (event.followerUsernames() == null || event.followerUsernames().isEmpty()) {
            return;
        }

        log.info("📢 [NOTIFICATION_SERVICE] Notifying {} followers about new job posting from company: {}", event.followerUsernames().size(), event.companyName());

        List<User> followers = userService.findAllByUsernameIn(event.followerUsernames());

        followers.forEach(user -> {
            try {
                Notification notification = new Notification();
                notification.setRecipientUser(user);
                notification.setType(NotificationType.NEW_JOB_POSTING);
                notification.setTitle("New Job: " + event.jobTitle());
                notification.setMessage(String.format("The company you follow, %s, posted a new job.", event.companyName()));
                notification.setHref("/jobseeker/job-detail/" + event.jobId());
                notification.setRead(false);
                notification.setAvatar(event.companyAvatar());
                notificationRepository.save(notification);

                var payload = new NotificationPayload(
                        notification.getMessage(),
                        notification.getTitle(),
                        event.companyAvatar(),
                        event.companyName(),
                        notification.getFullHref(),
                        notification.getType(),
                        LocalDateTime.now().toString(),
                        false
                );

                sendNotification(user.getUsername(), payload);
                log.info("✅ Sent WebSocket notification for new job to user '{}'", user.getUsername());

            } catch (Exception e) {
                log.error("❌ Failed to send new job notification to follower: {}. Error: {}", user.getUsername(), e.getMessage(), e);
            }
        });
    }

    @Override
    @Transactional
    public void notifyCompanyOfUpgradePlanSuccess(Long companyId, String companyName, AccountTier newTier) {
        Company company = companyRepository.findById(companyId).orElse(null);

        if (company == null) {
            log.error("[UPGRADE_PLAN_NOTIFICATION] Cannot send notification. Company not found with ID: {}", companyId);
            return;
        }

        try {
            Notification dbNotification = new Notification();
            dbNotification.setRecipientCompany(company);
            dbNotification.setType(NotificationType.PLAN_UPGRADED);
            dbNotification.setTitle("Account Plan Upgraded");
            String message = String.format("Congratulations! Your plan has been successfully upgraded to %s.", newTier.name());
            dbNotification.setMessage(message);
            dbNotification.setHref("/company/profile");
            dbNotification.setRead(false);
            dbNotification.setAvatar("/img/logo/logoShop.png");

            notificationRepository.save(dbNotification);
            log.info("[UPGRADE_PLAN_NOTIFICATION] Saved 'plan upgraded' notification to DB for CompanyID: {}", companyId);

            var payload = new NotificationPayload(
                    message,
                    "Account Plan Upgraded",
                    "/img/logo/logoShop.png",
                    "BaoNgoCV System",
                    dbNotification.getFullHref(),
                    NotificationType.PLAN_UPGRADED,
                    java.time.LocalDateTime.now().toString(),
                    false
            );
            sendNotification(company.getUsername(), payload);
            log.info("[UPGRADE_PLAN_NOTIFICATION] Sent WebSocket notification to company '{}'", company.getUsername());

        } catch (Exception e) {
            log.error("[UPGRADE_PLAN_NOTIFICATION] Failed to create and send notification for plan upgrade. Company ID: {}. Error: {}", companyId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void notifyCompanyOfDowngrade(Long companyId, String companyName, AccountTier oldTier) {
        Company company = companyRepository.findById(companyId).orElse(null);

        if (company == null) {
            log.error("[DOWNGRADE_PLAN_NOTIFICATION] Cannot send notification. Company not found with ID: {}", companyId);
            return;
        }

        try {
            Notification dbNotification = new Notification();
            dbNotification.setRecipientCompany(company);
            dbNotification.setType(NotificationType.PLAN_DOWNGRADED);
            dbNotification.setTitle("Subscription Plan Expired");
            String message = String.format("Your %s plan has expired. Your account has been downgraded to the FREE plan.", oldTier.name());
            dbNotification.setMessage(message);
            dbNotification.setHref("/company/profile");
            dbNotification.setRead(false);
            dbNotification.setAvatar("/img/logo/logoShop.png");

            notificationRepository.save(dbNotification);
            log.info("[DOWNGRADE_PLAN_NOTIFICATION] Saved 'plan downgraded' notification to DB for CompanyID: {}", companyId);

            var payload = new NotificationPayload(
                    message,
                    "Subscription Plan Expired",
                    "/img/logo/logoShop.png",
                    "BaoNgoCV System",
                    dbNotification.getFullHref(),
                    NotificationType.PLAN_DOWNGRADED,
                    java.time.LocalDateTime.now().toString(),
                    false
            );
            sendNotification(company.getUsername(), payload);
            log.info("[DOWNGRADE_PLAN_NOTIFICATION] Sent WebSocket notification to company '{}'", company.getUsername());

        } catch (Exception e) {
            log.error("[DOWNGRADE_PLAN_NOTIFICATION] Failed to create and send notification for plan downgrade. Company ID: {}. Error: {}", companyId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Notification createAndSendNotification(Long userId, String message, String link) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("Cannot create notification. User not found with ID: {}", userId);
            return null;
        }

        try {
            Notification notification = new Notification();
            notification.setRecipientUser(user);
            notification.setMessage(message);
            notification.setHref(link);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);

            var payload = new NotificationPayload(
                    message,
                    "New Notification",
                    "/img/logo/logoShop.png",
                    "BaoNgoCV System",
                    link,
                    NotificationType.SYSTEM_MAINTENANCE,
                    notification.getCreatedAt().toString(),
                    false
            );
            sendNotification(user.getUsername(), payload);
            return notification;
        } catch (Exception e) {
            log.error("Failed to create and send notification for user ID: {}. Error: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}
