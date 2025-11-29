package com.example.baoNgoCv.model.dto.jobposting;

import com.example.baoNgoCv.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PostJobRequest {

    @NotBlank(message = "{job.title.required}")
    @Size(min = 5, max = 200, message = "{job.title.size}")
    private String title;

    @NotNull(message = "{job.type.required}")
    private JobType jobType;

    @NotNull(message = "{job.location.required}")
    private LocationType location;

    @NotNull(message = "{job.category.required}")
    private IndustryType industryType;

    @NotNull(message = "{job.experience.required}")
    private ExperienceLevel experience;

    @NotNull(message = "{job.salary.required}")
    private SalaryRange salary;

    @NotEmpty(message = "{job.description.required}")
    @Size(min = 1, max = 5, message = "Job descriptions must contain between 1 and 5 items")
    private List<@NotBlank(message = "{job.description.point.empty}")
    @Size(max = 1000, message = "{job.description.point.size}") String> description;

    @NotEmpty(message = "{job.requirements.required}")
    @Size(min = 1, max = 5, message = "Job requirements must contain between 1 and 5 items")
    private List<@NotBlank(message = "{job.requirement.empty}")
    @Size(max = 500, message = "{job.requirement.size}") String> requirements;

    @NotEmpty(message = "{job.benefits.required}")
    @Size(min = 1, max = 5, message = "Job benefits must contain between 1 and 5 items")
    private List<@NotBlank(message = "{job.benefit.empty}")
    @Size(max = 500, message = "{job.benefit.size}") String> benefits;

    @NotNull(message = "{job.maxApplicants.required}")
    @Min(value = 1, message = "{job.maxApplicants.min}")
    @Max(value = 30, message = "{job.maxApplicants.max}")
    Integer targetHires;

    @NotNull(message = "{job.deadline.required}")
    @Future(message = "{job.deadline.future}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
}
