package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.entity.JobPosting;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
public class PutJobPostingResponse {
    private Long id;
    private String title;
    private String jobType;
    private String jobTypeDisplay;
    private String location;
    private String locationDisplay;
    private String salaryRange;
    private String salaryRangeDisplay;
    private String experience;
    private String experienceDisplay;
    private String industry;
    private String industryDisplay;
    private String applicationDeadline; // Formatted as dd/MM/yyyy
    private Integer maxApplicants;
    private List<String> descriptions;
    private List<String> requirements;
    private List<String> benefits;

    public static PutJobPostingResponse fromEntity(JobPosting entity) {
        return PutJobPostingResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .jobType(entity.getJobType().name())
                .jobTypeDisplay(entity.getJobType().getDisplayName())
                .location(entity.getLocation().name())
                .locationDisplay(entity.getLocation().getDisplayName())
                .salaryRange(entity.getSalaryRange().name())
                .salaryRangeDisplay(entity.getSalaryRange().getDisplayName())
                .experience(entity.getExperience().name())
                .experienceDisplay(entity.getExperience().getDisplayName())
                .industry(entity.getIndustry().name())
                .industryDisplay(entity.getIndustry().getDisplayName())
                .applicationDeadline(entity.getApplicationDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .maxApplicants(entity.getTargetHires())
                .descriptions(entity.getDescriptions())
                .requirements(entity.getRequirements())
                .benefits(entity.getBenefits())
                .build();
    }
}
