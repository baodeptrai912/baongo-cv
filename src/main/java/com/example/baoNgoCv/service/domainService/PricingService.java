package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;

public interface PricingService {
    int getPriceForPlan(AccountTier targetTier, BillingCycle billingCycle);
}
