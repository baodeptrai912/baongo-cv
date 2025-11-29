package com.example.baoNgoCv.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SalaryRange {
    UNDER_10M("Under 10M VND"),
    FROM_10M_TO_15M("10M - 15M VND"),
    FROM_15M_TO_20M("15M - 20M VND"),
    FROM_20M_TO_30M("20M - 30M VND"),
    FROM_30M_TO_50M("30M - 50M VND"),
    FROM_50M_TO_100M("50M - 100M VND"),
    ABOVE_100M("Above 100M VND"),
    NEGOTIABLE("Negotiable"),
    COMPETITIVE("Competitive");

    private final String displayName;
}
