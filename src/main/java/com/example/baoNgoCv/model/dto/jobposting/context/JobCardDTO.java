package com.example.baoNgoCv.model.dto.jobposting.context;

import com.example.baoNgoCv.model.enums.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JobCardDTO {
    private Long id;
    private String title;
    private String companyName;
    private String companyLogo;
    private LocationType location;
    private JobType jobType;
    private SalaryRange salaryRange;
    private ExperienceLevel experience;
    private IndustryType industry;
    private LocalDate postedDate;
    private LocalDate applicationDeadline;
    private Integer applicantCount;
    private Integer maxApplicants;
    private Integer viewCount;
    private boolean isTrending;
    private Double trendingScore; // ✨ [NEW] Add trendingScore field
    private List<String> topRequirements;

    public JobCardDTO(Long id, String title, String companyName, String companyLogo,
                      LocationType location, JobType jobType, SalaryRange salaryRange,
                      ExperienceLevel experience, IndustryType industry,
                      LocalDate postedDate, LocalDate applicationDeadline,
                      Integer applicantCount, Integer maxApplicants, Integer viewCount,
                      Boolean isTrending, Double trendingScore, List<String> topRequirements) {
        this.id = id;
        this.title = title;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.location = location;
        this.jobType = jobType;
        this.salaryRange = salaryRange;
        this.experience = experience;
        this.industry = industry;
        this.postedDate = postedDate;
        this.applicationDeadline = applicationDeadline;
        this.applicantCount = applicantCount;
        this.maxApplicants = maxApplicants;
        this.viewCount = viewCount;
        this.isTrending = isTrending != null ? isTrending : false;
        this.trendingScore = trendingScore;
        this.topRequirements = topRequirements != null ? topRequirements : List.of();
    }

}
