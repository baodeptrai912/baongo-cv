package com.example.baoNgoCv.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@RequiredArgsConstructor // Sẽ tạo constructor (int maxJobPostings, int price, String description)
public enum AccountTier {
    FREE(1, 0, "Free plan"),
    BASIC(3, 5000, "Basic plan"),
    PREMIUM(5, 10000, "Premium plan");


    private final int maxJobPostings;
    private final int price;
    private final String displayName;
}