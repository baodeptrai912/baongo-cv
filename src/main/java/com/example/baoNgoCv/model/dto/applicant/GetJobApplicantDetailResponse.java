package com.example.baoNgoCv.model.dto.applicant;

import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.entity.ApplicationReview;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// RECORD ROOT
public record GetJobApplicantDetailResponse(
        // 1. Thông tin về đơn ứng tuyển (Đã được gói lại)
        ApplicationInfo application,

        // 2. Thông tin người ứng tuyển
        CandidateInfo user,

        // 3. Thông tin công việc
        JobInfo jobPosting,

        // 4. Lịch sử
        List<ActivityLog> logs,

        // 5. [THÊM MỚI] Thông tin đánh giá
        ReviewInfo review

) {


    // --- STATIC FACTORY METHOD (Mapper) ---
    // Gọi cái này trong Service: GetJobApplicantDetailResponse.from(app);
    // [CẬP NHẬT] Thêm tham số baseUrl (frontendUrl)
    public static GetJobApplicantDetailResponse from(Applicant app, String baseUrl) {

        // 1. Map User
        User u = app.getUser();
        CandidateInfo candidateInfo = new CandidateInfo(
                u.getId(),
                u.getPersonalInfo().getFullName(),
                u.getContactInfo().getEmail(),
                u.getContactInfo().getPhoneNumber(),
                u.getContactInfo().getAddress(),
                u.getProfilePicture()
        );

        // 2. Map Job
        JobPosting job = app.getJobPosting();
        JobInfo jobInfo = new JobInfo(
                job.getTitle()
        );

        // 3. Map Application Info (Thông tin chính)
        ApplicationStatus currentStatus = ApplicationStatus.PENDING;
        if (app.getCurrentStatusHistory() != null) {
            currentStatus = app.getCurrentStatusHistory().getStatus();
        }

        // --- [NEW] LOGIC NỐI URL CV ---
        String rawResume = app.getResume();
        String fullResumeUrl = baseUrl + rawResume;


        // -----------------------------

        ApplicationInfo appInfo = new ApplicationInfo(
                app.getId(),
                app.getApplicationDate(),
                currentStatus,
                app.getCoverLetter(),
                fullResumeUrl // Trả về URL đầy đủ (Full Public URL)
        );

        // 4. Map Logs
        List<ActivityLog> activityLogs = app.getOrderedStatusHistory().stream()
                .map(history -> new ActivityLog(
                        history.getStatus().getLabel(),
                        history.getStatusDate()
                ))
                .collect(Collectors.toList());

        // 5. [THÊM MỚI] Map Review
        ReviewInfo reviewInfo = ReviewInfo.fromEntity(app.getReview());

        // Trả về Root DTO
        return new GetJobApplicantDetailResponse(appInfo, candidateInfo, jobInfo, activityLogs, reviewInfo);
    }


    // --- NESTED RECORDS ---

    public record ApplicationInfo(
            Long id,                        // applicantId (cho JS/API)
            LocalDateTime applicationDate,  // Ngày nộp
            ApplicationStatus status,       // Trạng thái (Pending, Rejected...)
            String coverLetter,             // Thư xin việc
            String resume                   // Tên file CV
    ) {
    }

    // Context thông tin User (Candidate)
    public record CandidateInfo(
            Long id,
            String fullName,
            String email,
            String phoneNumber,
            String address,
            String avatar
    ) {
    }

    // Context thông tin Job
    public record JobInfo(
            String title
    ) {
    }

    // Context Log
    public record ActivityLog(
            String statusTitle,
            LocalDateTime timestamp
    ) {
    }

    // [THÊM MỚI] DTO con cho Review
    public record ReviewInfo(
            Double score,
            String note
    ) {
        public static ReviewInfo fromEntity(ApplicationReview review) {
            if (review == null) {
                return null; // Nếu chưa có review, trả về null
            }
            return new ReviewInfo(review.getRating(), review.getReviewComments());
        }
    }
}
