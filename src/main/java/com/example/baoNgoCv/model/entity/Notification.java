package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    // Constants for Welcome Notification
    private static final String WELCOME_TITLE = "Welcome to BaoNgoCv!";
    private static final String WELCOME_MESSAGE = "Your account has been created successfully. Complete your profile to start applying for jobs!";
    private static final String WELCOME_HREF = "/jobseeker/profile";
    private static final String SYSTEM_AVATAR = "/img/logo/logoShop.png";

    // Constants for Job Expired Notification
    private static final String JOB_EXPIRED_TITLE = "Job Posting Expired";
    private static final String JOB_EXPIRED_MESSAGE_FORMAT = "Your job posting '%s' has expired and is no longer active.";
    private static final String JOB_EXPIRED_HREF = "/company/job-posting-management";
    private static final String JOB_EXPIRED_AVATAR = "/img/system/expired-icon.png";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message", length = 5000)
    private String message;

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "sender_user_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User senderUser;

    @ManyToOne
    @JoinColumn(name = "sender_company_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Company senderCompany;

    @CreatedDate // Tự động gán ngày giờ tạo
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // Tự động gán ngày giờ cập nhật
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "recipient_company_id", referencedColumnName = "id", nullable = true)
    private Company recipientCompany;

    @ManyToOne
    @JoinColumn(name = "recipient_user_id", referencedColumnName = "id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User recipientUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Applicant applicant;

    @ManyToOne
    @JoinColumn(name = "job_posting_id", referencedColumnName = "id", nullable = true)
    private JobPosting jobPosting;

    @Column(name = "avatar")
    private String avatar;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "href")
    private String href;

    // Custom constructor for common use case
    public Notification(String title, NotificationType type, String avatar) {
        this.title = title;
        this.type = type;
        this.avatar = avatar;
        this.isRead = false;
    }

    /**
     * Factory method để tạo thông báo chào mừng người dùng mới.
     * Encapsulates the logic for creating a welcome notification.
     *
     * @param recipientUser Người dùng mới đăng ký.
     * @return Một thực thể Notification đã được cấu hình.
     */
    public static Notification createWelcomeNotification(User recipientUser) {
        return Notification.builder()
                .recipientUser(recipientUser)
                .title(WELCOME_TITLE)
                .message(WELCOME_MESSAGE)
                .type(NotificationType.WELCOME)
                .href(WELCOME_HREF)
                .avatar(SYSTEM_AVATAR)
                .isRead(false)
                .build();
    }

    /**
     * Factory method để tạo thông báo có ứng tuyển mới cho nhà tuyển dụng.
     *
     * @param employer    Công ty nhận thông báo.
     * @param jobSeeker   Người dùng ứng tuyển.
     * @param jobTitle    Chức danh công việc.
     * @param applicantId ID của đơn ứng tuyển.
     * @return Một thực thể Notification đã được cấu hình.
     */
    public static Notification createNewApplicationNotification(Company employer, User jobSeeker, String jobTitle, Long applicantId) {
        String message = String.format("You have received a new job application from %s for position: %s",
                jobSeeker.getPersonalInfo().getFullName(), jobTitle);

        return Notification.builder()
                .recipientCompany(employer)
                .senderUser(jobSeeker)
                .title("New Job Application")
                .message(message)
                .type(NotificationType.NEW_APPLICATION)
                .href("/company/job-application-detail/" + applicantId)
                .avatar(jobSeeker.getProfilePicture())
                .isRead(false)
                .build();
    }

    /**
     * Factory method để tạo thông báo tin tuyển dụng đã hết hạn cho nhà tuyển dụng.
     *
     * @param employer Công ty nhận thông báo.
     * @param jobTitle Tiêu đề của công việc đã hết hạn.
     * @return Một thực thể Notification đã được cấu hình.
     */
    public static Notification createJobExpiredNotification(Company employer, String jobTitle) {
        String message = String.format(JOB_EXPIRED_MESSAGE_FORMAT, jobTitle);

        return Notification.builder()
                .recipientCompany(employer)
                .title(JOB_EXPIRED_TITLE)
                .message(message)
                .type(NotificationType.JOB_EXPIRED)
                .href("/company/jobposting-managing")
                .avatar("/img/logo/logoShop.png")
                .isRead(false)
                .build();
    }

    /**
     * Factory method để tạo thông báo cho người dùng đã lưu công việc khi nó hết hạn.
     *
     * @param jobSeeker    Người dùng nhận thông báo.
     * @param jobTitle     Tiêu đề của công việc đã hết hạn.
     * @param jobPostingId ID của công việc để có thể tạo link sau này (nếu cần).
     * @return Một thực thể Notification đã được cấu hình.
     */
    public static Notification createJobExpiredForSaverNotification(User jobSeeker, String jobTitle, Long jobPostingId) {
        String message = String.format("The job posting '%s' you saved has expired and is no longer available for application.", jobTitle);

        return Notification.builder()
                .recipientUser(jobSeeker)
                .title("A Saved Job Has Expired")
                .message(message)
                .type(NotificationType.JOB_EXPIRED)
                .href("/jobseeker/job-save?highlight=" + jobPostingId)
                .avatar(SYSTEM_AVATAR)
                .isRead(false)
                .build();
    }

    /**
     * Hoàn thiện chuỗi href bằng cách nối ID của notification vào dưới dạng query parameter.
     * <p>
     * Ví dụ: "/path/to/page" sẽ trở thành "/path/to/page?notificationId=123".
     * Nếu href đã có query param, nó sẽ nối thêm bằng dấu "&".
     */
    public String getFullHref() {
        if (this.href != null && !this.href.isBlank() && this.id != null) {
            if (this.href.contains("?")) {
                return this.href + "&notificationId=" + this.id;
            } else {
                return this.href + "?notificationId=" + this.id;
            }
        }
        return this.href;
    }

}
