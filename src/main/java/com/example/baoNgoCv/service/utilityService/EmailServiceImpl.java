package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.exception.emailException.EmailSendingException;
import com.example.baoNgoCv.exception.emailException.InvalidVerificationCodeException;
import com.example.baoNgoCv.exception.securityException.RateLimitExceededException;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.VerificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${app.frontend.url}")
    private String frontendUrl;
    private final SendGridEmailClient sendGridEmailClient;
    // --- DEPENDENCIES ---
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    // --- CẤU HÌNH ---
    @Value("${app.mail.from.address}")
    private String mailFromAddress;
    @Value("${app.mail.recipient.contact}")
    private String contactRecipientEmail;
    @Value("${app.mail.recipient.application}")
    private String applicationRecipientEmail;
    @Value("${app.verification.code.expiry.seconds:60}")
    private long verificationCodeExpirySeconds;
    @Value("${app.verification.rate-limit.seconds:60}")
    private long rateLimitSeconds;


    private final Random random = new Random();

    // --- CÁC BỘ LƯU TRỮ IN-MEMORY ---
    private static class VerificationDetails {
        String code;
        LocalDateTime expiryTime;
        VerificationDetails(String code, LocalDateTime expiryTime) { this.code = code; this.expiryTime = expiryTime; }
    }
    private final Map<String, VerificationDetails> verificationStore = new ConcurrentHashMap<>();
    private final Map<String, Long> rateLimitStore = new ConcurrentHashMap<>();

    // ===================================================================================
    // SECTION 1: LOGIC GỬI MÃ XÁC THỰC (REFACTORED)
    // ===================================================================================

    @Override
    public void sendVerification(String email, VerificationType type) throws MessagingException, RateLimitExceededException {
        // 1. Kiểm tra giới hạn tần suất gửi email để chống spam.
        checkRateLimit(email);
        try {
            // 2. Ghi log về hành động sắp thực hiện.
            log.info("Proceeding to send new {} verification code for email: {}", type.name(), email);
            // 3. Tạo một mã xác thực ngẫu nhiên.
            String verificationCode = generateVerificationCode();
            // 4. Xây dựng context (dữ liệu) để đưa vào template email.
            Context context = buildVerificationContext(verificationCode, email);
            // 5. Gửi email với chủ đề và template được định nghĩa trong Enum.
            sendEmailWithLogo(email, type.getSubject(), type.getTemplateName(), context);
            // 6. Lưu mã xác thực và thời gian hết hạn vào bộ nhớ.
            storeVerificationCode(email, verificationCode);
            // 7. Ghi nhận lại thời điểm gửi email thành công để tính toán rate limit.
            recordSuccessfulAttempt(email);
        } catch (MessagingException e) {
            // 8. Nếu có lỗi trong quá trình gửi, ném ra một exception tùy chỉnh.
            throw new EmailSendingException("Failed to send verification code. Please try again later.", e, email);
        }
    }


    public String generateVerificationCode() {
        // 1. Tạo một số ngẫu nhiên từ 0 đến 999,999.
        // 2. Định dạng số này thành một chuỗi 6 chữ số, có đệm số 0 ở đầu nếu cần.
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private Context buildVerificationContext(String code, String email) {
        // 1. Tạo một đối tượng Context của Thymeleaf để chứa các biến.
        Context context = new Context();
        // 2. Đặt các biến cần thiết cho template email.
        context.setVariable("verificationCode", code);
        context.setVariable("expiryMinutes", verificationCodeExpirySeconds / 60);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("userName", extractUserName(email));
        // 3. Trả về đối tượng context đã được chuẩn bị.
        return context;
    }

    // ===================================================================================
    // SECTION 2: LOGIC XÁC THỰC MÃ VÀ RATE LIMIT
    // ===================================================================================


    public void storeVerificationCode(String email, String code) {
        // 1. Tính toán thời gian hết hạn của mã dựa trên thời gian hiện tại và cấu hình.
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(verificationCodeExpirySeconds);
        // 2. Lưu mã và thời gian hết hạn vào map, với key là email.
        verificationStore.put(email, new VerificationDetails(code, expiryTime));
        // 3. Ghi log để theo dõi.
        log.info("Stored verification code for email: {}", email);
    }

    @Override
    public void verifyCode(String email, String userInputCode) throws InvalidVerificationCodeException {
        // 1. Lấy chi tiết mã xác thực từ bộ nhớ dựa trên email.
        VerificationDetails details = verificationStore.get(email);
        // 2. Kiểm tra xem mã có tồn tại, khớp với người dùng nhập, và còn hạn hay không.
        boolean isValid = details != null && details.code.equals(userInputCode) && LocalDateTime.now().isBefore(details.expiryTime);
        // 3. Nếu không hợp lệ, xử lý lỗi và ném exception.
        if (!isValid) {
            // 3a. Nếu mã tồn tại nhưng đã hết hạn, xóa nó khỏi bộ nhớ.
            if (details != null && LocalDateTime.now().isAfter(details.expiryTime)) {
                verificationStore.remove(email);
                log.warn("Verification failed for {}: Code expired.", email);
            } else {
                // 3b. Nếu mã không tồn tại hoặc sai.
                log.warn("Verification failed for {}: Invalid code.", email);
            }
            // 3c. Ném exception để thông báo cho lớp gọi.
            throw new InvalidVerificationCodeException("The verification code is not valid or has expired.");
        }
        // 4. Nếu hợp lệ, xóa mã đã sử dụng khỏi bộ nhớ để tránh tái sử dụng.
        verificationStore.remove(email);
        // 5. Ghi log thành công.
        log.info("Code verified successfully for email: {}", email);
    }

    @Override
    public long getVerificationCodeExpirySeconds() {
        return this.verificationCodeExpirySeconds;
    }

    public void removeVerificationCode(String email) {
        // 1. Xóa mã xác thực khỏi bộ nhớ đệm dựa trên email.
        verificationStore.remove(email);
    }

    private void checkRateLimit(String email) throws RateLimitExceededException {
        // 1. Lấy thời điểm lần gửi cuối cùng từ bộ nhớ.
        Long lastAttemptTimestamp = rateLimitStore.get(email);
        // 2. Nếu đã có lần gửi trước đó.
        if (lastAttemptTimestamp != null) {
            // 3. Tính toán số giây đã trôi qua kể từ lần gửi cuối.
            long secondsSinceLastAttempt = (System.currentTimeMillis() - lastAttemptTimestamp) / 1000;
            // 4. Nếu số giây này nhỏ hơn giới hạn đã cấu hình, ném ra lỗi.
            if (secondsSinceLastAttempt < rateLimitSeconds) {
                long secondsRemaining = rateLimitSeconds - secondsSinceLastAttempt;
                throw new RateLimitExceededException("Please wait " + secondsRemaining + " seconds before requesting a new code.", secondsRemaining);
            }
        }
    }

    private void recordSuccessfulAttempt(String email) {
        // 1. Ghi lại thời điểm hiện tại (dưới dạng mili giây) vào bộ nhớ rate limit.
        rateLimitStore.put(email, System.currentTimeMillis());
    }

    // ===================================================================================
    // SECTION 3: CÁC LOẠI EMAIL THÔNG BÁO KHÁC (GIỮ NGUYÊN TỪ FILE GỐC)
    // ===================================================================================

    @Override
    public void sendWelcomeEmail(String toEmail, String fullName) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template chào mừng.
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("profileUrl", frontendUrl + "/jobseeker/profile");
        // 2. Gửi email sử dụng template 'welcome-email'.
        sendEmailWithLogo(toEmail, "Welcome to BaoNgoCv!", "emails/welcome-email", context);
    }

    @Override
    public void sendContactEmail(String name, String email, String messageContent) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template form liên hệ.
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("email", email);
        context.setVariable("messageContent", messageContent);
        // 2. Gửi email đến địa chỉ quản trị viên, sử dụng template 'contact-form'.
        sendEmailWithLogo(contactRecipientEmail, "BaoNgoCV - New Contact Form Submission from: " + name, "emails/contact-form", context);
    }

    @Override
    public void sendNewApplicantNotificationToCompany(String companyEmail, String companyName, String applicantName, String jobTitle, String applicationLink) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo ứng viên mới.
        Context context = new Context();
        context.setVariable("companyName", companyName);
        context.setVariable("applicantName", applicantName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("applicationLink", applicationLink);
        // 2. Gửi email cho công ty, sử dụng template 'new-applicant-notification-company'.
        sendEmailWithLogo(companyEmail, "New Application Received: " + applicantName + " for " + jobTitle, "emails/new-applicant-notification-company", context);
    }

    @Override
    public void sendApplicationConfirmation(String applicantEmail, String applicantName, String jobTitle, String companyName, String trackingUrl) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template xác nhận ứng tuyển.
        Context context = new Context();
        context.setVariable("applicantName", applicantName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("companyName", companyName);
        context.setVariable("trackingUrl", trackingUrl);
        // 2. Gửi email cho ứng viên, sử dụng template 'application-confirmation'.
        sendEmailWithLogo(applicantEmail, "Application Confirmation - " + jobTitle, "emails/application-confirmation", context);
    }

    @Override
    public void sendAccountDeletionConfirmationEmail(String userEmail) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template xác nhận xóa tài khoản.
        Context context = new Context();
        context.setVariable("email", userEmail);
        context.setVariable("deletionDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        context.setVariable("supportEmail", contactRecipientEmail); // ✅ Sử dụng biến cấu hình
        context.setVariable("websiteUrl", frontendUrl); // ✅ Sử dụng biến cấu hình
        // 2. Gửi email cho người dùng, sử dụng template 'account-delete-confirmation'.
        sendEmailWithLogo(userEmail, "Account Deletion Confirmation - BaoNgoCV", "emails/account-delete-confirmation", context);
    }

    @Override
    public void sendUpgradeSuccessEmail(String toEmail, String companyName, AccountTier newTier) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo nâng cấp thành công.
        Context context = new Context();
        context.setVariable("companyName", companyName);
        context.setVariable("newTier", newTier.getDisplayName());
        context.setVariable("tierBenefits", getTierBenefits(newTier));
        context.setVariable("upgradedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("dashboardUrl", frontendUrl + "/company/dashboard");
        context.setVariable("supportEmail", "support@baongocv.com");
        // 2. Gửi email cho công ty, sử dụng template 'account-upgrade-successful'.
        sendEmailWithLogo(toEmail, "🎉 Congratulations! Your Account Has Been Upgraded to " + newTier.getDisplayName(), "emails/account-upgrade-successful", context);
    }

    @Override
    public void sendSubscriptionDowngradedEmail(String toEmail, String companyName, AccountTier oldTier) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo hạ cấp.
        Context context = new Context();
        context.setVariable("companyName", companyName);
        context.setVariable("oldTierName", oldTier.getDisplayName());
        context.setVariable("upgradeUrl", frontendUrl + "/company/profile");
        context.setVariable("currentYear", Year.now().getValue());
        // 2. Gửi email cho công ty, sử dụng template 'subscription-downgraded'.
        sendEmailWithLogo(toEmail, "🚨 Important: Your Subscription Plan Has Expired", "emails/subscription-downgraded", context);
    }

    @Override
    public void sendShortlistNotificationEmail(String toEmail, String applicantName, String companyName, String jobTitle, String applicationLink) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo vào danh sách rút gọn.
        Context context = new Context();
        context.setVariable("applicantName", applicantName);
        context.setVariable("companyName", companyName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("applicationLink", frontendUrl+applicationLink);

        // 2. Gửi email cho ứng viên, sử dụng template 'shortlist-notification'.
        sendEmailWithLogo(toEmail, "Tin vui từ " + companyName + " | Chúc mừng bạn đã vào danh sách rút gọn!", "emails/shortlist-notification", context);
    }

    @Override
    public void sendRejectNotificationEmail(String toEmail, String applicantName, String companyName, String jobTitle, String applicationLink) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo từ chối hồ sơ.
        Context context = new Context();
        context.setVariable("applicantName", applicantName);
        context.setVariable("companyName", companyName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("applicationLink", applicationLink);
        // 2. Gửi email cho ứng viên, sử dụng template 'reject-notification'.
        sendEmailWithLogo(toEmail, "Thông tin về hồ sơ ứng tuyển của bạn tại " + companyName, "emails/reject-notification", context);
    }

    @Override
    public void sendApplicantWithdrawalNotification(String companyEmail, String companyName, String applicantName, String jobTitle, Long jobPostingId) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template.
        Context context = new Context();
        context.setVariable("companyName", companyName);
        context.setVariable("applicantName", applicantName);
        context.setVariable("jobTitle", jobTitle);

        // [FIX] Xây dựng URL tại đây, sử dụng frontendUrl đã được cấu hình
        String applicationLink = frontendUrl + "/company/job-view-applicants?jobId=" + jobPostingId;
        context.setVariable("applicationLink", applicationLink);
        context.setVariable("currentYear", Year.now().getValue());

        // 2. Tạo tiêu đề email.
        String subject = "Application Withdrawn: " + applicantName + " for " + jobTitle;

        // 3. Gửi email sử dụng template 'applicant-withdrew-notification'.
        sendEmailWithLogo(companyEmail, subject, "emails/applicant-withdrew-notification", context);
        log.info("✅ Sent applicant withdrawal notification to {} for job '{}'", companyEmail, jobTitle);
    }

    @Override
    public void sendReviewNotificationEmail(String toEmail, String applicantName, String companyName, String jobTitle, String reviewMessage, String reviewLink) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo có đánh giá mới.
        Context context = new Context();
        context.setVariable("applicantName", applicantName);
        context.setVariable("companyName", companyName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("reviewMessage", reviewMessage);
        context.setVariable("reviewLink", reviewLink);
        // 2. Gửi email cho ứng viên, sử dụng template 'review-notification'.
        sendEmailWithLogo(toEmail, "BaoNgoCV - Bạn có một phản hồi mới từ " + companyName, "emails/review-notification", context);
    }

    @Override
    public void sendCompanyWelcomeEmail(String toEmail, String companyName) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template chào mừng công ty.
        Context context = new Context();
        context.setVariable("companyName", companyName);
        context.setVariable("loginUrl", frontendUrl + "/user/login");
        context.setVariable("currentYear", Year.now().getValue());

        // 2. Gửi email sử dụng template 'company-welcome-email'.
        sendEmailWithLogo(toEmail, "Welcome to BaoNgoCv, " + companyName + "!", "emails/company-welcome-email", context);
    }


    @Override
    public void sendJobPostingConfirmationEmail(String companyEmail, String companyName, String jobTitle) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template xác nhận đăng tin.
        Context context = new Context();
        context.setVariable("companyName", companyName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("currentYear", Year.now().getValue());
        // 2. Gửi email cho công ty, sử dụng template 'job-posting-confirmation'.
        sendEmailWithLogo(companyEmail, "Your Job Posting '" + jobTitle + "' is Live!", "emails/job-posting-confirmation", context);
    }

    @Override
    public void sendJobExpiredNotification(String employerEmail, String jobTitle) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo tin hết hạn.
        Context context = new Context();
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("expirationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("supportEmail", "support@baongocv.com");
        context.setVariable("websiteUrl", frontendUrl);
        // 2. Gửi email cho nhà tuyển dụng, sử dụng template 'job-expired-notification'.
        sendEmailWithLogo(employerEmail, "⏰ Job Posting Expired - " + jobTitle, "emails/job-expired-notification", context);
    }

    @Override
    public void sendNewJobFromFollowedCompanyEmail(String to, String companyName, String jobTitle, String jobseekerName) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo có việc mới từ công ty theo dõi.
        Context context = new Context();
        context.setVariable("jobseekerName", jobseekerName);
        context.setVariable("companyName", companyName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("jobUrl", frontendUrl + "/job/" + jobTitle.toLowerCase().replace(" ", "-"));
        // 2. Gửi email cho người tìm việc, sử dụng template 'new-job-notification'.
        sendEmailWithLogo(to, "New Job Opportunity from " + companyName + "!", "emails/new-job-notification", context);
    }

    @Override
    public void sendJobPostingDeletedNotification(String toEmail, String applicantName, String jobTitle, String companyName) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template thông báo tin tuyển dụng đã bị xóa.
        Context context = new Context();
        context.setVariable("applicantName", applicantName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("companyName", companyName);
        // 2. Gửi email cho ứng viên, sử dụng template 'job-posting-deleted'.
        sendEmailWithLogo(toEmail, "Regarding your application for " + jobTitle, "emails/job-posting-deleted", context);
    }

    @Override
    public void sendJobPostingReminderEmail(String toEmail, String fullName, String jobTitle) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template nhắc nhở về công việc đã lưu.
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("jobUrl", frontendUrl + "/job/" + jobTitle.toLowerCase().replace(" ", "-"));
        context.setVariable("currentYear", Year.now().getValue());
        // 2. Gửi email cho người dùng, sử dụng template 'job-posting-reminder'.
        sendEmailWithLogo(toEmail, "Reminder: Don't miss out on the '" + jobTitle + "' opportunity!", "emails/job-posting-reminder", context);
    }

    @Override
    public void sendJobExpiringSoonReminderEmail(String to, String companyName, String jobTitle, Long jobId) {
        log.info("📧 Preparing job expiring soon reminder for job '{}' to {}", jobTitle, to);

        try {
            final String subject = "Reminder: Your Job Posting '" + jobTitle + "' is Expiring in 3 Days";

            // Tạo liên kết đến trang quản lý công việc
            final String jobManagementLink = frontendUrl + "/company/jobposting-managing";

            // Chuẩn bị các biến cho Thymeleaf template
            Context context = new Context();
            context.setVariable("companyName", companyName);
            context.setVariable("jobTitle", jobTitle);
            context.setVariable("jobManagementLink", jobManagementLink);
            context.setVariable("currentYear", Year.now().getValue());

            // Gửi email sử dụng template mới
            sendEmailWithLogo(
                    to,
                    subject,
                    "emails/job-expiring-soon-reminder", // Tên template mới
                    context
            );

            log.info("✅ Successfully sent job expiring soon reminder for job '{}' to {}", jobTitle, to);

        } catch (Exception e) {
            log.error("❌ Failed to send job expiring soon reminder email for job '{}' to {}. Error: {}",
                    jobTitle, to, e.getMessage(), e);
        }
    }

    @Override
    public void sendNewApplicationNotification(
            String employerEmail,
            String employerName,
            String jobTitle,
            String applicantName,
            String applicantAvatarUrl,
            Long applicationId
    ) throws MessagingException {

        // 1. XỬ LÝ AVATAR URL (Ghép với domain nếu cần)
        String fullAvatarUrl;
        if (applicantAvatarUrl == null || applicantAvatarUrl.trim().isEmpty()) {
            // Fallback về ảnh mặc định nếu user chưa có avatar
            fullAvatarUrl = frontendUrl + "/img/default/defaultProfilePicture.jpg";
        } else if (applicantAvatarUrl.startsWith("http")) {
            // Nếu đã là URL đầy đủ (Google/Facebook avatar) -> giữ nguyên
            fullAvatarUrl = applicantAvatarUrl;
        } else {
            // Nếu là path tương đối (/uploads/...) -> ghép với domain
            String cleanPath = applicantAvatarUrl.startsWith("/")
                    ? applicantAvatarUrl
                    : "/" + applicantAvatarUrl;
            fullAvatarUrl = frontendUrl + cleanPath;
        }

        // 2. XỬ LÝ APPLICATION VIEW URL (Build từ ID)
        String applicationViewUrl = frontendUrl + "/company/job-application-detail/" + applicationId;

        // 3. ĐẶT BIẾN VÀO CONTEXT
        Context context = new Context();
        context.setVariable("employerName", employerName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("applicantName", applicantName);
        context.setVariable("applicantAvatarUrl", fullAvatarUrl);
        context.setVariable("applicationViewUrl", applicationViewUrl);
        context.setVariable("currentYear", Year.now().getValue());

        // 4. GỬI EMAIL
        sendEmailWithLogo(
                employerEmail,
                "New Job Application - " + jobTitle,
                "emails/new-application-notification",
                context
        );

        log.info("✅ Sent new application notification to {} for job '{}'", employerEmail, jobTitle);
    }



    @Override
    public void sendCompanyDeletedNotificationToUser(User user, String companyName) {
        try {
            Context context = new Context();
            context.setVariable("applicantName", user.getPersonalInfo().getFullName());
            context.setVariable("companyName", companyName);
            context.setVariable("currentYear", Year.now().getValue());
            context.setVariable("websiteUrl", frontendUrl);

            sendEmailWithLogo(user.getContactInfo().getEmail(), "An Update Regarding a Company You Follow", "emails/company-deleted-notification", context);
            log.info("Sent company deletion notification email to user {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send company deletion notification email to user {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendInterviewInvitation(String candidateEmail, String subject, String content,Long applicantId) {
        try {
            // 1. TẠO CONTEXT CHO THYMELEAF
            Context context = new Context();

            // 2. ĐẶT BIẾN (Biến 'content' chứa toàn bộ thông tin ngày giờ địa điểm mà bạn đã gom lại ở frontend)
            // Chúng ta sẽ render nội dung này vào template.
            // Lưu ý: Vì 'content' từ frontend có xuống dòng (\n), ta cần xử lý để hiển thị đẹp trên HTML nếu muốn (hoặc để CSS lo).
            context.setVariable("emailContent", content);
            context.setVariable("currentYear", Year.now().getValue());
            context.setVariable("websiteUrl", frontendUrl+"/jobseeker/my-application/"+applicantId);

            // 3. GỌI HÀM GỬI EMAIL DÙNG CHUNG (Đã có logic logo, mime type...)
            // Template file: src/main/resources/templates/emails/interview-invitation.html
            sendEmailWithLogo(candidateEmail, subject, "emails/interview-invitation", context);

            log.info("✅ Interview invitation sent successfully to {}", candidateEmail);

        } catch (MessagingException e) {
            log.error("❌ Failed to send interview invitation to {}: {}", candidateEmail, e.getMessage());
            // Tùy chọn: Ném lại RuntimeException để Controller biết mà báo lỗi cho Frontend
            throw new EmailSendingException("Failed to send interview invitation", e, candidateEmail);
        }
    }

    @Override
    public void sendRejectionEmail(String toEmail, String fullName, String companyName, String jobTitle) throws MessagingException {
        // 1. Tạo context và đặt các biến cho template.
        Context context = new Context();
        context.setVariable("applicantName", fullName);
        context.setVariable("companyName", companyName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("jobSearchUrl", frontendUrl + "/jobseeker/job-search");

        // 2. Tạo tiêu đề email (Cần trang trọng và nhẹ nhàng)
        String subject = "Update regarding your application for " + jobTitle + " at " + companyName;

        // 3. Gửi email sử dụng template 'application-rejection'

        sendEmailWithLogo(toEmail, subject, "emails/application-rejection", context);

        log.info("✅ Sent rejection email to {} for job '{}'", toEmail, jobTitle);
    }

    // ===================================================================================
    // SECTION 4: HÀM TIỆN ÍCH VÀ SCHEDULED TASK
    // ===================================================================================

    private String extractUserName(String email) {
        // 1. Kiểm tra nếu email là null hoặc không chứa ký tự '@'.
        if (email == null || !email.contains("@")) { return "User"; }
        // 2. Trả về phần chuỗi trước ký tự '@'.
        return email.substring(0, email.indexOf("@"));
    }

    private List<String> getTierBenefits(AccountTier tier) {
        // 1. Trả về danh sách các quyền lợi tương ứng với từng gói tài khoản.
        return switch (tier) {
            case PREMIUM -> List.of("Unlimited job postings", "Advanced analytics and reporting", "Priority support", "Featured company profile", "Access to advanced filtering tools");
            case BASIC -> List.of("Up to 50 job postings per month", "Basic analytics", "Email support", "Standard company profile");
            case FREE -> List.of("Up to 5 job postings per month", "Community support");
        };
    }

    @Scheduled(fixedRate = 3600000) // Chạy mỗi giờ
    public void cleanupExpiredEntries() {
        // 1. Ghi log bắt đầu tác vụ dọn dẹp.
        log.info("Running scheduled task to clean up expired entries.");
        // 2. Xóa các mã xác thực đã hết hạn khỏi bộ nhớ.
        verificationStore.entrySet().removeIf(entry -> LocalDateTime.now().isAfter(entry.getValue().expiryTime));
        // 3. Xóa các bản ghi rate limit cũ hơn 2 giờ để tránh làm đầy bộ nhớ.
        long twoHoursAgo = System.currentTimeMillis() - (2 * 3600000);
        rateLimitStore.entrySet().removeIf(entry -> entry.getValue() < twoHoursAgo);
        // 4. Ghi log kết quả sau khi dọn dẹp.
        log.info("Finished cleaning up. Verification store size: {}, Rate limit store size: {}", verificationStore.size(), rateLimitStore.size());
    }

    private void sendEmailWithLogo(String to, String subject, String templateName, Context context)
            throws MessagingException {

        try {
            // 1. Thêm biến logo URL cho template (nếu cần)
            context.setVariable("logoUrl", frontendUrl + "/img/logo/logoShop.png");

            // 2. Render HTML bằng Thymeleaf
            String htmlContent = templateEngine.process(templateName, context);

            // 3. Gửi qua SendGrid HTTP
            sendGridEmailClient.sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send email via SendGrid to {} with subject {}: {}", to, subject, e.getMessage(), e);
            if (e instanceof MessagingException) {
                throw (MessagingException) e;
            }
            // Quăng ra EmailSendingException để logic phía trên vẫn dùng như cũ
            throw new EmailSendingException("Failed to send email via SendGrid", e, to);
        }
    }

}
