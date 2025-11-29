package com.example.baoNgoCv.event.jobposting;

import java.util.List;

public record JobPostingDeletedEvent(
        String jobTitle,
        String companyName,
        List<ApplicantInfo> applicants
) {
    public record ApplicantInfo(
            Integer userId,
            String userEmail,
            String userFullName,
            String username
    ) {}
}
