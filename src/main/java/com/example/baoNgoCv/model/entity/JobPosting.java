package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.exception.jobpostingException.InvalidJobCreationDataException;
import com.example.baoNgoCv.exception.jobpostingException.JobPostingUpdateException;

import com.example.baoNgoCv.model.dto.company.PutJobPostingRequest;
import com.example.baoNgoCv.model.dto.jobposting.PostJobRequest;
import com.example.baoNgoCv.model.valueObject.JobMetric;
import com.example.baoNgoCv.model.enums.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@Setter
@Builder
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Company company;

    @Column(name = "title")
    private String title;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_posting_descriptions",
            joinColumns = @JoinColumn(name = "job_posting_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(name = "description", length = 1000)
    private List<String> descriptions;

    @Column(name = "location")
    @Enumerated(EnumType.STRING)
    private LocationType location;


    @Column(name = "job_type")
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "salary_range")
    @Enumerated(EnumType.STRING)
    private SalaryRange salaryRange;

    @CreatedDate
    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobPostingStatus status;

    @Builder.Default
    @Embedded
    private JobMetric jobMetric = new JobMetric();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_posting_requirements",
            joinColumns = @JoinColumn(name = "job_posting_id")
    )
    @Column(name = "requirement", length = 500)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<String> requirements;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_posting_benefits",
            joinColumns = @JoinColumn(name = "job_posting_id")
    )
    @Column(name = "benefit", length = 500)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<String> benefits;

    @Column(name = "experience")
    @Enumerated(EnumType.STRING)
    private ExperienceLevel experience;

    @Enumerated(EnumType.STRING)
    @Column(name = "industry")
    private IndustryType industry;


    @Column(name = "target_hires", nullable = false)
    private Integer targetHires;

    @Column(name = "edit_count", nullable = false)
    @Builder.Default
    private Integer editCount = 0;


    @OneToMany(mappedBy = "jobPosting", orphanRemoval = true, fetch = FetchType.LAZY)

    private List<Applicant> applicants;

    @OneToMany(
            mappedBy = "jobPosting",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JobSaved> savedJobs;

    public void addRequirement(String requirementDescription) {
        if (this.requirements == null) {
            this.requirements = new ArrayList<>();
        }
        if (requirementDescription != null && !requirementDescription.trim().isEmpty()) {
            this.requirements.add(requirementDescription.trim());
        }
    }

    public void addRequirements(List<String> requirements) {
        if (requirements != null) {
            requirements.forEach(this::addRequirement);
        }
    }



    public boolean isActive() {
        return status == JobPostingStatus.OPEN &&
                applicationDeadline.isAfter(LocalDate.now());
    }

    public void close() {
        this.status = JobPostingStatus.CLOSED;
        this.updatedAt = LocalDate.now();
    }

    public static JobPosting createNewJobPosting(PostJobRequest request, Company company) {
        LocalDate newDeadline = request.getDeadline();
        LocalDate today = LocalDate.now();

        if (newDeadline == null || newDeadline.isBefore(today)) {
            throw new InvalidJobCreationDataException("The application deadline cannot be in the past.");
        }

        if (newDeadline.isAfter(today.plusDays(30))) {
            throw new InvalidJobCreationDataException("The application deadline cannot be more than 30 days in the future.");
        }



        JobPosting jobPosting = JobPosting.builder()
                .title(request.getTitle())
                .descriptions(request.getDescription())
                .jobType(request.getJobType())
                .company(company)
                .location(request.getLocation())
                .benefits(request.getBenefits())
                .requirements(request.getRequirements())
                .experience(request.getExperience())
                .salaryRange(request.getSalary())
                .industry(request.getIndustryType())
                .applicationDeadline(request.getDeadline())
                .targetHires(request.getTargetHires())
                .status(JobPostingStatus.OPEN)
                .jobMetric(new JobMetric())
                .build();

        return jobPosting;

    }

    public void updateFromRequest(PutJobPostingRequest request) {

        // Định nghĩa giới hạn số lần chỉnh sửa
        final int MAX_EDITS = 1; // Bạn có thể thay đổi giá trị này

        // Kiểm tra nếu đã đạt đến giới hạn
        if (this.editCount >= MAX_EDITS) {
            throw new JobPostingUpdateException("This job posting has reached the maximum number of edits (" + MAX_EDITS + ").");
        }

        // Validate and parse Application Deadline
        LocalDate newDeadline;
        try {
            newDeadline = LocalDate.parse(request.getApplicationDeadline());
        } catch (DateTimeParseException e) {
            throw new JobPostingUpdateException("Invalid date format for application deadline. Please use YYYY-MM-DD.");
        }

        if (newDeadline.isBefore(this.applicationDeadline)) {
            throw new JobPostingUpdateException("The new deadline cannot be earlier than the current deadline.");
        }

        if (newDeadline.isAfter(this.applicationDeadline.plusDays(5))) {
            throw new JobPostingUpdateException("The deadline can only be extended by a maximum of 5 days.");
        }

        this.title = request.getTitle();
        this.applicationDeadline = newDeadline;
        this.targetHires = request.getMaxApplicants();

        this.jobType = request.getJobType();
        this.location = request.getLocation();
        this.salaryRange = request.getSalaryRange();
        this.experience = request.getExperience();
        this.industry = request.getIndustry();
        this.descriptions.clear();
        if (request.getDescriptions() != null) {
            this.descriptions.addAll(request.getDescriptions());
        }

        this.requirements.clear();
        if (request.getRequirements() != null) {
            this.requirements.addAll(request.getRequirements());
        }

        this.benefits.clear();
        if (request.getBenefits() != null) {
            this.benefits.addAll(request.getBenefits());
        }

        // Tăng bộ đếm sau khi cập nhật thành công
        this.editCount++;
    }

    public boolean isExpired() {
        return this.applicationDeadline.isBefore(LocalDate.now()) ||
                this.status == JobPostingStatus.EXPIRED;
    }

    public boolean needsStatusUpdate() {
        return this.applicationDeadline.isBefore(LocalDate.now()) &&
                this.status != JobPostingStatus.EXPIRED;
    }

    // Trong JobPosting.java
    public void onNewApplicationReceived() {
        if (this.jobMetric == null) {
            this.jobMetric = new JobMetric();
        }
        this.jobMetric.incReceived();

    }

    public void expire() {
        this.status = JobPostingStatus.EXPIRED;
    }
}
