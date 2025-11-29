package com.example.baoNgoCv.model.dto;

public class JobPostingMetricsDTO {
    private long totalJobs;
    private long activeJobs;
    private long totalApplicants;
    private long expiringSoon;
    private long closedJobs;
    private long expiredJobs;
    private long filledJobs;

    // Constructors
    public JobPostingMetricsDTO() {}

    public JobPostingMetricsDTO(long totalJobs, long activeJobs, long totalApplicants,
                                long expiringSoon, long closedJobs, long expiredJobs, long filledJobs) {
        this.totalJobs = totalJobs;
        this.activeJobs = activeJobs;
        this.totalApplicants = totalApplicants;
        this.expiringSoon = expiringSoon;
        this.closedJobs = closedJobs;
        this.expiredJobs = expiredJobs;
        this.filledJobs = filledJobs;
    }

    // Getters and Setters
    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(long activeJobs) {
        this.activeJobs = activeJobs;
    }

    public long getTotalApplicants() {
        return totalApplicants;
    }

    public void setTotalApplicants(long totalApplicants) {
        this.totalApplicants = totalApplicants;
    }

    public long getExpiringSoon() {
        return expiringSoon;
    }

    public void setExpiringSoon(long expiringSoon) {
        this.expiringSoon = expiringSoon;
    }

    public long getClosedJobs() {
        return closedJobs;
    }

    public void setClosedJobs(long closedJobs) {
        this.closedJobs = closedJobs;
    }

    public long getExpiredJobs() {
        return expiredJobs;
    }

    public void setExpiredJobs(long expiredJobs) {
        this.expiredJobs = expiredJobs;
    }

    public long getFilledJobs() {
        return filledJobs;
    }

    public void setFilledJobs(long filledJobs) {
        this.filledJobs = filledJobs;
    }

    // Computed properties
    public double getActiveJobsPercentage() {
        return totalJobs > 0 ? (double) activeJobs / totalJobs * 100 : 0;
    }

    public double getAverageApplicantsPerJob() {
        return totalJobs > 0 ? (double) totalApplicants / totalJobs : 0;
    }

    public double getCompletionRate() {
        return totalJobs > 0 ? (double) filledJobs / totalJobs * 100 : 0;
    }
}
