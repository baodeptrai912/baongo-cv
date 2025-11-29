package com.example.baoNgoCv.jpa.projection.jobPosting;

public interface JobMetricForApplyJobProjection {
    // Core attribute
    Integer getApplicantCount();

    // Derived attributes
    default Integer getSafeApplicantCount() {
        return getApplicantCount() != null ? getApplicantCount() : 0;
    }
}