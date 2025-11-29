package com.example.baoNgoCv.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum BillingCycle {

    MONTHLY("Monthly Plan"),
    YEARLY("Yearly Plan");

    private final String displayName;

}
