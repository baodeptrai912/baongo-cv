package com.example.baoNgoCv.event.jobposting;

import java.util.List;

public record JobPostingReminderEvent(
        Long jobPostingId,
        String jobTitle,
        List<Long> savedUserIds
) {}