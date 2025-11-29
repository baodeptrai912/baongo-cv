package com.example.baoNgoCv.event.jobposting;

public record JobPostingExpiringSoonEvent(
        Long jobId,
        String jobTitle,
        Long companyId,
        String companyName,
        String companyEmail
) {
}