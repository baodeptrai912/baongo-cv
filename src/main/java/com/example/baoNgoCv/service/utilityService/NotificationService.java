package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.event.applicant.ApplicantStatusChangedEvent;
import com.example.baoNgoCv.event.applicant.ApplicantWithdrewEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingCreatedEvent;
import com.example.baoNgoCv.model.dto.NotificationAllDTO;
import com.example.baoNgoCv.model.dto.notification.GetAllNotificationResponse;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.Notification;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.NotificationType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface NotificationService {
    //1. Lấy danh sách thông báo cho người dùng hoặc công ty hiện tại, có phân trang và bộ lọc.
    @org.springframework.transaction.annotation.Transactional
    GetAllNotificationResponse getNotificationsForCurrentUser(Authentication authen, String status, String type, Pageable pageable);

    //2. Tạo thông báo chào mừng cho một công ty mới đăng ký thành công.
    @Transactional
    void createCompanyWelcomeNotification(Long companyId);

    //3. Gửi thông báo real-time (WebSocket) đến một người dùng cụ thể.
    void sendReviewNotificationToUser(String title, User user, String avatar, String sender, String href);

    //4. Tìm tất cả thông báo của một người dùng (có phân trang).
    Page<Notification> findByUser(User user, Pageable pageable);

    //5. Tìm thông báo của người dùng theo trạng thái (đã đọc/chưa đọc) và phân trang.
    Page<Notification> findByUserAndStatus(User user, String status, Pageable pageable);

    /**
     * 6. Đánh dấu tất cả thông báo của người dùng là đã đọc.
     * @deprecated Use {@link #markAllAsReadForCurrentUser(Authentication)} instead for better type safety.
     */
    void markAllAsRead(User user);

    //7. Xóa một thông báo cụ thể dựa vào ID.
    void deleteNotification(Long notificationId);

    //8. Lấy số lượng thông báo (tổng cộng và chưa đọc) cho người dùng hoặc công ty.
    Map<String, Integer> getNotificationCounts(User user, Company company);

    //9. Chuyển đổi trạng thái đã đọc/chưa đọc của một thông báo.
    Notification toggleReadStatus(Long notificationId, User user, Company company);

    //10. Lấy số lượng thông báo cho mỗi loại (type) có sẵn của người dùng hoặc công ty.
    Map<String, Integer> getAvailableTypeCounts(User user, Company company);

    //11. Tạo một bản ghi thông báo trong cơ sở dữ liệu khi có đơn ứng tuyển mới.
    Notification createApplicationNotification(Company employer, User jobSeeker, String jobTitle, Long applicantId);

    //13. Điều phối việc tạo và gửi thông báo cho nhà tuyển dụng về đơn ứng tuyển mới.
    void notifyEmployerOfNewApplication(
            String companyUsername, // Thay cho Company employer
            String candidateName,   // Thay cho User jobSeeker
            String candidateAvatar, // Thay cho User jobSeeker
            String jobTitle,
            Long applicantId
    );

    //14. Tạo thông báo chào mừng cho người dùng (job seeker) mới đăng ký.
    void createWelcomeNotification(Long userId);

    //15. Thông báo cho nhà tuyển dụng rằng một bài đăng của họ đã hết hạn.
    void notifyEmployerOfJobExpiry(Long companyId, String jobTitle);

    //16. Thông báo cho những người đã lưu một công việc rằng công việc đó đã hết hạn.
    void notifySaversOfJobExpiry(Long jobPostingId, String jobTitle);

    //17. Đánh dấu một thông báo cụ thể là đã đọc.
    void markAsRead(Long notificationId);

    //18. Thông báo cho những người theo dõi công ty về một bài đăng tuyển dụng mới.
    void notifyFollowersOfNewJob(JobPostingCreatedEvent event);

    //19. Thông báo cho công ty khi họ nâng cấp gói dịch vụ thành công.
    void notifyCompanyOfUpgradePlanSuccess(Long companyId, String companyName, AccountTier newTier);

    //20. Thông báo cho công ty khi gói dịch vụ của họ bị hạ cấp (ví dụ: do hết hạn).
    void notifyCompanyOfDowngrade(Long companyId, String companyName, AccountTier oldTier);

    //21. Tạo và gửi một thông báo chung cho người dùng với một tin nhắn và liên kết cụ thể.
    Notification createAndSendNotification(Long userId, String message, String link);

    //22. Đánh dấu tất cả thông báo của người dùng/công ty đang đăng nhập là đã đọc.
    void markAllAsReadForCurrentUser(Authentication authentication);

    //23
    void createAndSendStatusNotification(ApplicantStatusChangedEvent event);

    //24. [NEW] Listener for when an applicant withdraws
    void handleApplicantWithdrawal(ApplicantWithdrewEvent event);
}
