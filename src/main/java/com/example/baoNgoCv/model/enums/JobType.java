package com.example.baoNgoCv.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    INTERNSHIP,
    TEMPORARY,
    FREELANCE;

    public String getDisplayName() {
        return switch (this) {
            case FULL_TIME -> "Full-time";
            case PART_TIME -> "Part-time";
            case CONTRACT -> "Contract";
            case INTERNSHIP -> "Internship";
            case TEMPORARY -> "Temporary";
            case FREELANCE -> "Freelance";
        };
    }
}
