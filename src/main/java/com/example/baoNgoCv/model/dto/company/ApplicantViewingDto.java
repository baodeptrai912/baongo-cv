package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa toàn bộ dữ liệu cho trang "Applicant Viewing".
 * Sử dụng nested records để đóng gói dữ liệu một cách gọn gàng.
 */
public record ApplicantViewingDto(
    JobInfo currentJob,
    List<JobInfo> availableJobs,
    List<CandidateInfo> candidates,
    Map<String, Long> statusCounts
) {

    /**
     * Thông tin Job (Dùng cho cả currentJob và availableJobs).
     */
    public record JobInfo(
        Long id,
        String title,
        String location,
        LocalDateTime postedDate,
        boolean isActive,
        Long applicantCount
    ) {}

    /**
     * Thông tin Ứng viên (Dùng cho từng dòng trong Table).
     */
    public record CandidateInfo(
        Long id,
        String fullName,
        String email,
        String avatarLabel,
        String avatarUrl,
        String phoneNumber,
        LocalDateTime appliedDate,
        String status,
        String resumeFile
    ) {}

    public record StatusCount(
            ApplicationStatus status,
            Long count
    ) {}
}