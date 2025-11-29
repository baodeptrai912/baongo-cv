package com.example.baoNgoCv.model.dto.jobposting.context;

import com.example.baoNgoCv.model.enums.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@ToString
public class JobDetailResponse {

    private Long jobId;
    private String title;
    private String description;
    private String companyName;
    private String companyLogo;
    private LocationType location;
    private JobType jobType;
    private SalaryRange salaryRange;
    private ExperienceLevel experienceLevel;
    private IndustryType industry;
    private LocalDate applicationDeadline;
    private Integer maxApplicants;

    private boolean userCheck;
    private boolean isApplied;
    private boolean isDeleted;
    private boolean isSavedJob;

    private Long applicantId;
    private LocalDate appliedDate;
    private String applicationStatus;

    private String formattedPostedDate;
    private List<String> requirements;
    private List<String> benefits;

    private int daysLeft;
    private boolean isExpired;
    private int applicantCount;
}