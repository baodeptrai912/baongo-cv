package com.example.baoNgoCv.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceLevel {
    INTERN("Intern"),
    FRESHER("Fresher"),
    JUNIOR("Junior (1-3 years)"),
    MIDDLE("Middle (3-5 years)"),
    SENIOR("Senior (5+ years)"),
    TEAM_LEAD("Team Lead"),
    MANAGER("Manager"),
    NO_EXPERIENCE_REQUIRED("No experience required");

    private final String displayName;
}
