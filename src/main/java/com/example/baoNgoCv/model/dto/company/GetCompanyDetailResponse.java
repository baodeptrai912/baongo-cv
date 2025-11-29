package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.dto.jobposting.context.JobCardDTO;
import com.example.baoNgoCv.model.enums.*;
import com.example.baoNgoCv.jpa.projection.company.CompanyDetailDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * All-in-one DTO Response cho company detail page
 * Chứa tất cả data cần thiết trong 1 object duy nhất
 */
public record GetCompanyDetailResponse(
        CompanyDetailDTO company,
        List<JobCardDTO> jobPostingByCompany,
        Long currentUserId,
        Set<Long> companyFollowers
) {

    // ==================== FOLLOW LOGIC ====================

    public String followStatus() {
        if (currentUserId == null) {
            return "login to follow";
        }

        boolean isFollowed = companyFollowers != null &&
                companyFollowers.contains(currentUserId);

        return isFollowed ? "followed" : "not yet";
    }

    public boolean isFollowedByCurrentUser() {
        return "followed".equals(followStatus());
    }

    public boolean canUserFollow() {
        return !"login to follow".equals(followStatus());
    }

    // ==================== JOB STATISTICS ====================

    /**
     * Số lượng job đang trending
     */
    public long getTrendingJobsCount() {
        return jobPostingByCompany.stream()
                .filter(JobCardDTO::isTrending)
                .count();
    }

    /**
     * Tổng số applications của tất cả jobs
     */
    public long getTotalApplications() {
        return jobPostingByCompany.stream()
                .mapToInt(job -> job.getApplicantCount() != null ? job.getApplicantCount() : 0)
                .sum();
    }

    /**
     * Tổng số views của tất cả jobs
     */
    public long getTotalViews() {
        return jobPostingByCompany.stream()
                .mapToInt(job -> job.getViewCount() != null ? job.getViewCount() : 0)
                .sum();
    }

    /**
     * Số lượng job đang mở (size của list vì đã filter ACTIVE)
     */
    public long getOpenJobsCount() {
        return jobPostingByCompany.size();
    }

    // ==================== JOB FILTERING & CATEGORIZATION ====================

    /**
     * Danh sách jobs đang trending
     */
    public List<JobCardDTO> getTrendingJobs() {
        return jobPostingByCompany.stream()
                .filter(JobCardDTO::isTrending)
                .collect(Collectors.toList());
    }

    /**
     * Jobs có deadline trong vòng 7 ngày
     */
    public List<JobCardDTO> getUrgentJobs() {
        LocalDate now = LocalDate.now();
        return jobPostingByCompany.stream()
                .filter(job -> job.getApplicationDeadline() != null &&
                        ChronoUnit.DAYS.between(now, job.getApplicationDeadline()) <= 7)
                .collect(Collectors.toList());
    }

    /**
     * Số lượng urgent jobs
     */
    public long getUrgentJobsCount() {
        return getUrgentJobs().size();
    }

    // ==================== JOB TYPE & LOCATION DISTRIBUTION ====================

    /**
     * Phân bố jobs theo job type
     */
    public Map<JobType, Long> getJobsByType() {
        return jobPostingByCompany.stream()
                .collect(Collectors.groupingBy(
                        JobCardDTO::getJobType,
                        Collectors.counting()
                ));
    }

    /**
     * Phân bố jobs theo location
     */
    public Map<LocationType, Long> getJobsByLocation() {
        return jobPostingByCompany.stream()
                .collect(Collectors.groupingBy(
                        JobCardDTO::getLocation,
                        Collectors.counting()
                ));
    }

    /**
     * Số lượng remote jobs
     */
    public long getRemoteJobsCount() {
        return jobPostingByCompany.stream()
                .filter(job -> job.getLocation() == LocationType.REMOTE)
                .count();
    }

    /**
     * Số lượng full-time jobs
     */
    public long getFullTimeJobsCount() {
        return jobPostingByCompany.stream()
                .filter(job -> job.getJobType() == JobType.FULL_TIME)
                .count();
    }

    // ==================== SALARY & EXPERIENCE ANALYSIS ====================

    /**
     * Phân bố jobs theo salary range
     */
    public Map<SalaryRange, Long> getJobsBySalaryRange() {
        return jobPostingByCompany.stream()
                .collect(Collectors.groupingBy(
                        JobCardDTO::getSalaryRange,
                        Collectors.counting()
                ));
    }

    /**
     * Có jobs lương cao không (trên 30M)
     */
    public boolean hasHighSalaryJobs() {
        return jobPostingByCompany.stream()
                .anyMatch(job -> job.getSalaryRange() == SalaryRange.FROM_30M_TO_50M ||
                        job.getSalaryRange() == SalaryRange.FROM_50M_TO_100M);
    }

    /**
     * Phân bố jobs theo experience level
     */
    public Map<ExperienceLevel, Long> getJobsByExperience() {
        return jobPostingByCompany.stream()
                .collect(Collectors.groupingBy(
                        JobCardDTO::getExperience,
                        Collectors.counting()
                ));
    }

    /**
     * Số lượng entry-level jobs
     */
    public long getEntryLevelJobsCount() {
        return jobPostingByCompany.stream()
                .filter(job -> job.getExperience() == ExperienceLevel.NO_EXPERIENCE_REQUIRED ||
                        job.getExperience() == ExperienceLevel.JUNIOR)
                .count();
    }

    // ==================== APPLICATION PROGRESS METRICS ====================

    /**
     * Tỷ lệ ứng tuyển trung bình (%)
     */
    public double getAverageApplicationProgress() {
        return jobPostingByCompany.stream()
                .filter(job -> job.getMaxApplicants() != null && job.getMaxApplicants() > 0)
                .mapToDouble(job ->
                        (double) (job.getApplicantCount() != null ? job.getApplicantCount() : 0)
                                / job.getMaxApplicants() * 100)
                .average()
                .orElse(0.0);
    }

    /**
     * Jobs có tỷ lệ ứng tuyển cao (trên 80%)
     */
    public long getHighDemandJobsCount() {
        return jobPostingByCompany.stream()
                .filter(job -> job.getMaxApplicants() != null &&
                        job.getApplicantCount() != null &&
                        job.getMaxApplicants() > 0 &&
                        (double) job.getApplicantCount() / job.getMaxApplicants() > 0.8)
                .count();
    }

    // ==================== TIMELINE ANALYSIS ====================

    /**
     * Jobs đăng trong 7 ngày qua
     */
    public List<JobCardDTO> getRecentJobs() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        return jobPostingByCompany.stream()
                .filter(job -> job.getPostedDate().isAfter(sevenDaysAgo))
                .collect(Collectors.toList());
    }

    /**
     * Số lượng jobs đăng tuần này
     */
    public long getJobsPostedThisWeek() {
        return getRecentJobs().size();
    }

    /**
     * Jobs có deadline sắp hết (trong 3 ngày)
     */
    public long getJobsExpiringSoon() {
        LocalDate now = LocalDate.now();
        return jobPostingByCompany.stream()
                .filter(job -> job.getApplicationDeadline() != null &&
                        ChronoUnit.DAYS.between(now, job.getApplicationDeadline()) <= 3)
                .count();
    }

    public List<JobCardDTO> getAllJobsSortedBy(String sortType) {
        return switch(sortType) {
            case "deadline" -> jobPostingByCompany.stream()
                    .sorted(Comparator.comparing(JobCardDTO::getApplicationDeadline,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            case "popular" -> jobPostingByCompany.stream()
                    .sorted(Comparator.comparing(job -> job.getApplicantCount() != null ? job.getApplicantCount() : 0,
                            Comparator.reverseOrder()))
                    .toList();
            default -> jobPostingByCompany; // default order
        };
    }

    // ==================== INDUSTRY ANALYSIS ====================

    /**
     * Phân bố jobs theo industry type
     */
    public Map<IndustryType, Long> getJobsByIndustry() {
        return jobPostingByCompany.stream()
                .collect(Collectors.groupingBy(
                        JobCardDTO::getIndustry,
                        Collectors.counting()
                ));
    }

    /**
     * Industry chủ đạo của company (nhiều jobs nhất)
     */
    public IndustryType getDominantIndustry() {
        return getJobsByIndustry().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Có jobs không?
     */
    public boolean hasJobs() {
        return !jobPostingByCompany.isEmpty();
    }

    /**
     * Có trending jobs không?
     */
    public boolean hasTrendingJobs() {
        return getTrendingJobsCount() > 0;
    }

    /**
     * Company có đang tuyển dụng tích cực không? (>= 5 jobs)
     */
    public boolean isActivelyHiring() {
        return jobPostingByCompany.size() >= 5;
    }

    /**
     * Factory method với raw data, DTO tự compute
     */
    public static GetCompanyDetailResponse create(
            CompanyDetailDTO companyDTO,
            List<JobCardDTO> jobCardDTOs,
            Long currentUserId,
            Set<Long> companyFollowers) {

        return new GetCompanyDetailResponse(
                companyDTO,
                jobCardDTOs,
                currentUserId,
                companyFollowers
        );
    }
}
