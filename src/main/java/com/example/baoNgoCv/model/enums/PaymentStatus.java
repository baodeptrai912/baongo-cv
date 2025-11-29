package com.example.baoNgoCv.model.enums;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentStatus {
    PENDING("Pending"),
    SUCCESSFUL("Successful"),
    FAILED("Failed");


    private final String displayName;
    @JsonGetter("name")
    public String getEnumName() {
        return this.name();
    }
}