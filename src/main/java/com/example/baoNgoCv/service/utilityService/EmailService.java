package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.exception.emailException.InvalidVerificationCodeException;
import com.example.baoNgoCv.exception.securityException.RateLimitExceededException;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.VerificationType;
import jakarta.mail.MessagingException;
import com.example.baoNgoCv.model.entity.User;

public interface EmailService {

    // ===================================================================================
    // SECTION 1: LOGIC GỬI MÃ XÁC THỰC - Public API chính
    // ===================================================================================

    /**
     * Gửi mã xác thực cho một mục đích cụ thể (đăng ký, đổi mật khẩu, etc.).
     * Phương thức này đã bao gồm logic rate limit.
     */
    void sendVerification(String email, VerificationType type) throws MessagingException, RateLimitExceededException;

    /**
     * Xác thực mã OTP mà người dùng nhập.
     * Ném exception nếu mã không hợp lệ hoặc hết hạn.
     */
    void verifyCode(String email, String userInputCode) throws InvalidVerificationCodeException;

    /**
     * Lấy thời gian hết hạn của mã xác thực đã cấu hình (tính bằng giây).
     */
    long getVerificationCodeExpirySeconds();


    // ===================================================================================
    // SECTION 2: CÁC EMAIL THÔNG BÁO KHÁC
    // ===================================================================================

    // --- Account Management ---

    /**
     * Gửi email chào mừng đến người dùng mới sau khi đăng ký thành công.
     */
    void sendWelcomeEmail(String toEmail, String fullName) throws MessagingException;

    /**
     * Gửi email xác nhận tài khoản đã bị xóa vĩnh viễn.
     */
    void sendAccountDeletionConfirmationEmail(String userEmail) throws MessagingException;

    /**
     * Gửi email cảm ơn và xác nhận khi một tài khoản công ty bị xóa.
     */


    /**
     * Gửi email thông báo nâng cấp gói tài khoản thành công.
     */
    void sendUpgradeSuccessEmail(String toEmail, String companyName, AccountTier newTier) throws MessagingException;

    /**
     * Gửi email cảnh báo khi gói tài khoản trả phí đã hết hạn và bị hạ xuống gói miễn phí.
     */
    void sendSubscriptionDowngradedEmail(String toEmail, String companyName, AccountTier oldTier) throws MessagingException;

    // --- Application Flow ---

    /**
     * Gửi email chào mừng đến công ty mới sau khi đăng ký thành công.
     */
    void sendCompanyWelcomeEmail(String toEmail, String companyName) throws MessagingException;

    /**
     * Gửi email xác nhận cho ứng viên sau khi họ nộp hồ sơ thành công.
     */
    void sendApplicationConfirmation(String applicantEmail, String applicantName, String jobTitle, String companyName, String trackingUrl) throws MessagingException;

    /**
     * Gửi email thông báo cho nhà tuyển dụng khi có một ứng viên mới nộp hồ sơ.
     */
    void sendNewApplicantNotificationToCompany(String companyEmail, String companyName, String applicantName, String jobTitle, String applicationLink) throws MessagingException;

    /**
     * Gửi email thông báo cho ứng viên khi họ được đưa vào danh sách rút gọn (shortlisted).
     */
    void sendShortlistNotificationEmail(String toEmail, String applicantName, String companyName, String jobTitle, String applicationLink) throws MessagingException;

    /**
     * Gửi email thông báo cho ứng viên khi hồ sơ của họ bị từ chối.
     */
    void sendRejectNotificationEmail(String toEmail, String applicantName, String companyName, String jobTitle, String applicationLink) throws MessagingException;

    /**
     * [NEW] Gửi email thông báo cho nhà tuyển dụng khi một ứng viên rút hồ sơ.
     */
    void sendApplicantWithdrawalNotification(String companyEmail, String companyName, String applicantName, String jobTitle, Long jobPostingId) throws MessagingException;

    /**
     * Gửi email cho ứng viên khi nhà tuyển dụng để lại một đánh giá (review) về hồ sơ của họ.
     */
    void sendReviewNotificationEmail(String toEmail, String applicantName, String companyName, String jobTitle, String reviewMessage, String reviewLink) throws MessagingException;

    void sendNewApplicationNotification(
            String employerEmail,
            String employerName,
            String jobTitle,
            String applicantName,
            String applicantAvatarUrl,
            Long applicationId
    ) throws MessagingException;


    // --- Job Posting Flow ---

    /**
     * Gửi email xác nhận cho nhà tuyển dụng khi tin tuyển dụng của họ được đăng thành công.
     */
    void sendJobPostingConfirmationEmail(String companyEmail, String companyName, String jobTitle) throws MessagingException;

    /**
     * Gửi email thông báo cho nhà tuyển dụng khi tin tuyển dụng của họ đã hết hạn.
     */
    void sendJobExpiredNotification(String employerEmail, String jobTitle) throws MessagingException;

    /**
     * Gửi email cho người tìm việc khi một công ty họ theo dõi đăng một công việc mới.
     */
    void sendNewJobFromFollowedCompanyEmail(String to, String companyName, String jobTitle, String jobseekerName) throws MessagingException;

    /**
     * Gửi email cho ứng viên thông báo rằng tin tuyển dụng họ đã ứng tuyển đã bị xóa.
     */
    void sendJobPostingDeletedNotification(String toEmail, String applicantName, String jobTitle, String companyName) throws MessagingException;

    /**
     * Gửi email nhắc nhở người dùng về một công việc họ đã lưu hoặc quan tâm.
     */
    void sendJobPostingReminderEmail(String toEmail, String fullName, String jobTitle) throws MessagingException;

    /**
     * [NEW] Gửi email nhắc nhở nhà tuyển dụng rằng tin tuyển dụng của họ sắp hết hạn.
     * @param to          Địa chỉ email của công ty.
     * @param companyName Tên công ty.
     * @param jobTitle    Tiêu đề công việc.
     * @param jobId       ID của công việc để tạo liên kết trong email.
     */
    void sendJobExpiringSoonReminderEmail(String to, String companyName, String jobTitle, Long jobId) throws MessagingException;

    // --- General ---

    /**
     * Gửi email từ form liên hệ trên website đến người quản trị.
     */
    void sendContactEmail(String name, String email, String messageContent) throws MessagingException;

    /**
     * Gửi email cho người dùng thông báo rằng một công ty họ theo dõi/ứng tuyển đã bị xóa.
     */
    void sendCompanyDeletedNotificationToUser(User user, String companyName);

    void sendInterviewInvitation(String candidateEmail, String subject, String content, Long applicantId);

    void sendRejectionEmail(String toEmail, String fullName, String companyName, String jobTitle) throws MessagingException;
}
