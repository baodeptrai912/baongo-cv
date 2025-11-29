package com.example.baoNgoCv.model.dto.jobposting;

import com.example.baoNgoCv.model.enums.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record GetJobSearchRequest(

        // Basic search criteria
        @Size(max = 100, message = "Keyword must not exceed 100 characters")
        String keyword,

        @Size(max = 10, message = "Cannot select more than 10 locations")
        List<LocationType> locations,

        @Size(max = 10, message = "Cannot select more than 10 experience levels")
        List<ExperienceLevel> experiences,

        @Size(max = 10, message = "Cannot select more than 10 salary ranges")
        List<SalaryRange> salaryRanges,

        // Advanced search criteria
        @Size(max = 10, message = "Cannot select more than 10 job types")
        List<JobType> jobTypes,

        @Size(max = 10, message = "Cannot select more than 10 industries")
        List<IndustryType> industries,

        // Status filters - đã sửa
        JobPostingStatus jobStatus,    // OPEN, CLOSED, EXPIRED thay vì JobSearchStatus

        // Removed applicantStatus vì không có trong entity

        // Date filters
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @PastOrPresent(message = "Posted after date cannot be in the future")
        LocalDate postedAfter,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate deadlineBefore,

        // Sorting and pagination - đã sửa sort fields
        @Pattern(regexp = "^(postedDate|applicationDeadline|salaryRange|title|company)$",
                message = "Invalid sort field")
        String sortBy,

        @Pattern(regexp = "^(asc|desc)$", message = "Sort order must be 'asc' or 'desc'")
        String sortOrder,

        @Min(value = 0, message = "Page number must be non-negative")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        Integer size
) {

    // Factory method với default values - đã sửa
    public static GetJobSearchRequest withDefaults() {
        return new GetJobSearchRequest(
                null, null, null, null, null, null,
                JobPostingStatus.OPEN, // Sử dụng JobPostingStatus.OPEN
                null, null,
                "postedDate", "desc", 0, 12
        );
    }

    // Validation and normalization - đã sửa
    public GetJobSearchRequest validateAndNormalize() {
        return new GetJobSearchRequest(
                keyword != null ? keyword.trim() : null,
                locations,
                experiences,
                salaryRanges,
                jobTypes,
                industries,
                jobStatus != null ? jobStatus : JobPostingStatus.OPEN,
                postedAfter,
                deadlineBefore,
                sortBy != null ? sortBy : "postedDate",
                sortOrder != null ? sortOrder : "desc",
                page != null && page >= 0 ? page : 0,
                size != null && size > 0 && size <= 100 ? size : 12
        );
    }

    // Helper method - đã sửa
    public boolean hasAdvancedFilters() {
        return (jobTypes != null && !jobTypes.isEmpty()) ||
                (industries != null && !industries.isEmpty()) ||
                postedAfter != null ||
                deadlineBefore != null ||
                (jobStatus != null && jobStatus != JobPostingStatus.OPEN);
    }

    // Business validation
    @AssertTrue(message = "Posted date cannot be after deadline date")
    public boolean isDateRangeValid() {
        if (postedAfter != null && deadlineBefore != null) {
            return !postedAfter.isAfter(deadlineBefore);
        }
        return true;
    }
}
