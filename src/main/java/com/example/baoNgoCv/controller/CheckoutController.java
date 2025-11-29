package com.example.baoNgoCv.controller;

import com.example.baoNgoCv.exception.companyException.UpgradePlanException;
import com.example.baoNgoCv.model.dto.payOs.PaymentRequest;
import com.example.baoNgoCv.service.domainService.PlanUpgradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final PlanUpgradeService planUpgradeService;
    private final PayOS payOS;

    @PostMapping("/create")
    public ResponseEntity<?> createPaymentLink(@RequestBody PaymentRequest request) {
        try {
            CreatePaymentLinkResponse response = planUpgradeService.initiatePlanUpgrade(request);
            return ResponseEntity.ok(response);
        } catch (UpgradePlanException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (PayOSException e) {
            log.error("Error creating payment link with PayOS", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred with the payment gateway."));
        }
    }

    @GetMapping("/info/{orderCode}")
    public ResponseEntity<?> getPaymentInfo(@PathVariable Long orderCode) {
        try {
            var paymentInfo = payOS.paymentRequests().get(orderCode);
            return ResponseEntity.ok(paymentInfo);
        } catch (PayOSException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel/{orderCode}")
    public ResponseEntity<?> cancelPayment(
            @PathVariable Long orderCode,
            @RequestParam(required = false) String reason) {
        try {
            var canceledPayment = payOS.paymentRequests().cancel(orderCode, reason);
            return ResponseEntity.ok(canceledPayment);
        } catch (PayOSException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
