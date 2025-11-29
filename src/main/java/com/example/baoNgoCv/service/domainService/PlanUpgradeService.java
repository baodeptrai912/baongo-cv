package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.exception.companyException.UpgradePlanException;
import com.example.baoNgoCv.model.dto.payOs.PaymentRequest;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

public interface PlanUpgradeService {
    CreatePaymentLinkResponse initiatePlanUpgrade(PaymentRequest request) throws PayOSException, UpgradePlanException;
}
