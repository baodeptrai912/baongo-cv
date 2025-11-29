package com.example.baoNgoCv.model.dto.payOs;

import com.fasterxml.jackson.annotation.JsonProperty;

// DTO cho toàn bộ body của request webhook
public record PayOSWebhookRequestDto(
        @JsonProperty("code") String code,
        @JsonProperty("desc") String desc,
        @JsonProperty("success") boolean success,
        @JsonProperty("data") PayOSWebhookDataDto data,
        @JsonProperty("signature") String signature
) {}