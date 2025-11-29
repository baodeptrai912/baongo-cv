package com.example.baoNgoCv.event.company;

import com.example.baoNgoCv.model.enums.AccountTier;

public record UpgradePlanSuccessEvent(
        Long companyId,
        String companyUsername,
        String companyEmail,
        String companyName,
        AccountTier newTier
) {
}
