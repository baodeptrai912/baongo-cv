package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.enums.*;
import lombok.Data;

import java.util.List;

@Data
public class PutJobPostingRequest {
    private String title;
    private JobType jobType;
    private LocationType location;
    private SalaryRange salaryRange;
    private ExperienceLevel experience;
    private IndustryType industry;
    private String applicationDeadline;
    private Integer maxApplicants;
    private List<String> descriptions;
    private List<String> requirements;
    private List<String> benefits;
}
