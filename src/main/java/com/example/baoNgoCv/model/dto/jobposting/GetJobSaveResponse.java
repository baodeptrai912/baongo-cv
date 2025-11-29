package com.example.baoNgoCv.model.dto.jobposting;

import com.example.baoNgoCv.model.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class GetJobSaveResponse {

    private SavedJobsData savedJobsData;
    private PaginationData paginationData;
    private UserData userData;
    private Long highlightJobId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ToString
    public static class SavedJobsData {
        private List<SavedJobItem> savedJobs;
        private Integer totalCount;
        private Boolean hasJobs;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @ToString
        public static class SavedJobItem {
            // === DỮ LIỆU THÔ (RAW DATA) - MapStruct tự động điền ===
            // From JobPosting
            private Long jobId;
            private String title;
            private LocationType location;
            private JobType jobType;
            private SalaryRange salaryRange;
            private LocalDate postedDate;
            private LocalDate applicationDeadline;
            private JobPostingStatus status;
            private ExperienceLevel experience;
            private IndustryType industry;
            private Integer maxApplicants;
            private Long currentApplicants;

            private Long companyId;
            private String companyName;
            private String companyLogo;

            private LocalDateTime savedAt;

            private Boolean hasApplied;
            private ApplicationStatus applicationStatus;
            private LocalDateTime appliedAt;

            private String locationDisplay;
            private String jobTypeDisplay;
            private String salaryRangeDisplay;
            private String experienceDisplay;
            private String industryDisplay;
            private String statusDisplay;
            private String postedDateFormatted;
            private String applicationDeadlineFormatted;
            private String savedAtFormatted;
            private String timeAgoText;
            private String applicationStatusDisplay;
            private String appliedAtFormatted;
            private Boolean isExpired;
            private Boolean isOpen;
            private Boolean isActive;
            private Boolean canAcceptMoreApplicants;

            // === CÁC COLLECTION - Service đính kèm cuối cùng ===
            private List<String> descriptions;
            private List<String> requirements;
            private List<String> benefits;
        }

        public static SavedJobsData empty() {
            return SavedJobsData.builder()
                    .savedJobs(List.of())
                    .totalCount(0)
                    .hasJobs(false)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ToString
    public static class PaginationData {
        private Integer currentPage;
        private Integer totalPages;
        private Integer pageSize;
        private Long totalElements;
        private Boolean hasNext;
        private Boolean hasPrevious;
        private Boolean isFirst;
        private Boolean isLast;
        private Integer nextPage;
        private Integer previousPage;

        public static PaginationData fromPage(Page<?> page) {
            return PaginationData.builder()
                    .currentPage(page.getNumber())
                    .totalPages(page.getTotalPages())
                    .pageSize(page.getSize())
                    .totalElements(page.getTotalElements())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .isFirst(page.isFirst())
                    .isLast(page.isLast())
                    .nextPage(page.hasNext() ? page.getNumber() + 1 : null)
                    .previousPage(page.hasPrevious() ? page.getNumber() - 1 : null)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserData {
        private Long userId;
        private String username;

    }

    public boolean hasJobs() {
        return savedJobsData != null && savedJobsData.hasJobs != null && savedJobsData.hasJobs;
    }

    public boolean isEmpty() {
        return !hasJobs();
    }

    public int getTotalJobCount() {
        return savedJobsData != null && savedJobsData.totalCount != null ? savedJobsData.totalCount : 0;
    }
}
