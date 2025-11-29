package com.example.baoNgoCv.model.dto.jobposting;

import com.example.baoNgoCv.model.enums.*;
import com.example.baoNgoCv.jpa.projection.jobPosting.JobCardProjection;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO cho job search functionality
 * Chứa core job data, derived information và form options để support UI rendering
 */
public record GetJobSearchResponse(
        List<JobCardProjection> jobPostings,  // 1. Core job data - danh sách jobs trả về từ search
        PaginationInfo pagination,            // 2. Pagination metadata - thông tin phân trang
        DisplayInfo display,                  // 3. Display metadata - thông tin hiển thị UI
        SearchCriteria criteria,              // 4. Search state - criteria để maintain form state
        FormOptions formOptions              // 5. Form options - enum values cho dropdown rendering
) {

    /**
     * Form options chứa tất cả enum values để render dropdown options
     * Được tự động populate trong factory method
     */
    public static record FormOptions(
            LocationType[] locationTypes,        // 1. Location dropdown options
            ExperienceLevel[] experienceLevels,  // 2. Experience level dropdown options
            SalaryRange[] salaryRanges,         // 3. Salary range dropdown options
            JobType[] jobTypes,                 // 4. Job type dropdown options
            IndustryType[] industryTypes        // 5. Industry type dropdown options
    ) {
    }

    /**
     * Pagination information derived từ Spring Data Page
     * Chứa các thông tin cần thiết để render pagination component
     */
    public static record PaginationInfo(
            int currentPage,        // 1. Current page number (0-based)
            int totalPages,         // 2. Total number of pages available
            boolean hasNextPage,    // 3. Có page tiếp theo hay không
            boolean hasPreviousPage // 4. Có page trước đó hay không
    ) {
    }

    /**
     * Display information để support UI rendering logic
     * Chứa các derived data từ search results cho presentation layer
     */
    public static record DisplayInfo(
            String searchSummary,      // 1. Summary text hiển thị cho user ("Tìm thấy X công việc")
            long totalElements,        // 2. Tổng số jobs matching criteria
            boolean hasResults,        // 3. Có kết quả hay không (để show/hide content)
            int activeFiltersCount     // 4. Số filters đang active (để show filter badge)
    ) {
    }

    /**
     * Search criteria để maintain form state
     * Chứa các search parameters để pre-populate form khi render lại
     */
    public static record SearchCriteria(
            String keyword,                        // 1. Search keyword entered by user
            List<LocationType> locations,          // 2. Selected location filters
            List<ExperienceLevel> experiences,     // 3. Selected experience level filters
            List<SalaryRange> salaryRanges,       // 4. Selected salary range filters
            List<JobType> jobTypes,               // 5. Selected job type filters
            List<IndustryType> industries,        // 6. Selected industry filters
            LocalDate postedAfter,                // 7. Posted after date filter
            LocalDate deadlineBefore,             // 8. Deadline before date filter
            String sortBy                         // 9. Current sorting option
    ) {
    }

    /**
     * Factory method để create complete response từ request và page results
     * Tự động build tất cả derived data, form options và enum values trong one call
     */
    public static GetJobSearchResponse from(GetJobSearchRequest request, Page<JobCardProjection> pageResults) {
        return new GetJobSearchResponse(
                pageResults.getContent(),           // 1. Extract core job data từ page
                buildPaginationInfo(pageResults),   // 2. Build pagination metadata
                buildDisplayInfo(request, pageResults), // 3. Build display metadata với search context
                buildSearchCriteria(request),       // 4. Preserve search criteria để maintain form
                // 5. Inline form options creation với all enum values
                new FormOptions(
                        LocationType.values(),       // Auto-populate all locations
                        ExperienceLevel.values(),    // Auto-populate all experience levels
                        SalaryRange.values(),       // Auto-populate all salary ranges
                        JobType.values(),           // Auto-populate all job types
                        IndustryType.values()       // Auto-populate all industry types
                )
        );
    }

    /**
     * Build pagination info từ Spring Data Page object
     * Extract các thông tin cần thiết cho pagination component
     */
    private static PaginationInfo buildPaginationInfo(Page<JobCardProjection> page) {
        return new PaginationInfo(
                page.getNumber(),       // 1. Current page (0-based from Spring Data)
                page.getTotalPages(),   // 2. Total pages calculated by Spring Data
                page.hasNext(),         // 3. Has next page flag
                page.hasPrevious()      // 4. Has previous page flag
        );
    }

    /**
     * Build display info từ search request và results
     * Generate các derived information cho UI presentation
     */
    private static DisplayInfo buildDisplayInfo(GetJobSearchRequest request, Page<JobCardProjection> page) {
        // 1. Generate search summary message based on results
        String summary = page.getTotalElements() == 0
                ? "No jobs found matching your criteria"
                : "Found " + page.getTotalElements() + (page.getTotalElements() == 1 ? " job" : " jobs");

        return new DisplayInfo(
                summary,                    // 2. Summary text for user
                page.getTotalElements(),    // 3. Total matching jobs count
                !page.isEmpty(),           // 4. Has results flag
                countActiveFilters(request) // 5. Count active filters for badge
        );
    }

    /**
     * Build search criteria từ request để maintain form state
     * Preserve user's search input để pre-populate form khi render lại
     */
    private static SearchCriteria buildSearchCriteria(GetJobSearchRequest request) {
        return new SearchCriteria(
                request.keyword(),        // 1. Preserve keyword input
                request.locations(),      // 2. Preserve selected locations
                request.experiences(),    // 3. Preserve selected experiences
                request.salaryRanges(),   // 4. Preserve selected salary ranges
                request.jobTypes(),       // 5. Preserve selected job types
                request.industries(),     // 6. Preserve selected industries
                request.postedAfter(),    // 7. Preserve posted after date
                request.deadlineBefore(), // 8. Preserve deadline before date
                request.sortBy()          // 9. Preserve sort preference
        );
    }

    /**
     * Count number of active filters từ search request
     * Được sử dụng để hiển thị filter badge trong UI
     */
    private static int countActiveFilters(GetJobSearchRequest request) {
        int count = 0;
        if (request.keyword() != null && !request.keyword().trim().isEmpty()) count++;        // 1. Keyword filter
        if (request.locations() != null && !request.locations().isEmpty()) count++;          // 2. Location filters
        if (request.experiences() != null && !request.experiences().isEmpty()) count++;      // 3. Experience filters
        if (request.salaryRanges() != null && !request.salaryRanges().isEmpty()) count++;   // 4. Salary filters
        if (request.jobTypes() != null && !request.jobTypes().isEmpty()) count++;           // 5. Job type filters
        if (request.industries() != null && !request.industries().isEmpty()) count++;       // 6. Industry filters
        if (request.postedAfter() != null) count++;                                          // 7. Posted date filter
        if (request.deadlineBefore() != null) count++;                                       // 8. Deadline filter
        return count;
    }

    // Convenience methods cho easy template access
    public boolean hasResults() {
        return display.hasResults();
    }                              // 1. Quick access to has results

    public String searchSummary() {
        return display.searchSummary();
    }                        // 2. Quick access to summary

    public long totalElements() {
        return display.totalElements();
    }                          // 3. Quick access to total count

    public int currentPage() {
        return pagination.currentPage();
    }                            // 4. Quick access to current page

    public int currentPageDisplay() {
        return pagination.currentPage() + 1;
    }

    public int totalPages() {
        return pagination.totalPages();
    }                              // 5. Quick access to total pages

    public boolean hasNextPage() {
        return pagination.hasNextPage();
    }                        // 6. Quick access to next page flag

    public boolean hasPreviousPage() {
        return pagination.hasPreviousPage();
    }                // 7. Quick access to previous page flag
}
