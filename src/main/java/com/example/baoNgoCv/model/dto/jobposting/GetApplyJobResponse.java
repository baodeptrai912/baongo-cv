package com.example.baoNgoCv.model.dto.jobposting;

import com.example.baoNgoCv.jpa.projection.company.CompanyInfoProjection;
import com.example.baoNgoCv.jpa.projection.jobPosting.JobPostingForApplyJobProjection;
import com.example.baoNgoCv.jpa.projection.jobPosting.UserForApplyJobProjection;

public record GetApplyJobResponse(
        JobPostingForApplyJobProjection jobPosting,
        CompanyInfoProjection company,
        UserForApplyJobProjection user,
        boolean hasApplied,
        boolean isExpired,
        Long applicationId
) {
    public boolean canShowApplicationForm() {
        return jobPosting != null
                && user != null
                && jobPosting.canShowApplicationForm(user, hasApplied, isExpired);
    }

    public String getPrimaryMessage() {
        if (jobPosting == null) return null; // ✅ Thêm check null an toàn
        return jobPosting.getPrimaryMessage(hasApplied, isExpired);
    }

    public String getPrimaryMessageType() {
        if (jobPosting == null) return "info"; // ✅ Thêm check null an toàn
        return jobPosting.getPrimaryMessageType(hasApplied, isExpired);
    }

    public boolean canViewApplication() {
        return applicationId != null;
    }

    public boolean canWithdrawApplication() {
        return applicationId != null && !isExpired;
    }
}
