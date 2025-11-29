package com.example.baoNgoCv.mapper;

import com.example.baoNgoCv.model.dto.company.GetJobpostingManagingRequest;
import com.example.baoNgoCv.model.dto.jobposting.FilterData;
import com.example.baoNgoCv.model.dto.jobposting.GetJobDetailResponse;
import com.example.baoNgoCv.model.dto.jobposting.PaginationData;
import com.example.baoNgoCv.model.dto.jobposting.PostJobRequest;
import com.example.baoNgoCv.model.dto.jobposting.context.JobCardDTO;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.enums.*;

import com.example.baoNgoCv.jpa.projection.jobPosting.JobDetailWithUserStatusProjection;
import com.example.baoNgoCv.jpa.projection.jobPosting.RelevantJobProjection;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface JobPostingMapper {

    @Mappings({
            @Mapping(source = "description", target = "descriptions"),
            @Mapping(source = "salary", target = "salaryRange"),
            @Mapping(source = "industryType", target = "industry"),
            @Mapping(source = "deadline", target = "applicationDeadline"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "company", ignore = true),
            @Mapping(target = "postedDate", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "jobMetric", ignore = true),
            @Mapping(target = "applicants", ignore = true),
            @Mapping(target = "savedJobs", ignore = true)
    })
    JobPosting toEntity(PostJobRequest request);


    FilterData toFilterData(GetJobpostingManagingRequest request);

    @Mappings({
            @Mapping(target = "currentPageDisplay", expression = "java(page.getNumber() + 1)"),
            @Mapping(target = "startElement", expression = "java(page.isEmpty() ? 0 : page.getNumber() * page.getSize() + 1)"),
            @Mapping(target = "endElement", expression = "java(page.isEmpty() ? 0 : page.getNumber() * page.getSize() + page.getNumberOfElements())")
    })
    PaginationData toPaginationData(Page<?> page);




    @Mappings({
            @Mapping(source = "company.name", target = "companyName"),
            @Mapping(source = "requirements", target = "topRequirements", qualifiedByName = "getTop2"),
            @Mapping(source = "company.companyLogo", target = "companyLogo")
    })
    JobCardDTO toJobCardDTO(JobPosting jobPosting);


    @Named("getTop2")
    default List<String> getTop3Requirements(List<String> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return new ArrayList<>();
        }
        return requirements.stream()
                .limit(2)
                .collect(Collectors.toList());
    }


    default GetJobDetailResponse toResponseFromProjection(
            JobDetailWithUserStatusProjection projection,
            List<String> descriptions,
            List<String> requirements,
            List<String> benefits,
            List<RelevantJobProjection> relevantJobs
    ) {
        return GetJobDetailResponse.builder()
                .jobPosting(buildJobDetail(projection, descriptions, requirements, benefits))
                .company(buildCompany(projection))
                .jobMetric(buildJobMetric(projection))
                .applicationStatus(buildApplicationStatus(projection))
                .jobPostingRelevant(buildRelevantJobs(relevantJobs))
                .build();
    }


    default GetJobDetailResponse.JobPostingDetail buildJobDetail(JobDetailWithUserStatusProjection p,
                                                                 List<String> desc, List<String> req, List<String> ben) {
        return GetJobDetailResponse.JobPostingDetail.builder()
                .id(p.getId())
                .title(p.getTitle())
                .descriptions(desc)
                .requirements(req)
                .benefits(ben)
                .location(p.getLocation())
                .jobType(p.getJobType())
                .salaryRange(p.getSalaryRange())
                .experience(p.getExperience())
                .industry(p.getIndustry())
                .status(p.getStatus())
                .applicationDeadline(p.getApplicationDeadline())
                .maxApplicants(p.getMaxApplicants())
                .formattedPostedDate(formatPostedDate(p.getPostedDate()))
                .locationDisplay(safeGetLocationDisplay(p.getLocation()))
                .jobTypeDisplay(safeGetJobTypeDisplay(p.getJobType()))
                .salaryRangeDisplay(safeGetSalaryRangeDisplay(p.getSalaryRange()))
                .experienceDisplay(safeGetExperienceDisplay(p.getExperience()))
                .industryDisplay(safeGetIndustryDisplay(p.getIndustry()))
                .statusDisplay(safeGetStatusDisplay(p.getStatus()))
                .open(isJobOpen(p))
                .canAcceptMoreApplicants(canAcceptMoreApplicants(p))
                .daysUntilDeadline(calculateDaysUntilDeadline(p.getApplicationDeadline()))
                .applicationDeadlineFormatted(formatDeadline(p.getApplicationDeadline()))
                .build();
    }

    default GetJobDetailResponse.CompanyBasic buildCompany(JobDetailWithUserStatusProjection p) {
        return GetJobDetailResponse.CompanyBasic.builder()
                .id(p.getCompanyId()).name(p.getCompanyName()).companyLogo(p.getCompanyLogo())
                .build();
    }

    default GetJobDetailResponse.JobMetricLite buildJobMetric(JobDetailWithUserStatusProjection p) {
        return GetJobDetailResponse.JobMetricLite.builder().applicantCount(p.getApplicantCount()).build();
    }

    default GetJobDetailResponse.ApplicationStatusForJobDetail buildApplicationStatus(JobDetailWithUserStatusProjection p) {
        return GetJobDetailResponse.ApplicationStatusForJobDetail.builder()
                .userCheck(true)
                .applied(p.getApplicationId() != null && p.getApplicationDeletedAt() == null)
                .deleted(p.getApplicationDeletedAt() != null)
                .savedJob(p.getJobSaved())
                .applicantMatching(p.getApplicationId() != null ?
                        GetJobDetailResponse.ApplicationStatusForJobDetail.ApplicantMatching.builder().id(p.getApplicationId()).status(p.getApplicationStatus()).build() : null)
                .build();
    }

    default List<GetJobDetailResponse.JobPostingRelevant> buildRelevantJobs(List<RelevantJobProjection> projections) {
        return projections.stream().map(p -> GetJobDetailResponse.JobPostingRelevant.builder()
                .id(p.getId())
                .title(p.getTitle())
                .location(p.getLocation().toString())
                .companyName(p.getCompanyName())
                .companyLogo(p.getCompanyLogo())
                .build()).collect(Collectors.toList());
    }

    default String formatPostedDate(LocalDate postedDate) {
        if (postedDate == null) return null;
        return postedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    default String formatDeadline(LocalDate deadline) {
        if (deadline == null) return null;
        return deadline.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Business logic calculations
    default Boolean isJobOpen(JobDetailWithUserStatusProjection p) {
        return p.getStatus() == JobPostingStatus.OPEN &&
                (p.getApplicationDeadline() == null || p.getApplicationDeadline().isAfter(LocalDate.now()));
    }

    default Boolean canAcceptMoreApplicants(JobDetailWithUserStatusProjection p) {
        if (p.getMaxApplicants() == null) return true;
        return p.getApplicantCount() < p.getMaxApplicants();
    }

    default Long calculateDaysUntilDeadline(LocalDate deadline) {
        if (deadline == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    // ✅ Thêm các helper null-safe
    default String safeGetLocationDisplay(LocationType location) {
        return location != null ? location.getDisplayName() : "Not specified";
    }

    default String safeGetJobTypeDisplay(JobType jobType) {
        return jobType != null ? jobType.getDisplayName() : "Not specified";
    }

    default String safeGetSalaryRangeDisplay(SalaryRange salaryRange) {
        return salaryRange != null ? salaryRange.getDisplayName() : "Negotiable";
    }

    default String safeGetExperienceDisplay(ExperienceLevel experience) {
        return experience != null ? experience.getDisplayName() : "Not specified";
    }

    default String safeGetIndustryDisplay(IndustryType industry) {
        return industry != null ? industry.getDisplayName() : "General";
    }

    default String safeGetStatusDisplay(JobPostingStatus status) {
        return status != null ? status.name() : "Unknown";
    }




}



