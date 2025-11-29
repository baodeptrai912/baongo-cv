package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.exception.application.ApplicantStatusTransitionException;
import com.example.baoNgoCv.exception.application.ReviewNotAllowedException;
import com.example.baoNgoCv.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OrderBy;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(mappedBy = "applicant", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_posting_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private JobPosting jobPosting;

    @Column(name = "application_date")
    private LocalDateTime applicationDate;

    @Column(name = "resume", length = 1000)
    private String resume;

    @Column(name = "cover_letter", length = 2000)
    private String coverLetter;

    @OneToOne(mappedBy = "applicant", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private ApplicationReview review;

    @OneToMany(mappedBy = "applicant",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    @OrderBy("statusDate ASC")
    @Builder.Default
    private List<ApplicationStatusHistory> statusHistory = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "applicant",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private InterviewSchedule interviewSchedule;

    @PrePersist
    private void addInitialStatusHistory() {

        if (this.statusHistory == null) this.statusHistory = new ArrayList<>();
        if (this.statusHistory.isEmpty()) {
            ApplicationStatusHistory initialHistory = ApplicationStatusHistory.builder()
                    .applicant(this)
                    .status(ApplicationStatus.PENDING)
                    .statusDate(LocalDateTime.now())
                    .isCurrent(true)
                    .build();
            this.statusHistory.add(initialHistory);
        }
    }

    public ApplicationStatusHistory getCurrentStatusHistory() {
        return statusHistory.stream()
                .filter(ApplicationStatusHistory::isCurrent)
                .findFirst()
                .orElse(null);
    }

    public List<ApplicationStatusHistory> getOrderedStatusHistory() {
        return statusHistory.stream()
                .sorted(Comparator.comparing(ApplicationStatusHistory::getStatusDate).reversed()) // Đảo ngược thứ tự
                .collect(Collectors.toList());
    }

    public String getFormattedCurrentStatus() {
        ApplicationStatusHistory current = getCurrentStatusHistory();
        return current != null ? current.getStatus().name() : "UNKNOWN";
    }

    public String getFormattedApplicationDate() {
        if (applicationDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return applicationDate.format(formatter);
        }
        return "N/A";
    }

    public static Applicant createNewApplication(
            User user,
            JobPosting jobPosting,
            String resumeFilePath,
            String coverLetter) {
        return Applicant.builder()
                .user(user)
                .jobPosting(jobPosting)
                .resume(resumeFilePath)
                .coverLetter(coverLetter)
                .applicationDate(LocalDateTime.now())
                .build();
    }

    public ApplicationStatusHistory updateCurrentStatus(ApplicationStatus newStatus, String statusNote) {
        ApplicationStatusHistory currentStatus = getCurrentStatusHistory();
        if (currentStatus != null) {
            currentStatus.setCurrent(false);
        }
        ApplicationStatusHistory newStatusHistory = ApplicationStatusHistory.builder()
                .applicant(this)
                .status(newStatus)
                .statusDate(LocalDateTime.now())
                .isCurrent(true)
                .build();
        this.statusHistory.add(newStatusHistory);
        return newStatusHistory;
    }

    public void updateStatusTo(ApplicationStatus newStatus) {
        this.statusHistory.stream()
                .filter(ApplicationStatusHistory::isCurrent)
                .findFirst()
                .ifPresent(h -> h.setCurrent(false));

        ApplicationStatusHistory newHistory = ApplicationStatusHistory.builder()
                .applicant(this)
                .status(newStatus)
                .statusDate(LocalDateTime.now())
                .isCurrent(true)
                .build();

        this.statusHistory.add(newHistory);
    }

    public void markAsReviewed() {
        ApplicationStatusHistory currentHistory = this.getCurrentStatusHistory();
        if (currentHistory == null) return;
        ApplicationStatus currentStatus = currentHistory.getStatus();

        if (currentStatus == ApplicationStatus.PENDING) {
            this.updateStatusTo(ApplicationStatus.REVIEWING);
        }
    }

    // =========================================================================
    //  [NEW] CÁC METHOD MỚI CHO LOGIC SHORTLIST/REJECT (KHÔNG ẢNH HƯỞNG CŨ)
    // =========================================================================

    /**
     * Logic duyệt hồ sơ an toàn
     */
    public void shortlist() {

        // 1. Lấy trạng thái hiện tại (Tái sử dụng method cũ)
        ApplicationStatusHistory currentHistory = getCurrentStatusHistory();
        ApplicationStatus currentStatus = currentHistory != null ? currentHistory.getStatus() : null;

        // RULE 1: BLOCK TỪ INTERVIEW
        if (currentStatus == ApplicationStatus.INTERVIEW_SCHEDULED) {
            throw new ApplicantStatusTransitionException("Hồ sơ đã chuyển sang trạng thái Phỏng Vấn và không thể chuyển ngược về Shortlist.");
        }

        // 2. Kiểm tra xóa
        if (currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new ApplicantStatusTransitionException("Cannot shortlist a withdrawn application.");
        }

        // Rule: Đã reject thì không được duyệt lại (tùy nghiệp vụ)
        if (currentStatus == ApplicationStatus.REJECTED) {
            throw new ApplicantStatusTransitionException("Cannot shortlist a rejected application.");
        }

        // 3. Thực hiện chuyển đổi (Tái sử dụng method cũ của bạn)
        this.updateStatusTo(ApplicationStatus.SHORTLISTED);

    }

    /**
     * Logic từ chối hồ sơ an toàn
     */
    public boolean reject() {
        ApplicationStatusHistory currentHistory = getCurrentStatusHistory();
        ApplicationStatus currentStatus = currentHistory != null ? currentHistory.getStatus() : null;

        if (currentStatus == ApplicationStatus.INTERVIEW_SCHEDULED) {
            throw new ApplicantStatusTransitionException("Hồ sơ đã chuyển sang trạng thái Phỏng Vấn và không thể chuyển ngược về Shortlist.");
        }

        if (currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new ApplicantStatusTransitionException("Cannot reject a withdrawn application.");
        }

        if (currentStatus == ApplicationStatus.REJECTED) {
            throw new ApplicantStatusTransitionException("Cannot reject a rejected application.");
        }

        if (currentStatus == ApplicationStatus.REJECTED) {
            return true;
        }


        this.updateStatusTo(ApplicationStatus.REJECTED);
        return true;
    }

    /**
     * Logic rút đơn
     */
    public void withdraw(Long requestorId) {
        ApplicationStatusHistory current = getCurrentStatusHistory();
        ApplicationStatus currentStatus = current != null ? current.getStatus() : null;

        // RULE 1: Chỉ chủ nhân của đơn mới được rút
        if (!this.getUser().getId().equals(requestorId)) {
            throw new ApplicantStatusTransitionException("You do not have permission to withdraw this application.");
        }

        // Rule: Không cho rút nếu đã đến giai đoạn cuối (Hired/Rejected)
        if (currentStatus == ApplicationStatus.REJECTED || currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new ApplicantStatusTransitionException("Cannot withdraw an application that is already " + currentStatus.getLabel() + ".");
        }

        // Chỉ chuyển trạng thái
        this.updateStatusTo(ApplicationStatus.WITHDRAWN);
    }

    public void scheduleInterview(LocalDateTime dateTime, String type, String location, String content) {
        ApplicationStatusHistory currentHistory = getCurrentStatusHistory();
        ApplicationStatus currentStatus = currentHistory != null ? currentHistory.getStatus() : null;

        // 1. Dùng Static Factory để tạo Entity InterviewSchedule mới
        InterviewSchedule newSchedule = InterviewSchedule.createNew(
                this,
                dateTime,
                type,
                location,
                content
        );
        this.interviewSchedule = newSchedule;
        // RULE: Chặn các trạng thái cuối cùng hoặc bị từ chối
        if (currentStatus == ApplicationStatus.REJECTED || currentStatus == ApplicationStatus.WITHDRAWN ) {
            throw new ApplicantStatusTransitionException(
                    String.format("Can not make a schedule with  a rejected application!")
            );
        }

        // Nếu trạng thái hiện tại đã là INTERVIEW_SCHEDULED thì báo lỗi đã gửi rồi
        if (currentStatus == ApplicationStatus.INTERVIEW_SCHEDULED) {
            throw new ApplicantStatusTransitionException(
                    "An interview invitation has already been sent for this application."
            );
        }

        // Thực hiện chuyển đổi trạng thái
        this.updateStatusTo(ApplicationStatus.INTERVIEW_SCHEDULED);
    }

    /**
     * Thêm hoặc cập nhật đánh giá (review) cho hồ sơ ứng tuyển này.
     * Logic này được đặt trong entity Applicant để đảm bảo tính toàn vẹn.
     *
     * @param reviewer Công ty thực hiện đánh giá.
     */
    public void addOrUpdateReview(Double rating, String comments, Company reviewer) {
        if (this.getCurrentStatusHistory().getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new ReviewNotAllowedException("Cannot review a withdrawn application.");
        }
        if (this.getReview() == null) {
            this.review = ApplicationReview.create(this, rating, comments, reviewer);
        } else {
            this.getReview().update(rating, comments);
        }
        this.markAsReviewed();
    }
}