package com.example.baoNgoCv.model.dto.jobposting;

import com.example.baoNgoCv.model.enums.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GetJobDetailResponse {

    private JobPostingDetail jobPosting;

    private ApplicationStatusForJobDetail applicationStatus;

    private List<JobPostingRelevant> jobPostingRelevant;

    private CompanyBasic company;
    private JobMetricLite jobMetric;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class JobPostingDetail {

        private Long id;
        private String title;

        private List<String> descriptions;
        private List<String> requirements;
        private List<String> benefits;

        private LocationType location;
        private JobType jobType;
        private SalaryRange salaryRange;
        private ExperienceLevel experience;
        private IndustryType industry;
        private JobPostingStatus status;

        private String locationDisplay;
        private String jobTypeDisplay;
        private String salaryRangeDisplay;
        private String experienceDisplay;
        private String industryDisplay;
        private String statusDisplay;

        private LocalDate applicationDeadline;
        private String applicationDeadlineFormatted;

        private Integer maxApplicants;
        private String formattedPostedDate;
        private Boolean open;
        private Boolean canAcceptMoreApplicants;
        private Long daysUntilDeadline;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ApplicationStatusForJobDetail {
        private boolean userCheck;
        private boolean applied;
        private boolean deleted;
        private boolean savedJob;
        private ApplicantMatching applicantMatching;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @ToString
        public static class ApplicantMatching {
            private Long id;
            private String status;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class JobPostingRelevant {
        private Long id;
        private String title;
        private String location;
        private String companyName;
        private String companyLogo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class CompanyBasic {
        private Long id;
        private String name;
        private String companyLogo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class JobMetricLite {
        private Long applicantCount;

    }
}
