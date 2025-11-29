package com.example.baoNgoCv.jpa.projection.company;

import com.example.baoNgoCv.model.enums.AccountTier;

public record SubscriptionUsageProjection(
        Integer used,
        Integer limit,
        AccountTier accountTier,
        Integer percentage
) {}
