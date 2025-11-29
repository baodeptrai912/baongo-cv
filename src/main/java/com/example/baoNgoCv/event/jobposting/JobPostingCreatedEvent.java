package com.example.baoNgoCv.event.jobposting;

import java.util.List;

public record JobPostingCreatedEvent(
        Long jobId,
        List<String> followerUsernames,
        String companyEmail,
        String jobTitle,
        String companyName,
        String companyAvatar
) {
}
