package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;

public interface PaymentService {
    void saveOrder(String paymentLinkId, Long companyId, Long orderCode, Integer amount, BillingCycle billingCycle, AccountTier targetTier, String qrCode);

    void handleSuccessfulPayment(Long orderCode);

}
