package com.example.baoNgoCv.event.company;

import java.util.Set;

public record CompanyAccountDeletedEvent(
        Long companyId,
        String companyName,
        String companyEmail,
        String companyLogoPath,
        Set<Long> followerUserIds,
        Set<Long> applicantUserIds
) {
}