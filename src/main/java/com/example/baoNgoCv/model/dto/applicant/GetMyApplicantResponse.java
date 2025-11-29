package com.example.baoNgoCv.model.dto.applicant;

import com.example.baoNgoCv.model.enums.ApplicationStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public record GetMyApplicantResponse(
        List<ApplicantCard> applicants,
        Long highlightApplicantId
) {
    // --- Computed Methods (Logic thống kê giữ nguyên) ---
    public boolean isEmpty() { return applicants == null || applicants.isEmpty(); }
    public int getTotalApplications() { return applicants != null ? applicants.size() : 0; }
    public int getPendingCount() {
        return (int) (applicants != null ? applicants.stream().filter(a -> a.currentStatus() == ApplicationStatus.PENDING).count() : 0);
    }
    public int getInterviewCount() {
        return (int) (applicants != null ? applicants.stream().filter(a -> a.currentStatus() == ApplicationStatus.INTERVIEW_SCHEDULED).count() : 0);
    }
    public int getResponseRate() {
        if (getTotalApplications() == 0) return 0;
        long respondedCount = applicants.stream().filter(a -> a.currentStatus() != ApplicationStatus.PENDING).count();
        return (int) Math.round((double) respondedCount / getTotalApplications() * 100);
    }
    public Map<String, List<ApplicantCard>> getGroupedByMonth() {
        if (applicants == null) return new LinkedHashMap<>();
        return applicants.stream().collect(Collectors.groupingBy(ApplicantCard::getYearMonth, LinkedHashMap::new, Collectors.toList()));
    }

    // =========================================================================
    // RECORD CARD (Vừa là DTO, vừa là Projection)
    // =========================================================================
    public record ApplicantCard(
            Long id,
            LocalDateTime applicationDate,
            String formattedApplicationDate,
            String resume,
            Long jobPostingId,
            String jobTitle,
            String location,
            String salary,
            Long companyId,
            String companyName,
            String companyLogo,
            ApplicationStatus currentStatus,
            String currentStatusLowerCase,
            String currentStatusDisplayName,
            Long reviewId,
            List<StatusHistory> statusHistory // <--- List này sẽ được điền sau
    ) {

        // --- CONSTRUCTOR DÀNH RIÊNG CHO JPQL (QUERY 1) ---
        // Hibernate sẽ gọi cái này. Nó nhận dữ liệu thô và tự format luôn.
        public ApplicantCard(
                Long id, LocalDateTime applicationDate, String resume,
                Long jobPostingId, String jobTitle, String location, String salary, Long companyId,
                String companyName, String companyLogo, // <-- Thêm companyLogo vào đây
                String currentStatusStr, Long reviewId
        ) {
            this(
                    id,
                    applicationDate,
                    applicationDate != null ? applicationDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "",
                    resume,
                    jobPostingId,
                    jobTitle,
                    location,
                    salary,
                    companyId,
                    companyName,
                    companyLogo, // <-- Sử dụng giá trị được truyền vào
                    ApplicationStatus.valueOf(currentStatusStr),
                    mapToProgressBarStatus(ApplicationStatus.valueOf(currentStatusStr)), // Giữ nguyên cho filter
                    formatStatus(currentStatusStr),
                    reviewId,
                    Collections.emptyList() // <--- QUAN TRỌNG: Khởi tạo List rỗng ban đầu
            );
        }

        // Helper method: Tạo bản sao mới với List History đã được điền
        public ApplicantCard withStatusHistory(List<StatusHistory> newHistory) {
            return new ApplicantCard(
                    id, applicationDate, formattedApplicationDate, resume,
                    jobPostingId, jobTitle, location, salary,
                    companyId, companyName, companyLogo, currentStatus,
                    currentStatusLowerCase, currentStatusDisplayName,
                    reviewId,
                    newHistory
            );
        }

        // [ROLLBACK] Reverted this logic.
        private static String mapToProgressBarStatus(ApplicationStatus status) {
            return status.name().toLowerCase();
        }

        // Helper format string
        public String getYearMonth() {
            return applicationDate != null ? applicationDate.format(DateTimeFormatter.ofPattern("yyyy-MM")) : "unknown";
        }
        private static String formatStatus(String s) {
            return Arrays.stream(s.split("_")).map(w -> w.charAt(0) + w.substring(1).toLowerCase()).collect(Collectors.joining(" "));
        }
    }

    // =========================================================================
    // RECORD HISTORY
    // =========================================================================
    public record StatusHistory(
            Long applicantId,
            String status,
            String statusDisplayName,
            LocalDateTime timestamp,
            String formattedDate,
            String note,
            boolean isCurrent // <--- ✅ ĐÃ THÊM MỚI
    ) {
        // Constructor cho JPQL (Phải khớp thứ tự trong câu Select)
        public StatusHistory(
                Long applicantId,
                String status,
                LocalDateTime timestamp,
                String note,
                boolean isCurrent // <--- Nhận tham số từ DB
        ) {
            this(
                    applicantId,
                    status,
                    formatStatus(status),
                    timestamp,
                    timestamp != null ? timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : "",
                    note,
                    isCurrent // <--- Gán vào Record chính
            );
        }

        private static String formatStatus(String s) {
            return s.charAt(0) + s.substring(1).toLowerCase().replace("_", " ");
        }
    }
}