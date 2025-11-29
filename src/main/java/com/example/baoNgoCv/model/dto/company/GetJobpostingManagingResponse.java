package com.example.baoNgoCv.model.dto.company;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO response cuối cùng, đã được tái cấu trúc cho API quản lý job.
 * Cấu trúc này hoàn toàn "thuần túy" và chỉ chứa các DTO/record khác,
 * tách biệt hoàn toàn khỏi tầng dữ liệu (Projection).
 */
public record GetJobpostingManagingResponse(
        List<CompanyJobManagement> jobPostings,
        PaginationData pagination,
        SubscriptionUsageInfo subscriptionUsage,
        JobStatistics statistics
) {
    /**
     * Compact constructor để đảm bảo an toàn dữ liệu.
     */
    public GetJobpostingManagingResponse {
        // Đảm bảo các list không bao giờ là null khi serialize thành JSON
        if (jobPostings == null) {
            jobPostings = List.of();
        }
    }


    public record SubscriptionUsageInfo(
            Integer used,
            Integer limit,
            String planName,
            Integer percentage
    ) {
        // Các logic nghiệp vụ liên quan đến DTO này được giữ lại, rất hữu ích
        public boolean isNearLimit() {
            return percentage >= 80;
        }

        public boolean hasReachedLimit() {
            return used >= limit;
        }
    }


    public record JobStatistics(
            Integer totalJobs,
            Integer openJobs,
            Integer closedJobs,
            Integer totalApplications,
            Integer expiringSoonCount
    ) {}

    public record CompanyJobManagement(
            Long id,
            String title,
            String jobType,
            String location,
            String salaryRange,
            String experience,
            String industry,
            LocalDate applicationDeadline,
            String status,
            Integer applicantCount,
            Integer maxApplicants,
            String companyName,

            List<String> descriptions,
            List<String> requirements,
            List<String> benefits,
            Integer editCount
    ) {}
    public record PaginationData(
            Integer currentPage,
            Integer totalPages,
            Long totalElements
    ) {}

}
