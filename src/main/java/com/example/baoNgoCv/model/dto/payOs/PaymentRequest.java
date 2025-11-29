package com.example.baoNgoCv.model.dto.payOs;

import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "Target tier cannot be null")
        AccountTier targetTier,

        @NotNull(message = "Billing cycle cannot be null")
        BillingCycle billingCycle
) {}
